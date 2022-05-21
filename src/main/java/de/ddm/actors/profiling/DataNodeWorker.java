package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import de.ddm.actors.Master;
import de.ddm.actors.patterns.LargeMessageProxy;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.structures.Column;
import de.ddm.structures.InclusionDependency;
import de.ddm.structures.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import de.ddm.profiler.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataNodeWorker extends AbstractBehavior<DataNodeWorker.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable, LargeMessageProxy.LargeMessage {
    }

    // TODO do we need this here?
    @AllArgsConstructor
    public static class ReceptionistListingMessage implements Message {
        private static final long serialVersionUID = -5246338806092216222L;
        Receptionist.Listing listing;
    }

    @AllArgsConstructor
    public static class PostTableMessage implements Message {
        private static final long serialVersionUID = 200;
        public String attribute;
        public List<ValueWithPosition> values;
    }

    @AllArgsConstructor
    public static class MergeMessage implements Message {
        private static final long serialVersionUID = 201;
        String attribute;
    }

    @AllArgsConstructor
    public static class SubsetCheckMessage implements Message {
        private static final long serialVersionUID = 202;
        String referencedAttribute;
        String dependentAttribute;
        ActorRef<DataNodeWorker.Message> dependentWorker;
    }

    @AllArgsConstructor
    public static class MetadataRequestMessage implements Message {
        private static final long serialVersionUID = 203;
        String attribute;
        ActorRef<DataNodeWorker.Message> requestor; // meine eigene??
    }

    @AllArgsConstructor
    public static class MetadataMessage implements Message {
        private static final long serialVersionUID = 205;
        private String attribute;
        Metadata metadata;
    }

    @AllArgsConstructor
    public static class ValueRequestMessage implements Message {
        private static final long serialVersionUID = 207;
        public String attribute;
        ActorRef<DataNodeWorker.Message> requestor;
    }

    @AllArgsConstructor
    public static class ValueMessage implements Message {
        private static final long serialVersionUID = 209;
        String attribute;
        List<Value> valueList;
    }

    /*
     * @Getter
     * 
     * @NoArgsConstructor
     * 
     * @AllArgsConstructor
     * public static class TaskMessage implements Message {
     * private static final long serialVersionUID = -4667745204456518160L;
     * ActorRef<LargeMessageProxy.Message> dependencyMinerLargeMessageProxy;
     * 
     * Task task;
     * Map<String, Set<String>> distinctValuesA;
     * Map<String, Set<String>> distinctValuesB;
     * 
     * private int getSetMemorySize(Set<String> set) {
     * return set.stream().mapToInt(value -> value.length() * 2).sum();
     * }
     * 
     * public int getMemorySize() {
     * return
     * distinctValuesA.values().stream().mapToInt(this::getSetMemorySize).sum() +
     * distinctValuesB.values().stream().mapToInt(this::getSetMemorySize).sum();
     * }
     * }
     */

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static final String DEFAULT_NAME = "DataNodeWorker";

    public static Behavior<Message> create() {
        return Behaviors.setup(DataNodeWorker::new);
    }

    private DataNodeWorker(ActorContext<Message> context) {
        super(context);

        final ActorRef<Receptionist.Listing> listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class,
                ReceptionistListingMessage::new);
        context.getSystem().receptionist()
                .tell(Receptionist.subscribe(DependencyMiner.dependencyMinerService, listingResponseAdapter));

        this.largeMessageProxy = this.getContext().spawn(
                LargeMessageProxy.create(this.getContext().getSelf().unsafeUpcast()), LargeMessageProxy.DEFAULT_NAME);
    }

    /////////////////
    // Actor State //
    /////////////////

    private final ActorRef<LargeMessageProxy.Message> largeMessageProxy;
    Map<String, AttributeState> attributes;
    List<SubsetCheckMessage> pendingSubsetChecks;
    ActorRef<MasterNodeWorker.Message> master;

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceptionistListingMessage.class, this::handle)
                .onMessage(PostTableMessage.class, this::handle)
                .onMessage(MergeMessage.class, this::handle)
                .onMessage(SubsetCheckMessage.class, this::handle)
                .onMessage(MetadataRequestMessage.class, this::handle)
                .onMessage(MetadataMessage.class, this::handle)
                .onMessage(ValueRequestMessage.class, this::handle)
                .onMessage(ValueMessage.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(ReceptionistListingMessage message) {
        Set<ActorRef<DependencyMiner.Message>> dependencyMiners = message.getListing()
                .getServiceInstances(DependencyMiner.dependencyMinerService);
        for (ActorRef<DependencyMiner.Message> dependencyMiner : dependencyMiners)
            dependencyMiner
                    .tell(new DependencyMiner.RegistrationMessage(this.getContext().getSelf(), this.largeMessageProxy));
        return this;
    }

    /*
     * private Behavior<Message> handle(TaskMessage message) {
     * this.getContext().getLog().info(
     * "Received task table {} with {} columns and {} distinct values, task table {} with {} columns and {} distinct values"
     * ,
     * message.task.getTableNameA(), message.task.getColumnNamesA().size(),
     * message.distinctValuesA.values().stream().mapToInt(set -> set.size()).sum(),
     * message.task.getTableNameB(), message.task.getColumnNamesB().size(),
     * message.distinctValuesB.values().stream().mapToInt(set -> set.size()).sum(),
     * message.getMemorySize());
     * 
     * List<InclusionDependency> inclusionDeps = new ArrayList<>();
     * message.distinctValuesA.forEach((columnA, setA) -> {
     * message.distinctValuesB.forEach((columnB, setB) -> {
     * int cardinalityA = setA.size();
     * int cardinalityB = setB.size();
     * 
     * // NOTE both or none of these branches may be executed
     * if (cardinalityA <= cardinalityB && setB.containsAll(setA)) {
     * inclusionDeps.add(new InclusionDependency(message.task.getTableNameA(),
     * message.task.getTableNameB(), columnA, columnB));
     * }
     * if (cardinalityB <= cardinalityA && setA.containsAll(setB)) {
     * inclusionDeps.add(new InclusionDependency(message.task.getTableNameB(),
     * message.task.getTableNameA(), columnB, columnA));
     * }
     * });
     * });
     * 
     * this.getContext().getLog().info(
     * "Found {} INDs for table {} and table {}: {}",
     * inclusionDeps.size(), message.task.getTableNameA(),
     * message.task.getTableNameB(), inclusionDeps);
     * 
     * LargeMessageProxy.LargeMessage completionMessage = new
     * DependencyMiner.CompletionMessage(this.getContext().getSelf(),
     * inclusionDeps);
     * this.largeMessageProxy.tell(new
     * LargeMessageProxy.SendMessage(completionMessage,
     * message.getDependencyMinerLargeMessageProxy()));
     * 
     * return this;
     * }
     */
    private Behavior<Message> handle(PostTableMessage message) {
        this.attributes.
        return this;
    }

    private Behavior<Message> handle(MergeMessage message) {
        return this;
    }

    private Behavior<Message> handle(SubsetCheckMessage message) {
        this.pendingSubsetChecks.add(message);
        message.dependentWorker
                .tell(new MetadataRequestMessage(message.dependentAttribute, this.getContext().getSelf()));
        return this;
    }

    private Behavior<Message> handle(MetadataRequestMessage message) {
        Metadata metadata = this.attributes.get(message.attribute).metadata;
        message.requestor.tell(new MetadataMessage(message.attribute, metadata));
        return this;
    }

    private Behavior<Message> handle(MetadataMessage message) {
        for (SubsetCheckMessage subsetCheck : pendingSubsetChecks) {
            if (subsetCheck.dependentAttribute != message.attribute)
                continue;
            Metadata referencedMetadata = this.attributes.get(subsetCheck.referencedAttribute).metadata;
            if (referencedMetadata.distinctCount < message.metadata.distinctCount) {
                this.master.tell(new MasterNodeWorker.SubsetCheckResultMessage(subsetCheck.referencedAttribute,
                        subsetCheck.dependentAttribute,
                        SubsetCheckResult.ruledOutByCardinality()));
            }
            // TODO else if()
            else {
                subsetCheck.dependentWorker
                        .tell(new ValueRequestMessage(subsetCheck.dependentAttribute, this.getContext().getSelf()));
            }
        }

        return this;
    }

    private Behavior<Message> handle(ValueRequestMessage message) {
        List<Value> valueList = this.attributes.get(message.attribute).currentSegment.queryRange()
                .collect(Collectors.toList());
        message.requestor.tell(new ValueMessage(message.attribute, valueList));
        return this;
    }
    // TODO largeMessageProxy + nur Hälfte schicken + Logik für nur Hälfte schicken

    private Behavior<Message> handle(ValueMessage message) {
        for (SubsetCheckMessage subsetCheck : pendingSubsetChecks) {
            if (subsetCheck.dependentAttribute != message.attribute)
                continue;
            boolean result = this.attributes.get(subsetCheck.referencedAttribute).currentSegment
                    .containsAll(message.valueList.stream());
            if (result) {
                this.master.tell(new MasterNodeWorker.SubsetCheckResultMessage(subsetCheck.referencedAttribute,
                        subsetCheck.dependentAttribute,
                        SubsetCheckResult.passedCheck()));
            } else {
                this.master.tell(new MasterNodeWorker.SubsetCheckResultMessage(subsetCheck.referencedAttribute,
                        subsetCheck.dependentAttribute,
                        SubsetCheckResult.failedCheck(new HashMap<Value, Integer>())));
            }
        }
        return this;
    }

}

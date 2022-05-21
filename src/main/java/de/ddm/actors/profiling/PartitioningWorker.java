package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
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

public class PartitioningWorker extends AbstractBehavior<DataNodeWorker.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable, LargeMessageProxy.LargeMessage {
    }

    @AllArgsConstructor
    public class PostTableMessage extends Message {
        private static final long serialVersionUID = 300;

        Table table;
    }

    @AllArgsConstructor
    public interface BroadcastMessage {
        private static final long serialVersionUID = 301;

        String attribute;
        Optional<Integer> rangeBegin;
        Optional<Integer> rangeEndInclusive;
        DataNodeWorker.Message message;
    }

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static final String DEFAULT_NAME = "PartitioningWorker";

    public static Behavior<Message> create(PartitioningStrategy partitioningStrategy) {
        return Behaviors.setup(ctx -> PartitioningWorker::new(ctx, partitioningStrategy);
    }

    private PartitioningWorker(ActorContext<Message> context, PartitioningStrategy partitioningStrategy) {
        super(context);
        this.partitioningStrategy = partitioningStrategy;

        // final ActorRef<Receptionist.Listing> listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class,
        //         ReceptionistListingMessage::new);
        // context.getSystem().receptionist()
        //         .tell(Receptionist.subscribe(DependencyMiner.dependencyMinerService, listingResponseAdapter));

        // this.largeMessageProxy = this.getContext().spawn(
        //         LargeMessageProxy.create(this.getContext().getSelf().unsafeUpcast()), LargeMessageProxy.DEFAULT_NAME);
    }

    /////////////////
    // Actor State //
    /////////////////

    private final List<ActorRef<DataNodeWorker.Message>> workers = new ArrayList<>();
    private final PartitioningStrategy partitioningStrategy;

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(PostTableMessage.class, this::handle)
                .onMessage(BroadcastMessage.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(PostTableMessage message) {
        this.partitioningStrategy.partitionTable(table, (partition, values) -> {
            this.workers.get(partition.targetWorkerId)
                .tell(new DataNodeWorker.EnqueueUpdatesMessage(partition.attribute, partition.values));
        });
        return this;
    }

    private Behavior<Message> handle(BroadcastMessage message) {
        return this;
    }





}

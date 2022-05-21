package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import de.ddm.actors.patterns.LargeMessageProxy;
import de.ddm.serialization.AkkaSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import de.ddm.profiler.*;
import java.util.*;

// TODO naming: CandidateGeneratorWorker?
public class MasterNodeWorker extends AbstractBehavior<MasterNodeWorker.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable, LargeMessageProxy.LargeMessage {
    }

    @Getter
    @AllArgsConstructor
    public static class SetChangeMessage implements Message {
        private static final long serialVersionUID = 100;
        private String attribute;
        private SetChange setChange;
    }

    @Getter
    @AllArgsConstructor
    public static class SubsetCheckResultMessage implements Message {
        private static final long serialVersionUID = 101;
        private String attribute;
        private SubsetCheckResult result;
    }

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static final String DEFAULT_NAME = "MasterNodeWorker";

    public static Behavior<Message> create(CandidateGenerator candidateGenerator) {
        return Behaviors.setup(ctx -> MasterNodeWorker::new(ctx, candidateGenerator);
    }

    private MasterNodeWorker(ActorContext<Message> context, CandidateGenerator candidateGenerator) {
        super(context);
        this.candidateGenerator = candidateGenerator;

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

    private final List<ActorRef<DataNodeWorker.Message>> dataNodeWorkers = new ArrayList<>();
    private final ActorRef<PartitioningWorker> partitioningWorker;
    private final CandidateGenerator candidateGenerator;

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(SetChangeMessage.class, this::handle)
                .onMessage(SubsetCheckResultMessage.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(SetChangeMessage message) {
        return this;
    }

    private Behavior<Message> handle(SubsetCheckResultMessage message) {
        return this;
    }
}

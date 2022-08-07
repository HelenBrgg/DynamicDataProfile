package de.ddm.actors.profiling;

import akka.actor.Cancellable;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import de.ddm.actors.patterns.LargeMessageProxy;
import de.ddm.actors.profiling.DataWorker.SubsetCheckRequest;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.structures.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.*;

// TODO naming: CandidateGeneratorWorker?
public class Planner extends AbstractBehavior<Planner.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable {}

    @AllArgsConstructor
    @Getter
    public static class MergeResultPart implements Message {
        private static final long serialVersionUID = 0x97A2_0001;
        private Table.Attribute attribute;
        private boolean additions;
        private boolean removals;
        private Metadata metadata;
        private boolean finalPart;
        private int workerId;
    }

    @AllArgsConstructor
    @Getter
    public static class SubsetCheckResult implements Message {
        private static final long serialVersionUID = 0x97A2_0002;
        private Candidate candidate;
        private CandidateStatus status;
        private int workerId;
    }

    /////////////////
    // Actor State //
    /////////////////

    private final CandidateGenerator candidateGenerator;
    private final ActorRef<InputWorker.Message> inputWorker;
    private final ActorRef<DataWorker.Message> dataWorker;
    private int pendingSubsetChecks = 0; // TODO remove this? theoretically, you can interrupt them with a MergeRequest

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static Behavior<Message> create(
        CandidateGenerator candidateGenerator,
        ActorRef<InputWorker.Message> inputWorker,
        ActorRef<DataWorker.Message> dataWorker
    ){
        return Behaviors.setup(ctx -> new Planner(ctx, candidateGenerator, inputWorker, dataWorker));
    }

    private Planner(
        ActorContext<Message> context,
        CandidateGenerator candidateGenerator,
        ActorRef<InputWorker.Message> inputWorker,
        ActorRef<DataWorker.Message> dataWorker
    ){
        super(context);
        this.candidateGenerator = candidateGenerator;
        this.inputWorker = inputWorker;
        this.dataWorker = dataWorker;

        DataWorker.MergeRequest initialMerge = new DataWorker.MergeRequest(this.getContext().getSelf().narrow());
        this.getContext().scheduleOnce(Duration.ofMillis(1000), this.dataWorker, initialMerge);
    }

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(MergeResultPart.class, this::handle)
                .onMessage(SubsetCheckResult.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(MergeResultPart result) {
        this.candidateGenerator.updateAttribute(result.getAttribute(), result.isAdditions(), result.isRemovals(), result.getMetadata());

        if (result.isFinalPart()) {
            /* generate new candidates to check */
            Set<Candidate> newCandidates = this.candidateGenerator.generateCandidates();

            /* send subset-check requests */
            newCandidates.forEach(candidate -> {
                DataWorker.SubsetCheckRequest request = new SubsetCheckRequest(candidate, Optional.empty(), this.getContext().getSelf().narrow());
                this.dataWorker.tell(request);
                this.pendingSubsetChecks += 1;
            });
        }

        return this;
    }

    private Behavior<Message> handle(SubsetCheckResult result) {
        this.candidateGenerator.updateCandidate(result.getCandidate(), Optional.of(result.getStatus()));

        this.pendingSubsetChecks -= 1;
        if (this.pendingSubsetChecks <= 0) {
            this.pendingSubsetChecks = 0;

            InputWorker.IdleMessage idle = new InputWorker.IdleMessage();
            this.inputWorker.tell(idle);
            
            /* schedule next merge */
            DataWorker.MergeRequest nextMerge = new DataWorker.MergeRequest(this.getContext().getSelf().narrow());
            this.getContext().scheduleOnce(Duration.ofMillis(2000), this.dataWorker, nextMerge);
        }

        return this;
    }
}

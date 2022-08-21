package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.stream.javadsl.Merge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import de.ddm.actors.profiling.DataWorker.SubsetCheckRequest;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.structures.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

// TODO naming: CandidateGeneratorWorker?
public class Planner extends AbstractBehavior<Planner.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable {}

    @Getter
    public static class MergeResult implements Message {
        private static final long serialVersionUID = 0x97A2_0001;

        @AllArgsConstructor
        @Getter
        public static class Entry {
            private boolean additions;
            private boolean removals;
            private Metadata metadata;

            public Entry combineWith(Entry other){
                return new Entry(
                    this.isAdditions() || other.isAdditions(),
                    this.isRemovals() || other.isRemovals(),
                    this.getMetadata().combineWith(other.getMetadata()));
            }

        }

        private Map<Table.Attribute, Entry> entries = new HashMap<>();
        private int workerId;

        public MergeResult(int workerId){
            this.workerId = workerId;
        }
        public void addEntry(Table.Attribute attribute, Entry entry){
            this.entries.put(attribute, entry);
        }
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

    private final Sink sink;
    private final CandidateGenerator candidateGenerator;
    private final ActorRef<InputWorker.Message> inputWorker;
    private final ActorRef<DataWorker.Message> dataWorker;
    private int pendingSubsetChecks = 0; // TODO remove this? theoretically, you can interrupt them with a MergeRequest

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static Behavior<Message> create(
        Sink sink,
        ActorRef<InputWorker.Message> inputWorker,
        ActorRef<DataWorker.Message> dataWorker
    ){
        return Behaviors.setup(ctx -> new Planner(ctx, sink, inputWorker, dataWorker));
    }

    private Planner(
        ActorContext<Message> context,
        Sink sink,
        ActorRef<InputWorker.Message> inputWorker,
        ActorRef<DataWorker.Message> dataWorker
    ){
        super(context);
        this.sink = sink;
        this.candidateGenerator = new CandidateGenerator();
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
                .onMessage(MergeResult.class, this::handle)
                .onMessage(SubsetCheckResult.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(MergeResult result) {
        this.getContext().getLog().info("received merge-result from worker {}", result.getWorkerId());

        result.getEntries().forEach((attribute, entry) -> {
            this.candidateGenerator.updateAttribute(attribute, entry.isAdditions(), entry.isRemovals(), entry.getMetadata());
        });

        /* generate new candidates to check */
        Map<Candidate, Optional<CandidateStatus>> generated = this.candidateGenerator.generateCandidates();

        this.getContext().getLog().info("generated candidates: {}", generated.toString());

        generated.forEach((candidate, status) -> {
            if (status.isPresent()) {
                this.sink.putResult(candidate, status.get());
            } else {
                /* send subset-check requests */
                DataWorker.SubsetCheckRequest request = new SubsetCheckRequest(candidate, Optional.empty(), this.getContext().getSelf().narrow());
                this.dataWorker.tell(request);
                this.pendingSubsetChecks += 1;
            }
        });

        /* no new candidates: inform input-worker we want more data  */
        if (generated.values().stream().filter(status -> status.isEmpty()).count() == 0) {
            this.inputWorker.tell(new InputWorker.IdleMessage());

            // TODO move this into a function
            /* schedule next merge */
            DataWorker.MergeRequest nextMerge = new DataWorker.MergeRequest(this.getContext().getSelf().narrow());
            this.getContext().scheduleOnce(Duration.ofMillis(1000), this.dataWorker, nextMerge);
        }

        return this;
    }

    private Behavior<Message> handle(SubsetCheckResult result) {
        this.sink.putResult(result.getCandidate(), result.getStatus());
        this.candidateGenerator.updateCandidate(result.getCandidate(), Optional.of(result.getStatus()));

        this.pendingSubsetChecks -= 1;
        if (this.pendingSubsetChecks <= 0) {
            this.pendingSubsetChecks = 0;

            InputWorker.IdleMessage idle = new InputWorker.IdleMessage();
            this.inputWorker.tell(idle);
            
            // TODO move this into a function
            /* schedule next merge */
            DataWorker.MergeRequest nextMerge = new DataWorker.MergeRequest(this.getContext().getSelf().narrow());
            this.getContext().scheduleOnce(Duration.ofMillis(2000), this.dataWorker, nextMerge);
        }

        return this;
    }
}

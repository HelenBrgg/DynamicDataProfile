package de.ddm.actors.profiling;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import de.ddm.actors.profiling.DataWorker.SubsetCheckRequest;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.structures.Candidate;
import de.ddm.structures.CandidateGenerator;
import de.ddm.structures.CandidateStatus;
import de.ddm.structures.Metadata;
import de.ddm.structures.Sink;
import de.ddm.structures.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

            // NOTE used for vertical partitioning
            // public Entry combineWith(Entry other){
            //     return new Entry(
            //         this.isAdditions() || other.isAdditions(),
            //         this.isRemovals() || other.isRemovals(),
            //         this.getMetadata().combineWith(other.getMetadata()));
            // }

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

    @NoArgsConstructor
    public static class StartMessage implements Message {
        private static final long serialVersionUID = 0x97A2_9998;
    }

    @NoArgsConstructor
    public static class ShutdownMessage implements Message {
        private static final long serialVersionUID = 0x97A2_9999;
    }

    /////////////////
    // Actor State //
    /////////////////

    private final Sink sink;
    private final CandidateGenerator candidateGenerator;
    private final ActorRef<InputWorker.Message> inputWorker;
    private final ActorRef<DataWorker.Message> dataWorker;
    private boolean lastMerge = false;

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

        this.getContext().watchWith(this.inputWorker, new ShutdownMessage());
    }

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(MergeResult.class, this::handle)
                .onMessage(SubsetCheckResult.class, this::handle)
                .onMessage(StartMessage.class, this::handle)
                .onMessage(ShutdownMessage.class, this::handle)
                .build();
    }

    private void scheduleNextMerge() {
        DataWorker.MergeRequest nextMerge = new DataWorker.MergeRequest(this.getContext().getSelf().narrow());
        this.getContext().scheduleOnce(Duration.ofMillis(1000), this.dataWorker, nextMerge);
    }

    private Behavior<Message> handle(MergeResult result) {
        this.getContext().getLog().debug("received merge-result from worker {} for {} attributes", result.getWorkerId(), result.getEntries().size());

        result.getEntries().forEach((attribute, entry) -> {
            this.candidateGenerator.updateAttribute(attribute, entry.isAdditions(), entry.isRemovals(), entry.getMetadata());
        });

        /* generate new candidates to check */
        Map<Candidate, Optional<CandidateStatus>> generated = this.candidateGenerator.generateCandidates();

        this.getContext().getLog().debug("generated candidates: {}", generated.toString());

        generated.forEach((candidate, status) -> {
            if (status.isPresent()) {
                this.sink.putLiveResult(candidate, status.get());
            } else {
                /* send subset-check requests */
                DataWorker.SubsetCheckRequest request = new SubsetCheckRequest(candidate, Optional.empty(), this.getContext().getSelf().narrow());
                this.dataWorker.tell(request);
            }
        });

        if (this.lastMerge) {
            this.getContext().scheduleOnce(Duration.ofMillis(1000), this.getContext().getSelf(), new ShutdownMessage());
            return this;
        }

        this.scheduleNextMerge();
        return this;
    }

    private Behavior<Message> handle(SubsetCheckResult result) {
        this.getContext().getLog().debug("received subset-check-result from worker {} for {}", result.getWorkerId(), result.getCandidate());

        this.sink.putLiveResult(result.getCandidate(), result.getStatus());
        this.candidateGenerator.updateCandidate(result.getCandidate(), Optional.of(result.getStatus()));

        return this;
    }

    private Behavior<Message> handle(StartMessage _msg) {
        // kickoff merging events
        DataWorker.MergeRequest initialMerge = new DataWorker.MergeRequest(this.getContext().getSelf().narrow());
        this.getContext().scheduleOnce(Duration.ofMillis(500), this.dataWorker, initialMerge);
        return this;
    }

    private Behavior<Message> handle(ShutdownMessage _msg) {
        // TODO run last merge
        if (!this.lastMerge) {
            this.getContext().getLog().info("waiting for last merge before shutdown");
            this.lastMerge = true;
            return this;
        }

        this.getContext().getLog().info("last merge done, shutting down");
        this.sink.putFinalResults(this.candidateGenerator.getCandidates());

        return Behaviors.stopped();
    }
}

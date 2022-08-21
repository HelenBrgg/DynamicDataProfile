package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import de.ddm.structures.CandidateStatus;
import de.ddm.structures.ColumnArray;
import de.ddm.structures.ColumnSet;
import de.ddm.structures.PartitioningStrategy;
import de.ddm.structures.Table;
import de.ddm.structures.Value;
import lombok.AllArgsConstructor;
import lombok.Getter;
import de.ddm.actors.profiling.DataWorker.MergeRequest;
import de.ddm.actors.profiling.DataWorker.Message;
import de.ddm.actors.profiling.DataWorker.NewBatchMessage;
import de.ddm.actors.profiling.DataWorker.SetQueryRequest;
import de.ddm.actors.profiling.DataWorker.SetQueryResult;
import de.ddm.actors.profiling.DataWorker.SubsetCheckRequest;
import de.ddm.actors.profiling.Planner.MergeResult;
import de.ddm.actors.profiling.Planner.SubsetCheckResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataDistributor extends AbstractBehavior<Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    @AllArgsConstructor
    @Getter
    private static class WrappedMergeResult implements Message {
        private Planner.MergeResult message;
    }
    // TODO currently unused
    @AllArgsConstructor
    @Getter
    private static class WrappedSubsetCheckResult implements Message {
        private Planner.SubsetCheckResult message;
    }

    /////////////////
    // Actor State //
    /////////////////

    PartitioningStrategy partitioningStrategy;
    List<ActorRef<Message>> dataWorkers = new ArrayList<>();
    ActorRef<Planner.MergeResult> mergeResultPartAdapter;
    ActorRef<Planner.SubsetCheckResult> subsetCheckResultAdapter;

    Optional<MergeRequest> pendingMergeRequest = Optional.empty();
    List<Planner.MergeResult> pendingMergeResults = new ArrayList<>();

    Optional<SubsetCheckRequest> pendingSubsetCheckRequest = Optional.empty();
    List<Planner.SubsetCheckResult> pendingSubsetCheckResults = new ArrayList<>();

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static Behavior<Message> create(
        ColumnArray.Factory arrayFactory,
        ColumnSet.Factory setFactory,
        PartitioningStrategy partitioningStrategy,
        int numWorkers
    ){
        return Behaviors.setup(ctx -> new DataDistributor(ctx, arrayFactory, setFactory, partitioningStrategy, numWorkers));
    }

    private DataDistributor(
        ActorContext<Message> context, 
        ColumnArray.Factory arrayFactory,
        ColumnSet.Factory setFactory,
        PartitioningStrategy partitioningStrategy,
        int numWorkers
    ){
        super(context);
        this.partitioningStrategy = partitioningStrategy;
        for (int i = 0; i < numWorkers; ++i) {
            ActorRef<DataWorker.Message> worker = this.getContext().spawn(DataWorker.create(i, arrayFactory, setFactory), "data-worker-" + (i + 1));
            this.dataWorkers.add(worker.narrow());
        }
        this.mergeResultPartAdapter = this.getContext().messageAdapter(Planner.MergeResult.class, WrappedMergeResult::new);
        this.subsetCheckResultAdapter = this.getContext().messageAdapter(Planner.SubsetCheckResult.class, WrappedSubsetCheckResult::new);
    }

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
            .onMessage(NewBatchMessage.class, this::handle)
            .onMessage(MergeRequest.class, this::handle)
            .onMessage(WrappedMergeResult.class, this::handle)
            .onMessage(SetQueryRequest.class, this::handle)
            .onMessage(SetQueryResult.class, this::handle)
            .onMessage(SubsetCheckRequest.class, this::handle)
            .onMessage(WrappedSubsetCheckResult.class, this::handle)
            .build();
    }

    private Behavior<Message> handle(NewBatchMessage message) {
        this.partitioningStrategy.partitionTable(message.getBatchTable()).forEach((workerId, table) -> {
            NewBatchMessage workerMessage = new NewBatchMessage(table);
            this.dataWorkers.get(workerId).tell(workerMessage);
        });
        return this;
    }

    private Behavior<Message> handle(MergeRequest request) {
        this.pendingMergeRequest = Optional.of(request);
        this.pendingMergeResults.clear();

        MergeRequest workerMessage = new MergeRequest(this.mergeResultPartAdapter);
        this.dataWorkers.forEach(worker -> worker.tell(workerMessage));

        return this;
    }

    private Behavior<Message> handle(WrappedMergeResult part){
        this.getContext().getLog().info("received merge result part from data worker {}", part.getMessage().getWorkerId());

        this.pendingMergeResults.add(part.getMessage());

        if (this.pendingMergeResults.size() != this.dataWorkers.size()) {
            return this; // still waiting for some data workers
        }

        this.getContext().getLog().info("received all merge result parts");

        Map<Table.Attribute, MergeResult.Entry> combinedEntries = new HashMap<>();
        this.pendingMergeResults.forEach(workerResult -> {
            workerResult.getEntries().forEach((attribute, workerEntry) -> {
                if (!combinedEntries.containsKey(attribute)) {
                   combinedEntries.put(attribute, workerEntry);
                } else {
                   combinedEntries.get(attribute).combineWith(workerEntry);
                }
            });
        });

        MergeResult result = new MergeResult(0); // TODO document data worker id 0 somewhere
        combinedEntries.forEach(result::addEntry);

        this.pendingMergeRequest.get().getResultRef().tell(result);

        return this;
    }

    // TODO we dont need this at all, remove`
    private Behavior<Message> handle(SetQueryRequest request) {
        /*
        Optional<Integer> workerId = this.partitioningStrategy.getAttributeWorkerId(request.getAttribute());
        
        // no data workers have this attribute
        if (workerId.isEmpty()){
            SetQueryResult result = new SetQueryResult(request.getAttribute(), List.of(), true, 0);
            request.getResultRef().tell(result);
            return this;
        }

        this.dataWorkers.get(workerId.get()).tell(request);
        */
        assert false;
        
        return this;
    }

    private Behavior<Message> handle(SetQueryResult queryResult) {
        assert false;
        return this;
    }

    private Behavior<Message> handle(SubsetCheckRequest request) {
        Optional<Integer> workerIdA = this.partitioningStrategy.getAttributeWorkerId(request.getCandidate().getAttributeA());
        Optional<Integer> workerIdB = this.partitioningStrategy.getAttributeWorkerId(request.getCandidate().getAttributeB());

        if (workerIdA.isEmpty()) {
            // case: worker A has no data, worker B has data
            // case: worker A and B have no data
            SubsetCheckResult result = new SubsetCheckResult(request.getCandidate(), CandidateStatus.succeededCheck(0), -1);
            request.getResultRef().tell(result);
        } else if (workerIdB.isEmpty()) {
            // case: worker A has data, worker B has no data
            SubsetCheckResult result = new SubsetCheckResult(request.getCandidate(), CandidateStatus.failedCheck(0), -1);
            request.getResultRef().tell(result);
        } else if (workerIdA.get() == workerIdB.get()) {
            // case: worker A and B have data, but are the same
            this.dataWorkers.get(workerIdA.get()).tell(request);
        } else {
            // case: worker A and B have data, B needs to query A
            ActorRef<DataWorker.Message> remoteRef = this.dataWorkers.get(workerIdA.get());
            SubsetCheckRequest workerRequest = new SubsetCheckRequest(request.getCandidate(), Optional.of(remoteRef.narrow()), request.getResultRef());
            this.dataWorkers.get(workerIdB.get()).tell(workerRequest);
        }

        return this;
    }

    private Behavior<Message> handle(WrappedSubsetCheckResult result){
        assert false; // not yet implemented (necessary for horizontal partitioning)
        return this;
    }

}

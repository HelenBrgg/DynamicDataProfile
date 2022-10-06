package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import de.ddm.actors.patterns.LargeMessageProxy;
import de.ddm.actors.profiling.Planner.SubsetCheckResult;
import de.ddm.structures.AttributeState;
import de.ddm.structures.Candidate;
import de.ddm.structures.CandidateStatus;
import de.ddm.structures.ColumnArray;
import de.ddm.structures.ColumnSet;
import de.ddm.structures.SetDiff;
import de.ddm.structures.Table;
import de.ddm.structures.Value;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

public class DataWorker extends AbstractBehavior<DataWorker.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends LargeMessageProxy.LargeMessage {}

    @AllArgsConstructor
    @Getter
    public static class NewBatchMessage implements Message {
        private static final long serialVersionUID = 0xDADA_0001;

        // TODO timestamp
        private Table batchTable;
    }

    @AllArgsConstructor
    @Getter
    public static class MergeRequest implements Message {
        private static final long serialVersionUID = 0xDADA_0002;

        private ActorRef<Planner.MergeResult> resultRef;
    }

    @AllArgsConstructor
    @Getter
    public static class SetQueryRequest implements Message {
        private static final long serialVersionUID = 0xDADA_0003;

        private Table.Attribute attribute;
        private Optional<Value> fromValue;

        private int requestorWorkerId;
        private ActorRef<SetQueryResult> resultRef;
    }

    @AllArgsConstructor
    @Getter
    public static class SetQueryResult implements Message {
        private static final long serialVersionUID = 0xDADA_0004;

        private Table.Attribute attribute;
        private List<Value> values; // TODO should this be Stream<Value>?
        private boolean endOfSet; // NOTE if this is true, .values contains the max-value

        private int workerId;
        private ActorRef<DataWorker.SetQueryRequest> workerRef;

        public Optional<Value> lastValue() {
            assert !this.endOfSet == !this.values.isEmpty();
            if (this.values.isEmpty()) return Optional.empty();
            return Optional.of(this.values.get(this.values.size() - 1));
        }
    }

    @AllArgsConstructor
    @Getter
    public static class SubsetCheckRequest implements Message {
        private static final long serialVersionUID = 0xDADA_0005;

        private Candidate candidate;
        // TODO private int mergeId;

        private Optional<ActorRef<SetQueryRequest>> remoteRef;
        private ActorRef<Planner.SubsetCheckResult> resultRef;
    }

    /////////////////
    // Actor State //
    /////////////////

    private final int workerId;
    private final ColumnArray.Factory arrayFactory;
    private final ColumnSet.Factory setFactory;
    private final Map<Table.Attribute, AttributeState> attributeStates = new HashMap<>();

    @RequiredArgsConstructor
    static class PendingSubsetCheck {
        final SubsetCheckRequest request;
        int comparisonCount = 0;
    }

    // grouped by A of A c B (A is stored remotely)
    private final Map<Table.Attribute, List<PendingSubsetCheck>> remoteSubsetChecks = new HashMap<>();

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static Behavior<DataWorker.Message> create(
        int workerId,
        ColumnArray.Factory arrayFactory,
        ColumnSet.Factory setFactory
    ){
        return Behaviors.setup(ctx -> new DataWorker(ctx, workerId, arrayFactory, setFactory));
    }

    private DataWorker(
        ActorContext<DataWorker.Message> context, 
        int workerId,
        ColumnArray.Factory arrayFactory,
        ColumnSet.Factory setFactory
    ){
        super(context);
        this.workerId = workerId;
        this.arrayFactory = arrayFactory;
        this.setFactory = setFactory;
    }

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
            .onMessage(NewBatchMessage.class, this::handle)
            .onMessage(MergeRequest.class, this::handle)
            .onMessage(SetQueryRequest.class, this::handle)
            .onMessage(SetQueryResult.class, this::handle)
            .onMessage(SubsetCheckRequest.class, this::handle)
            .build();
    }

    private Behavior<Message> handle(NewBatchMessage message) {
        // TODO add Table.getRowCount() ?
        this.getContext().getLog().info("received new batch of {} containing {} rows", message.getBatchTable().name, message.getBatchTable().positions.size());

        for (int i = 0; i < message.batchTable.attributes.size(); ++i){
            Table.Attribute attr = message.batchTable.attributes.get(i);
            AttributeState state = this.attributeStates.computeIfAbsent(attr, (_attr) -> new AttributeState(this.arrayFactory, this.setFactory));

            state.currentSegmentArray.setValues(message.batchTable.streamColumnWithPositions(i));
        }
        return this;
    }

    private Behavior<Message> handle(MergeRequest request) {
        this.getContext().getLog().info("received merge request ({} attributes), aborting all subset checks", this.attributeStates.size());
        this.remoteSubsetChecks.clear();

        Planner.MergeResult result = new Planner.MergeResult(this.workerId);
        this.attributeStates.forEach((attr, state) -> {
            SetDiff diff = state.mergeSegments();
            boolean additions = !diff.getInserted().isEmpty();
            boolean removals = !diff.getRemoved().isEmpty();
            result.addEntry(attr, new Planner.MergeResult.Entry(additions, removals, state.metadata));
        });
        request.resultRef.tell(result);

        return this;
    }

    private Behavior<Message> handle(SetQueryRequest request) {
        this.getContext().getLog().info("received set query request for {} from data worker {}", request.getAttribute(), request.getRequestorWorkerId());

        if (!this.attributeStates.containsKey(request.attribute)) {
            SetQueryResult result = new SetQueryResult(request.attribute, List.of(), true, this.workerId, this.getContext().getSelf().narrow());
            request.resultRef.tell(result);
            return this;
        }

        AttributeState state = this.attributeStates.get(request.attribute);

        List<Value> values = state.oldSegmentSet.queryChunk(request.fromValue).collect(Collectors.toList());
        boolean endOfSet = values.isEmpty() || state.metadata.getMax().get().compareTo(values.get(values.size() - 1)) == 0;
        
        SetQueryResult result = new SetQueryResult(request.attribute, values, endOfSet, this.workerId, this.getContext().getSelf().narrow());
        request.resultRef.tell(result);

        return this;
    }

    private Behavior<Message> handle(SetQueryResult result) {
        this.getContext().getLog().info("received set query result for {} (end={}) from data worker {}", result.getAttribute(), result.isEndOfSet(), result.getWorkerId());

        List<PendingSubsetCheck> attrSubsetChecks = this.remoteSubsetChecks.getOrDefault(result.getAttribute(), List.of());

        this.getContext().getLog().info("updating {} pending subset checks for attribute: {}", attrSubsetChecks.size(), attrSubsetChecks.stream().map(check -> check.request.getCandidate().toString()).collect(Collectors.joining(", ")));

        // update all running checks on this dependent attribute
        attrSubsetChecks.removeIf(check -> {
            AttributeState stateB = this.attributeStates.get(check.request.candidate.getAttributeB());

            check.comparisonCount += result.getValues().size();

            if (!stateB.oldSegmentSet.containsAll(result.getValues().stream())) {
                this.getContext().getLog().info("subset check {} failed: [{}...] ⊈ [{}]",
                    check.request.getCandidate(),
                    result.values.stream().limit(2).map(Object::toString).collect(Collectors.joining(",")),
                    stateB.metadata.getMinMax().stream().map(Object::toString).collect(Collectors.joining(":")));

                CandidateStatus status = CandidateStatus.failedCheck();
                SubsetCheckResult checkResult = new SubsetCheckResult(check.request.candidate, status, this.workerId);
                check.request.resultRef.tell(checkResult);
                return true;
            }

            // NOTE order here is important! subset check only succeeds iff A in B and end of set!
            if (result.isEndOfSet()) {
                this.getContext().getLog().info("subset check {} succeeded: [{}...] ⊆ [{}] {}", // TODO remove cardinality
                    check.request.getCandidate(),
                    result.values.stream().limit(2).map(Object::toString).collect(Collectors.joining(",")),
                    stateB.metadata.getMinMax().stream().map(Object::toString).collect(Collectors.joining(":")), stateB.oldSegmentSet.getCardinality());

                CandidateStatus status = CandidateStatus.succeededCheck();
                SubsetCheckResult checkResult = new SubsetCheckResult(check.request.candidate, status, this.workerId);
                check.request.resultRef.tell(checkResult);
                return true;
            }

            this.getContext().getLog().info("subset check {} still running", check.request.getCandidate());

            return false;
        });

        // if there are remote values left and are any remote checks left, we need to query more
        if (!result.isEndOfSet() && !attrSubsetChecks.isEmpty()) {
            this.getContext().getLog().info("not end of set, need to query more for {} pending subset checks", attrSubsetChecks.size());

            SetQueryRequest nextRequest = new SetQueryRequest(result.getAttribute(), result.lastValue(), this.workerId, this.getContext().getSelf().narrow());
            result.getWorkerRef().tell(nextRequest);
        }

        return this;
    }

    private Behavior<Message> handle(SubsetCheckRequest request) {
        this.getContext().getLog().info("received subset check request for {}", request.getCandidate());

        Table.Attribute attrA = request.getCandidate().getAttributeA();
        Table.Attribute attrB = request.getCandidate().getAttributeB();

        // if we don't have any data yet, it's fine to start out with an empty AttributeState
        AttributeState stateB = this.attributeStates.computeIfAbsent(attrB, _attrB -> new AttributeState(arrayFactory, setFactory));

        if (request.getRemoteRef().isPresent() && !request.getRemoteRef().equals(this.getContext().getSelf())) {
            this.getContext().getLog().info("querying remote data worker {} for subset check", request.getRemoteRef().get().toString());
            // TODO check if identical subset-check-req is already present
            this.remoteSubsetChecks
                .computeIfAbsent(attrA, _attrA -> new ArrayList<>())
                .add(new PendingSubsetCheck(request));
            SetQueryRequest queryRequest = new SetQueryRequest(attrA, Optional.empty(), this.workerId, this.getContext().getSelf().narrow());
            request.remoteRef.get().tell(queryRequest);
            return this;
        }

        AttributeState stateA = this.attributeStates.computeIfAbsent(attrA, _attrA -> new AttributeState(arrayFactory, setFactory));
        CandidateStatus status;
        if (stateB.oldSegmentSet.containsAll(stateA.oldSegmentSet.queryAll())) {
                this.getContext().getLog().info("subset check {} succeeded: [{}] c [{}]",
                    request.getCandidate(),
                    stateA.metadata.getMinMax().stream().map(Object::toString).collect(Collectors.joining("..")),
                    stateB.metadata.getMinMax().stream().map(Object::toString).collect(Collectors.joining("..")));
            status = CandidateStatus.succeededCheck();
        } else {
                this.getContext().getLog().info("subset check {} failed: [{}] ⊈ [{}]",
                    request.getCandidate(),
                    stateA.metadata.getMinMax().stream().map(Object::toString).collect(Collectors.joining("..")),
                    stateB.metadata.getMinMax().stream().map(Object::toString).collect(Collectors.joining("..")));
            status = CandidateStatus.failedCheck();
        }
        SubsetCheckResult result = new SubsetCheckResult(request.getCandidate(), status, this.workerId);
        request.getResultRef().tell(result);

        return this;
    }
}
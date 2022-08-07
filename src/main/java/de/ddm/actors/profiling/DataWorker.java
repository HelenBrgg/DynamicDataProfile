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
import de.ddm.actors.profiling.Planner.SubsetCheckResult;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.structures.AttributeState;
import de.ddm.structures.Candidate;
import de.ddm.structures.CandidateStatus;
import de.ddm.structures.ColumnArray;
import de.ddm.structures.ColumnSet;
import de.ddm.structures.Metadata;
import de.ddm.structures.SetDiff;
import de.ddm.structures.Table;
import de.ddm.structures.Value;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static class MergeRequest implements Message {
        private static final long serialVersionUID = 0xDADA_0002;

        private ActorRef<Planner.MergeResultPart> resultRef;
    }

    @AllArgsConstructor
    @Getter
    public static class SetQueryRequest implements Message {
        private static final long serialVersionUID = 0xDADA_0003;

        private Table.Attribute attribute;
        private Optional<Value> fromValue;

        private ActorRef<SetQueryResult> resultRef;
    }

    @AllArgsConstructor
    @Getter
    public static class SetQueryResult implements Message {
        private static final long serialVersionUID = 0xDADA_0004;

        private Table.Attribute attribute;
        private List<Value> values; // TODO should this be Stream<Value>?
        private boolean endOfSet;

        private int workerId;

        public Optional<Value> lastValue() {
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

    // grouped by A of A c B (A is stored remotely)
    private final Map<Table.Attribute, List<SubsetCheckRequest>> remoteSubsetChecks = new HashMap<>();


    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static Behavior<Message> create(
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
        this.getContext().getLog().info("received merge request, aborting all subset-checks");

        this.remoteSubsetChecks.clear();

        // FIXME hack
        int[] partsLeft = new int[]{ this.attributeStates.size() };
        this.attributeStates.forEach((attr, state) -> {
            SetDiff diff = state.mergeSegments();
            boolean additions = !diff.getInserted().isEmpty();
            boolean removals = !diff.getRemoved().isEmpty();
            partsLeft[0] -= 1;
            Planner.MergeResultPart result = new Planner.MergeResultPart(attr, additions, removals, state.metadata, partsLeft[0] == 0, this.workerId);
            request.resultRef.tell(result);
        });

        return this;
    }

    private Behavior<Message> handle(SetQueryRequest request) {
        if (!this.attributeStates.containsKey(request.attribute)) {
            SetQueryResult result = new SetQueryResult(request.attribute, List.of(), true, this.workerId);
            request.resultRef.tell(result);
            return this;
        }

        AttributeState state = this.attributeStates.get(request.attribute);

        List<Value> values = state.oldSegmentSet.queryChunk(request.fromValue).collect(Collectors.toList());
        boolean hasMore = !values.isEmpty() && state.metadata.getMax().map((max) -> values.get(values.size() - 1).isLessThan(max)).orElse(false);
        
        SetQueryResult result = new SetQueryResult(request.attribute, values, hasMore, this.workerId);
        request.resultRef.tell(result);

        return this;
    }

    private Behavior<Message> handle(SetQueryResult queryResult) {
        if (!this.attributeStates.containsKey(queryResult.attribute)) {
            return this;
        }

        List<SubsetCheckRequest> attrSubsetChecks = this.remoteSubsetChecks.get(queryResult.getAttribute());

        // update all running checks on this dependent attribute
        attrSubsetChecks.removeIf(checkRequest -> {
            AttributeState stateB = this.attributeStates.get(checkRequest.candidate.getAttributeB());

            if (!stateB.oldSegmentSet.containsAll(queryResult.values.stream())) {
                CandidateStatus status = CandidateStatus.failedCheck();
                SubsetCheckResult checkResult = new SubsetCheckResult(checkRequest.candidate, status, this.workerId);
                checkRequest.resultRef.tell(checkResult);
                return true;
            }

            if (queryResult.isEndOfSet()) {
                CandidateStatus status = CandidateStatus.succeededCheck();
                SubsetCheckResult result = new SubsetCheckResult(checkRequest.candidate, status, this.workerId);
                checkRequest.resultRef.tell(result);
                return true;
            }

            return false;
        });

        // if there are remote values left and are any remote checks left, we need to query more
        if (!queryResult.isEndOfSet() && !remoteSubsetChecks.isEmpty()) {
            SetQueryRequest nextQuery = new SetQueryRequest(queryResult.getAttribute(), queryResult.lastValue(), this.getContext().getSelf().narrow());

            attrSubsetChecks.forEach(checkRequest -> checkRequest.remoteRef.get().tell(nextQuery));
        }

        return this;
    }

    private Behavior<Message> handle(SubsetCheckRequest request) {
        this.getContext().getLog().info("received subset-check request for {}, remote={}", request.getCandidate(), request.getRemoteRef().isPresent());

        Table.Attribute attrA = request.getCandidate().getAttributeA();
        Table.Attribute attrB = request.getCandidate().getAttributeB();

        // if we don't have any data yet, it's fine to start out with an empty AttributeState
        AttributeState stateB = this.attributeStates.computeIfAbsent(attrB, _attrB -> new AttributeState(arrayFactory, setFactory));

        if (request.getRemoteRef().isPresent()) {
            // TODO check if identical subset-check-req is already present
            this.remoteSubsetChecks
                .computeIfAbsent(attrA, _attrA -> new ArrayList<>())
                .add(request);
            SetQueryRequest queryRequest = new SetQueryRequest(attrA, Optional.empty(), this.getContext().getSelf().narrow());
            request.remoteRef.get().tell(queryRequest);
            return this;
        }


        AttributeState stateA = this.attributeStates.computeIfAbsent(attrA, _attrA -> new AttributeState(arrayFactory, setFactory));
        CandidateStatus status;
        if (stateB.oldSegmentSet.containsAll(stateA.oldSegmentSet.queryAll())) {
            status = CandidateStatus.succeededCheck();
        } else {
            status = CandidateStatus.failedCheck();
        }
        SubsetCheckResult result = new SubsetCheckResult(request.getCandidate(), status, this.workerId);
        request.getResultRef().tell(result);

        return this;
    }
}
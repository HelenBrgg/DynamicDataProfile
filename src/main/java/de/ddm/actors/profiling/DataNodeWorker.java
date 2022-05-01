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
import de.ddm.profiler.*;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.structures.Column;
import de.ddm.structures.InclusionDependency;
import de.ddm.structures.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

public class DataNodeWorker extends AbstractBehavior<DataNodeWorker.Message> {

	////////////////////
	// Actor Messages //
	////////////////////

	public interface Message extends AkkaSerializable, LargeMessageProxy.LargeMessage {
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ReceptionistListingMessage implements Message {
		private static final long serialVersionUID = -5246338806092216222L;
		Receptionist.Listing listing;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EnqueueUpdatesMessage implements Message {
		private static final long serialVersionUID = 1;
		public String attribute;
		public SetCommand cmds;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MergeTriggerMessage implements Message {
		private static final long serialVersionUID = 6;
		String Attribute;
		ActorRef<Master.SetChangeMessage> master;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SubsetTriggerMessage implements Message {
		private static final long serialVersionUID = 9;
		String Attribute;
		ActorRef<Master.SubsetCheckResultMessage> master;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MetadataRequestMessage implements Message {
		private static final long serialVersionUID = 2;
		String Attribute;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MetadataRequestedMessage implements Message {
		private static final long serialVersionUID = 2;
		String Attribute;
		ActorRef<DataNodeWorker.MetadataRequestMessage> requestor; // meine eigene??
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MetadataReceivedMessage implements Message {
		private static final long serialVersionUID = 3;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SendMetadataMessage implements Message {
		private static final long serialVersionUID = 11;
		String attribute;
		Metadata metadata;
	}

	@AllArgsConstructor
	public static class QueryRequestMessage implements Message {
		private static final long serialVersionUID = 4;
		public String Attribute;
	}

	@AllArgsConstructor
	public static class QueryRequestedMessage implements Message {
		private static final long serialVersionUID = 10;
		public String Attribute;
		ActorRef<DataNodeWorker.QueryRequestMessage> requestor;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QueryReceivedMessage implements Message {
		private static final long serialVersionUID = 5;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SendQueryResultsMessage implements Message {
		private static final long serialVersionUID = 5;
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
	Map<String, ActorRef<SubsetCheckResult>> pendingSubsetChecks;

	////////////////////
	// Actor Behavior //
	////////////////////

	@Override
	public Receive<Message> createReceive() {
		return newReceiveBuilder()
				.onMessage(ReceptionistListingMessage.class, this::handle)
				.onMessage(EnqueueUpdatesMessage.class, this::handle)
				.onMessage(MergeTriggerMessage.class, this::handle)
				.onMessage(SubsetTriggerMessage.class, this::handle)
				.onMessage(MetadataRequestedMessage.class, this::handle)
				.onMessage(QueryRequestedMessage.class, this::handle)
				.onMessage(QueryReceivedMessage.class, this::handle)
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
	private Behavior<Message> handle(EnqueueUpdatesMessage message) {
		this.attributes.
		return this;
	}

	private Behavior<Message> handle(MergeTriggerMessage message) {
		return this;
	}

	private Behavior<Message> handle(SubsetTriggerMessage message) {
		return this;
	}

	private Behavior<Message> handle(MetadataRequestedMessage message) {
		return this;
	}

	private Behavior<Message> handle(QueryRequestedMessage message) {
		return this;
	}

	private Behavior<Message> handle(QueryReceivedMessage message) {
		return this;
	}

}

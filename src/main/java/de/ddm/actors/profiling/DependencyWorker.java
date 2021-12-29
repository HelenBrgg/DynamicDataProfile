package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import de.ddm.actors.patterns.LargeMessageProxy;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.structures.Column;
import de.ddm.structures.InclusionDependency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

public class DependencyWorker extends AbstractBehavior<DependencyWorker.Message> {

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
	public static class TaskMessage implements Message {
		private static final long serialVersionUID = -4667745204456518160L;
		ActorRef<LargeMessageProxy.Message> dependencyMinerLargeMessageProxy;

		String tableNameA;
		String tableNameB;
		List<String> headerA;
		List<String> headerB;
		List<Column> columnsA;
		List<Column> columnsB;

		public int getMemorySize() {
			return columnsA.stream().mapToInt(col -> col.getMemorySize()).sum() + columnsB.stream().mapToInt(col -> col.getMemorySize()).sum();
		}
	}

	////////////////////////
	// Actor Construction //
	////////////////////////

	public static final String DEFAULT_NAME = "dependencyWorker";

	public static Behavior<Message> create() {
		return Behaviors.setup(DependencyWorker::new);
	}

	private DependencyWorker(ActorContext<Message> context) {
		super(context);

		final ActorRef<Receptionist.Listing> listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ReceptionistListingMessage::new);
		context.getSystem().receptionist().tell(Receptionist.subscribe(DependencyMiner.dependencyMinerService, listingResponseAdapter));

		this.largeMessageProxy = this.getContext().spawn(LargeMessageProxy.create(this.getContext().getSelf().unsafeUpcast()), LargeMessageProxy.DEFAULT_NAME);
	}

	/////////////////
	// Actor State //
	/////////////////

	private final ActorRef<LargeMessageProxy.Message> largeMessageProxy;

	////////////////////
	// Actor Behavior //
	////////////////////

	@Override
	public Receive<Message> createReceive() {
		return newReceiveBuilder()
				.onMessage(ReceptionistListingMessage.class, this::handle)
				.onMessage(TaskMessage.class, this::handle)
				.build();
	}

	private Behavior<Message> handle(ReceptionistListingMessage message) {
		Set<ActorRef<DependencyMiner.Message>> dependencyMiners = message.getListing().getServiceInstances(DependencyMiner.dependencyMinerService);
		for (ActorRef<DependencyMiner.Message> dependencyMiner : dependencyMiners)
			dependencyMiner.tell(new DependencyMiner.RegistrationMessage(this.getContext().getSelf(), this.largeMessageProxy));
		return this;
	}

	private static Map<String, Set<String>> extractUniqueValues(List<String> header, List<Column> columns) {
		Map<String, Set<String>> unique = new HashMap<>();
		for (int i = 0; i < header.size(); ++i) {
			unique.put(header.get(i), columns.get(i).getUniqueValues());
		}
		return unique;
	}


	private Behavior<Message> handle(TaskMessage message) {
		this.getContext().getLog().info(
			"Received task table {} with {} columns and {} lines, task table {} with {} columns and {} lines (memory size {})",
			message.tableNameA, message.headerA.size(), message.columnsA.get(0).size(),
			message.tableNameB, message.headerB.size(), message.columnsB.get(0).size(),
			message.getMemorySize());

		Map<String, Set<String>> uniqueValuesA = extractUniqueValues(message.headerA, message.columnsA);
		Map<String, Set<String>> uniqueValuesB = extractUniqueValues(message.headerB, message.columnsB);

		List<InclusionDependency> inclusionDeps = new ArrayList<>();
		uniqueValuesA.forEach((columnA, setA) -> {
			uniqueValuesB.forEach((columnB, setB) -> {
				int cardinalityA = setA.size();
				int cardinalityB = setB.size();

				// NOTE both or none of these branches may be executed
				if (cardinalityA <= cardinalityB && setB.containsAll(setA)) {
					inclusionDeps.add(new InclusionDependency(message.tableNameA, message.tableNameB, columnA, columnB));
				}
				if (cardinalityB <= cardinalityA && setA.containsAll(setB)) {
					inclusionDeps.add(new InclusionDependency(message.tableNameB, message.tableNameA, columnB, columnA));
				}
			});
		});

		this.getContext().getLog().info(
			"Found {} INDs for table {} and table {}: {}",
			inclusionDeps.size(), message.tableNameA, message.tableNameB, inclusionDeps);

		LargeMessageProxy.LargeMessage completionMessage = new DependencyMiner.CompletionMessage(this.getContext().getSelf(), inclusionDeps);
		this.largeMessageProxy.tell(new LargeMessageProxy.SendMessage(completionMessage, message.getDependencyMinerLargeMessageProxy()));

		return this;
	}
}

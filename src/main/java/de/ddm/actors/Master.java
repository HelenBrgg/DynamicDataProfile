package de.ddm.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import de.ddm.actors.patterns.Reaper;
import de.ddm.actors.profiling.DataDistributor;
import de.ddm.actors.profiling.DataWorker;
import de.ddm.actors.profiling.InputWorker;
import de.ddm.actors.profiling.Planner;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.singletons.SystemConfigurationSingleton;
import de.ddm.structures.CandidateGenerator;
import de.ddm.structures.CsvLogSink;
import de.ddm.structures.DataGeneratorSource;
import de.ddm.structures.HeapColumnArray;
import de.ddm.structures.HeapColumnSet;
import de.ddm.structures.ModuloPartitioningStrategy;
import de.ddm.structures.PartitioningStrategy;
import de.ddm.structures.Sink;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class Master extends AbstractBehavior<Master.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable {
    }

    @NoArgsConstructor
    public static class StartMessage implements Message {
        private static final long serialVersionUID = -1963913294517850454L;
    }

    @NoArgsConstructor
    public static class ShutdownMessage implements Message {
        private static final long serialVersionUID = 7516129288777469221L;
    }

    /////////////////
    // Actor State //
    /////////////////

    private ActorRef<DataWorker.Message> dataWorker;
    private ActorRef<InputWorker.Message> inputWorker;
    private ActorRef<Planner.Message> planner;

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static final String DEFAULT_NAME = "master";

    public static Behavior<Message> create() {
        return Behaviors.setup(Master::new);
    }

    private Master(ActorContext<Message> context) {
        super(context);
        // Reaper.watchWithDefaultReaper(this.getContext().getSelf());

        // this.dataWorker = context.spawn(DataWorker.create(1, HeapColumnArray::new, HeapColumnSet::new), "data-worker");
        int numWorkers = SystemConfigurationSingleton.get().getNumWorkers();
        PartitioningStrategy partitioning = new ModuloPartitioningStrategy(numWorkers);
        this.dataWorker = context.spawn(DataDistributor.create(HeapColumnArray::new, HeapColumnSet::new, partitioning, numWorkers), "data-distributor");
        this.inputWorker = context.spawn(InputWorker.create(this.dataWorker.narrow()), "input-worker");
        

        // TODO move this into constructor
        Sink sink = null;
        try {
             sink = new CsvLogSink();
        } catch (Exception ex) {
            System.out.println("failed to create csv log sink" + ex.toString());
            System.exit(1);
        }
        this.planner = context.spawn(Planner.create(sink, this.inputWorker.narrow(), this.dataWorker.narrow()), "planner");

        // NOTE planner terminates once inputWorker terminates and all work is done
        this.getContext().watchWith(this.planner, new ShutdownMessage());

        // FIXME this is rather dirty hack so we dont have to implement shutdown everywhere
        Reaper.watchWithDefaultReaper(this.getContext().getSelf());
    }

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartMessage.class, this::handle)
                .onMessage(ShutdownMessage.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(StartMessage message) {
        // this.getContext().scheduleOnce(Duration.ofMillis((int) this.pollDelayMillis), this.getContext().getSelf(), new PollingMessage());
        return this;
    }

    private Behavior<Message> handle(ShutdownMessage message) {
        // If we expect the system to still be active when the a ShutdownMessage is issued,
        // we should propagate this ShutdownMessage to all active child actors so that they
        // can end their protocols in a clean way. Simply stopping this actor also stops all
        // child actors, but in a hard way!
        return Behaviors.stopped();
    }
}

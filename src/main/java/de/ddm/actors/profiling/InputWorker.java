package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.singletons.InputConfigurationSingleton;
import de.ddm.structures.DataGeneratorSource;
import de.ddm.structures.Source;
import de.ddm.structures.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InputWorker extends AbstractBehavior<InputWorker.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable {}

    @AllArgsConstructor
    @Getter
    public static class AdjustPollFrequency implements Message {
        private static final long serialVersionUID = 0x1290_0001;

        private float multiplier;
    }

    @AllArgsConstructor
    @Getter
    public static class PollingMessage implements Message {
        private static final long serialVersionUID = 0x1290_0002;
    }

    //////////////////
    // Actor State //
    /////////////////

    private List<Source> sources = new ArrayList<>();
    private final ActorRef<DataWorker.NewBatchMessage> dataWorker;
    private float pollDelayMillis = 1f;

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static Behavior<Message> create(ActorRef<DataWorker.NewBatchMessage> dataWorker) {
        return Behaviors.setup(ctx -> new InputWorker(ctx, dataWorker));
    }

    private InputWorker(
        ActorContext<InputWorker.Message> context, 
        ActorRef<DataWorker.NewBatchMessage> dataWorker
    ){
        super(context);

        String[] env = new String[0]; // unused for now
        List<String[]> commands = InputConfigurationSingleton.get().getDataGeneratorCommands();
        try {
            for (String[] command : commands){
                this.sources.add(new DataGeneratorSource(env, command));
            }
        } catch (IOException ex) {
            System.out.println("failed to create data generator source" + ex.toString());
            System.exit(1);
        }

        this.dataWorker = dataWorker.narrow();
        // kickoff polling events
        this.getContext().scheduleOnce(Duration.ofMillis((int) this.pollDelayMillis), this.getContext().getSelf(), new PollingMessage());
    }

    @Override
    public Receive<InputWorker.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(PollingMessage.class, this::handle)
                .onMessage(AdjustPollFrequency.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(PollingMessage _message) {
        // even if all sources are finished, we still may have gotten new batches
        boolean hasNewBatches = false;
        for (Source source: sources) {
            Optional<Table> batch = source.nextTable();
            if (batch.isEmpty())
                continue;

            hasNewBatches = true;
            // TODO use LargeMessageProxy if remote and large-msg-proxy enabled
            this.dataWorker.tell(new DataWorker.NewBatchMessage(batch.get()));
        }

        this.sources.removeIf(source -> source.isFinished());

        if (sources.isEmpty() && !hasNewBatches) {
            this.getContext().getLog().info("all sources are exhausted and no more batches");
            return Behaviors.stopped();
        }

        // schedule next polling
        this.getContext().scheduleOnce(Duration.ofMillis((int) this.pollDelayMillis), this.getContext().getSelf(), new PollingMessage());

        return this;
    }

    private Behavior<Message> handle(AdjustPollFrequency freq) {
        this.getContext().getLog().info("adjusting poll frequency by {}", freq.getMultiplier());
        
        assert freq.getMultiplier() > 0.0;
        this.pollDelayMillis *= freq.getMultiplier();

        return this;
    }

}

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InputWorker extends AbstractBehavior<InputWorker.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable {
    }

    public static class IdleMessage implements Message {
        private static final long serialVersionUID = 0x1290_0001;
    }

    //////////////////
    // Actor State //
    /////////////////

    private List<Source> sources = new ArrayList<>();
    private final ActorRef<DataWorker.NewBatchMessage> dataWorker;

    public static Behavior<Message> create(ActorRef<DataWorker.NewBatchMessage> dataWorker) {
        return Behaviors.setup(ctx -> new InputWorker(ctx, dataWorker));
    }

    private InputWorker(
        ActorContext<InputWorker.Message> context, 
        ActorRef<DataWorker.NewBatchMessage> dataWorker
    ){

        super(context);

        List<String[]> commands = InputConfigurationSingleton.get().getDataGeneratorCommands();
        String[] env = InputConfigurationSingleton.get().getDataGeneratorEnv();
        try {
            for (String[] command : commands){
                this.sources.add(new DataGeneratorSource(env, command));
            }
        } catch (IOException ex) {
            System.out.println("failed to create data generator source" + ex.toString());
            System.exit(1);
        }

        this.dataWorker = dataWorker.narrow();
    }

    @Override
    public Receive<InputWorker.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(IdleMessage.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(IdleMessage message) {
        this.getContext().getLog().info("received idle messsage");

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

        if (sources.isEmpty() && !hasNewBatches)
            return Behaviors.stopped();

        return this;
    }

}

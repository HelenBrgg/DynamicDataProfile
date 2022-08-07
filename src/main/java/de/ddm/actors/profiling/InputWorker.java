package de.ddm.actors.profiling;

import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import de.ddm.serialization.AkkaSerializable;
import de.ddm.structures.Source;
import de.ddm.structures.Table;

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

    private final Source source;
    private final ActorRef<DataWorker.NewBatchMessage> dataWorker;

    public static Behavior<Message> create(Source source, ActorRef<DataWorker.NewBatchMessage> dataWorker) {
        return Behaviors.setup(ctx -> new InputWorker(ctx, source, dataWorker));
    }

    private InputWorker(
        ActorContext<InputWorker.Message> context, 
        Source source,
        ActorRef<DataWorker.NewBatchMessage> dataWorker
    ){
        super(context);
        this.source = source;
        this.dataWorker = dataWorker.narrow();
    }

    @Override
    public Receive<InputWorker.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(IdleMessage.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(IdleMessage message) {
        Optional<Table> batch = this.source.nextTable();

        if (batch.isEmpty()) {
            if (source.isFinished()) return Behaviors.stopped();
            return this;
        }

        this.dataWorker.tell(new DataWorker.NewBatchMessage(batch.get()));
        // TODO use LargeMessageProxy if remote and large-msg-proxy enabled

        return this;
    }

}

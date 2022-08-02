package de.ddm.actors.profiling;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import de.ddm.actors.patterns.LargeMessageProxy;
import de.ddm.profiler.Source;
import de.ddm.profiler.Table;
import de.ddm.serialization.AkkaSerializable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InputWorker extends AbstractBehavior<InputWorker.Message> {

    ////////////////////
    // Actor Messages //
    ////////////////////

    public interface Message extends AkkaSerializable, LargeMessageProxy.LargeMessage {
    }

    public static class IdleMessage implements Message {
        private static final long serialVersionUID = 3898968967564674L;
    }

    //////////////////
    // Actor State //
    /////////////////

    private final Source source;
    private final ActorRef<DataNodeWorker.Message> dataWorker;

    public static Behavior<Message> create(Source source, ActorRef<DataNodeWorker.Message> dataWorker) {
        return Behaviors.setup(ctx -> new InputWorker(ctx, source, dataWorker));
    }

    private InputWorker(ActorContext<InputWorker.Message> context, Source source,
            ActorRef<DataNodeWorker.Message> dataWorker) {
        super(context);
        this.source = source;
        this.dataWorker = dataWorker;
    }

    @Override
    public Receive<InputWorker.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(IdleMessage.class, this::handle)
                .build();
    }

    private Behavior<Message> handle(IdleMessage message) {
        Table table = source.nextTable();
        if (table == null)
            return Behaviors.stopped();

        return this;
    }

}

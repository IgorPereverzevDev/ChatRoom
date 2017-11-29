import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.japi.pf.ReceiveBuilder;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.InetSocketAddress;

/**
 * Room
 */
class Room extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private String id;
    private final int maxNumberOfMessages = 128;
    private ActorRef tcpActor;
    CircularFifoQueue<Messages.PublishToChatRoom> chatHistory = new CircularFifoQueue<>(maxNumberOfMessages);

    public Room(String id) {
        this.id = id;

    }

    public Room(ActorRef tcpActor) {
        this.tcpActor = tcpActor;
    }

    static Props props(String id) {
        // the actual type of the returned actor
        return Props.create(Room.class, () -> new Room(id));
    }

    public void preStart() throws Exception {
        if (tcpActor == null) {
            tcpActor = Tcp.get(getContext().system()).manager();
        }
        tcpActor.tell(TcpMessage.bind(getSelf(),
                new InetSocketAddress("localhost", 8080), 100), getSelf());
    }

    //Handling input messages
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Messages.newClientInRoom.class, msg -> getSender().tell(postChatHistoryToClients(msg), self()))
                .match(Messages.PublishToChatRoom.class, msg -> getSender().tell(sendMessageToClients(msg), self()))
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private boolean sendMessageToClients(Messages.PublishToChatRoom msg) {
        for (Client client : msg.client.subscribeClientsToRoom) {
            client.getSender().tell(msg.message, self());
        }
        chatHistory.add(msg);
        log.info("new message");
        return true;
    }

    private boolean postChatHistoryToClients(Messages.newClientInRoom msg) {
        msg.client.getSender().tell(chatHistory, self());
        msg.client.subscribeClientsToRoom.add(msg.client);
        return true;
    }

    String getId() {
        return id;
    }
}

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.japi.pf.ReceiveBuilder;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Server
 */
public class Server extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef tcpActor;
    Map<String, Client> joinedClient = new HashMap<>();

    public Server(ActorRef tcpActor) {
        this.tcpActor = tcpActor;
    }


    public static Props props(ActorRef tcpActor) {
        return Props.create(Server.class, tcpActor);
    }

    public void preStart() throws Exception {
        if (tcpActor == null) {
            tcpActor = Tcp.get(getContext().system()).manager();
        }
        tcpActor.tell(TcpMessage.bind(getSelf(),
                new InetSocketAddress("localhost", 8080), 100), getSelf());
    }

    //Handling input messages
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Messages.SubscribeToChatRoom.class, msg -> getSender().tell(joinChatRoom(msg), self()))
                .match(Messages.UnSubscribeFromChatRoom.class, msg -> getSender().tell(disJoinChatRoom(msg), self()))
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    boolean joinChatRoom(Messages.SubscribeToChatRoom msg) {
        if (isValidClientSubscribe(msg)) {
            //getting the client is history room
            for (Room room : msg.listRooms) {
                room.getSender().tell(Messages.newClientInRoom.class, self());
                joinedClient.put(msg.client.getUsername() + room.getId(), msg.client);
            }
            log.info("connect new client");
            return true;
        }
        return false;
    }

    boolean disJoinChatRoom(Messages.UnSubscribeFromChatRoom msg) {
        if (isValidClientUnSubscribe(msg)) {
            for (Room room : msg.listRooms) {
                room.getSender().tell(Messages.ClientOutRoom.class, self());
                joinedClient.remove(msg.client.getUsername());
            }
            log.info("client disconnect");
            return true;
        }
        return false;
    }

    private boolean isValidClientSubscribe(Messages.SubscribeToChatRoom msg) {
        return joinedClient.containsValue(msg.client);
    }

    private boolean isValidClientUnSubscribe(Messages.UnSubscribeFromChatRoom msg) {
        return joinedClient.containsValue(msg.client);
    }

}

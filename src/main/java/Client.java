import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.japi.pf.ReceiveBuilder;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
/**
 * Client
 */
public class Client extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private String username;

    public Client(ActorRef tcpActor) {
        this.tcpActor = tcpActor;
    }

    private ActorRef tcpActor;
    private InetSocketAddress remote;
    List<Client> subscribeClientsToRoom = new ArrayList<>();


    public Client(InetSocketAddress remote, ActorRef tcpActor, String username) {
        this.remote = remote;
        this.tcpActor = tcpActor;
        this.username = username;
        if (tcpActor == null) {
            tcpActor = Tcp.get(getContext().system()).manager();
        }
        tcpActor.tell(TcpMessage.connect(remote), getSelf());
    }

    String getUsername() {
        return username;
    }

    static Props props(InetSocketAddress remote, ActorRef tcpActor, String username) {
        // the actual type of the returned actor
        return Props.create(Client.class, () -> new Client(remote, tcpActor, username));
    }

    //Handling input messages
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Messages.PublishToChatRoom.class, msg -> getSender().tell(sendMessage(msg), self()))
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private boolean sendMessage(Messages.PublishToChatRoom msg) {
        Messages message = new Messages();
        msg.room.getSender().tell(message.getMaxLength(), self());
        log.info("client sended message");
        return true;
    }
}

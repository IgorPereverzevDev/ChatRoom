import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.apache.commons.cli.BasicParser;
import java.net.InetSocketAddress;
/**
 * CLI
 */
public class Runner extends BasicParser {
    public static void main(String[] args) {
        //Create hierarchy actors
        ActorSystem system = ActorSystem.create("ChatRoom");
        //Create references to actors
        ActorRef refServer = system.actorOf(Server.props(null), "Server");
        //tell to server
        refServer.tell(Messages.SubscribeToChatRoom.class, ActorRef.noSender());
        for (int i = 0; i <= args.length; ++i) {
            //Create references to actors
            ActorRef refClient = system.actorOf(Client.props(new InetSocketAddress("localhost", 8080),
                    null,args[i]));
            ActorRef refRoom = system.actorOf(Room.props(null),args[i]);
            //tell to server
            refClient.tell(Messages.PublishToChatRoom.class, ActorRef.noSender());
            refRoom.tell(Messages.newClientInRoom.class, ActorRef.noSender());
        }
    }
}

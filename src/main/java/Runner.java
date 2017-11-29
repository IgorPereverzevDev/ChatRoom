import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.net.InetSocketAddress;

/**
 * CLI
 */
public class Runner {
    public static void main(String[] args) {
        //Create hierarchy actors
        ActorSystem serverActorSystem = ActorSystem.create("ServerSystemChatRoom");
        ActorSystem clientActorSystem = ActorSystem.create("ClientSystemChatRoom");
        ActorSystem roomActorSystem = ActorSystem.create("RoomSystemChatRoom");
        //Create references to actors
        ActorRef refServer = serverActorSystem.actorOf(Server.props(null), "server");
        //tell to server
        refServer.tell(Messages.SubscribeToChatRoom.class, ActorRef.noSender());
        for (int i = 0; i <= args.length; ++i) {
            //Create references to actors
            ActorRef refClient = clientActorSystem.actorOf(Client.props(new InetSocketAddress("localhost", 8080),
                    null, args[i]));
            ActorRef refRoom = roomActorSystem.actorOf(Room.props(null), args[i]);
            //tell to server
            refClient.tell(Messages.PublishToChatRoom.class, ActorRef.noSender());
            refRoom.tell(Messages.newClientInRoom.class, ActorRef.noSender());
        }
    }
}

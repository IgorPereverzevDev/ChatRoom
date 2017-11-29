import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import akka.testkit.javadsl.TestKit;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


public class ChatRoomTest extends TestCase {

    static ActorSystem system = ActorSystem.apply();

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testShouldAddUserToSubscribeUsersWhenJoiningTest() {
        new TestKit(system) {
            {
                //Given a ChatRoom has no users
                ActorRef refServer = system.actorOf(Server.props(null), "server");
                ActorRef refClient = system.actorOf(Client.props(new InetSocketAddress("localhost", 8080),
                        null, "1"));

                final String room1 = "1";
                final String room2 = "2";

                List<Room> listRooms = new ArrayList<>();
                listRooms.add(new Room(room1));
                listRooms.add(new Room(room2));

                //When it receives a request from a client to join the chatRoom
                Client client = new Client(new InetSocketAddress("localhost", 8080),
                        null, "client");
                Messages.SubscribeToChatRoom request = new Messages.SubscribeToChatRoom(client, listRooms);
                refServer.tell(request, system.deadLetters());
                refClient.tell(request, system.deadLetters());

                //It should add the Client to its list of joined clients
                assertEquals(request.client.subscribeClientsToRoom.get(0), client);
            }
        };
    }

    @Test
    public void testShouldRemoveFromUsersWhenDisJoiningTest() {
        new TestKit(system) {
            {
                ActorRef refServer = system.actorOf(Server.props(null), "server");
                ActorRef refClient = system.actorOf(Client.props(new InetSocketAddress("localhost", 8080),
                        null, "1"));

                final String room = "1";

                List<Room> listRooms = new ArrayList<>();
                listRooms.add(new Room(room));

                //When it receives a request from a client to disJoin the chatRoom
                Client client = new Client(new InetSocketAddress("localhost", 8080),
                        null, "client");
                Messages.UnSubscribeFromChatRoom request = new Messages.UnSubscribeFromChatRoom(client, listRooms);
                refServer.tell(request, system.deadLetters());
                refClient.tell(request, system.deadLetters());

                //It should remove the Client from list of clients
                assertNull(request.client.subscribeClientsToRoom.get(0));
            }
        };
    }

    @Test
    public void testShouldSendHistoryWhenUserJoin() {
        new TestKit(system) {
            {
                //Given
                ActorRef refRoom = system.actorOf(Room.props(null), "1");
                ActorRef refClient = system.actorOf(Client.props(new InetSocketAddress("localhost", 8080),
                        null, "1"));

                //When it receives a request from a client to disJoin the chatRoom
                Client client = new Client(new InetSocketAddress("localhost", 8080),
                        null, "client");
                Room room = new Room("1");
                Messages message = new Messages();

                Messages.newClientInRoom request = new Messages.newClientInRoom(client);
                Messages.PublishToChatRoom msg = new Messages.PublishToChatRoom(client, room, message);

                room.chatHistory.add(msg);

                refRoom.tell(request, system.deadLetters());
                refClient.tell(request, system.deadLetters());

                List expected = new ArrayList<Messages.PublishToChatRoom>();
                expected.add(msg);
                expectMsgEquals(duration("1 second"), expected);
            }
        };
    }

    @Test
    public void testShouldAddMessageToRoomWhenPublishToChatMessageTest() {
        new TestKit(system) {{
            //Given a ChatRoom has no users
            ActorRef refRoom = system.actorOf(Room.props(null), "1");
            ActorRef refClient = system.actorOf(Client.props(new InetSocketAddress("localhost", 8080),
                    null, "1"));
            final String room = "1";
            Messages message = new Messages();

            //When publish a message from a client to chatRoom
            Client client = new Client(new InetSocketAddress("localhost", 8080),
                    null, "client");
            Messages.PublishToChatRoom request = new Messages.PublishToChatRoom(client, new Room(room), message);
            refClient.tell(request, system.deadLetters());
            refRoom.tell(request, system.deadLetters());

            //It should add the Client to its list of joined clients
            assertEquals(request.room.chatHistory.get(0).message, message);
        }};
    }
}
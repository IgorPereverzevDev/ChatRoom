import java.util.ArrayList;
import java.util.List;

/**
 * Messages
 */
class Messages {

    private final byte maxLength = (byte) 0xFE;

    /**
     * Server messages:
     * -Subscribe
     * -UnSubscribe
     * -newClientInRoom
     * -ClientOutRoom
     */
    static class SubscribeToChatRoom {
        final Client client;
        List<Room> listRooms = new ArrayList<>();

        public SubscribeToChatRoom(Client client, List<Room> listRooms) {
            this.listRooms = listRooms;
            this.client = client;
        }
    }

    static class UnSubscribeFromChatRoom {
        final Client client;
        List<Room> listRooms = new ArrayList<>();

        public UnSubscribeFromChatRoom(Client client, List<Room> listRooms) {
            this.client = client;
            this.listRooms = listRooms;
        }
    }

    static class newClientInRoom {
        final Client client;

        public newClientInRoom(Client client) {
            this.client = client;
        }
    }

    static class ClientOutRoom {
        final Client client;

        public ClientOutRoom(Client client) {
            this.client = client;
        }
    }

    /**
     * Client messages:
     * -PublishToChatRoom
     */
    static class PublishToChatRoom {
        final Client client;
        final Messages message;
        final Room room;

        public PublishToChatRoom(Client client, Room room, Messages message) {
            this.client = client;
            this.message = message;
            this.room = room;
        }
    }

    public byte getMaxLength() {
        return maxLength;
    }
}

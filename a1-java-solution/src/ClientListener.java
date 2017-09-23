import java.util.Set;

public interface ClientListener {
    boolean createChatRoom(String roomName );
//    void leaveChatRoom(ServerClientThread socketThread, ChatRoom currentRoom);
    boolean joinChatRoom(ServerClientThread socketThread, String roomName );
    Set<String> listChatRooms();
}

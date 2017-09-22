import java.util.Set;

public interface ClientListener {
    void createChatRoom(String roomName );
    void leaveChatRoom(ServerClientThread socketThread, ChatRoom currentRoom);
    void joinChatRoom(ServerClientThread socketThread, String roomName );
    Set<String> listChatRooms();
}

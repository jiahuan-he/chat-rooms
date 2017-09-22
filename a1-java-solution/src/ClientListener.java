import java.net.Socket;

public interface ClientListener {
    void createChatRoom(String roomName );
    void leaveChatRoom(ServerClientThread socketThread, ChatRoom currentRoom);
    void joinChatRoom(ServerClientThread socketThread, String roomName );
    void listChatRooms();
}

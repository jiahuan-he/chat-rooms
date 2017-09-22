import java.net.Socket;

public interface ClientListener {
    void createChatRoom(String name);
    void leaveChatRoom(ServerClientThread socketThread,ChatRoom currentRoom);
    void joinChatRoom(String name);
    void listChatRooms();
}

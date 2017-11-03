import java.util.Set;

public interface ClientListener {
    boolean createChatRoom(String roomName );
    boolean joinChatRoom(ServerClientThread socketThread, String roomName );
    Set<String> listChatRooms();
}

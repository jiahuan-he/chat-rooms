import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;


public class Server implements ClientListener{
    private int port = 8088;
    private HashMap<String, ChatRoom> chatRooms;
    private ServerSocket serverSocket;

    private void startServer() throws IOException {
        ChatRoom defautRoom = new ChatRoom("default-room");
        chatRooms = new HashMap<>();
        chatRooms.put(defautRoom.name, defautRoom);
        serverSocket = new ServerSocket(port);

        Socket newSocket;
        while ((newSocket = serverSocket.accept())!= null){
            ServerClientThread newClient = new ServerClientThread(newSocket, defautRoom, this);
            defautRoom.connectedSockets.add(newClient);
            newClient.start();
        }
    }

    @Override
    public void createChatRoom(String name) {
        this.chatRooms.put( name,new ChatRoom(name));
    }

    @Override
    public void leaveChatRoom(ServerClientThread socket, ChatRoom currentRoom) {
        if(!currentRoom.connectedSockets.remove(socket)){
            throw new java.lang.RuntimeException("Leave chat room failed");
        }
    }

    @Override
    public void joinChatRoom(ServerClientThread clientThread ,String roomName) {
        chatRooms.get(roomName).connectedSockets.add(clientThread);
    }

    @Override
    public void listChatRooms() {

    }

    public static void main(String argus[]){
        try {
            new Server().startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

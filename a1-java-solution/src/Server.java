import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;


public class Server {
    private int port = 8088;
    private LinkedList<ChatRoom> chatRooms;
    private ServerSocket serverSocket;

    private void startServer() throws IOException {
        ChatRoom defautRoom = new ChatRoom("public-room");
        chatRooms = new LinkedList<>();
        chatRooms.add(defautRoom);
        serverSocket = new ServerSocket(port);


        while (true){
            Socket newSocket = serverSocket.accept();
            SocketThread newClient = new SocketThread(newSocket, defautRoom);
            defautRoom.connectedSockets.add(newClient);
            newClient.start();
        }
    }


    public static void main( String argus[]){
        try {
            new Server().startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

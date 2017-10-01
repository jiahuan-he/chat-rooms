import sun.rmi.server.InactiveGroupException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


public class Server implements ClientListener{
    private int port = 8088;
    HashMap<String, ChatRoom> chatRooms;
    private ServerSocket serverSocket;

    private void startServer(String argus[]) throws IOException {

        ChatRoom defautRoom = new ChatRoom("default-room");
        chatRooms = new HashMap<>();
        chatRooms.put(defautRoom.name, defautRoom);

        if(argus == null || argus.length==0){
            serverSocket = new ServerSocket(port);
        }
        else{
            serverSocket = new ServerSocket(Integer.parseInt(argus[0]));
        }

        System.out.println("Socket listening on "+serverSocket.getLocalPort());
        Socket newSocket;
        while ((newSocket = serverSocket.accept())!= null){
            System.out.println("New connection");
            ServerClientThread newClient = new ServerClientThread(newSocket, defautRoom, this);
            defautRoom.connectedSockets.add(newClient);
            newClient.start();
        }
    }

    @Override
    public boolean createChatRoom(String name) {
        if(chatRooms.containsKey(name)){
            return false;
        }
        this.chatRooms.put( name, new ChatRoom(name));
        return true;
    }

//    @Override
//    public void leaveChatRoom(ServerClientThread socket, ChatRoom currentRoom) {
//        if(!currentRoom.connectedSockets.remove(socket)){
//            throw new java.lang.RuntimeException("Leave chat room error!");
//        }
//    }

    @Override
    public boolean joinChatRoom(ServerClientThread clientThread ,String roomName) {
        if(chatRooms.containsKey(roomName)){
            if(chatRooms.get(roomName).connectedSockets.add(clientThread)){
                clientThread.chatRooms.put(roomName,chatRooms.get(roomName));
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> listChatRooms() {
        return chatRooms.keySet();

    }

    public static void main(String argus[]){
        try {
                new Server().startServer(argus);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

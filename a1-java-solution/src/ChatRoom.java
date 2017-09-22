import java.util.*;

public class ChatRoom {
    String name;
    LinkedList<SocketThread> connectedSockets = new LinkedList<>();
    ArrayList<String> history = new ArrayList<>();

    ChatRoom(String name){
        this.name = name;
    }
}



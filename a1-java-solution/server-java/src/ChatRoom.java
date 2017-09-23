import java.util.*;

public class ChatRoom {
    String name;
    LinkedList<ServerClientThread> connectedSockets = new LinkedList<>();
    ArrayList<String> history = new ArrayList<>();

    ChatRoom(String name){
        this.name = name;
    }
}



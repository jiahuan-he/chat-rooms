import java.util.ArrayList;
import java.util.HashMap;

public class ChatRoom {
    String roomName;
    HashMap<String, ServerClient> clients = new HashMap<>();
    ArrayList<String> history = new ArrayList<>();
    ChatRoom(String str){
        this.roomName = str;
    }
}


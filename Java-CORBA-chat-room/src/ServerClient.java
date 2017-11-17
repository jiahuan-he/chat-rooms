import java.util.ArrayList;
import java.util.HashMap;

public class ServerClient {
    String clientName;
    HashMap<String, ChatRoom> joinedRooms = new HashMap<>();
    ChatRoom currnetRoom;
    ArrayList<String> messageQueue = new ArrayList<>();

    ServerClient(String str){
        this.clientName = str;
    }
}

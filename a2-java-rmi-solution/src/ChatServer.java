import java.util.HashMap;

class Room{
    String name;
    HashMap<String, Client> clients;
}

public class ChatServer implements Server{
    HashMap<String, Client> clients = new HashMap<>();
    HashMap<String, Room> rooms = new HashMap<>();

    @Override
    public void rename(String name) {
        if (clients.get(name) == null){
            clients.put(name, new ChatClient(name));
        } else {
            ((ChatClient)clients.get(name)).name = name;
        }
    }

    @Override
    public void connect() {

    }

    @Override
    public void sendTo() {

    }

    @Override
    public void joinRoom(String room, String client) {

    }

    @Override
    public void leaveRoom(String room, String client) {

    }

    @Override
    public void createRoom(String room) {

    }

    @Override
    public void listRoom() {

    }

    @Override
    public void broadcast(String message, String client) {

    }
}

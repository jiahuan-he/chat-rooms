import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote{

    void rename(String name);
    void connect();
    void sendTo();
    void joinRoom(String room, String client);
    void leaveRoom(String room, String client);
    void createRoom(String room);
    void listRoom();
    void broadcast(String message, String client);




}

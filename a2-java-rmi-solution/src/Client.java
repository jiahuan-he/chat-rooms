import java.rmi.Remote;

public interface Client extends Remote{
    void print(String message);
    void rename();
}

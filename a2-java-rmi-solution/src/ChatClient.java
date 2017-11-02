import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ChatClient implements Client{
    Server server;
    String name;

    ChatClient(String name){
        this.name = name;
    }

    @Override
    public void print(String message) {

    }

    @Override
    public void rename() {

    }

    public void main(String[] argus){
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            // host is 1099 by default
            Registry registry = LocateRegistry.getRegistry();
            Hello stub = (Hello) registry.lookup("Hello");
            String response = stub.sayHello();
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }


    }
}

package client;

import server.ChatServer;
import server.Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ChatClient extends UnicastRemoteObject implements Client{
    Server server;
    public String name;
    String currentRoom;

    ChatClient(Server server, String name) throws RemoteException{
        this.server = server;
        this.name = name;
    }

    @Override
    public void print(String message) {
        System.out.println(message);
    }

    @Override
    public void rename() {

    }

    @Override
    public String getName() throws RemoteException {
        return this.name;
    }

    public static void main(String[] argus){
        try {
            // host is 1099 by default
            Registry registry = LocateRegistry.getRegistry();
            Server stubServer = (Server) registry.lookup("Server");

            Scanner scanner = new Scanner(System.in);
            System.out.println("System => Please enter your name: ");
            String name = scanner.next();
            ChatClient client = new ChatClient(stubServer, name);
            stubServer.connect(client);
            String line;
            while ((line = scanner.next())!= null){
                stubServer.broadcast(line, client.getName(), ChatServer.type.MESSAGE);
            }




        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

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
            if (!stubServer.connect(client)){
                System.out.println("Connect failed");
                return;
            };
            String line;
            while ((line = scanner.nextLine())!= null){
                String trimmedLine = line.trim();

                if (trimmedLine.equals("\n") || trimmedLine.equals("")){
                    continue;
                }

                String[] sLine = trimmedLine.split(" ");
                int length = sLine.length;
                if (trimmedLine.startsWith("/create")){
                    if (length>1){
                        for (int i=1; i<length; i++){
                            stubServer.createRoom(sLine[i], client.name);
                        }
                    }
                } else if (trimmedLine.startsWith("/list")){
                    String[] rooms = stubServer.listRoom();
                    for (String s: rooms){
                        System.out.println(s);
                    }
                } else if (trimmedLine.startsWith("/join")){
                    if (length>1){
                        for (int i=1; i<length; i++){
                            stubServer.joinRoom(sLine[i], client.name);
                        }
                    } else {
                        System.out.println("System => Error: wrong parameter");
                    }
                } else if (trimmedLine.startsWith("/leave")){

                } else if (trimmedLine.startsWith("switch")){

                } else {
                    stubServer.broadcast(line, client.getName(), ChatServer.type.MESSAGE);
                }
            }




        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

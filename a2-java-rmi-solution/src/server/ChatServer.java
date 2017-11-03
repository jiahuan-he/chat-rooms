package server;

import client.Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

class Room{
    String name;
    HashMap<String, Client> clients = new HashMap<>();
    Room(String name){
        this.name = name;

    }
}

public class ChatServer implements Server{
    HashMap<String, Client> clients = new HashMap<>();
    HashMap<String, Room> rooms = new HashMap<>();
    HashMap<String, String> clientCurrentRoom = new HashMap<>();
    public enum type{
        MESSAGE,
        PLAIN
    }

    private ChatServer(){

        String room = "default-room";
        rooms.put(room, new server.Room(room));
    }

    @Override
    public void connect(Client client) {
        try {
            if (clients.get(client.getName()) == null){
                this.clients.put(client.getName(), client);
                rooms.get("default-room").clients.put(client.getName(), client);
                clientCurrentRoom.put(client.getName(), "default-room");
                broadcast(
                        "SYSTEM => Welcome new user \"" + client.getName()+"\" joining room \"default-room\"",
                        client.getName(),
                        type.PLAIN
                );

            } else {
                // TODO handle duplicate users

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("Current clients "+ this.clients.keySet().size());
    }


    @Override
    public void sendTo(String message, String client) {
        try {
            clients.get(client).print(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void joinRoom(String room, String client) {

    }

    @Override
    public void leaveRoom(String room, String client) {

    }

    @Override
    public void createRoom(String room, String client) {
        String successMessage = "SYSTEM => Success: created room "+room;
        String errorMessage = "SYSTEM => Error: room "+room + " is already existing";

        if (rooms.get(room) == null){
            rooms.put(room, new Room(room));
            sendTo(successMessage, client);
        } else {
            sendTo(errorMessage, client);
        }
    }

    @Override
    public void listRoom() {

    }

    @Override
    public void broadcast(String message, String client, type type) throws RemoteException {

        String roomName = clientCurrentRoom.get(client);

        if (message.trim().equals("")){
            return;
        }
        for (Client c: rooms.get(roomName).clients.values()){
            switch (type){
                case MESSAGE:
                    c.print("("+roomName+") " + "("+message+")"+" => "+message);
                    break;
                case PLAIN:
                    c.print(message);

            }

        }
    }

    public static void main(String[] argus){
        try {
            ChatServer obj = new ChatServer();
            Server stubServer = (Server) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Server", stubServer);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

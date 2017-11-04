package server;

import client.Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
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
    public boolean connect(Client client) {
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
                sendTo("SYSTEM => Error: this name already exists", client);
                return false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("Current clients "+ this.clients.keySet().size());
        return true;
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
    public void sendTo(String message, Client client) {
        try {
            client.print(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void joinRoom(String room, String client) {
        Room r = rooms.get(room);
        if (r == null){
            sendTo("SYSTEM => Error: " + room + " doesn't exist", client);
        } else {
            r.clients.put(client, clients.get(client));
            sendTo("SYSTEM => Success: Joined " + room, client);
        }
    }

    @Override
    public void leaveRoom(String room, String client) {
        Room r = rooms.get(room);
        if (r == null){
            sendTo("SYSTEM => Error: " + room + " doesn't exist", client);
        } else {
            if (r.clients.get(client) == null){
                sendTo("SYSTEM => Error: You are not in room " + room, client);
            } else {
                r.clients.remove(client);
                sendTo("SYSTEM => Success: Left " + room, client);
                if (clientCurrentRoom.get(client).equals(room)){
                    clientCurrentRoom.remove(client);
                    sendTo("SYSTEM => Warning: You are not in any room, please join and switch to a room", client);
                }
            }
        }
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
    public void listRoom(String client) throws RemoteException {
        String[] roomNames = new String[rooms.size()];
        int i=0;
        Client c = clients.get(client);
        for (String r: rooms.keySet()){
            roomNames[i] = r;
            i++;
        }
        Arrays.sort(roomNames);
        System.err.println(client+" : " + clientCurrentRoom.get(client));
        for (String r: roomNames){
            if (clientCurrentRoom.get(client) != null && clientCurrentRoom.get(client).equals(r)){
                c.print("SYSTEM => (current) (joined) " + r);
            } else if (rooms.get(r).clients.containsKey(client)){
                c.print("SYSTEM =>           (joined) " + r);
            }  else {
                c.print("SYSTEM =>                    " + r);
            }
        }

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
                    c.print("("+roomName+") " + "("+c.getName()+")"+" => "+message);
                    break;
                case PLAIN:
                    c.print(message);
            }
        }
    }

    @Override
    public void switchRoom(String client, String toRoom) throws RemoteException {
        if (rooms.get(toRoom).clients.containsKey(client)){
            clientCurrentRoom.put(client, toRoom);
            clients.get(client).print("SYSTEM => Success: Switched to room "+ toRoom);
        } else {
            clients.get(client).print("SYSTEM => Error: You have to join "+ toRoom + " first");
        }

        System.err.println(clientCurrentRoom.get(client));
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

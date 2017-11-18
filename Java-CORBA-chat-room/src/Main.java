import _ChatRoom.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.ArrayList;
import java.util.HashMap;

class ServerImpl extends ServerPOA {
    private ORB orb;

    HashMap<String, ChatRoom> chatRooms = new HashMap<>();
    HashMap<String, ServerClient> clients= new HashMap<>();

    ServerImpl(){
        chatRooms.put("default-room", new ChatRoom("default-room"));
    }

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    // implement sayHello() method
    public String sayHello() {
        return "\nHello world !!\n";
    }

    // implement shutdown() method
    public void shutdown() {
        orb.shutdown(false);
    }

    @Override
    public boolean connect(String clientName) {
        if (clients.containsKey(clientName)){
            return false;
        } else {

            ChatRoom defaultRoom = chatRooms.get("default-room");
            ServerClient newClient = new ServerClient(clientName);
            this.clients.put(clientName, newClient);
            defaultRoom.clients.put(clientName, newClient);
            newClient.currnetRoom = defaultRoom;
            for (ServerClient sc: defaultRoom.clients.values()){
                sc.messageQueue.add("SYSTEM => Welcome new user: "+clientName+" join default-room");
            }
            System.out.println("New connection: User: "+clientName);
            return true;
        }
    }


    @Override
    public boolean createRoom(String clientName, String roomName) {
        if (chatRooms.containsKey(roomName)){
            return false;
        } else {
            ChatRoom newRoom = new ChatRoom(roomName);
            chatRooms.put(roomName, newRoom);
            ServerClient sc = clients.get(clientName);
            sc.messageQueue.add("SYSTEM => Success: Created room: " + roomName);
            return true;
        }
    }

    @Override
    public boolean switchRoom(String clientName, String roomName) {
        ServerClient sc = clients.get(clientName);
        if (chatRooms.containsKey(roomName)){
            if (sc.joinedRooms.containsKey(roomName)){
                sc.currnetRoom = sc.joinedRooms.get(roomName);
                sc.messageQueue.add("System => Success: Switched to room: "+ roomName);
                return true;
            } else {
                sc.messageQueue.add("System => Error: You have to join "+ roomName+" first");
                return false;
            }
        } else {
            sc.messageQueue.add("System => Error: Room "+ roomName+" doesn't exist");
            return false;
        }
    }

    @Override
    public boolean joinRoom(String clientName, String roomName) {
        ServerClient sc = clients.get(clientName);
        if (chatRooms.containsKey(roomName)){
            if (sc.joinedRooms.containsKey(roomName)){
                sc.messageQueue.add("SYSTEM => Error: You've already joined room "+roomName);
                return false;
            } else {
                ChatRoom room = chatRooms.get(roomName);
                sc.joinedRooms.put(roomName, room);
                room.clients.put(clientName, sc);
                sc.messageQueue.add("SYSTEM => Success: joined room: " + roomName);
                return true;
            }
        } else {
            sc.messageQueue.add("SYSTEM => Error: Room "+roomName+" doesn't exist");
            return false;
        }
    }

    @Override
    public boolean leaveRoom(String clientName, String roomName) {
        ServerClient sc = clients.get(clientName);
        if (chatRooms.containsKey(roomName)){
            ChatRoom room = chatRooms.get(roomName);
            if (sc.joinedRooms.containsKey(roomName)){
                sc.joinedRooms.remove(roomName);
                room.clients.remove(clientName);
                sc.messageQueue.add("SYSTEM => Success: Left room: " + roomName);
                return true;
                //TODO Check if left current room
            } else {
                sc.messageQueue.add("SYSTEM => Error: You haven't joined room: " + roomName);
                return false;
            }
        } else {
            sc.messageQueue.add("SYSTEM => Error: Room " + roomName+" doesn't exist");
            return false;
        }
    }

    @Override
    public void listRoom(String clientName) {
        ServerClient sc = clients.get(clientName);
        for (ChatRoom room: chatRooms.values()){
            String message;
            // TODO sort room names
            if (sc.currnetRoom == room){
                message = "SYSTEM => (Current) (Joined) "+room.roomName;
            } else if (room.clients.containsKey(clientName)){
                message = "SYSTEM =>           (Joined) "+room.roomName;
            } else {
                message = "SYSTEM =>                    "+room.roomName;
            }
            sc.messageQueue.add(message);
        }
    }

    @Override
    public String[] retrieve(String clientName) {
        if (clients.get(clientName).messageQueue.size() == 0){
            return new String[0];
        } else {
            ArrayList<String> mq = clients.get(clientName).messageQueue;
            int size = mq.size();
            String[] result = new String[size];
            int i=0;
            for (String msg : mq){
                result[i] = msg;
                i++;
            }
            clients.get(clientName).messageQueue.clear();
            return result;
        }
    }

    @Override
    public boolean speak(String clientName, String message) {
        if (message.equals("")){
            return false;
        }
        ChatRoom room = clients.get(clientName).currnetRoom;
        message = "("+room.roomName+") "+clientName+" => "+message;
        for (ServerClient sc : room.clients.values()){
            sc.messageQueue.add(message);
        }
        return true;
    }
}


public class Main {

    public static void main(String args[]) {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            ServerImpl serverImpl = new ServerImpl();
            serverImpl.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(serverImpl);
            Server href = ServerHelper.narrow(ref);

            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef =
                    orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            String name = "Server";
            NameComponent path[] = ncRef.to_name( name );
            ncRef.rebind(path, href);

            System.out.println("Main ready and waiting ...");

            // wait for invocations from clients
            orb.run();
        }

        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

        System.out.println("Main Exiting ...");
    }
}
 
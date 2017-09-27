import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

class ServerClientThread extends Thread{

    String name;
    HashMap<String ,ChatRoom> chatRooms;
    private Socket socket;
    private ChatRoom currentRoom;

    private String message;
    private ClientListener server;

    ServerClientThread(Socket socket, ChatRoom chatRoom, ClientListener listener){
        this.socket = socket;
        this.chatRooms = new HashMap<>();
        this.chatRooms.put(chatRoom.name ,chatRoom);
        this.server = listener;
    }

    enum TYPE{
        JOIN,
        TEXT,
    }

//    enum MESSAGE{
//        WELCOME,
//        TEXT,
//    }

    private String getMessage(TYPE type, ChatRoom room, Socket socket, String text){
        String message;
        switch (type){
            case JOIN:
                message =">> Welcome new user joining room: "+room.name + "\n>> Current users: " + room.connectedSockets.size();
                break;
            case TEXT:
                if(text == null){
                    text = " ";
                }
                message =room.name+"> " + this.name+ " says:" + text;
                break;

            default:
                throw new RuntimeException();
        }
        return message;
    }

    @Override
    public void run() {
        try {
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ChatRoom defaultRoom = (ChatRoom) chatRooms.values().toArray()[0];
            this.currentRoom = defaultRoom;
            message = getMessage(TYPE.JOIN,defaultRoom, socket, null);
            broadCast(message, defaultRoom);
            for(String str: defaultRoom.history){
                sendTo(this.socket, str);
            }

            String newMessage;
            while ( (newMessage= socketReader.readLine())!= null){
                if(newMessage.trim().isEmpty()){
                    for(ChatRoom room: chatRooms.values()){
                        message =getMessage(TYPE.TEXT, room, socket, null);
                        broadCast(message, room, this);
                    }
                    continue;
                }
                String[] m = newMessage.split(" ");
                String command = m[0];
                String param=null;
                if(m.length > 1){
                    param = m[1];
                }

                switch (command){
                    case "/create":
                        if(m.length <= 1){
                            sendTo(this.socket,">> Wrong parameters");
                            break;
                        }
                        for(int i=1; i<m.length; i++){
                            if(this.server.createChatRoom(m[i])){
                                sendTo(this.socket, ">> Create room: "+m[i]+" success!");
                            }
                            else{
                                sendTo(this.socket, ">> Create room: "+m[i]+" failed. The room with this name already exists");
                            }
                        }
                        break;

                    case "/leave":
                        if(m.length <= 1){
                            sendTo(this.socket, ">> Wrong parameters");
                            break;
                        }

                        for(int i=1; i<m.length; i++){
                            if(this.chatRooms.containsKey(m[i])){
                                if(m[i].equals(this.currentRoom.name)){
                                    this.currentRoom = null;
                                }
                                if(!chatRooms.get(m[i]).connectedSockets.remove(this)){
                                    throw new java.lang.RuntimeException("Leave chat room error!");
                                }
                                this.chatRooms.remove(m[i]);
                                sendTo(this.socket, ">> Leave room: "+m[i]+" success!");
                            }
                            else{
                                sendTo(this.socket, ">> Leave room: "+m[i]+" failed. There is no room named "+m[i]);
                            }
                        }
                        sendTo(this.socket, " >> To join a new room, enter /join <ROOM_NAMES...>");
                        break;

                    case "/join":

                        for(int i=1; i<m.length; i++){
                            if(this.server.joinChatRoom(this, m[i])){
                                sendTo(this.socket, ">> Join room: "+m[i]+" success!");
//                                broadCast(getMessage(TYPE.JOIN, chatRooms.get(m[i]), socket, null), chatRooms.get(m[i]));
                                for (String str: chatRooms.get(m[i]).history){
                                    sendTo(this.socket, str);
                                }
                            }
                            else{
                                sendTo(this.socket, ">> Join room: "+m[i]+" failed. There is no room named "+m[i]);
                            }

                        }

                        break;

                    case "/list":
                        Set<String> rooms = this.server.listChatRooms();
                        Set<String> joinedRooms = this.chatRooms.keySet();
                        joinedRooms.retainAll(rooms);

                        if(this.currentRoom == null){
                            sendTo(socket, "Warning: you don't have a current room. Join and switch to a room to speak");
                        }

                        for (String room: rooms){
                            if (joinedRooms.contains(room)){
                                if (this.currentRoom != null && room.equals(this.currentRoom.name)){
                                    sendTo(this.socket, ">> (joined) (current) " + room);
                                }
                                else{
                                    sendTo(this.socket, ">> (joined)           " + room);
                                }

                            }
                            else{
                                sendTo(this.socket, ">>                    " + room);
                            }
                        }
                        break;

                    case "/switch":
                        if(m.length != 2){
                            sendTo(this.socket,">> Wrong parameters");
                            break;
                        }
                        if(this.chatRooms.containsKey(m[1])){
                            this.currentRoom = this.chatRooms.get(m[1]);
                            sendTo(this.socket, ">> Switched to room: "+m[1]+" success!");
                        }
                        else{
                            sendTo(this.socket, ">> Switch to room: "+m[1]+" failed. Either there is no room named "+m[1] + " or you have not joined "+ m[1]);
                        }
                        break;

                    case "/rename":
                        if(m.length != 2){
                            sendTo(this.socket, ">> Wrong parameters");
                            break;
                        }
                        for(ChatRoom room: this.chatRooms.values()){
                            for (ServerClientThread client: room.connectedSockets){
                                if (client.name != null && client.name.equals(m[1])){
                                    sendTo(this.socket, ">> This name already exists");
                                    break;
                                }
                            }
                        }
                        this.name = m[1];
                        break;

                    default:
                        if(this.currentRoom == null){
                            this.sendTo(socket, "Your current room is empty. You might have to join and switch to a room");
                        }
                        else{
                            message = getMessage(TYPE.TEXT, this.currentRoom, socket, newMessage);
                            this.currentRoom.history.add(message);
                            broadCast(message, this.currentRoom, this);
                        }
                        break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTo(Socket socket, String message) throws IOException{
        PrintWriter socketPrinter = new PrintWriter(socket.getOutputStream(), true);
        socketPrinter.println(message);
        socketPrinter.flush();
    }


    private void broadCast(String message, ChatRoom room, ServerClientThread from) throws IOException {
            for(ServerClientThread client: room.connectedSockets){
                if (client == from){
                    sendTo(client.socket, "(me) " + message);
                }
                else{
                    sendTo(client.socket, message);
                }
            }
        }

    private void broadCast(String message, ChatRoom room) throws IOException {
        for(ServerClientThread client: room.connectedSockets){
                sendTo(client.socket, message);
        }
    }

}

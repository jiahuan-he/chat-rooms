import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

class ServerClientThread extends Thread{

    String clientName;
//    private ChatRoom chatRoom;
    HashMap<String ,ChatRoom> chatRooms;
    private Socket socket;
    private BufferedReader socketReader;

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
        JOIN_SUCCESS,
        LEAVE_SUCCESS,
        ANY
    }

    enum MESSAGE{
        WELCOME,
        TEXT,
    }

    private String getMessage(TYPE type, ChatRoom room, Socket socket, String text){
        String message = null;
        switch (type){
            case JOIN:
                message ="Welcome new user joining room: "+room.name + "\ncurrent users: " + room.connectedSockets.size();
                break;
            case TEXT:
                message =room.name+"> "+ text;
                break;
            case JOIN_SUCCESS:
                message = "Join new room success!";
                break;
            case LEAVE_SUCCESS:
                message = "Leave room success!";
                break;

            case ANY:
                message = text;
                break;

            default:
                throw new RuntimeException();
        }
        return message;
    }

    @Override
    public void run() {
        try {
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ChatRoom defaultRoom = (ChatRoom) chatRooms.values().toArray()[0];
            message = getMessage(TYPE.JOIN,defaultRoom, socket, null);
            broadCast(message, defaultRoom);

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

                        for (String room: rooms){
                            if (joinedRooms.contains(room)){
                                sendTo(this.socket, ">> (joined) " + room);
                            }
                            else{
                                sendTo(this.socket, ">>          " + room);
                            }

                        }
                        break;

                    default:
                        for(ChatRoom room: chatRooms.values()){
                            message = getMessage(TYPE.TEXT, room, socket, newMessage);
                            broadCast(message, room, this);
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

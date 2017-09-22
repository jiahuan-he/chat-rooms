import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ServerClientThread extends Thread{

    String clientName;
    ChatRoom chatRoom;
    Socket socket;
    private BufferedReader socketReader;

    private String message;
    private ClientListener server;

    ServerClientThread(Socket socket, ChatRoom chatRoom, ClientListener listener){
        this.socket = socket;
        this.chatRoom = chatRoom;
        this.server = listener;
    }

    enum TYPE{
        JOIN,
        TEXT,
        JOIN_SUCCESS,
        LEAVE_SUCCESS,
        ANY
    }

    private String getMessage(TYPE type, ChatRoom room, Socket socket, String text){
        String message = null;
        switch (type){
            case JOIN:
                message ="Welcome new user! \nCurrent room: "+room.name + "\ncurrent users: " + room.connectedSockets.size();
                break;
            case TEXT:
                message = socket.getInetAddress() + " says " + text;
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
            message = getMessage(TYPE.JOIN, chatRoom, socket, null);
            broadCast(message);

            String newMessage = null;
            while ( (newMessage= socketReader.readLine())!= null){
                if(newMessage.trim().isEmpty()){
                    message = socket.getInetAddress() + " says " + newMessage;
                    broadCast(message);
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
                        this.server.createChatRoom(param);

                    case "/leave":
                        this.server.leaveChatRoom(this, this.chatRoom);
                        sendTo(this.socket, getMessage(TYPE.LEAVE_SUCCESS, null, null, null));
                        sendTo(this.socket, getMessage(TYPE.ANY, null, null, "To join a new room, enter /join <ROOM_NAME>"));
                        break;

                    case "/join":
                        this.server.joinChatRoom(this, param);
                        sendTo(this.socket, getMessage(TYPE.JOIN_SUCCESS, null, null, null));
//                        broadCast(getMessage(TYPE.ANY, null, null, "welcome new user!"));
                        break;

                    default:
                        message = getMessage(TYPE.TEXT, null, socket, newMessage);
                        broadCast(message);
                        break;
                }


            }
//            while (true){
//                message = socket.getInetAddress() + " says " + socketReader.readLine();
//                broadCast();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTo(Socket socket, String message) throws IOException{
        PrintWriter socketPrinter = new PrintWriter(socket.getOutputStream(), true);
        socketPrinter.println(message);
        socketPrinter.flush();
    }


    private void broadCast(String message) throws IOException {
        PrintWriter socketPrinter;
        for(ServerClientThread client: chatRoom.connectedSockets){
//            socketPrinter = new PrintWriter(client.socket.getOutputStream(), true);
//            socketPrinter.println(message);
//            socketPrinter.flush();
            sendTo(client.socket, message);
        }
    }

}

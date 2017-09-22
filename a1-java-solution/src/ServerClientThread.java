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
    private PrintWriter socketPrinter;
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
            broadCast();

            String newMessage = null;
            while ( (newMessage= socketReader.readLine())!= null){
                if(newMessage.trim().isEmpty()){
                    message = socket.getInetAddress() + " says " + newMessage;
                    broadCast();
                    continue;
                }

                String[] m = newMessage.split(" ");
                String command = m[0];
                String param=null;
                if(m.length > 1){
                    param = m[1];
                }

                switch (command){
                    case "/leave":
                        this.server.leaveChatRoom(this, this.chatRoom);
                        break;

                    case "/join":
                        this.server.joinChatRoom(this, param);
                        break;

                    default:
                        message = getMessage(TYPE.TEXT, null, socket, newMessage);
                        broadCast();
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



    private void broadCast() throws IOException {
        for(ServerClientThread client: chatRoom.connectedSockets){
            socketPrinter = new PrintWriter(client.socket.getOutputStream(), true);
            socketPrinter.println(message);
            socketPrinter.flush();
        }
    }

}

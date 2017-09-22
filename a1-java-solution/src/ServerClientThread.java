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

    @Override
    public void run() {
        try {
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            message ="current room: "+chatRoom.name+" welcome: " + socket.getInetAddress() + " current users: " + chatRoom.connectedSockets.size();
            broadCast();
            while ( (message= socketReader.readLine())!= null){
                switch (message){
                    case "/leave":
                        this.server.leaveChatRoom(this, this.chatRoom);
                        break;


                    default:
                        message = socket.getInetAddress() + " says " + message;
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

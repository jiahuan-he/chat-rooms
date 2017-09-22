import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class SocketThread extends Thread{

    String clientName;
    ChatRoom chatRoom;
    Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private String message;

    SocketThread(Socket socket, ChatRoom chatRoom){
        this.socket = socket;
        this.chatRoom = chatRoom;
    }

    @Override
    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            message = "welcome!" + socket.getInetAddress() + "current users: " + chatRoom.connectedSockets.size();
            broadCast();
            while ( (message=bufferedReader.readLine())!= null){
                message = socket.getInetAddress() + " says " + message;
                broadCast();
            }

//            while (true){
//                message = socket.getInetAddress() + " says " + bufferedReader.readLine();
//                broadCast();
//            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadCast() throws IOException {
        for(SocketThread client: chatRoom.connectedSockets){
            printWriter = new PrintWriter(client.socket.getOutputStream(), true);
            printWriter.println(message);
            printWriter.flush();
        }
    }

}

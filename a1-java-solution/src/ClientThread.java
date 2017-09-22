import javax.jws.soap.SOAPBinding;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread{
    Socket socket;

    ClientThread(Socket socket){
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter socketPrinter = new PrintWriter(socket.getOutputStream(), true);
            String message;

            while ((message = consoleReader.readLine()) != null) {
                socketPrinter.println(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

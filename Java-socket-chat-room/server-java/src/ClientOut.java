import javax.jws.soap.SOAPBinding;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientOut extends Thread{
    private Socket socket;
//    private String name;

    ClientOut(Socket socket){
        this.socket = socket;
//        this.name = name;
    }



    public void run() {
        try {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter socketPrinter = new PrintWriter(socket.getOutputStream(), true);
            String message;
//            socketPrinter.println("/rename "+this.name);
            while ((message = consoleReader.readLine()) != null) {
                socketPrinter.println(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

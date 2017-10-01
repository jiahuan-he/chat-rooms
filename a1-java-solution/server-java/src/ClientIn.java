import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class ClientIn {
//    private int port = 8088;
    private Socket socket;
    String name = "user1";
    private ClientOut clientThread;

    ClientIn() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the port you want to connect:");
        int port = sc.nextInt();
//        System.out.println("Enter your name:");
//        name = sc.next();
        socket = new Socket("localhost", port);
        System.out.println("connect to port "+ port + " success!");
        clientThread = new ClientOut(socket);
        clientThread.start();

        // This is a main blocking thread listening to the socket and print message on the console
        BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message;
        while ((message = socketReader.readLine()) != null) {
            System.out.println(message);
        }
    }

    public static void main(String argus[]) throws IOException {

        new ClientIn();
    }


}

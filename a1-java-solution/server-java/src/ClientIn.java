import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class ClientIn {
//    private int port = 8088;
    private Socket socket;
    String name = "user1";
    private ClientOut clientThread;

    ClientIn() throws IOException {
        int port = 8088;
//        System.out.println("Enter your name:");
//        name = sc.next();
        socket = new Socket("localhost", port);
        System.out.println("connect to port "+ port + " success!");
        clientThread = new ClientOut(socket);
        clientThread.start();

        // This is a main blocking thread listening to the socket and print message on the console
        BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message;
        final String yellow = (char)27 + "[33m";
        final String red = (char)27 + "[31m";
        final String green = (char)27 + "[32m";
        final String black = (char)27 + "[30m";
        while ((message = socketReader.readLine()) != null) {
            if (message.startsWith("SYSTEM => Success")){
                System.out.print(green+message);
            } else if (message.startsWith("SYSTEM => Error")){
                System.out.print(red+message);
            } else if (message.startsWith("SYSTEM => ")) {
                System.out.print(yellow+message);
            } else {
                System.out.print(message);
            }
            System.out.println(black);
        }
    }

    public static void main(String argus[]) throws IOException {

        new ClientIn();
    }


}

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {
    private int port = 8088;
    Socket socket;
    String name = "user1";
    ClientThread clientThread;

    Client(String name) throws IOException {
        this.name = name;
        socket = new Socket("localhost", port);
        clientThread = new ClientThread(socket);
        clientThread.start();

        // This is a main blocking thread listening to the socket and print message on the console
        BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message;
        while ((message = socketReader.readLine()) != null) {
            System.out.println(message);
        }
    }

    public static void main(String argus[]) throws IOException {

        new Client("newC");

//        ArrayList<User> users = new ArrayList<>();
//        Scanner scanner = new Scanner(System.in);  // Reading from System.in
//
//        int uid;
//        boolean shouldBreak = false;
//
//        while (!shouldBreak){
//            System.out.println("Enter a command: ");
//            String command = scanner.next();
//            switch (command){
//                case "c":
//                    System.out.println("Enter a uid: ");
//                    uid = scanner.nextInt();
//                    User newUser = new User(uid);
//                    users.add(newUser);
//                    break;
//
//                case "q":
//                    shouldBreak = true;
//                    break;
//
//                default:
//                    System.out.println("Wrong command");
//                    break;
//            }
//        }
    }


}

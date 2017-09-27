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
        System.out.println("Enter your name:");
        name = sc.next();
        socket = new Socket("localhost", port);
        System.out.println("connect to port "+ port + " success!");
        clientThread = new ClientOut(socket, name);
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

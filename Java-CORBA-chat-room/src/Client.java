import _ChatRoom.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;

import java.util.Scanner;

public class Client
{
    static Server serverImpl;

    public static void main(String args[])
    {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get the root naming context
            org.omg.CORBA.Object objRef =
                    orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is
            // part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            String name = "Server";
            serverImpl = ServerHelper.narrow(ncRef.resolve_str(name));

            System.out.println("Obtained a handle on server object: " + serverImpl);
            Scanner scanner = new Scanner(System.in);
            System.out.println("System => Please enter your name: ");
            String clientName = scanner.next();
            if (!serverImpl.connect(clientName)){
                System.out.println("System => Error: connection failed");
            } else {
                ClientListener clientListener = new ClientListener(clientName, serverImpl);
                clientListener.start();
                String line;
                while ((line = scanner.nextLine())!= null){
                    String trimmedLine = line.trim();
                    if (trimmedLine.equals("\n") || trimmedLine.equals("")){
                        continue;
                    }

                    String[] sLine = trimmedLine.split(" ");
                    int length = sLine.length;
                    if (trimmedLine.startsWith("/create")){
                        if (length>1){
                            for (int i=1; i<length; i++){
                                serverImpl.createRoom(clientName , sLine[i]);
                            }
                        }
                    } else if (trimmedLine.startsWith("/list")){
                        serverImpl.listRoom(clientName);
                    } else if (trimmedLine.startsWith("/join")){
                        if (length>1){
                            for (int i=1; i<length; i++){
                                serverImpl.joinRoom(clientName, sLine[i]);
                            }
                        } else {
                            System.out.println("System => Error: wrong parameter");
                        }
                    } else if (trimmedLine.startsWith("/leave")){
                        if (length>1){
                            for (int i=1; i<length; i++){
                                serverImpl.leaveRoom(clientName, sLine[i]);
                            }
                        } else {
                            System.out.println("System => Error: wrong parameter");
                        }
                    } else if (trimmedLine.startsWith("/switch")){
                        if (sLine.length == 2){
                            serverImpl.switchRoom(clientName, sLine[1]);
                        } else {
                            System.out.println("System => Error: wrong parameter");
                        }

                    } else {
                        serverImpl.speak(clientName, line);
                    }
                }
            }


            serverImpl.shutdown();

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }
}
 
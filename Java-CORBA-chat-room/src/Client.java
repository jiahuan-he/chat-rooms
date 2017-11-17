import _ChatRoom.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;

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
            System.out.println(serverImpl.connect("NEWClient"));
            serverImpl.shutdown();

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }

}
 
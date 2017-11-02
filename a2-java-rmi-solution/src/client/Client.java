package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote{
    void print(String message) throws RemoteException;
    void rename() throws RemoteException;
    String getName() throws RemoteException;
}

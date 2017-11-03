package server;

import client.Client;
import com.sun.org.apache.regexp.internal.RE;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote{

    boolean connect(Client client) throws RemoteException;
    void sendTo(String message, Client client) throws RemoteException;
    void sendTo(String message, String client) throws RemoteException;
    void joinRoom(String room, String client)throws RemoteException;
    void leaveRoom(String room, String client)throws RemoteException;
    void createRoom(String room, String client)throws RemoteException;
    String[] listRoom()throws RemoteException;
    void broadcast(String message, String client, ChatServer.type type)throws RemoteException;
}

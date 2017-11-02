package server;

import client.Client;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote{

    void connect(Client client) throws RemoteException;
    void sendTo() throws RemoteException;
    void joinRoom(String room, String client)throws RemoteException;
    void leaveRoom(String room, String client)throws RemoteException;
    void createRoom(String room)throws RemoteException;
    void listRoom()throws RemoteException;
    void broadcast(String message, String client, ChatServer.type type)throws RemoteException;
}

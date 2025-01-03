package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RoomService extends UnicastRemoteObject implements RoomServiceInterface {
    public RoomService() throws RemoteException
    {
        super();
    }
}

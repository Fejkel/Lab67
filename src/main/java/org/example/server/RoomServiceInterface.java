package org.example.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RoomServiceInterface extends Remote {
    String createRoom(String roomName) throws RemoteException;
    int deleteRoom(String roomToken) throws RemoteException;
    int joinRoom(String playerToken, String roomToken) throws RemoteException;
    int leaveRoom(String playerToken, String roomToken) throws RemoteException;
    int resetRoom(String playerToken, String roomToken) throws RemoteException;
    boolean isYourTurn(String playerToken, String roomToken) throws RemoteException;
    int makeMove(String playerToken, String roomToken, int move) throws RemoteException;
    String checkWinner(String roomToken) throws RemoteException;
    String getOponent(String roomToken, String playerToken) throws RemoteException;
    ArrayList<String> getRooms() throws RemoteException;
    int getPlayerNumber(String roomToken) throws RemoteException;
}

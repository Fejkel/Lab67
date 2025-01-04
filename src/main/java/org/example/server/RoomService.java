package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class RoomService extends UnicastRemoteObject implements RoomServiceInterface {
    private static ArrayList<Room> Rooms = new ArrayList<>();
    public RoomService() throws RemoteException
    {
        super();
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
            String roomToken = UUID.randomUUID() + "/" + roomName;
            Room room = new Room(roomToken);
            if (Rooms.contains(room)) {
                Logger.log("Room exited "+ roomToken);
                System.out.println("Room exited "+ roomToken);
            } else {
                Rooms.add(room);
                Logger.log("Created new room: " + roomToken);
                System.out.println("Created new room: " + roomToken);
            }

    }

    @Override
    public void deleteRoom(String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                Rooms.remove(room);
                System.out.println("Room "+roomToken+" deleted");
                Logger.log("Deleted room: "+roomToken);
            }
        }
        System.out.println("Room "+roomToken+" not found");
    }

    @Override
    public void joinRoom(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                if(room.getPlayerNumber() < 2) {
                    System.out.println("Player " + playerToken + " joined room " + roomToken);
                    Logger.log("Player " + playerToken + " joined room " + roomToken);
                    room.joinRoom(playerToken);
                }
            }
        }
        System.out.println("Room "+roomToken+" not found");
    }

    @Override
    public void leaveRoom(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                room.leaveRoom(playerToken);
                System.out.println("Player "+playerToken+" left room "+roomToken);
                Logger.log("Player "+playerToken+" left room "+roomToken);
                if(room.getPlayerNumber() == 0){
                    deleteRoom(roomToken);
                }
            }
        }
        System.out.println("Room "+roomToken+" not found");

    }

    @Override
    public void resetRoom(String playerToken, String roomToken) throws RemoteException {
        for (Room room : Rooms) {
            if (room.getToken().equals(roomToken)) {
                room.resetRoom(playerToken);
            }
        }
    }

    @Override
    public boolean isYourTurn(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                System.out.println("User turn "+ room.isYourTurn(playerToken) + ", roomToken: " + roomToken);
                return room.isYourTurn(playerToken);
            }
        }
        return false;
    }

    @Override
    public synchronized void makeMove(String playerToken, String roomToken, int move) throws RemoteException {
        System.out.println("Received move: position=" + move + ", symbol=" + playerToken);
        if(isYourTurn(playerToken,roomToken)){
            for(Room room : Rooms){
                if(room.getToken().equals(roomToken)){
                    room.makeMove(playerToken,move);
                    System.out.println("Updated board: " + Arrays.toString(room.getBoard()));
                }
            }
        }
    }

    @Override
    public synchronized String checkWinner(String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){

                if(room.checkWinners() != null){
                    Logger.log("Room "+roomToken+" ended with winner: "+room.checkWinners());
                    return room.checkWinners();
                }else if(room.isBoardFull()){
                    Logger.log("Room "+roomToken+" ended with draw");
                    return "Draw";
                }
            }
        }
        return null;
    }

    @Override
    public String getOponent(String roomToken, String playerToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                return room.getOpponentToken(playerToken);
            }
        }
        return null;
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        ArrayList<String> rooms = new ArrayList<>();
        for(Room room : Rooms){
            rooms.add(room.getToken());
        }
        return rooms;
    }
    @Override
    public int getPlayerNumber(String roomToken){
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                return room.getPlayerNumber();
            }
        }
        return 0;
    }

    @Override
    public String[] getBoard(String roomToken) {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                return room.getBoard();
            }
        }
        return null;
    }
}

package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

public class RoomService extends UnicastRemoteObject implements RoomServiceInterface {
    private static ArrayList<Room> Rooms = new ArrayList<>();
    public RoomService() throws RemoteException
    {
        super();
    }

    @Override
    public String createRoom(String roomName) throws RemoteException {
        String roomToken = UUID.randomUUID() + "/" + roomName;
        Room room = new Room(roomToken);
        if(Rooms.contains(room))
        {
            return "Room already exists";
        }else {
            Rooms.add(room);
            Logger.log("Created new room: "+roomToken);
            System.out.println("Created new room: "+roomToken);
            return roomToken;
        }
    }

    @Override
    public int deleteRoom(String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                Rooms.remove(room);
                System.out.println("Room "+roomToken+" deleted");
                Logger.log("Deleted room: "+roomToken);
                return 1;
            }
        }
        System.out.println("Room "+roomToken+" not found");
        return 0;
    }

    @Override
    public int joinRoom(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                System.out.println("Player "+playerToken+" joined room "+roomToken);
                Logger.log("Player "+playerToken+" joined room "+roomToken);
                return room.joinRoom(playerToken);
            }
        }
        System.out.println("Room "+roomToken+" not found");
        return 0;
    }

    @Override
    public int leaveRoom(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                room.leaveRoom(playerToken);
                System.out.println("Player "+playerToken+" left room "+roomToken);
                Logger.log("Player "+playerToken+" left room "+roomToken);
                if(room.getPlayerNumber() == 0){
                    deleteRoom(roomToken);
                }
                return 1;
            }
        }
        System.out.println("Room "+roomToken+" not found");
        return 0;
    }

    @Override
    public int resetRoom(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                room.resetRoom(playerToken);
                Logger.log("Room reseted: "+roomToken);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean isYourTurn(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                return room.isYourTurn(playerToken);
            }
        }
        return false;
    }

    @Override
    public int makeMove(String playerToken, String roomToken, int move) throws RemoteException {
        if(isYourTurn(playerToken,roomToken)){
            for(Room room : Rooms){
                if(room.getToken().equals(roomToken)){
                    room.makeMove(playerToken,move);
                    return 1;
                }
            }
        }
        return 0;
    }

    @Override
    public String checkWinner(String roomToken) throws RemoteException {
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

}

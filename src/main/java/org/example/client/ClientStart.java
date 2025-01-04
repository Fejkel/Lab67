package org.example.client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.server.RoomServiceInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientStart extends Application{
    public static RoomServiceInterface roomService;
    static String host;
    static int port;
    public static String userToken;
    public static String userName;
    public static String roomToken;
    private static ServerService serverService;


    @Override
    public void start(Stage stage) {
        try {
            host= "localhost";
            port = 1001;

            Registry registry = LocateRegistry.getRegistry(host, port);
            roomService = (RoomServiceInterface) registry.lookup("RoomService");
            serverService = new ServerService(roomService);
            FXMLLoader fxmlLoader = new FXMLLoader(ClientStart.class.getResource("ClientLoginView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 964, 595);
            stage.setScene(scene);
            stage.show();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if( roomToken != null)
                        roomService.leaveRoom(userToken,roomToken);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (Exception e) {
            System.out.println("Failed to start client: " + e.getMessage());
        }
    }

    public static ServerService getServerService() {
        return serverService;
    }

    public static void main(String[] args) {
        launch();
    }
    public static void setUserToken(String userToken) {
        ClientStart.userToken = userToken;
    }
    public static void setUserName(String userName) {
        ClientStart.userName = userName;
    }
    public static void setRoomToken(String roomToken) {
        ClientStart.roomToken = roomToken;
    }
}

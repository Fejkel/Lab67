package org.example.client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.server.RoomServiceInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
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

    @Override
    public void start(Stage stage) {
        try {
            host = "127.0.0.1";
            port = 1001;
            Registry registry = LocateRegistry.getRegistry(host, port);
            roomService = (RoomServiceInterface) registry.lookup("RoomService");

            FXMLLoader fxmlLoader = new FXMLLoader(ClientStart.class.getResource("ClientLoginView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 964, 595);
            stage.setResizable(false);
            stage.setTitle("Client Login");
            stage.setScene(scene);
            stage.show();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    roomService.leaveRoom(userToken,roomToken);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }));
        }catch (IOException | NotBoundException e){
            System.out.println(" Client failed " + e);
        }
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

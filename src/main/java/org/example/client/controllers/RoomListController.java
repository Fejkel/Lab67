   package org.example.client.controllers;

   import javafx.fxml.FXML;
   import javafx.fxml.FXMLLoader;
   import javafx.scene.Node;
   import javafx.scene.Scene;
   import javafx.scene.control.Alert;
   import javafx.scene.layout.VBox;
   import javafx.scene.control.Label;
   import javafx.scene.control.TextField;
   import javafx.stage.Stage;
   import org.example.client.ClientStart;


   import java.io.IOException;
   import java.rmi.RemoteException;
   import java.util.ArrayList;

   public class RoomListController {
   
       @FXML
       private VBox roomContainer;
       @FXML
       private Label usernameLabel;
       @FXML
       private TextField roomName;

       @FXML
       public void initialize() {
           loadUsername();
           loadRooms();
       }
       @FXML
       private void loadRooms(){
           roomContainer.getChildren().clear();
           try {
               ArrayList<String> RoomsToken = ClientStart.roomService.getRooms();
               RoomsToken.forEach(token -> {
                   String roomName = token.split("/")[1];
                   javafx.scene.control.Button button;

                   try {
                       button = new javafx.scene.control.Button(roomName + "  " + ClientStart.roomService.getPlayerNumber(token) + "/2");
                   } catch (RemoteException e) {
                       throw new RuntimeException(e);
                   }

                   button.setOnAction(event -> {
                       try {
                           ClientStart.roomService.joinRoom(ClientStart.userToken, token);
                           ClientStart.setRoomToken(token);

                           FXMLLoader fxmlLoader = new FXMLLoader(ClientStart.class.getResource("GameView.fxml"));
                           Scene scene = new Scene(fxmlLoader.load());
                           Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                           stage.setScene(scene);
                           stage.show();

                       } catch (IOException ex) {
                           throw new RuntimeException(ex);
                       }
                   });
                   roomContainer.getChildren().add(button);
               });
           } catch (RemoteException e) {
               throw new RuntimeException(e);
           }
       }

       private void loadUsername()
       {
           usernameLabel.setText(ClientStart.userName);
       }

       @FXML
       private void handleCreateRoom() {
           try {
               ClientStart.roomService.createRoom(roomName.getText());
               loadRooms();
               roomName.clear();
           } catch (Exception e) {
               Alert alert = new Alert(Alert.AlertType.ERROR);
               alert.setContentText("Could not create room.");
               alert.show();
               System.out.println(e.getMessage());
           }
       }

   }
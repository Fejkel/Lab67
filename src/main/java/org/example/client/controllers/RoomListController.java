   package org.example.client.controllers;

   import javafx.collections.FXCollections;
   import javafx.collections.ObservableList;
   import javafx.fxml.FXML;
   import javafx.scene.control.Alert;
   import javafx.scene.control.TableView;
   import org.example.client.ClientStart;

   import java.util.List;

   public class RoomListController {

       @FXML
       private TableView<String> roomTable;

       @FXML
       public void initialize() {
           loadRooms();
       }

       private void loadRooms() {
           try {
               List<String> rooms = ClientStart.roomService.getRooms();
               ObservableList<String> roomData = FXCollections.observableArrayList(rooms);
               roomTable.setItems(roomData);
           } catch (Exception e) {
               showError("Failed to load rooms.");
           }
       }

       @FXML
       private void handleCreateRoom() {
           try {
               ClientStart.roomService.createRoom("New Room");
               loadRooms();
           } catch (Exception e) {
               showError("Could not create room.");
           }
       }

       private void showError(String message) {
           Alert alert = new Alert(Alert.AlertType.ERROR);
           alert.setContentText(message);
           alert.show();
       }
   }
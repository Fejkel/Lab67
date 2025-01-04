   package org.example.client.controllers;

   import javafx.fxml.FXML;
   import javafx.fxml.FXMLLoader;
   import javafx.scene.control.Alert;
   import javafx.scene.control.TextField;
   import javafx.stage.Stage;
   import org.example.client.ClientStart;

   import java.util.UUID;

   public class ClientLoginController {

       @FXML
       private TextField usernameField;

       @FXML
       private void handleLogin() {
           try {
               if(usernameField.getLength()<3)
               {
                   Alert alert = new Alert(Alert.AlertType.ERROR);
                   alert.setTitle("Error");
                   alert.setHeaderText(null);
                   alert.setContentText("Username must be at least 3 characters long.");
                   alert.showAndWait();
                   return;
               }
               String username = usernameField.getText();
               ClientStart.setUserToken(UUID.randomUUID()+"@"+username);
               ClientStart.setUserName(username);
               Stage stage = (Stage) usernameField.getScene().getWindow();
               FXMLLoader fxmlLoader = new FXMLLoader(ClientStart.class.getResource("RoomListView.fxml"));
               stage.getScene().setRoot(fxmlLoader.load());

           } catch (Exception e) {
               System.out.println(e.getMessage());

           }
       }
   }
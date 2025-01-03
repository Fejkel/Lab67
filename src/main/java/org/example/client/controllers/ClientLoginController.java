   package org.example.client.controllers;

   import javafx.fxml.FXML;
   import javafx.scene.control.Alert;
   import javafx.scene.control.PasswordField;
   import javafx.scene.control.TextField;
   import javafx.stage.Stage;
   import org.example.client.ClientStart;

   public class ClientLoginController {

       @FXML
       private TextField usernameField;
       @FXML
       private PasswordField passwordField;

       @FXML
       private void handleLogin() {
           String username = usernameField.getText();
           String password = passwordField.getText();

           try {
               boolean loggedIn = ClientStart.roomService.login(username, password);

               if (loggedIn) {
                   // Gdy logowanie się uda, przejdź do listy pokoi
                   Stage stage = (Stage) usernameField.getScene().getWindow();
                   FXMLLoader fxmlLoader = new FXMLLoader(ClientStart.class.getResource("roomListView.fxml"));
                   stage.getScene().setRoot(fxmlLoader.load());
               } else {
                   showError("Invalid username or password.");
               }
           } catch (Exception e) {
               showError("Could not connect to server.");
           }
       }

       private void showError(String message) {
           Alert alert = new Alert(Alert.AlertType.ERROR);
           alert.setContentText(message);
           alert.show();
       }
   }
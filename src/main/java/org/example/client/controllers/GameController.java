package org.example.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.client.ClientStart;
import org.example.client.SceneController;
import org.example.client.ServerService;


public class GameController {
    @FXML
    private Label whosturn;
    @FXML
    private GridPane gridPane;

    @FXML
    public void initialize() {
        ServerService serverService = ClientStart.getServerService();
        serverService.waitForAnotherPlayer(ClientStart.userToken, ClientStart.roomToken,whosturn,gridPane);
    }

    @FXML
    public void handleLeave() {
        try {
            ClientStart.getServerService().leaveRoom(ClientStart.userToken, ClientStart.roomToken);
            ClientStart.setRoomToken(null);
            Stage stage = (Stage) whosturn.getScene().getWindow();
            SceneController sceneController = new SceneController(stage);
            sceneController.switchTo("RoomListView.fxml");

        } catch (Exception e) {
            System.out.println("Leave room error: " + e.getMessage());
        }
    }


}

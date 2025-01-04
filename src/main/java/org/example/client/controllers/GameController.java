package org.example.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.client.ClientStart;

import java.io.IOException;
import java.rmi.RemoteException;


public class GameController {
    @FXML
    Label whosturn;

    @FXML
    public void initialize() throws RemoteException, InterruptedException {

    }

    public void handleLeave(ActionEvent event) {
        try {
            ClientStart.roomService.leaveRoom(ClientStart.userToken,ClientStart.roomToken);
            ClientStart.setRoomToken(null);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

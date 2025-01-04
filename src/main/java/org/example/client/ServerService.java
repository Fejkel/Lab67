package org.example.client;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import javafx.stage.Stage;
import org.example.server.RoomServiceInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerService {

    private final RoomServiceInterface roomService;
    private final Map<String, Task<?>> taskMap = new ConcurrentHashMap<>();

    public ServerService(RoomServiceInterface roomService) {
        this.roomService = roomService;
    }

    public Task<ArrayList<String>> loadRoomsTask() {
        return new Task<>() {
            @Override
            protected ArrayList<String> call() throws RemoteException {
                return roomService.getRooms();
            }
        };
    }

    public void createRoom(String roomName) throws RemoteException {
        roomService.createRoom(roomName);
    }

    public void joinRoom(String userToken, String roomToken) throws RemoteException {
        roomService.joinRoom(userToken, roomToken);
    }

    public void leaveRoom(String userToken, String roomToken) throws RemoteException {
        roomService.leaveRoom(userToken, roomToken);
        Task<?> task = taskMap.remove(roomToken);
        if (task != null) {
            task.cancel();
        }
    }

    public int getPlayerNumber(String roomToken) throws RemoteException {
        return roomService.getPlayerNumber(roomToken);
    }

    public void handleGame(String userToken, String roomToken, Label waitingLabel, GridPane gridPane) throws RemoteException {
        System.out.println("handleGame started.");

        // Uruchamianie harmonogramu sprawdzającego stan gry co sekundę
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Pobranie kluczowego stanu gry z serwera
                int playerCount = ClientStart.roomService.getPlayerNumber(roomToken);
                boolean isYourTurn = ClientStart.roomService.isYourTurn(userToken, roomToken);
                String winner = ClientStart.roomService.checkWinner(roomToken);

                // Jeśli przeciwnik opuścił pokój
                if (playerCount < 2) {
                    Platform.runLater(() -> {
                        gridPane.getChildren().clear();
                        waitingLabel.setText("Opponent left the room. Waiting for a new opponent...");
                    });
                    return; // Zatrzymaj dalsze sprawdzanie w tej iteracji
                }

                // Obsługa sytuacji, gdy zostanie rozstrzygnięta gra
                if (winner != null) {
                    if (winner.contains("@")) {
                        winner = winner.split("@")[1];
                    }

                    System.out.println("Winner: " + winner);
                    String finalWinner = winner;

                    Platform.runLater(() -> {
                        gridPane.getChildren().clear();
                        waitingLabel.setText("Winner: " + finalWinner);

                        Button rematchButton = new Button("Rematch");
                        rematchButton.setPrefSize(100, 50);
                        rematchButton.setOnAction(e -> {
                            try {
                                ClientStart.roomService.resetRoom(userToken, roomToken);
                                String[] updatedBoard = ClientStart.roomService.getBoard(roomToken);
                                refreshBoard(gridPane, updatedBoard, "Waiting for opponent to restart...", waitingLabel);
                            } catch (RemoteException ex) {
                                System.out.println("Remote exception: " + ex.getMessage());
                            }
                        });


                        gridPane.add(rematchButton, 1, 1);
                    });

                    scheduler.shutdown();
                    return;
                }


                if (isYourTurn && playerCount == 2) {
                    String[] board = ClientStart.roomService.getBoard(roomToken);
                    Platform.runLater(() -> refreshBoard(gridPane, board, "Your turn", waitingLabel));
                }

                else if (playerCount == 2) {
                    Platform.runLater(() -> waitingLabel.setText("Waiting for opponent's move..."));
                }
            } catch (RemoteException e) {
                System.out.println("Remote exception: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    private void refreshBoard(GridPane gridPane, String[] board, String message, Label waitingLabel) {
        gridPane.getChildren().clear();
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null || board[i].isEmpty()) {
                Button button = new Button();
                button.setPrefSize(60, 60);
                final int position = i; // Indeks pozycji
                button.setOnAction(e -> {
                    try {
                        ClientStart.roomService.makeMove(ClientStart.userToken, ClientStart.roomToken, position);
                        String[] updatedBoard = ClientStart.roomService.getBoard(ClientStart.roomToken);
                        refreshBoard(gridPane, updatedBoard, "Waiting...", waitingLabel);
                    } catch (RemoteException ex) {
                        System.out.println("Remote exception: " + ex.getMessage());
                    }
                });
                gridPane.add(button, i % 3, i / 3);
            } else {
                Label label = new Label(board[i]);
                label.setPrefSize(60, 60);
                gridPane.add(label, i % 3, i / 3);
            }
        }
        waitingLabel.setText(message);
    }

    public void waitForAnotherPlayer(String userToken, String roomToken, Label waitingLabel, GridPane gridPane) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int iterations = 0;


                Platform.runLater(() -> waitingLabel.setText("Waiting for another player..."));

                while (true) {
                    try {
                        if (getPlayerNumber(roomToken) == 2) {
                            System.out.println("Player count in room: " + getPlayerNumber(roomToken));

                            Platform.runLater(() -> {
                                try {
                                    handleGame(userToken, roomToken, waitingLabel, gridPane);
                                } catch (RemoteException e) {
                                    System.out.println("Remote exception: " + e.getMessage());
                                }
                            });

                            break;
                        }

                        Thread.sleep(1000);
                        iterations++;

                        if (iterations > 30) {
                            Platform.runLater(() -> {
                                System.out.println("Timeout waiting for another player. Leaving room...");
                                ClientStart.setRoomToken(null);
                                Stage stage = (Stage) waitingLabel.getScene().getWindow();
                                SceneController sceneController = new SceneController(stage);
                                sceneController.switchTo("RoomListView.fxml");
                            });
                            break;
                        }
                    } catch (InterruptedException e) {
                        Platform.runLater(() -> System.out.println("Task interrupted: " + e.getMessage()));
                        break;
                    } catch (RemoteException e) {
                        Platform.runLater(() -> System.out.println("Remote exception: " + e.getMessage()));
                        break;
                    }
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        System.out.println("Task thread started successfully.");
        taskMap.put(userToken, task);
    }
}
package org.example.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Room {
    private String token;

    private HashMap<String, String> players;
    private ArrayList<String> playerVotesReset;
    private String playerTurn;

    private String[] board = new String[9];

    public Room(String token) {
        this.setToken(token);
        this.players = new HashMap<>();
        this.playerVotesReset = new ArrayList<>();
        Arrays.fill(this.board, null);
    }
    protected int joinRoom(String playerToken) {
        if (getPlayerNumber() < 2)
        {
            if (players.isEmpty())
            {
                players.put(playerToken, "X");
            } else
            {
                players.put(playerToken, "O");
            }
            return 1;
        }
        return 0;
    }
    protected void leaveRoom(String playerToken){
        players.remove(playerToken);

    }
    protected boolean isYourTurn(String playerToken){
        return playerToken.equals(playerTurn);
    }
    protected String firstMove(){
        Random r = new Random();
        if(players.size() == 2)
        {
            return players.keySet().toArray()[r.nextInt(2)].toString();
        }
        return "";
    }
    protected String checkWinners() {
        for (int a = 0; a < 8; a++) {
            String line = switch (a) {
                case 0 -> board[0] + board[1] + board[2];
                case 1 -> board[3] + board[4] + board[5];
                case 2 -> board[6] + board[7] + board[8];
                case 3 -> board[0] + board[3] + board[6];
                case 4 -> board[1] + board[4] + board[7];
                case 5 -> board[2] + board[5] + board[8];
                case 6 -> board[0] + board[4] + board[8];
                case 7 -> board[2] + board[4] + board[6];
                default -> null;
            };

            if (line.equals("XXX") || line.equals("OOO")) {
                return players.get(playerTurn);
            }
        }
        return null;
    }
    protected void makeMove(String playerToken, int move){
        if(board[move] == null) {
            board[move] = players.get(playerToken);
        }
        for(String player : players.keySet()){
            if(!player.equals(playerToken)){
                playerTurn = player;
                break;
            }
        }
    }
    protected void resetRoom(String playerToken){
        playerVotesReset.add(playerToken);

        if(playerVotesReset.size() == 2){
            Arrays.fill(board, null);
            this.playerTurn = firstMove();
            playerVotesReset.clear();
        }
    }
    public boolean isBoardFull() {
        for (String position : board) {
            if (position == null) {
                return false;
            }
        }
        return true;
    }
    protected int getPlayerNumber(){
        return this.players.size();
    }
    protected String getOpponentToken(String playerToken){
        for(String player : players.keySet()){
            if(!player.equals(playerToken))
                return player;
        }
        return playerToken;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}

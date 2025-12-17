package controller;

import model.*;
import java.util.LinkedList;
import java.util.Queue;

public class GameController {
    private Graph graph;
    private Queue<Player> playerQueue;
    private Dice dice;
    private Player currentPlayer;
    private boolean gameStarted;
    private boolean gameEnded;

    public GameController() {
        this.graph = new Graph();
        this.playerQueue = new LinkedList<>();
        this.dice = new Dice();
        this.gameStarted = false;
        this.gameEnded = false;
    }

    // Inisialisasi pemain
    public void initializePlayers(int numPlayers) {
        String[] colors = {"red", "blue", "green", "yellow", "purple", "orange", "pink", "cyan"};

        for (int i = 0; i < numPlayers && i < colors.length; i++) {
            Player player = new Player("Player " + (i + 1), colors[i]);
            playerQueue.offer(player);
        }

        if (!playerQueue.isEmpty()) {
            currentPlayer = playerQueue.peek();
        }
    }

    // Start game
    public void startGame() {
        gameStarted = true;
        gameEnded = false;
    }

    // Execute turn untuk current player
    public TurnResult executeTurn() {
        if (gameEnded || currentPlayer == null) {
            return null;
        }

        // Roll dadu
        Dice.DiceResult diceResult = dice.roll();
        int diceNumber = diceResult.getNumber();
        String diceColor = diceResult.getColor();

        Food food = null;
        int stepsMoved = 0;
        int oldPosition = currentPlayer.getCurrentPosition();

        // Proses berdasarkan warna dadu
        if (diceResult.isGreen()) {
            // Dapat makanan dan energi untuk maju
            food = Food.getFoodByDiceNumber(diceNumber);
            stepsMoved = food.getEnergyValue();
            currentPlayer.moveForward(stepsMoved);
        } else {
            // Dadu merah - mundur
            stepsMoved = -diceNumber;  // Negatif untuk mundur
            currentPlayer.moveBackward(diceNumber);
        }

        int newPosition = currentPlayer.getCurrentPosition();

        // Cek apakah player menang
        if (currentPlayer.hasWon()) {
            gameEnded = true;
        }

        // Create turn result
        TurnResult result = new TurnResult(
                currentPlayer,
                diceResult,
                food,
                oldPosition,
                newPosition,
                stepsMoved
        );

        // Pindah ke player berikutnya (poll dan push back ke queue)
        if (!gameEnded) {
            nextPlayer();
        }

        return result;
    }

    // Pindah ke player berikutnya menggunakan Queue
    private void nextPlayer() {
        Player player = playerQueue.poll();  // Ambil dari depan
        if (player != null) {
            playerQueue.offer(player);       // Taruh kembali di belakang
        }
        currentPlayer = playerQueue.peek();  // Set current player
    }

    // Getters
    public Graph getGraph() { return graph; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public Queue<Player> getPlayerQueue() { return playerQueue; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isGameEnded() { return gameEnded; }
    public Dice getDice() { return dice; }

    // Inner class untuk menyimpan hasil turn
    public static class TurnResult {
        private Player player;
        private Dice.DiceResult diceResult;
        private Food food;
        private int oldPosition;
        private int newPosition;
        private int stepsMoved;

        public TurnResult(Player player, Dice.DiceResult diceResult, Food food,
                          int oldPosition, int newPosition, int stepsMoved) {
            this.player = player;
            this.diceResult = diceResult;
            this.food = food;
            this.oldPosition = oldPosition;
            this.newPosition = newPosition;
            this.stepsMoved = stepsMoved;
        }

        // Getters
        public Player getPlayer() { return player; }
        public Dice.DiceResult getDiceResult() { return diceResult; }
        public Food getFood() { return food; }
        public int getOldPosition() { return oldPosition; }
        public int getNewPosition() { return newPosition; }
        public int getStepsMoved() { return stepsMoved; }
    }
}

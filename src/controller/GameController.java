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

    // Method baru untuk reset game
    public void reset() {
        this.playerQueue.clear();
        this.currentPlayer = null;
        this.gameStarted = false;
        this.gameEnded = false;
    }

    public void initializePlayers(int numPlayers) {
        String[] colors = {"red", "blue", "green", "yellow", "purple", "orange", "pink", "cyan"};

        for (int i = 0; i < numPlayers; i++) {
            String name = "Player " + (i + 1);
            String color = (i < colors.length) ? colors[i] : "gray";
            // Asumsi nama file: "player 1.png", "player 2.png", dst.
            String imagePath = "resources/images/player " + (i + 1) + ".png";

            Player player = new Player(name, color, imagePath);
            playerQueue.offer(player);
        }

        if (!playerQueue.isEmpty()) {
            currentPlayer = playerQueue.peek();
        }
    }

    public void startGame() { gameStarted = true; gameEnded = false; }

    public TurnResult executeTurn() {
        if (gameEnded || currentPlayer == null) return null;
        Dice.DiceResult diceResult = dice.roll();
        int diceNumber = diceResult.getNumber();
        Food food = null;
        int stepsMoved = 0;
        int oldPosition = currentPlayer.getCurrentPosition();

        if (diceResult.isGreen()) {
            food = Food.getFoodByDiceNumber(diceNumber);
            stepsMoved = food.getEnergyValue();
            currentPlayer.moveForward(stepsMoved);
        } else {
            stepsMoved = -diceNumber;
            currentPlayer.moveBackward(diceNumber);
        }

        if (currentPlayer.hasWon()) gameEnded = true;

        TurnResult result = new TurnResult(currentPlayer, diceResult, food, oldPosition, currentPlayer.getCurrentPosition(), stepsMoved);
        if (!gameEnded) nextPlayer();
        return result;
    }

    private void nextPlayer() {
        Player player = playerQueue.poll();
        if (player != null) playerQueue.offer(player);
        currentPlayer = playerQueue.peek();
    }

    public Graph getGraph() { return graph; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public Queue<Player> getPlayerQueue() { return playerQueue; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isGameEnded() { return gameEnded; }
    public Dice getDice() { return dice; }

    public static class TurnResult {
        private Player player;
        private Dice.DiceResult diceResult;
        private Food food;
        private int oldPosition, newPosition, stepsMoved;
        public TurnResult(Player player, Dice.DiceResult diceResult, Food food, int oldPosition, int newPosition, int stepsMoved) {
            this.player = player; this.diceResult = diceResult; this.food = food;
            this.oldPosition = oldPosition; this.newPosition = newPosition; this.stepsMoved = stepsMoved;
        }
        public Player getPlayer() { return player; }
        public Dice.DiceResult getDiceResult() { return diceResult; }
        public Food getFood() { return food; }
        public int getOldPosition() { return oldPosition; }
        public int getNewPosition() { return newPosition; }
        public int getStepsMoved() { return stepsMoved; }
    }
}
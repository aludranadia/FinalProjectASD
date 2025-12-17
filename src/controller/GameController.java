package controller;

import model.*;
import java.util.*;

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

    public void reset() {
        this.playerQueue.clear();
        this.currentPlayer = null;
        this.gameStarted = false;
        this.gameEnded = false;
        this.graph = new Graph(); // Reset graph agar posisi koin dan shortcut berubah
    }

    public void initializeCustomPlayers(List<String> names, List<String> imagePaths) {
        String[] defaultColors = {"red", "blue", "green", "yellow", "purple", "orange", "pink", "cyan"};
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name == null || name.trim().isEmpty()) name = "Player " + (i + 1);
            String imagePath = imagePaths.get(i);
            String color = (i < defaultColors.length) ? defaultColors[i] : "gray";
            playerQueue.offer(new Player(name, color, imagePath));
        }
        if (!playerQueue.isEmpty()) currentPlayer = playerQueue.peek();
    }

    public void startGame() { gameStarted = true; gameEnded = false; }

    public TurnResult executeTurn() {
        if (gameEnded || currentPlayer == null) return null;

        Dice.DiceResult diceResult = dice.roll();
        int diceNumber = diceResult.getNumber();
        Food food = null;
        int energySteps = 0;
        int oldPosition = currentPlayer.getCurrentPosition();
        List<Integer> movementPath = new ArrayList<>();
        boolean usedShortcut = false;

        if (diceResult.isGreen()) {
            food = Food.getFoodByDiceNumber(diceNumber);
            energySteps = food.getEnergyValue();
            boolean canUseShortcut = graph.isPrime(oldPosition);
            int currentSimulatedPos = oldPosition;

            for (int i = 0; i < energySteps; i++) {
                if (currentSimulatedPos >= 64) break;
                int destShortcut = graph.getShortcutDestination(currentSimulatedPos);
                if (canUseShortcut && destShortcut != -1) {
                    currentSimulatedPos = destShortcut;
                    usedShortcut = true;
                } else {
                    currentSimulatedPos++;
                }
                if (currentSimulatedPos > 64) currentSimulatedPos = 64;
                movementPath.add(currentSimulatedPos);
                currentPlayer.setPosition(currentSimulatedPos);
            }
        } else {
            energySteps = -diceNumber;
            int stepsToMoveBack = diceNumber;
            int currentSimulatedPos = oldPosition;
            for(int i=0; i<stepsToMoveBack; i++){
                if(currentSimulatedPos > 1) {
                    currentSimulatedPos--;
                    movementPath.add(currentSimulatedPos);
                }
            }
            currentPlayer.moveBackward(diceNumber);
        }

        // --- TAMBAHAN: LOGIKA AMBIL KOIN ---
        Node finalNode = graph.getNode(currentPlayer.getCurrentPosition());
        int coinEffect = 0;
        if (finalNode != null) {
            coinEffect = finalNode.getCoinValue();
            currentPlayer.addCoins(coinEffect);
        }

        if (currentPlayer.hasWon()) gameEnded = true;

        boolean bonusTurn = false;
        int finalPos = currentPlayer.getCurrentPosition();
        if (!gameEnded && finalPos % 5 == 0 && finalPos != 0) {
            bonusTurn = true;
        }

        TurnResult result = new TurnResult(
                currentPlayer, diceResult, food,
                oldPosition, currentPlayer.getCurrentPosition(),
                energySteps, usedShortcut, movementPath, bonusTurn, coinEffect
        );

        if (!gameEnded && !bonusTurn) {
            nextPlayer();
        }
        return result;
    }

    // --- LEADERBOARD ---
    public PriorityQueue<Player> getScoreLeaderboard() {
        PriorityQueue<Player> pq = new PriorityQueue<>();
        pq.addAll(playerQueue);
        if (currentPlayer != null && !pq.contains(currentPlayer)) {
            pq.add(currentPlayer);
        }
        return pq;
    }

    private void nextPlayer() {
        Player player = playerQueue.poll();
        if (player != null) playerQueue.offer(player);
        currentPlayer = playerQueue.peek();
    }

    // Getters standard
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
        private boolean usedShortcut;
        private List<Integer> movementPath;
        private boolean bonusTurn;
        private int coinEffect; // Tambahan: Efek koin

        public TurnResult(Player player, Dice.DiceResult diceResult, Food food,
                          int oldPosition, int newPosition, int stepsMoved,
                          boolean usedShortcut, List<Integer> movementPath, boolean bonusTurn, int coinEffect) {
            this.player = player; this.diceResult = diceResult; this.food = food;
            this.oldPosition = oldPosition; this.newPosition = newPosition;
            this.stepsMoved = stepsMoved; this.usedShortcut = usedShortcut;
            this.movementPath = movementPath;
            this.bonusTurn = bonusTurn;
            this.coinEffect = coinEffect;
        }
        // Getters
        public Player getPlayer() { return player; }
        public Dice.DiceResult getDiceResult() { return diceResult; }
        public Food getFood() { return food; }
        public int getOldPosition() { return oldPosition; }
        public int getNewPosition() { return newPosition; }
        public int getStepsMoved() { return stepsMoved; }
        public boolean isUsedShortcut() { return usedShortcut; }
        public List<Integer> getMovementPath() { return movementPath; }
        public boolean isBonusTurn() { return bonusTurn; }
        public int getCoinEffect() { return coinEffect; }
    }
}
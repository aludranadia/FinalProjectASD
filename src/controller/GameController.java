package controller;

import model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List; // Tambahkan import List

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
        this.graph.generateRandomShortcuts();
    }

    // --- METHOD BARU: Untuk Setup Custom (Nama & Karakter) ---
    public void initializeCustomPlayers(List<String> names, List<String> imagePaths) {
        String[] defaultColors = {"red", "blue", "green", "yellow", "purple", "orange", "pink", "cyan"};

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            // Jika nama kosong, beri default
            if (name == null || name.trim().isEmpty()) {
                name = "Player " + (i + 1);
            }

            String imagePath = imagePaths.get(i);
            String color = (i < defaultColors.length) ? defaultColors[i] : "gray";

            Player player = new Player(name, color, imagePath);
            playerQueue.offer(player);
        }

        if (!playerQueue.isEmpty()) {
            currentPlayer = playerQueue.peek();
        }
    }

    // Method lama (bisa dihapus atau dibiarkan untuk fallback)
    public void initializePlayers(int numPlayers) {
        // ... (Kode lama)
    }

    public void startGame() { gameStarted = true; gameEnded = false; }

    public TurnResult executeTurn() {
        if (gameEnded || currentPlayer == null) return null;

        Dice.DiceResult diceResult = dice.roll();
        int diceNumber = diceResult.getNumber();
        Food food = null;
        int energySteps = 0;
        int oldPosition = currentPlayer.getCurrentPosition();

        // List untuk merekam jejak langkah agar animasi akurat
        List<Integer> movementPath = new ArrayList<>();

        boolean usedShortcut = false;

        if (diceResult.isGreen()) {
            // 1. Hitung Energi
            food = Food.getFoodByDiceNumber(diceNumber);
            energySteps = food.getEnergyValue();

            // 2. Cek Syarat Shortcut: Posisi AWAL turn harus Prima (dan bukan 1)
            boolean canUseShortcut = graph.isPrime(oldPosition);

            // 3. Simulasi Gerakan Step-by-Step
            int currentSimulatedPos = oldPosition;

            for (int i = 0; i < energySteps; i++) {
                // Cek kemenangan sebelum melangkah
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
                    currentSimulatedPos--; // Mundur linear visual
                    movementPath.add(currentSimulatedPos);
                }
            }
            currentPlayer.moveBackward(diceNumber);
        }

        if (currentPlayer.hasWon()) gameEnded = true;

        TurnResult result = new TurnResult(
                currentPlayer, diceResult, food,
                oldPosition, currentPlayer.getCurrentPosition(),
                energySteps, usedShortcut, movementPath
        );

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
        private boolean usedShortcut;
        private List<Integer> movementPath;

        public TurnResult(Player player, Dice.DiceResult diceResult, Food food,
                          int oldPosition, int newPosition, int stepsMoved,
                          boolean usedShortcut, List<Integer> movementPath) {
            this.player = player; this.diceResult = diceResult; this.food = food;
            this.oldPosition = oldPosition; this.newPosition = newPosition;
            this.stepsMoved = stepsMoved;
            this.usedShortcut = usedShortcut;
            this.movementPath = movementPath;
        }

        public Player getPlayer() { return player; }
        public Dice.DiceResult getDiceResult() { return diceResult; }
        public Food getFood() { return food; }
        public int getOldPosition() { return oldPosition; }
        public int getNewPosition() { return newPosition; }
        public int getStepsMoved() { return stepsMoved; }
        public boolean isUsedShortcut() { return usedShortcut; }
        public List<Integer> getMovementPath() { return movementPath; }
    }
}
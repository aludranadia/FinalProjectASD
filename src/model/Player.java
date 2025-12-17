package model;

import java.util.Stack;

// Implements Comparable untuk sorting Leaderboard berdasarkan koin
public class Player implements Comparable<Player> {
    private String name;
    private int currentPosition;
    private Stack<Integer> steps;
    private String color;
    private String imagePath;

    // Tambahan: Total Koin
    private int coins;

    public Player(String name, String color, String imagePath) {
        this.name = name;
        this.currentPosition = 1;
        this.steps = new Stack<>();
        this.steps.push(1);
        this.color = color;
        this.imagePath = imagePath;
        this.coins = 0; // Mulai dengan 0 koin
    }

    // --- LOGIKA KOIN ---
    public void addCoins(int amount) {
        this.coins += amount;
        if (this.coins < 0) this.coins = 0; // Tidak boleh minus
    }

    public int getCoins() { return coins; }

    // --- LOGIKA SORTING (Priority Queue) ---
    @Override
    public int compareTo(Player other) {
        // Urutkan dari koin terbanyak ke terkecil
        return Integer.compare(other.coins, this.coins);
    }

    // --- METHOD LAMA TETAP SAMA ---
    public void moveForward(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            if (currentPosition < 64) { currentPosition++; steps.push(currentPosition); } else break;
        }
    }
    public void setPosition(int pos) { this.currentPosition = pos; this.steps.push(pos); }
    public void moveBackward(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            if (steps.size() > 1) { steps.pop(); currentPosition = steps.peek(); } else { currentPosition = 1; break; }
        }
    }
    public boolean hasWon() { return currentPosition == 64; }
    public int getTotalSteps() { return steps.size() - 1; }
    public String getName() { return name; }
    public int getCurrentPosition() { return currentPosition; }
    public String getColor() { return color; }
    public String getImagePath() { return imagePath; }
}
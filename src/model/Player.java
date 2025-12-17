package model;

import java.util.Stack;

public class Player {
    private String name;
    private int currentPosition;           // Posisi saat ini (1-64)
    private Stack<Integer> steps;          // History posisi yang pernah dikunjungi
    private String color;                  // Warna karakter untuk identifikasi
    private String imagePath;              // Path gambar karakter

    public Player(String name, String color) {
        this.name = name;
        this.currentPosition = 1;          // Semua player start dari node 1
        this.steps = new Stack<>();
        this.steps.push(1);                // Push posisi awal
        this.color = color;
        this.imagePath = "resources/images/player_" + color + ".png";
    }

    public Player(String name, String color, String imagePath) {
        this.name = name;
        this.currentPosition = 1;
        this.steps = new Stack<>();
        this.steps.push(1);
        this.color = color;
        this.imagePath = imagePath;
    }

    // Maju sejumlah langkah
    public void moveForward(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            if (currentPosition < 64) {
                currentPosition++;
                steps.push(currentPosition);
            } else {
                break; // Sudah di node 64
            }
        }
    }

    // Mundur sejumlah langkah menggunakan pop stack
    public void moveBackward(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            if (steps.size() > 1) {          // Minimal tetap ada 1 posisi (node 1)
                steps.pop();
                currentPosition = steps.peek();
            } else {
                currentPosition = 1;         // Tidak bisa negatif
                break;
            }
        }
    }

    // Check apakah sudah menang (mencapai node 64)
    public boolean hasWon() {
        return currentPosition == 64;
    }

    // Get total langkah yang sudah diambil
    public int getTotalSteps() {
        return steps.size() - 1; // -1 karena posisi awal tidak dihitung sebagai step
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    public Stack<Integer> getSteps() { return steps; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return name + " (Position: " + currentPosition + ", Total Steps: " + getTotalSteps() + ")";
    }
}

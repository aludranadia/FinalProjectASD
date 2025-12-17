package model;

import java.util.Stack;

public class Player {
    private String name;
    private int currentPosition;
    private Stack<Integer> steps;
    private String color;
    private String imagePath; // Path ke file gambar

    // Constructor updated
    public Player(String name, String color, String imagePath) {
        this.name = name;
        this.currentPosition = 1;
        this.steps = new Stack<>();
        this.steps.push(1);
        this.color = color;
        this.imagePath = imagePath;
    }

    // Logic movement tetap sama...
    public void moveForward(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            if (currentPosition < 64) {
                currentPosition++;
                steps.push(currentPosition);
            } else {
                break;
            }
        }
    }

    public void moveBackward(int numSteps) {
        for (int i = 0; i < numSteps; i++) {
            if (steps.size() > 1) {
                steps.pop();
                currentPosition = steps.peek();
            } else {
                currentPosition = 1;
                break;
            }
        }
    }

    public boolean hasWon() {
        return currentPosition == 64;
    }

    public int getTotalSteps() {
        return steps.size() - 1;
    }

    // Getters Setters
    public String getName() { return name; }
    public int getCurrentPosition() { return currentPosition; }
    public String getColor() { return color; }
    public String getImagePath() { return imagePath; } // Penting untuk View

    @Override
    public String toString() {
        return name + " at " + currentPosition;
    }
}
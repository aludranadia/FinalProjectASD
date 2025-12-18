package maze.model;

import java.awt.Color;

public enum CellType {
    TERRACE(0, Color.WHITE),            // Cost 0
    GRASS(1, new Color(144, 238, 144)), // Cost 1
    MUD(5, new Color(139, 69, 19)),     // Cost 5
    WATER(10, new Color(65, 105, 225)), // Cost 10
    WALL(Integer.MAX_VALUE, Color.BLACK); // Cost Tak Terhingga

    // Field untuk menyimpan harga/cost dan warna
    private final int cost;
    private final Color color;

    CellType(int cost, Color color) {
        this.cost = cost;
        this.color = color;
    }

    public int getCost() {
        return cost;
    }

    public Color getColor() {
        return color;
    }
}
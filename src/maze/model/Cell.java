package maze.model;

import java.util.ArrayList;
import java.util.List;

public class Cell implements Comparable<Cell> {
    private final int row;
    private final int col;
    private CellType type;

    // Graph representation: Adjacency List (Tetangga)
    private List<Cell> neighbors;

    // Variabel bantu untuk algoritma (visited, parent, distance)
    private boolean visited;
    private List<Cell> parents;
    private double distance; // g-score (biaya dari start ke node ini)
    private double fScore;   // f-score (g-score + heuristic untuk A*)

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.type = CellType.WALL; // Default semua tembok dulu sebelum digenerate
        this.neighbors = new ArrayList<>();
        this.parents = new ArrayList<>();
        resetAlgorithmData();
    }

    public void addNeighbor(Cell cell) {
        if (!neighbors.contains(cell)) {
            neighbors.add(cell);
        }
    }

    public void resetAlgorithmData() {
        this.visited = false;
        this.parents.clear();
        this.distance = Double.MAX_VALUE;
        this.fScore = Double.MAX_VALUE;
    }

    // --- Getters & Setters ---
    public int getRow() { return row; }
    public int getCol() { return col; }
    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }
    public List<Cell> getNeighbors() { return neighbors; }

    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }

    public List<Cell> getParents() { return parents; }
    public void addParent(Cell parent) {
        if (!this.parents.contains(parent)) {
            this.parents.add(parent);
        }
    }
    public void setParent(Cell parent) {
        this.parents.clear();
        this.parents.add(parent);
    }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    public double getFScore() { return fScore; }
    public void setFScore(double fScore) { this.fScore = fScore; }

    @Override
    public int compareTo(Cell other) {
        // PriorityQueue akan mengurutkan berdasarkan fScore (untuk A*) atau distance (Dijkstra)
        return Double.compare(this.fScore, other.fScore);
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
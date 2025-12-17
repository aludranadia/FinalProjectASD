package model;

public class Node {
    private int number;      // Nomor node (1-64)
    private int row;         // Posisi baris (0-7)
    private int col;         // Posisi kolom (0-7)
    private double x;        // Koordinat X untuk GUI
    private double y;        // Koordinat Y untuk GUI

    public Node(int number, int row, int col) {
        this.number = number;
        this.row = row;
        this.col = col;
    }

    public Node(int number, int row, int col, double x, double y) {
        this.number = number;
        this.row = row;
        this.col = col;
        this.x = x;
        this.y = y;
    }

    // Getters and Setters
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    @Override
    public String toString() {
        return "Node " + number + " (" + row + "," + col + ")";
    }
}
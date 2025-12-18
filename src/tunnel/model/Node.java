package tunnel.model;

public class Node {
    private int number;
    private int row;
    private int col;
    private int coinValue; // Positif (+) = Bonus, Negatif (-) = Dicuri

    public Node(int number, int row, int col) {
        this.number = number;
        this.row = row;
        this.col = col;
        this.coinValue = 0;
    }

    public int getNumber() { return number; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public int getCoinValue() { return coinValue; }
    public void setCoinValue(int coinValue) { this.coinValue = coinValue; }
}
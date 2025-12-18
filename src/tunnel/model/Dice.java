package tunnel.model;

import java.util.Random;

public class Dice {
    private Random random;
    private int lastNumber;        // Angka terakhir yang keluar (1-6)
    private String lastColor;      // Warna terakhir ("GREEN" atau "RED")

    public Dice() {
        this.random = new Random();
    }

    public DiceResult roll() {
        lastNumber = random.nextInt(6) + 1;

        // 70% hijau, 30% merah
        double probability = random.nextDouble();
        lastColor = (probability <= 0.7) ? "GREEN" : "RED";

        return new DiceResult(lastNumber, lastColor);
    }

    public int getLastNumber() { return lastNumber; }
    public String getLastColor() { return lastColor; }

    // Inner class untuk menyimpan hasil roll
    public static class DiceResult {
        private int number;
        private String color;

        public DiceResult(int number, String color) {
            this.number = number;
            this.color = color;
        }

        public int getNumber() { return number; }
        public String getColor() { return color; }

        public boolean isGreen() { return color.equals("GREEN"); }
        public boolean isRed() { return color.equals("RED"); }

        @Override
        public String toString() {
            return color + " " + number;
        }
    }
}
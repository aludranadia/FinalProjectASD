package model;

public class Food {
    private String name;
    private int energyValue;    // Energi yang dihasilkan (1 energi = 1 langkah)

    public Food(String name, int energyValue) {
        this.name = name;
        this.energyValue = energyValue;
    }

    // Get food berdasarkan angka dadu
    public static Food getFoodByDiceNumber(int diceNumber) {
        switch (diceNumber) {
            case 1: return new Food("🍬 Permen", 1);
            case 2: return new Food("🍜 Mie Instan", 2);
            case 3: return new Food("🍞 Roti", 3);
            case 4: return new Food("🍚 Nasi Goreng", 4);
            case 5: return new Food("🥩 Steak", 5);
            case 6: return new Food("🍱 Buffet Lengkap", 6);
            default: return new Food("❓ Unknown", 0);
        }
    }

    // Getters
    public String getName() { return name; }
    public int getEnergyValue() { return energyValue; }

    @Override
    public String toString() {
        return name + " (Energi: " + energyValue + ")";
    }
}
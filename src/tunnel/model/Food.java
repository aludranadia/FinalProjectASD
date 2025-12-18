package tunnel.model;

public class Food {
    private String name;
    private int energyValue;

    public Food(String name, int energyValue) {
        this.name = name;
        this.energyValue = energyValue;
    }

    public static Food getFoodByDiceNumber(int diceNumber) {
        switch (diceNumber) {
            // Menggunakan emoji yang lebih standard/lama agar terbaca di semua Windows
            case 1: return new Food("🍬 Permen", 1);
            case 2: return new Food("🍜 Mie", 2);
            case 3: return new Food("🍞 Roti", 3);
            case 4: return new Food("🍚 Nasi", 4);
            case 5: return new Food("🍖 Daging", 5); // GANTI Steak (🥩) jadi Daging (🍖) biar aman
            case 6: return new Food("💎 Buffet", 6);
            default: return new Food("❓ Unknown", 0);
        }
    }

    public String getName() { return name; }
    public int getEnergyValue() { return energyValue; }
}
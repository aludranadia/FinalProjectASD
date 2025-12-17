import controller.GameController;
import view.IntroScreen;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set look and feel ke sistem operasi
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Jalankan GUI di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            GameController gameController = new GameController();
            IntroScreen introScreen = new IntroScreen(gameController);
            introScreen.setVisible(true);
        });
    }
}
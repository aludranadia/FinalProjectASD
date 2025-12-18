package tunnel;

import tunnel.controller.GameController;
import tunnel.view.IntroScreen;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            new IntroScreen(controller).setVisible(true);
        });
    }
}
import javax.swing.*;
import java.awt.*;

// Import komponen dari Game 1 (Tunnel)
import tunnel.controller.GameController;
import tunnel.view.IntroScreen;

// Import komponen dari Game 2 (Maze)
import maze.view.MazePanel;

public class MainLauncher extends JFrame {

    public MainLauncher() {
        setTitle("Final Project ASD - Game Center");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        getContentPane().setBackground(new Color(44, 62, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Jarak antar elemen
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Tombol memenuhi lebar

        // --- JUDUL ---
        JLabel titleLabel = new JLabel("PILIH GAME", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.BOLD, 36));
        titleLabel.setForeground(new Color(236, 240, 241));
        gbc.gridy = 0;
        add(titleLabel, gbc);

        // --- TOMBOL GAME 1: TUNNEL ESCAPE ---
        JButton btnTunnel = createStyledButton("🎮 Tunnel Escape (Queue & Stack)");
        btnTunnel.addActionListener(e -> launchTunnelGame());
        gbc.gridy = 1;
        add(btnTunnel, gbc);

        // --- TOMBOL GAME 2: MAZE SOLVER ---
        JButton btnMaze = createStyledButton("🧠 Maze Graph Solver (BFS/DFS/A*)");
        btnMaze.addActionListener(e -> launchMazeGame());
        gbc.gridy = 2;
        add(btnMaze, gbc);

        // --- TOMBOL KELUAR ---
        JButton btnExit = createStyledButton("❌ Keluar");
        btnExit.setBackground(new Color(192, 57, 43)); // Merah
        btnExit.addActionListener(e -> System.exit(0));
        gbc.gridy = 3;
        add(btnExit, gbc);
    }

    // --- LOGIKA MEMBUKA GAME TUNNEL ---
    private void launchTunnelGame() {
        this.dispose(); // Tutup Launcher
        SwingUtilities.invokeLater(() -> {
            // Panggil Controller & View Game Tunnel
            GameController controller = new GameController();
            new IntroScreen(controller).setVisible(true);
        });
    }

    private void launchMazeGame() {
        this.dispose(); // Tutup Launcher
        SwingUtilities.invokeLater(() -> {
            JFrame mazeFrame = new JFrame("Maze Graph Solver - BFS/DFS/Dijkstra/A*");
            mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mazeFrame.setSize(1000, 750);
            mazeFrame.setLocationRelativeTo(null);
            mazeFrame.setResizable(false);

            mazeFrame.add(new MazePanel());

            mazeFrame.setVisible(true);
        });
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(230, 126, 34));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(300, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MainLauncher().setVisible(true);
        });
    }
}
import javax.swing.*;
import java.awt.*;

// Import entry point game Tunnel kamu (sesuaikan nama package setelah refactor tadi)
import tunnel.controller.GameController;
import tunnel.view.IntroScreen;
import maze.view.MazePanel;

public class MainLauncher extends JFrame {

    public MainLauncher() {
        setTitle("Final Project ASD - Game Center");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1, 10, 10));

        JLabel title = new JLabel("Pilih Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title);

        // --- TOMBOL GAME 1 (Tunnel Escape) ---
        JButton btnTunnel = new JButton("Mainkan Tunnel Escape");
        btnTunnel.addActionListener(e -> {
            // Logika memanggil game lama
            this.dispose(); // Tutup menu launcher

            // Panggil Controller & View game lama
            // (Copy dari public static void main game lama kamu)
            SwingUtilities.invokeLater(() -> {
                GameController controller = new GameController();
                new IntroScreen(controller).setVisible(true);
            });
        });
        add(btnTunnel);

        JButton btnGameBaru = new JButton("Mainkan Maze Solver (Graph)");
        btnGameBaru.addActionListener(e -> {
            this.dispose(); // Tutup launcher

            // Buat Window Baru untuk Maze
            JFrame mazeFrame = new JFrame("Maze Graph Solver - BFS/DFS/Dijkstra/A*");
            mazeFrame.setSize(1000, 700);
            mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mazeFrame.setLocationRelativeTo(null);

            // Tambahkan Panel Maze
            mazeFrame.add(new MazePanel());

            mazeFrame.setVisible(true);
        });
        add(btnGameBaru);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainLauncher().setVisible(true);
        });
    }
}
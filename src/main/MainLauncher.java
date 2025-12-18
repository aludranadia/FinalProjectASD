package main;

import javax.swing.*;
import java.awt.*;

// Import komponen dari Game 1 (Tunnel)
import tunnel.controller.GameController;
import tunnel.view.IntroScreen;

// Import komponen dari Game 2 (Maze)
import maze.view.IntroScreenMaze;

public class MainLauncher extends JFrame {

    public MainLauncher() {
        setTitle("Final Project ASD - Game Center");
        setSize(500, 450); // Ukuran sedikit diperbesar agar lega
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Background Biru Gelap Elegan
        getContentPane().setBackground(new Color(44, 62, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- JUDUL ---
        JLabel titleLabel = new JLabel("PILIH GAME", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.BOLD, 42));
        titleLabel.setForeground(new Color(236, 240, 241));
        gbc.gridy = 0;
        add(titleLabel, gbc);

        // --- TOMBOL GAME 1: TUNNEL ESCAPE ---
        JButton btnTunnel = createStyledButton("Tunnel Escape", new Color(230, 126, 34));
        btnTunnel.addActionListener(e -> launchTunnelGame());
        gbc.gridy = 1;
        add(btnTunnel, gbc);

        // --- TOMBOL GAME 2: MAZE SOLVER ---
        JButton btnMaze = createStyledButton("Maze Graph Solver", new Color(52, 152, 219));
        btnMaze.addActionListener(e -> launchMazeGame());
        gbc.gridy = 2;
        add(btnMaze, gbc);

        // --- TOMBOL KELUAR ---
        JButton btnExit = createStyledButton("Keluar", new Color(192, 57, 43));
        btnExit.addActionListener(e -> System.exit(0));
        gbc.gridy = 3;
        add(btnExit, gbc);
    }

    // --- LOGIKA MEMBUKA GAME TUNNEL ---
    private void launchTunnelGame() {
        this.dispose(); // Tutup Menu Utama
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            new IntroScreen(controller).setVisible(true);
        });
    }

    // --- LOGIKA MEMBUKA GAME MAZE ---
    private void launchMazeGame() {
        this.dispose(); // Tutup Menu Utama
        SwingUtilities.invokeLater(() -> {
            new IntroScreenMaze().setVisible(true);
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE); // Teks Putih agar kontras
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // Flat style
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(350, 55));
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
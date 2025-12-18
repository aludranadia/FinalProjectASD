package main;

import javax.swing.*;
import java.awt.*;

import tunnel.controller.GameController;
import tunnel.view.IntroScreen;
import maze.view.IntroScreenMaze;

public class MainLauncher extends JFrame {

    private SoundManagerMain soundManager;

    public MainLauncher() {
        soundManager = new SoundManagerMain();
        soundManager.playBGM();

        setTitle("Final Project ASD - Game Center");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        getContentPane().setBackground(new Color(44, 62, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("PILIH GAME", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.BOLD, 42));
        titleLabel.setForeground(new Color(236, 240, 241));
        gbc.gridy = 0;
        add(titleLabel, gbc);

        JButton btnTunnel = createStyledButton("Tunnel Escape", new Color(230, 126, 34));
        btnTunnel.addActionListener(e -> {
            soundManager.playClick();
            launchTunnelGame();
        });
        gbc.gridy = 1;
        add(btnTunnel, gbc);

        JButton btnMaze = createStyledButton("Maze Graph Solver", new Color(52, 152, 219));
        btnMaze.addActionListener(e -> {
            soundManager.playClick();
            launchMazeGame();
        });
        gbc.gridy = 2;
        add(btnMaze, gbc);

        JButton btnExit = createStyledButton("Keluar", new Color(192, 57, 43));
        btnExit.addActionListener(e -> {
            soundManager.playClick();
            soundManager.stopBGM();

            Timer timer = new Timer(400, event -> System.exit(0)); // Delay 400ms
            timer.setRepeats(false);
            timer.start();
        });
        gbc.gridy = 3;
        add(btnExit, gbc);
    }

    private void launchTunnelGame() {
        soundManager.stopBGM();
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            new IntroScreen(controller).setVisible(true);
        });
    }

    private void launchMazeGame() {
        soundManager.stopBGM();
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            new IntroScreenMaze().setVisible(true);
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(350, 55));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });

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
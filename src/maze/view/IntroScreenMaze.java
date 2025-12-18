package maze.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class IntroScreenMaze extends JFrame {
    private Image backgroundImage;

    public IntroScreenMaze() {
        setTitle("Maze Graph Solver - Intro");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load Background Image
        try {
            File bgFile = new File("resources/maze/images/maze_bg.jpg");
            if (bgFile.exists()) {
                backgroundImage = ImageIO.read(bgFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Panel Utama dengan Custom Painting untuk Background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

                    g.setColor(new Color(0, 0, 0, 150));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, new Color(10, 20, 40), 0, getHeight(), new Color(30, 60, 90));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        // --- ELEMEN UI ---

        // 1. Judul Game
        JLabel titleLabel = new JLabel("THE LABYRINTH");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Cinzel", Font.BOLD, 60)); // Font seram/elegan jika ada, atau Serif
        titleLabel.setForeground(new Color(255, 215, 0)); // Emas

        JLabel subtitleLabel = new JLabel("Graph Algorithm Visualizer");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        subtitleLabel.setForeground(new Color(200, 200, 200));

        // 2. Deskripsi Singkat
        JTextArea descArea = new JTextArea(
                "Explore the power of Graph Theory.\n" +
                        "Visualize how algorithms like BFS, DFS, Dijkstra, and A* \n" +
                        "find their way through complex mazes and weighted terrains."
        );
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setFont(new Font("SansSerif", Font.ITALIC, 16));
        descArea.setForeground(Color.WHITE);
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        descArea.setMaximumSize(new Dimension(600, 100));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        // Center alignment text trick
        // (JTextArea agak tricky buat center text, jadi kita pakai alignmentX container saja)

        // 3. Tombol Start
        JButton startButton = new JButton("ENTER THE MAZE");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 22));
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(new Color(0, 150, 136)); // Teal color
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efek Hover pada Tombol
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                startButton.setBackground(new Color(0, 121, 107)); // Lebih gelap saat hover
                startButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 215, 0), 2), // Border jadi emas
                        BorderFactory.createEmptyBorder(10, 30, 10, 30)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                startButton.setBackground(new Color(0, 150, 136));
                startButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        BorderFactory.createEmptyBorder(10, 30, 10, 30)
                ));
            }
        });

        // Action Listener untuk masuk ke Game
        startButton.addActionListener(e -> {
            this.dispose(); // Tutup Intro
            openGameWindow(); // Buka Game Utama
        });

        // --- MENYUSUN LAYOUT ---
        mainPanel.add(Box.createVerticalGlue()); // Push ke tengah
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        mainPanel.add(descArea);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        mainPanel.add(startButton);
        mainPanel.add(Box.createVerticalGlue()); // Push ke tengah

        add(mainPanel);
    }

    private void openGameWindow() {
        JFrame gameFrame = new JFrame("Maze Graph Solver - Gameplay");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(1000, 750);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(false);

        // Memanggil MazePanel yang sudah kamu punya
        gameFrame.add(new MazePanel());

        gameFrame.setVisible(true);
    }

    // Main method untuk testing tampilan ini saja
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IntroScreenMaze().setVisible(true));
    }
}
package maze.view;

import main.MainLauncher;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class IntroScreenMaze extends JFrame {
    private BufferedImage backgroundImage;

    public IntroScreenMaze() {
        setTitle("The Maze - Graph Visualizer");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        loadBackground();
        initComponents();
    }

    private void loadBackground() {
        try {
            File bgFile = new File("resources/maze/images/maze_bg.jpg");
            if (bgFile.exists()) {
                backgroundImage = ImageIO.read(bgFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        // PANEL UTAMA DENGAN BACKGROUND DAN OVERLAY GELAP
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gambar Background
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                    // Overlay Gelap agar teks putih terbaca jelas
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(10, 20, 40), 0, getHeight(), new Color(0, 0, 0));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // --- 1. JUDUL UTAMA ---
        JLabel titleLabel = new JLabel("THE MAZE");
        titleLabel.setFont(new Font("Impact", Font.BOLD, 90));
        titleLabel.setForeground(new Color(255, 215, 0)); // Emas

        // Efek Shadow Tebal
        titleLabel.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 200));
                g2.drawString("THE MAZE", 8, 88); // Shadow offset
                super.paint(g, c);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(titleLabel, gbc);

        // --- 2. SUB-JUDUL ---
        JLabel subtitleLabel = new JLabel("GRAPH ALGORITHM VISUALIZER");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        subtitleLabel.setForeground(new Color(200, 200, 200)); // Abu-abu terang

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(subtitleLabel, gbc);

        // --- 3. DESKRIPSI SINGKAT (UPDATED) ---
        String descText = "<html><div style='text-align: center; color: white; font-family: Segoe UI; font-size: 20px; font-weight: bold;'>" +
                "Choose your graph algorithm to find the way out" +
                "</div></html>";

        JLabel descLabel = new JLabel(descText);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 60, 0); // Jarak ke tombol
        mainPanel.add(descLabel, gbc);

        // --- 4. TOMBOL AKSI ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        // Tombol Back
        JButton backBtn = createStyledButton("BACK TO MENU", new Color(192, 57, 43), Color.WHITE);
        backBtn.addActionListener(e -> {
            this.dispose();
            new MainLauncher().setVisible(true);
        });

        // Tombol Start
        JButton startBtn = createStyledButton("ENTER THE MAZE", new Color(255, 193, 7), Color.BLACK);
        startBtn.setPreferredSize(new Dimension(250, 55));
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        startBtn.addActionListener(e -> {
            this.dispose();
            openGameWindow();
        });

        buttonPanel.add(backBtn);
        buttonPanel.add(startBtn);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 50));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });

        return btn;
    }

    private void openGameWindow() {
        JFrame gameFrame = new JFrame("Maze Graph Solver - Gameplay");
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setSize(1000, 800);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(false);

        gameFrame.add(new MazePanel());

        gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                new MainLauncher().setVisible(true);
            }
        });

        gameFrame.setVisible(true);
    }
}
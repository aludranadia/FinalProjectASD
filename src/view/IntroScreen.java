package view;

import controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class IntroScreen extends JFrame {
    private GameController gameController;
    private float pulseAlpha = 0.5f;
    private boolean pulseUp = true;

    public IntroScreen(GameController gameController) {
        this.gameController = gameController;
        initComponents();
        startPulseAnimation();
    }

    private void initComponents() {
        setTitle("Tunnel Escape - The Journey");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(10, 15, 30),
                        getWidth(), getHeight(), new Color(25, 40, 60)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(new Color(255, 215, 0, 20));
                g2d.fillOval(getWidth()-300, -100, 500, 500);
                g2d.setColor(new Color(0, 200, 255, 20));
                g2d.fillOval(-100, getHeight()-300, 400, 400);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // --- TITLE ---
        JLabel titleLabel = new JLabel("TUNNEL ESCAPE");
        titleLabel.setFont(new Font("Impact", Font.BOLD, 72));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(new Color(0,0,0, 100));
                g2d.drawString("TUNNEL ESCAPE", 5, 65);
                super.paint(g, c);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);

        // --- DESCRIPTION BOX (Glassmorphism) ---
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            }
        };
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setPreferredSize(new Dimension(500, 200));
        glassPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        glassPanel.setOpaque(false);

        String descText = "<html><center>" +
                "<font size='5' color='#4db8ff'><b>MISSION:</b></font><br><br>" +
                "<font size='4' color='white'>Escape the dark tunnel by reaching Node 64.<br>" +
                "Collect energy (food) to move faster.<br>" +
                "Beware of the <font color='#ff6b6b'>RED DICE</font> that pushes you back!</font>" +
                "</center></html>";
        JLabel descLabel = new JLabel(descText);
        // PERBAIKAN 1: Teks di tengah
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setAlignmentX(CENTER_ALIGNMENT);
        glassPanel.add(descLabel);

        gbc.gridy = 1;
        mainPanel.add(glassPanel, gbc);

        // --- PLAY BUTTON ---
        JButton playButton = new JButton("START GAME");
        playButton.setFont(new Font("Arial", Font.BOLD, 24));
        playButton.setForeground(Color.BLACK);
        playButton.setBackground(new Color(255, 215, 0));
        playButton.setFocusPainted(false);
        playButton.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playButton.addActionListener(e -> showPlayerSetup());

        gbc.gridy = 2;
        gbc.insets = new Insets(40, 0, 0, 0);
        mainPanel.add(playButton, gbc);

        add(mainPanel);
    }

    private void startPulseAnimation() {
        new Timer(50, e -> {
            if (pulseUp) {
                pulseAlpha += 0.02f;
                if (pulseAlpha >= 1.0f) pulseUp = false;
            } else {
                pulseAlpha -= 0.02f;
                if (pulseAlpha <= 0.5f) pulseUp = true;
            }
            repaint();
        }).start();
    }

    private void showPlayerSetup() {
        JDialog dialog = new JDialog(this, "Setup", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 40));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel("Number of Players:");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setAlignmentX(CENTER_ALIGNMENT);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, 2, 8, 1));
        spinner.setMaximumSize(new Dimension(100, 40));
        spinner.setFont(new Font("Arial", Font.BOLD, 20));

        // PERBAIKAN 2: Tombol Start Game di Dialog disamakan gayanya
        JButton btn = new JButton("START GAME");
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setBackground(new Color(255, 215, 0)); // Emas
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        btn.addActionListener(e -> {
            gameController.initializePlayers((int) spinner.getValue());
            gameController.startGame();
            new GameBoard(gameController).setVisible(true);
            dialog.dispose();
            this.dispose();
        });

        panel.add(Box.createVerticalGlue());
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(spinner);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(btn);
        panel.add(Box.createVerticalGlue());

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
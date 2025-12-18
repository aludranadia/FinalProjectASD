package view;

import controller.GameController;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IntroScreen extends JFrame {
    private GameController gameController;
    private float pulseAlpha = 0.5f;
    private boolean pulseUp = true;
    private SoundManager soundManager;
    private BufferedImage backgroundImage;

    // Daftar Avatar
    private final String[] AVATAR_PATHS = {
            "resources/images/player 1.png", "resources/images/player 2.png",
            "resources/images/player 3.png", "resources/images/player 4.png",
            "resources/images/player 5.png", "resources/images/player 6.png",
            "resources/images/player 7.png", "resources/images/player 8.png"
    };

    private final String[] AVATAR_NAMES = {
            "Miner Bob", "Explorer", "Knight", "Dwarf",
            "Elf", "Wizard", "Rogue", "Paladin"
    };

    public IntroScreen(GameController gameController) {
        this.gameController = gameController;
        this.soundManager = new SoundManager();
        this.soundManager.playLoop("intro_bgm");

        loadBackground(); // Load background image
        initComponents();
        startPulseAnimation();
    }

    private void loadBackground() {
        try {
            File bgFile = new File("resources/images/bg.png");
            if (bgFile.exists()) {
                backgroundImage = ImageIO.read(bgFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setTitle("Tunnel Escape - The Journey");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw Background Image if available
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                    // Add Dark Overlay for readability
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    // Fallback Gradient
                    GradientPaint gp = new GradientPaint(
                            0, 0, new Color(15, 20, 35),
                            getWidth(), getHeight(), new Color(25, 40, 60)
                    );
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // --- TITLE ---
        JLabel titleLabel = new JLabel("TUNNEL ESCAPE");
        titleLabel.setFont(new Font("Impact", Font.BOLD, 80));
        titleLabel.setForeground(new Color(255, 215, 0));
        // Shadow Effect Logic
        titleLabel.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                // Drop Shadow
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.drawString("TUNNEL ESCAPE", 8, 73);
                // Main Text handled by super
                super.paint(g, c);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);

        // --- DESCRIPTION BOX (Glass Effect) ---
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Darker glass for better contrast
                g2d.setColor(new Color(20, 15, 10, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                // Gold Border
                g2d.setColor(new Color(255, 215, 0, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            }
        };
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setPreferredSize(new Dimension(650, 260));
        glassPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        glassPanel.setOpaque(false);

        // Gunakan Font yang aman untuk Emoji
        String fontStyle = "font-family: 'Segoe UI Emoji', 'Segoe UI', sans-serif;";

        String descText = "<html><div style='text-align: center; " + fontStyle + " color: white;'>" +
                "<h2 style='color: #FFD700; margin-bottom: 15px; font-size: 18px;'>⚡ MISSION PROTOCOL ⚡</h2>" +
                "<p style='font-size: 14px; line-height: 1.6; margin-bottom: 10px;'>" +
                "🏃‍♂️ <b>Goal:</b> Reach <span style='color: #4db8ff;'>Node 64</span> to escape the dark tunnel.<br>" +
                "🍖 <b>Survival:</b> Roll dice to collect food & energy to move forward.<br>" +
                "⚠️ <b>Danger:</b> Watch out for the <span style='color: #ff6b6b;'>RED DICE</span>! <br>" +
                "The wind will blow you backwards!</p>" +
                "</div></html>";

        JLabel descLabel = new JLabel(descText);
        descLabel.setAlignmentX(CENTER_ALIGNMENT);
        glassPanel.add(descLabel);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 40, 20);
        mainPanel.add(glassPanel, gbc);

        // --- PLAY BUTTON ---
        JButton playButton = new JButton("START ADVENTURE") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient Button (Gold to Orange)
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 215, 0), 0, getHeight(), new Color(255, 140, 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        playButton.setPreferredSize(new Dimension(300, 65));
        playButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        playButton.setForeground(new Color(40, 20, 0)); // Dark Brown Text
        playButton.setFocusPainted(false);
        playButton.setContentAreaFilled(false);
        playButton.setBorderPainted(false);
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playButton.addActionListener(e -> showPlayerCountDialog());

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 20, 0);
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

    // --- DIALOGS (Sama seperti sebelumnya, hanya styling minor) ---
    private void showPlayerCountDialog() {
        JDialog dialog = new JDialog(this, "Setup", true);
        dialog.setUndecorated(true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(35, 30, 25)); // Dark Brown theme
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 100, 50), 2),
                new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel lbl = new JLabel("Number of Players");
        lbl.setForeground(new Color(255, 200, 100));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lbl.setAlignmentX(CENTER_ALIGNMENT);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, 1, 8, 1));
        spinner.setMaximumSize(new Dimension(150, 50));
        spinner.setFont(new Font("Segoe UI", Font.BOLD, 28));

        // Styling Spinner Editor
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setBackground(new Color(60, 50, 40));
            ((JSpinner.DefaultEditor)editor).getTextField().setForeground(Color.WHITE);
            ((JSpinner.DefaultEditor)editor).getTextField().setHorizontalAlignment(JTextField.CENTER);
        }
        spinner.setBorder(null);

        JButton btn = createStyledButton("NEXT", new Color(230, 126, 34), new Color(40,20,0));
        btn.setMaximumSize(new Dimension(200, 50));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            int count = (int) spinner.getValue();
            dialog.dispose();
            showPlayerDetailsDialog(count);
        });

        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(spinner);
        panel.add(Box.createRigidArea(new Dimension(0, 40)));
        panel.add(btn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showPlayerDetailsDialog(int playerCount) {
        JDialog dialog = new JDialog(this, "Customize", true);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(this);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(30, 25, 20));

        JLabel header = new JLabel("SETUP HEROES");
        header.setFont(new Font("Impact", Font.PLAIN, 36));
        header.setForeground(new Color(255, 215, 0));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setBorder(new EmptyBorder(20, 0, 20, 0));
        mainContainer.add(header, BorderLayout.NORTH);

        JPanel inputsPanel = new JPanel();
        inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));
        inputsPanel.setBackground(new Color(30, 25, 20));

        List<JTextField> nameFields = new ArrayList<>();
        List<JComboBox<AvatarOption>> avatarCombos = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            row.setBackground(new Color(45, 40, 35));
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 70, 60)));
            row.setMaximumSize(new Dimension(550, 70));

            JLabel numLabel = new JLabel("P" + (i + 1));
            numLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            numLabel.setForeground(new Color(230, 126, 34));

            JLabel nameLbl = new JLabel("Name:");
            nameLbl.setForeground(Color.LIGHT_GRAY);

            JTextField nameField = new JTextField("Player " + (i + 1), 10);
            styleTextField(nameField);

            JComboBox<AvatarOption> avatarCombo = new JComboBox<>();
            for (int j = 0; j < AVATAR_PATHS.length; j++) {
                avatarCombo.addItem(new AvatarOption(AVATAR_NAMES[j], AVATAR_PATHS[j]));
            }
            avatarCombo.setSelectedIndex(i % AVATAR_PATHS.length);
            avatarCombo.setRenderer(new AvatarRenderer());
            avatarCombo.setPreferredSize(new Dimension(170, 35));

            nameFields.add(nameField);
            avatarCombos.add(avatarCombo);

            row.add(numLabel);
            row.add(nameLbl);
            row.add(nameField);
            row.add(avatarCombo);

            inputsPanel.add(row);
            inputsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(inputsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        footer.setBackground(new Color(30, 25, 20));
        footer.setBorder(new EmptyBorder(20, 0, 20, 0));

        JButton backBtn = createStyledButton("BACK", Color.GRAY, Color.WHITE);
        backBtn.setPreferredSize(new Dimension(120, 50));
        backBtn.addActionListener(e -> {
            dialog.dispose();
            showPlayerCountDialog();
        });

        JButton startBtn = createStyledButton("START ADVENTURE!", new Color(46, 204, 113), Color.WHITE);
        startBtn.setPreferredSize(new Dimension(250, 50));
        startBtn.addActionListener(e -> {
            soundManager.stop("intro_bgm");
            List<String> names = new ArrayList<>();
            List<String> images = new ArrayList<>();
            for (int i = 0; i < playerCount; i++) {
                names.add(nameFields.get(i).getText());
                AvatarOption selected = (AvatarOption) avatarCombos.get(i).getSelectedItem();
                images.add(selected.path);
            }
            gameController.initializeCustomPlayers(names, images);
            gameController.startGame();
            new GameBoard(gameController).setVisible(true);
            dialog.dispose();
            this.dispose();
        });

        footer.add(backBtn);
        footer.add(startBtn);
        mainContainer.add(footer, BorderLayout.SOUTH);

        dialog.add(mainContainer);
        dialog.setVisible(true);
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(new Color(60, 50, 40));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 80, 60)),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    // --- COMBOBOX HELPERS ---
    private static class AvatarOption {
        String name; String path; ImageIcon icon;
        public AvatarOption(String name, String path) {
            this.name = name; this.path = path;
            try {
                ImageIcon original = new ImageIcon(path);
                Image img = original.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                this.icon = new ImageIcon(img);
            } catch (Exception e) { this.icon = null; }
        }
        @Override public String toString() { return name; }
    }

    private static class AvatarRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof AvatarOption) {
                AvatarOption option = (AvatarOption) value;
                label.setIcon(option.icon);
                label.setText(option.name);
                label.setIconTextGap(10);
            }
            return label;
        }
    }
}
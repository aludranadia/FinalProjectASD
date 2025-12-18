package tunnel.view;

import tunnel.controller.GameController;
import main.MainLauncher;

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
            "resources/tunnel/images/player 1.png", "resources/tunnel/images/player 2.png",
            "resources/tunnel/images/player 3.png", "resources/tunnel/images/player 4.png",
            "resources/tunnel/images/player 5.png", "resources/tunnel/images/player 6.png",
            "resources/tunnel/images/player 7.png", "resources/tunnel/images/player 8.png"
    };

    private final String[] AVATAR_NAMES = {
            "Miner Bob", "Explorer", "Knight", "Dwarf",
            "Elf", "Wizard", "Rogue", "Paladin"
    };

    public IntroScreen(GameController gameController) {
        this.gameController = gameController;

        // 1. Init Sound & Play Intro BGM
        this.soundManager = new SoundManager();
        this.soundManager.playLoop("intro_bgm");

        loadBackground();
        initComponents();
        startPulseAnimation();

        // 2. Stop music on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (soundManager != null) {
                    soundManager.stopAllBGM();
                }
            }
        });
    }

    private void loadBackground() {
        try {
            File bgFile = new File("resources/tunnel/images/BgIntro.png");
            if (bgFile.exists()) {
                backgroundImage = ImageIO.read(bgFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setTitle("Tunnel Escape - The Journey");
        setSize(1000, 700);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full Screen
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(15, 20, 35), getWidth(), getHeight(), new Color(25, 40, 60));
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
        titleLabel.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.drawString("TUNNEL ESCAPE", 8, 73);
                super.paint(g, c);
            }
        });
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);

        // --- DESCRIPTION (VISUAL FIX: KOTAK HITAM KEMBALI) ---
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background Hitam Transparan
                g2d.setColor(new Color(0, 0, 0, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Border Emas
                g2d.setColor(new Color(255, 215, 0, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            }
        };
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setPreferredSize(new Dimension(650, 260));
        glassPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        glassPanel.setOpaque(false); // Penting agar paintComponent custom terlihat

        String fontStyle = "font-family: 'Segoe UI Emoji', 'Segoe UI', sans-serif;";
        String descText = "<html><div style='text-align: center; " + fontStyle + "'>" +
                "<h2 style='color: #FFD700; margin-bottom: 15px; font-size: 20px;'>⚡ MISSION PROTOCOL ⚡</h2>" +
                "<span style='font-size: 15px; color: #FFFFFF;'>" +
                "🏃‍♂️ <b>Goal:</b> Reach <span style='color: #00FFFF;'>Node 64</span> to escape the dark tunnel.<br>" +
                "🍖 <b>Survival:</b> Roll dice to collect food & energy to move forward.<br>" +
                "⚠️ <b>Danger:</b> Watch out for the <span style='color: #FF3333;'>RED DICE</span>!<br>" +
                "The wind will blow you backwards!" +
                "</span></div></html>";

        JLabel descLabel = new JLabel(descText);
        descLabel.setAlignmentX(CENTER_ALIGNMENT);
        glassPanel.add(descLabel);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 40, 20);
        mainPanel.add(glassPanel, gbc);

        // --- BUTTONS ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        // Start Button
        JButton playButton = createStyledButton("START ADVENTURE", new Color(255, 215, 0), new Color(40, 20, 0));
        playButton.setPreferredSize(new Dimension(280, 60));
        playButton.addActionListener(e -> showPlayerCountDialog());

        // Back Button
        JButton backButton = createStyledButton("BACK TO MENU", new Color(192, 57, 43), Color.WHITE);
        backButton.setPreferredSize(new Dimension(200, 60));
        backButton.addActionListener(e -> {
            soundManager.stopAllBGM();
            this.dispose();
            new MainLauncher().setVisible(true);
        });

        buttonPanel.add(backButton);
        buttonPanel.add(playButton);

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 20, 0);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
    }

    // --- HELPER BUTTON DENGAN SOUND CLICK ---
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Tambahkan efek klik otomatis (PENTING: Listener ini akan dijalankan SEBELUM listener lain)
        btn.addActionListener(e -> {
            if (soundManager != null) soundManager.playClick();
        });

        return btn;
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

    // --- DIALOG 1: JUMLAH PEMAIN ---
    private void showPlayerCountDialog() {
        JDialog dialog = new JDialog(this, "Setup", true);
        dialog.setUndecorated(true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(35, 30, 25));
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

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setBackground(new Color(60, 50, 40));
            ((JSpinner.DefaultEditor)editor).getTextField().setForeground(Color.WHITE);
            ((JSpinner.DefaultEditor)editor).getTextField().setHorizontalAlignment(JTextField.CENTER);
        }
        spinner.setBorder(null);

        // Next Button (FIX: Pake createStyledButton agar ada suaranya)
        JButton btn = createStyledButton("NEXT", new Color(230, 126, 34), new Color(40,20,0));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setMaximumSize(new Dimension(200, 50));
        btn.setAlignmentX(CENTER_ALIGNMENT);

        btn.addActionListener(e -> {
            // TIDAK PERLU PANGGIL soundManager.playClick() DISINI LAGI
            // KARENA SUDAH ADA DI createStyledButton
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

    // --- DIALOG 2: CUSTOMIZE HEROES ---
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
            // VISUAL FIX: Warna teks Light Gray
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

        // Tombol BACK (FIX: Pake createStyledButton agar ada suaranya)
        JButton backBtn = createStyledButton("BACK", Color.GRAY, Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        backBtn.setPreferredSize(new Dimension(120, 50));
        backBtn.addActionListener(e -> {
            dialog.dispose();
            showPlayerCountDialog();
        });

        // Tombol START (FIX: Pake createStyledButton agar ada suaranya)
        JButton startBtn = createStyledButton("START ADVENTURE!", new Color(46, 204, 113), Color.WHITE);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startBtn.setPreferredSize(new Dimension(250, 50));
        startBtn.addActionListener(e -> {
            soundManager.stopAllBGM(); // Matikan intro BGM sebelum main
            List<String> names = new ArrayList<>();
            List<String> images = new ArrayList<>();
            for (int i = 0; i < playerCount; i++) {
                names.add(nameFields.get(i).getText());
                AvatarOption selected = (AvatarOption) avatarCombos.get(i).getSelectedItem();
                images.add(selected.path);
            }
            gameController.initializeCustomPlayers(names, images);
            gameController.startGame();

            // Masuk ke GameBoard
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
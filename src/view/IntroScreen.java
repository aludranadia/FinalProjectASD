// comment

package view;

import controller.GameController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class IntroScreen extends JFrame {
    private GameController gameController;
    private float pulseAlpha = 0.5f;
    private boolean pulseUp = true;
    private SoundManager soundManager;

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

        initComponents();
        startPulseAnimation();
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

                // Background Gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 20, 35),
                        getWidth(), getHeight(), new Color(25, 40, 60)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Ornamen Lingkaran
                g2d.setColor(new Color(255, 215, 0, 30));
                g2d.fillOval(getWidth()-250, -100, 400, 400);
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
        // Shadow Effect
        titleLabel.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(new Color(0,0,0, 150));
                g2d.drawString("TUNNEL ESCAPE", 5, 65);
                super.paint(g, c);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        // PERBAIKAN POSISI: Margin atas dikurangi agar judul naik
        gbc.insets = new Insets(10, 0, 20, 0);
        mainPanel.add(titleLabel, gbc);

        // --- DESCRIPTION BOX ---
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2d.setColor(new Color(255, 215, 0, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 40, 40);
            }
        };
        glassPanel.setLayout(new BoxLayout(glassPanel, BoxLayout.Y_AXIS));
        glassPanel.setPreferredSize(new Dimension(650, 280));
        glassPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        glassPanel.setOpaque(false);

        String descText = "<html><div style='text-align: center; font-family: Segoe UI, sans-serif; color: white;'>" +
                "<h2 style='color: #FFD700; margin-bottom: 15px; font-size: 18px;'>⚡ MISSION PROTOCOL ⚡</h2>" +
                "<p style='font-size: 14px; line-height: 1.5; margin-bottom: 10px;'>" +
                "🏃‍♂️ <b>Goal:</b> Reach <span style='color: #4db8ff;'>Node 64</span> to escape the dark tunnel.<br>" +
                "🍖 <b>Survival:</b> Roll dice to collect food & energy to move forward.<br>" +
                "⚠️ <b>Danger:</b> Watch out for the <span style='color: #ff6b6b;'>RED DICE</span>! <br>" +
                "The wind will blow you backwards!</p>" +
                "</div></html>";

        JLabel descLabel = new JLabel(descText);
        descLabel.setAlignmentX(CENTER_ALIGNMENT);
        glassPanel.add(descLabel);

        gbc.gridy = 1;
        // PERBAIKAN POSISI: Jarak antara judul dan deskripsi, dan deskripsi ke tombol
        gbc.insets = new Insets(0, 20, 30, 20);
        mainPanel.add(glassPanel, gbc);

        // --- PLAY BUTTON ---
        JButton playButton = createStyledButton("START GAME", new Color(255, 215, 0), Color.BLACK);
        playButton.setPreferredSize(new Dimension(280, 60));
        playButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        playButton.addActionListener(e -> showPlayerCountDialog());

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 40, 20, 30);
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

    // --- DIALOG 1: PILIH JUMLAH PEMAIN (Limit 1-8, Tombol NEXT Tengah) ---
    private void showPlayerCountDialog() {
        JDialog dialog = new JDialog(this, "Setup", true);
        dialog.setUndecorated(true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(35, 40, 50));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel lbl = new JLabel("Number of Players");
        lbl.setForeground(new Color(255, 215, 0));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lbl.setAlignmentX(CENTER_ALIGNMENT);

        // PERBAIKAN LIMIT: min 1, max 8, start 2
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, 1, 8, 1));
        spinner.setMaximumSize(new Dimension(150, 50));
        spinner.setFont(new Font("Segoe UI", Font.BOLD, 28));

        // Styling Spinner
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor de = (JSpinner.DefaultEditor) editor;
            de.getTextField().setBackground(new Color(60, 65, 75));
            de.getTextField().setForeground(Color.WHITE);
            de.getTextField().setHorizontalAlignment(JTextField.CENTER);
            de.getTextField().setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        }
        spinner.setBorder(BorderFactory.createEmptyBorder());

        // PERBAIKAN TOMBOL: Teks jadi "NEXT" dan alignment tengah
        JButton btn = createStyledButton("NEXT", new Color(52, 152, 219), Color.WHITE);
        btn.setMaximumSize(new Dimension(200, 50));
        btn.setAlignmentX(CENTER_ALIGNMENT); // Memastikan tengah secara horizontal di BoxLayout
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

    // --- DIALOG 2: CUSTOMIZE PLAYERS (Ada Tombol BACK) ---
    private void showPlayerDetailsDialog(int playerCount) {
        JDialog dialog = new JDialog(this, "Customize", true);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(this);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(30, 35, 45));

        JLabel header = new JLabel("SETUP HEROES");
        header.setFont(new Font("Impact", Font.PLAIN, 36));
        header.setForeground(new Color(255, 215, 0));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setBorder(new EmptyBorder(20, 0, 20, 0));
        mainContainer.add(header, BorderLayout.NORTH);

        JPanel inputsPanel = new JPanel();
        inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));
        inputsPanel.setBackground(new Color(30, 35, 45));

        List<JTextField> nameFields = new ArrayList<>();
        List<JComboBox<AvatarOption>> avatarCombos = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            row.setBackground(new Color(40, 45, 55));
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 70)));
            row.setMaximumSize(new Dimension(550, 70));

            JLabel numLabel = new JLabel("P" + (i + 1));
            numLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            numLabel.setForeground(new Color(52, 152, 219));

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
            avatarCombo.setBackground(Color.WHITE);

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

        // --- FOOTER DENGAN TOMBOL BACK DAN START ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // FlowLayout untuk jejer samping
        footer.setBackground(new Color(30, 35, 45));
        footer.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Tombol BACK
        JButton backBtn = createStyledButton("BACK", Color.GRAY, Color.WHITE);
        backBtn.setPreferredSize(new Dimension(120, 50));
        backBtn.addActionListener(e -> {
            dialog.dispose();
            showPlayerCountDialog(); // Kembali ke dialog sebelumnya
        });

        // Tombol START
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

    // --- HELPER METHODS ---

    private void styleTextField(JTextField tf) {
        tf.setBackground(new Color(60, 65, 75));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
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
package view;

import controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class IntroScreen extends JFrame {
    private GameController gameController;
    private float titleAlpha = 0.0f;
    private Timer fadeInTimer;
    private JButton playButton;

    public IntroScreen(GameController gameController) {
        this.gameController = gameController;
        initComponents();
        startFadeInAnimation();
    }

    private void initComponents() {
        setTitle("Tunnel Escape - Welcome");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with animated gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Multi-color gradient background
                Color color1 = new Color(15, 32, 39);
                Color color2 = new Color(32, 58, 67);
                Color color3 = new Color(44, 83, 100);

                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color3);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add some decorative circles
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                g2d.setColor(new Color(255, 255, 255));
                g2d.fillOval(-50, -50, 200, 200);
                g2d.fillOval(getWidth() - 150, getHeight() - 150, 200, 200);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title with shadow effect
        JLabel titleLabel = new JLabel("TUNNEL ESCAPE") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Shadow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f * titleAlpha));
                g2d.setColor(Color.BLACK);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x + 3, y + 3);

                // Main text
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, titleAlpha));
                g2d.setColor(new Color(255, 215, 0)); // Gold color
                g2d.drawString(getText(), x, y);
            }
        };
        titleLabel.setFont(new Font("Arial", Font.BOLD, 54));
        titleLabel.setPreferredSize(new Dimension(700, 80));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        // Subtitle with glow effect
        JLabel subtitleLabel = new JLabel("A Journey Through the Dark");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 22));
        subtitleLabel.setForeground(new Color(200, 220, 255));
        gbc.gridy = 1;
        mainPanel.add(subtitleLabel, gbc);

        // Description panel with rounded border
        JPanel descPanel = new RoundedPanel(20, new Color(255, 255, 255, 30));
        descPanel.setLayout(new BorderLayout());
        descPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JTextArea descArea = new JTextArea(
                ">> You are trapped in a mysterious tunnel!\n\n" +
                        ">> Roll the dice to get food and energy\n" +
                        ">> Collect energy to move forward\n" +
                        ">> Reach Node 64 to escape and WIN!\n\n" +
                        ">> Beware of red dice - it pushes you back!"
        );
        descArea.setFont(new Font("Arial", Font.PLAIN, 15));
        descArea.setForeground(new Color(220, 230, 240));
        descArea.setBackground(new Color(0, 0, 0, 0));
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descPanel.add(descArea);

        gbc.gridy = 2;
        gbc.insets = new Insets(30, 10, 30, 10);
        mainPanel.add(descPanel, gbc);

        // Modern play button with hover animation
        playButton = new RoundedButton("START ADVENTURE", 15);
        playButton.setFont(new Font("Arial", Font.BOLD, 26));
        playButton.setForeground(Color.WHITE);
        playButton.setBackground(new Color(46, 204, 113));
        playButton.setPreferredSize(new Dimension(320, 65));
        playButton.setFocusPainted(false);
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playButton.setBackground(new Color(39, 174, 96));
                animateButtonScale(playButton, 1.05f);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playButton.setBackground(new Color(46, 204, 113));
                animateButtonScale(playButton, 1.0f);
            }
        });

        playButton.addActionListener(e -> showPlayerSetup());

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(playButton, gbc);

        add(mainPanel);
    }

    private void startFadeInAnimation() {
        fadeInTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                titleAlpha += 0.05f;
                if (titleAlpha >= 1.0f) {
                    titleAlpha = 1.0f;
                    fadeInTimer.stop();
                }
                repaint();
            }
        });
        fadeInTimer.start();
    }

    private void animateButtonScale(JButton button, float scale) {
        Dimension originalSize = new Dimension(320, 65);
        int newWidth = (int)(originalSize.width * scale);
        int newHeight = (int)(originalSize.height * scale);
        button.setPreferredSize(new Dimension(newWidth, newHeight));
        button.revalidate();
    }

    private void showPlayerSetup() {
        // Modern dialog with custom styling
        JDialog dialog = new JDialog(this, "Player Setup", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(44, 62, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel label = new JLabel("How many players?");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, 1, 8, 1));
        spinner.setFont(new Font("Arial", Font.BOLD, 28));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        spinner.setMaximumSize(new Dimension(150, 50));
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new RoundedButton("START GAME", 10);
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(new Color(52, 152, 219));
        startButton.setMaximumSize(new Dimension(200, 45));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setFocusPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        startButton.addActionListener(e -> {
            int numPlayers = (Integer) spinner.getValue();
            dialog.dispose();
            startGame(numPlayers);
        });

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(spinner);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(startButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void startGame(int numPlayers) {
        gameController.initializePlayers(numPlayers);
        gameController.startGame();

        GameBoard gameBoard = new GameBoard(gameController);
        gameBoard.setVisible(true);

        dispose();
    }
}

// Custom rounded panel class
class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;

    public RoundedPanel(int radius, Color bgColor) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
    }
}

// Custom rounded button class
class RoundedButton extends JButton {
    private int cornerRadius;

    public RoundedButton(String text, int radius) {
        super(text);
        this.cornerRadius = radius;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2d.setColor(getBackground().darker());
        } else {
            g2d.setColor(getBackground());
        }

        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        super.paintComponent(g);
    }
}
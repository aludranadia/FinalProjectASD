package view;

import controller.GameController;
import model.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GameBoard extends JFrame {
    private GameController gameController;
    private JPanel boardPanel;
    private JLabel currentPlayerLabel;
    private JLabel statusLabel;
    private JLabel diceResultLabel;
    private JButton rollDiceButton;
    private JTextArea gameLogArea;
    private JPanel playersInfoPanel;

    private Map<Player, PlayerVisual> playerVisuals;
    private static final int CELL_SIZE = 75;
    private static final int PLAYER_SIZE = 20;

    // Animation variables
    private Timer animationTimer;
    private List<Integer> animationPath;
    private int animationIndex;
    private Player animatingPlayer;

    // Dice animation
    private int diceAnimationValue = 1;
    private float diceRotation = 0;

    public GameBoard(GameController gameController) {
        this.gameController = gameController;
        this.playerVisuals = new HashMap<>();
        initComponents();
        initializePlayerVisuals();
    }

    private void initComponents() {
        setTitle("Tunnel Escape Game");
        setSize(1100, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(20, 30, 48),
                        0, getHeight(), new Color(36, 59, 85)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel
        JPanel topPanel = createTopPanel();

        // Center container (board + right panel)
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        // Game board
        boardPanel = createBoardPanel();
        centerContainer.add(boardPanel, BorderLayout.CENTER);

        // Right panel (player info)
        playersInfoPanel = createPlayersInfoPanel();
        centerContainer.add(playersInfoPanel, BorderLayout.EAST);

        // Bottom panel
        JPanel bottomPanel = createBottomPanel();

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Title with glow effect
        JLabel titleLabel = new JLabel("TUNNEL ESCAPE GAME") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(new Color(255, 215, 0));
                for (int i = 1; i <= 3; i++) {
                    g2d.setFont(getFont());
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(getText(), x - i, y - i);
                    g2d.drawString(getText(), x + i, y + i);
                }

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                super.paintComponent(g);
            }
        };
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        currentPlayerLabel = new JLabel("Current Turn: Player 1");
        currentPlayerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        currentPlayerLabel.setForeground(new Color(100, 200, 255));
        currentPlayerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Click 'Roll Dice' to start your adventure!");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        statusLabel.setForeground(new Color(255, 180, 180));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(currentPlayerLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(statusLabel);

        return panel;
    }

    private JPanel createBoardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawEnhancedBoard(g);
                drawPlayers(g);
            }
        };
        panel.setPreferredSize(new Dimension(8 * CELL_SIZE + 40, 8 * CELL_SIZE + 40));
        panel.setBackground(new Color(15, 25, 35));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 200), 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    private void drawEnhancedBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int nodeNumber = 1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int x = col * CELL_SIZE + 20;
                int y = row * CELL_SIZE + 20;

                // Cell shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRoundRect(x + 3, y + 3, CELL_SIZE - 6, CELL_SIZE - 6, 15, 15);

                // Cell gradient background
                Color startColor, endColor;
                if (nodeNumber == 1) {
                    startColor = new Color(46, 204, 113);
                    endColor = new Color(39, 174, 96);
                } else if (nodeNumber == 64) {
                    startColor = new Color(231, 76, 60);
                    endColor = new Color(192, 57, 43);
                } else {
                    startColor = new Color(70, 90, 110);
                    endColor = new Color(52, 73, 94);
                }

                GradientPaint gp = new GradientPaint(x, y, startColor, x, y + CELL_SIZE, endColor);
                g2d.setPaint(gp);
                g2d.fillRoundRect(x, y, CELL_SIZE - 6, CELL_SIZE - 6, 15, 15);

                // Glossy effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(x, y, CELL_SIZE - 6, (CELL_SIZE - 6) / 2, 15, 15);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                // Border with glow for special nodes
                if (nodeNumber == 1 || nodeNumber == 64) {
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(x - 2, y - 2, CELL_SIZE - 2, CELL_SIZE - 2, 15, 15);
                }

                // Node number with shadow
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String numStr = String.valueOf(nodeNumber);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (CELL_SIZE - 6 - fm.stringWidth(numStr)) / 2;
                int textY = y + ((CELL_SIZE - 6 - fm.getHeight()) / 2) + fm.getAscent();

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(numStr, textX + 1, textY + 1);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.drawString(numStr, textX, textY);

                // Special node labels
                if (nodeNumber == 1) {
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2d.drawString("START", x + 15, y + CELL_SIZE - 15);
                } else if (nodeNumber == 64) {
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2d.drawString("FINISH", x + 12, y + CELL_SIZE - 15);
                }

                nodeNumber++;
            }
        }
    }

    private void drawPlayers(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Group players by position
        Map<Integer, List<Player>> playersByPosition = new HashMap<>();
        for (Player player : gameController.getPlayerQueue()) {
            playersByPosition.computeIfAbsent(player.getCurrentPosition(), k -> new ArrayList<>()).add(player);
        }

        // Draw players
        for (Map.Entry<Integer, List<Player>> entry : playersByPosition.entrySet()) {
            int position = entry.getKey();
            List<Player> players = entry.getValue();

            int row = (position - 1) / 8;
            int col = (position - 1) % 8;
            int baseX = col * CELL_SIZE + 20 + (CELL_SIZE - 6) / 2;
            int baseY = row * CELL_SIZE + 20 + (CELL_SIZE - 6) / 2 + 10;

            int numPlayers = players.size();
            int spacing = PLAYER_SIZE * 2 + 8;
            int startX = baseX - ((numPlayers - 1) * spacing) / 2;

            for (int i = 0; i < numPlayers; i++) {
                Player player = players.get(i);
                PlayerVisual visual = playerVisuals.get(player);
                if (visual != null) {
                    int x = startX + (i * spacing);

                    // Shadow
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(x - PLAYER_SIZE + 2, baseY - PLAYER_SIZE + 2, PLAYER_SIZE * 2, PLAYER_SIZE * 2);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                    // Player circle with gradient
                    RadialGradientPaint rgp = new RadialGradientPaint(
                            new Point2D.Float(x, baseY - 5),
                            PLAYER_SIZE,
                            new float[]{0.0f, 1.0f},
                            new Color[]{visual.color.brighter(), visual.color}
                    );
                    g2d.setPaint(rgp);
                    g2d.fillOval(x - PLAYER_SIZE, baseY - PLAYER_SIZE, PLAYER_SIZE * 2, PLAYER_SIZE * 2);

                    // Glossy highlight
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(x - PLAYER_SIZE / 2, baseY - PLAYER_SIZE + 3, PLAYER_SIZE, PLAYER_SIZE / 2);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                    // Border
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawOval(x - PLAYER_SIZE, baseY - PLAYER_SIZE, PLAYER_SIZE * 2, PLAYER_SIZE * 2);

                    // Current player indicator
                    if (player == gameController.getCurrentPlayer()) {
                        g2d.setColor(new Color(255, 215, 0));
                        g2d.setStroke(new BasicStroke(2));
                        for (int ring = 1; ring <= 2; ring++) {
                            g2d.drawOval(
                                    x - PLAYER_SIZE - ring * 3,
                                    baseY - PLAYER_SIZE - ring * 3,
                                    (PLAYER_SIZE * 2) + ring * 6,
                                    (PLAYER_SIZE * 2) + ring * 6
                            );
                        }
                    }
                }
            }
        }
    }

    private JPanel createPlayersInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 40, 60, 200));
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 200), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("PLAYERS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        return panel;
    }

    private void updatePlayersInfo() {
        playersInfoPanel.removeAll();

        JLabel titleLabel = new JLabel("PLAYERS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playersInfoPanel.add(titleLabel);
        playersInfoPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        for (Player player : gameController.getPlayerQueue()) {
            JPanel playerCard = createPlayerCard(player);
            playersInfoPanel.add(playerCard);
            playersInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        playersInfoPanel.revalidate();
        playersInfoPanel.repaint();
    }

    private JPanel createPlayerCard(Player player) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (player == gameController.getCurrentPlayer()) {
                    g2d.setColor(new Color(52, 152, 219, 100));
                } else {
                    g2d.setColor(new Color(50, 60, 80, 150));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(new Color(100, 150, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(190, 80));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel(player.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);

        JLabel posLabel = new JLabel("Position: Node " + player.getCurrentPosition());
        posLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        posLabel.setForeground(new Color(200, 220, 255));

        JLabel stepsLabel = new JLabel("Steps: " + player.getTotalSteps());
        stepsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stepsLabel.setForeground(new Color(200, 220, 255));

        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(posLabel);
        card.add(stepsLabel);

        return card;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Controls panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.setOpaque(false);
        controlsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        rollDiceButton = new RoundedButton("ROLL DICE", 15);
        rollDiceButton.setFont(new Font("Arial", Font.BOLD, 20));
        rollDiceButton.setForeground(Color.WHITE);
        rollDiceButton.setBackground(new Color(46, 204, 113));
        rollDiceButton.setPreferredSize(new Dimension(180, 50));
        rollDiceButton.setFocusPainted(false);
        rollDiceButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        rollDiceButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                rollDiceButton.setBackground(new Color(39, 174, 96));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rollDiceButton.setBackground(new Color(46, 204, 113));
            }
        });

        rollDiceButton.addActionListener(e -> handleRollDice());

        diceResultLabel = new JLabel("Dice: Ready");
        diceResultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        diceResultLabel.setForeground(Color.WHITE);

        controlsPanel.add(rollDiceButton);
        controlsPanel.add(diceResultLabel);

        // Game log
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setOpaque(false);

        JLabel logLabel = new JLabel("GAME LOG:");
        logLabel.setFont(new Font("Arial", Font.BOLD, 14));
        logLabel.setForeground(Color.WHITE);

        gameLogArea = new JTextArea(5, 50);
        gameLogArea.setEditable(false);
        gameLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        gameLogArea.setBackground(new Color(20, 30, 40));
        gameLogArea.setForeground(new Color(0, 255, 150));
        gameLogArea.setCaretColor(Color.WHITE);
        gameLogArea.setText("=== GAME LOG ===\n[OK] Game started! All players at Node 1.\n");

        JScrollPane scrollPane = new JScrollPane(gameLogArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 2));

        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(controlsPanel, BorderLayout.NORTH);
        panel.add(logPanel, BorderLayout.CENTER);

        return panel;
    }

    private void initializePlayerVisuals() {
        Color[] playerColors = {
                new Color(231, 76, 60),     // Red
                new Color(52, 152, 219),    // Blue
                new Color(46, 204, 113),    // Green
                new Color(241, 196, 15),    // Yellow
                new Color(155, 89, 182),    // Purple
                new Color(230, 126, 34),    // Orange
                new Color(236, 112, 171),   // Pink
                new Color(26, 188, 156)     // Cyan
        };

        int colorIndex = 0;
        for (Player player : gameController.getPlayerQueue()) {
            PlayerVisual visual = new PlayerVisual(playerColors[colorIndex % playerColors.length]);
            playerVisuals.put(player, visual);
            colorIndex++;
        }

        updatePlayersInfo();
        boardPanel.repaint();
    }

    private void handleRollDice() {
        rollDiceButton.setEnabled(false);
        animateDiceRoll(() -> {
            GameController.TurnResult result = gameController.executeTurn();

            if (result != null) {
                updateGameState(result);
                animatePlayerMovement(result, () -> {
                    updatePlayersInfo();
                    if (gameController.isGameEnded()) {
                        handleGameEnd(result.getPlayer());
                    } else {
                        rollDiceButton.setEnabled(true);
                        updateCurrentPlayerLabel();
                    }
                });
            }
        });
    }

    private void animateDiceRoll(Runnable onComplete) {
        final int[] count = {0};
        Timer timer = new Timer(80, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                diceAnimationValue = (int)(Math.random() * 6) + 1;
                diceRotation += 45;
                diceResultLabel.setText("Rolling... " + diceAnimationValue);
                count[0]++;

                if (count[0] >= 15) {
                    ((Timer)e.getSource()).stop();
                    onComplete.run();
                }
            }
        });
        timer.start();
    }

    private void animatePlayerMovement(GameController.TurnResult result, Runnable onComplete) {
        animatingPlayer = result.getPlayer();
        int oldPos = result.getOldPosition();
        int newPos = result.getNewPosition();

        animationPath = new ArrayList<>();
        if (newPos > oldPos) {
            for (int i = oldPos + 1; i <= newPos; i++) {
                animationPath.add(i);
            }
        } else {
            for (int i = oldPos - 1; i >= newPos; i--) {
                animationPath.add(i);
            }
        }

        animationIndex = 0;

        animationTimer = new Timer(250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (animationIndex < animationPath.size()) {
                    boardPanel.repaint();
                    animationIndex++;
                } else {
                    ((Timer)e.getSource()).stop();
                    animatingPlayer = null;
                    boardPanel.repaint();
                    onComplete.run();
                }
            }
        });
        animationTimer.start();
    }

    private void updateGameState(GameController.TurnResult result) {
        String colorIndicator = result.getDiceResult().isGreen() ? "[GREEN]" : "[RED]";
        diceResultLabel.setText(String.format("Dice: %s %d",
                colorIndicator, result.getDiceResult().getNumber()));

        if (result.getDiceResult().isGreen()) {
            statusLabel.setText(String.format("[OK] %s got %s! +%d steps",
                    result.getPlayer().getName(),
                    result.getFood().getName(),
                    result.getStepsMoved()));
        } else {
            statusLabel.setText(String.format("[WARNING] %s got RED dice! -%d steps",
                    result.getPlayer().getName(),
                    Math.abs(result.getStepsMoved())));
        }

        String logEntry = String.format("\n%s %s: %s -> Node %d -> %d",
                result.getDiceResult().isGreen() ? "[OK]" : "[WARN]",
                result.getPlayer().getName(),
                result.getDiceResult().toString(),
                result.getOldPosition(),
                result.getNewPosition());

        gameLogArea.append(logEntry);
        gameLogArea.setCaretPosition(gameLogArea.getDocument().getLength());
    }

    private void updateCurrentPlayerLabel() {
        currentPlayerLabel.setText("Current Turn: " + gameController.getCurrentPlayer().getName());
    }

    private void handleGameEnd(Player winner) {
        // Victory animation
        Timer victoryTimer = new Timer(100, null);
        final int[] flashCount = {0};

        victoryTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (flashCount[0] % 2 == 0) {
                    statusLabel.setForeground(new Color(255, 215, 0));
                } else {
                    statusLabel.setForeground(new Color(255, 100, 100));
                }
                flashCount[0]++;

                if (flashCount[0] >= 6) {
                    victoryTimer.stop();
                    showVictoryDialog(winner);
                }
            }
        });

        statusLabel.setText("*** GAME OVER! " + winner.getName() + " WINS! ***");
        rollDiceButton.setEnabled(false);
        victoryTimer.start();
    }

    private void showVictoryDialog(Player winner) {
        JDialog dialog = new JDialog(this, "Victory!", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(46, 204, 113),
                        0, getHeight(), new Color(39, 174, 96)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel trophyLabel = new JLabel("*** WINNER ***");
        trophyLabel.setFont(new Font("Arial", Font.BOLD, 36));
        trophyLabel.setForeground(new Color(255, 215, 0));
        trophyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel winnerLabel = new JLabel(winner.getName());
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 32));
        winnerLabel.setForeground(Color.WHITE);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stepsLabel = new JLabel("Total Steps: " + winner.getTotalSteps());
        stepsLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        stepsLabel.setForeground(new Color(255, 255, 255, 200));
        stepsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeButton = new RoundedButton("Close", 10);
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(52, 73, 94));
        closeButton.setMaximumSize(new Dimension(150, 40));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> dialog.dispose());

        panel.add(trophyLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(winnerLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(stepsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(closeButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private static class PlayerVisual {
        Color color;
        BufferedImage image;

        PlayerVisual(Color color) {
            this.color = color;
            this.image = null;
        }
    }
}
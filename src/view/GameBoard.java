package view;

import controller.GameController;
import model.Player;
import model.Node;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;

public class GameBoard extends JFrame {
    private GameController gameController;
    private JPanel boardPanel;
    private JPanel rightPanel;
    private JButton rollDiceButton;
    private JButton newGameButton;
    private JTextArea gameLogArea;

    private Map<String, BufferedImage> loadedImages;
    private BufferedImage backgroundImage;
    private BufferedImage shoeImage;

    private SoundManager soundManager;

    // --- KONFIGURASI POSISI GRID (FINAL STATIS) ---
    private static final int GRID_START_X = 155;
    private static final int GRID_START_Y = 137;
    private static final int CELL_STEP_X = 73;
    private static final int CELL_STEP_Y = 75;

    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;

    private Timer animationTimer;
    private Player animatingPlayer;
    private int visualCurrentNode;
    private List<Integer> animationPath;

    private static final int SPEED_NORMAL = 300; // Milidetik per langkah biasa
    private static final int SPEED_FAST = 100;   // Milidetik saat lari di shortcut

    private int currentDiceNumber = 1;
    private String currentDiceColor = "WHITE";
    private boolean isRolling = false;

    // FONT KHUSUS AGAR EMOJI KEDETECT DI WINDOWS
    private static final Font EMOJI_FONT = new Font("Segoe UI Emoji", Font.BOLD, 14);

    public GameBoard(GameController gameController) {
        this.gameController = gameController;
        this.loadedImages = new HashMap<>();
        this.soundManager = new SoundManager();
        this.soundManager.playLoop("game_bgm");

        loadResources();
        initComponents();
    }

    private void loadResources() {
        try {
            File bgFile = new File("resources/images/bg.png");
            if (bgFile.exists()) backgroundImage = ImageIO.read(bgFile);

            File shoeFile = new File("resources/images/shoe.png");
            if (shoeFile.exists()) shoeImage = ImageIO.read(shoeFile);

            for (Player p : gameController.getPlayerQueue()) {
                if (!loadedImages.containsKey(p.getImagePath())) {
                    File pFile = new File(p.getImagePath());
                    if (pFile.exists()) loadedImages.put(p.getImagePath(), ImageIO.read(pFile));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void initComponents() {
        setTitle("Tunnel Escape - Gameplay");
        setSize(1150, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); // STATIS

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(15, 10, 5));

        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGameGraphics((Graphics2D) g);
            }
        };
        // Ukuran Board Fix
        boardPanel.setPreferredSize(new Dimension(850, 800));
        boardPanel.setBackground(new Color(15, 10, 5));

        rightPanel = createRightPanel();

        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, getHeight()));
        panel.setBackground(new Color(25, 20, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("PLAYER STATUS");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(255, 200, 100));
        header.setAlignmentX(CENTER_ALIGNMENT);

        JPanel playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setOpaque(false);
        updatePlayerListPanel(playerListPanel);

        JPanel dicePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDiceVisual((Graphics2D) g, getWidth()/2 - 40, 10);
            }
        };
        dicePanel.setPreferredSize(new Dimension(250, 120));
        dicePanel.setMaximumSize(new Dimension(250, 120));
        dicePanel.setOpaque(false);

        rollDiceButton = new JButton("ROLL DICE");
        rollDiceButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        rollDiceButton.setBackground(new Color(230, 126, 34));
        rollDiceButton.setForeground(new Color(30, 30, 30));
        rollDiceButton.setFocusPainted(false);
        rollDiceButton.setAlignmentX(CENTER_ALIGNMENT);
        rollDiceButton.setMaximumSize(new Dimension(250, 50));
        rollDiceButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rollDiceButton.addActionListener(e -> handleRoll());

        newGameButton = new JButton("NEW GAME");
        newGameButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        newGameButton.setBackground(new Color(52, 152, 219));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.setAlignmentX(CENTER_ALIGNMENT);
        newGameButton.setMaximumSize(new Dimension(250, 40));
        newGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newGameButton.addActionListener(e -> handleNewGame());

        gameLogArea = new JTextArea();
        gameLogArea.setEditable(false);
        gameLogArea.setBackground(new Color(40, 30, 20));
        gameLogArea.setForeground(new Color(200, 255, 200));
        // Ganti font log juga biar emoji kebaca
        gameLogArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));

        JScrollPane scrollLog = new JScrollPane(gameLogArea);
        scrollLog.setPreferredSize(new Dimension(250, 150));
        scrollLog.setBorder(BorderFactory.createLineBorder(new Color(100, 80, 60)));

        panel.add(header);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(playerListPanel);
        panel.add(Box.createVerticalGlue());
        panel.add(dicePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(rollDiceButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(newGameButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scrollLog);

        return panel;
    }

    private void handleNewGame() {
        soundManager.stop("game_bgm");
        gameController.reset();
        this.dispose();
        new IntroScreen(gameController).setVisible(true);
    }

    private void updatePlayerListPanel(JPanel panel) {
        panel.removeAll();
        for (Player p : gameController.getPlayerQueue()) {
            JPanel card = new JPanel(new BorderLayout());
            card.setMaximumSize(new Dimension(280, 50));
            card.setBackground(new Color(50, 40, 30));
            card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 60, 40)));

            JLabel nameLbl = new JLabel(" " + p.getName());
            nameLbl.setForeground(Color.WHITE);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLbl.setBorder(new EmptyBorder(0, 10, 0, 0));

            // Visual Status Koin
            JLabel statusLbl = new JLabel("<html><div style='text-align:right'>Pos: " + p.getCurrentPosition() +
                    "<br><font color='#FFD700'>Coins: " + p.getCoins() + "</font></div></html>");
            statusLbl.setForeground(Color.ORANGE);
            statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statusLbl.setBorder(new EmptyBorder(0, 0, 0, 10));

            if (p == gameController.getCurrentPlayer()) {
                card.setBackground(new Color(80, 60, 40));
                nameLbl.setForeground(Color.YELLOW);
                nameLbl.setText(" > " + p.getName());
            }

            card.add(nameLbl, BorderLayout.WEST);
            card.add(statusLbl, BorderLayout.EAST);
            panel.add(card);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        panel.revalidate();
        panel.repaint();
    }

    private void drawGameGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, 850, 800, null);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();

        // GRID & COIN
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int nodeNum;
                if (row % 2 == 0) nodeNum = (row * GRID_COLS) + col + 1;
                else nodeNum = (row * GRID_COLS) + (GRID_COLS - 1 - col) + 1;

                Point centerPt = getNodeCoordinates(nodeNum);
                if (centerPt == null) continue;
                int centerX = centerPt.x;
                int centerY = centerPt.y;

                // A. Angka Node (Tengah, Ghostly White)
                String numStr = String.valueOf(nodeNum);
                int textW = fm.stringWidth(numStr);
                int textH = fm.getAscent() - fm.getDescent();

                g2.setColor(new Color(255, 255, 255, 60)); // Transparan
                g2.drawString(numStr, centerX - (textW / 2), centerY + (textH / 2));

                // B. Nilai Koin (Pojok Kanan Atas)
                Node node = gameController.getGraph().getNode(nodeNum);
                if (node != null && node.getCoinValue() != 0) {
                    int val = node.getCoinValue();
                    String coinText = (val > 0 ? "+" : "") + val;

                    Font originalFont = g2.getFont();
                    g2.setFont(new Font("Arial", Font.BOLD, 12));

                    int coinX = centerX + 12;
                    int coinY = centerY - 12;

                    g2.setColor(Color.BLACK); // Shadow
                    g2.drawString(coinText, coinX + 1, coinY + 1);
                    g2.drawString(coinText, coinX - 1, coinY - 1);

                    if (val > 0) g2.setColor(new Color(255, 215, 0)); // Emas
                    else g2.setColor(new Color(255, 80, 80)); // Merah
                    g2.drawString(coinText, coinX, coinY);

                    g2.setFont(originalFont);
                }
            }
        }

        // DRAW SHORTCUTS
        Map<Integer, Integer> shortcuts = gameController.getGraph().getShortcuts();
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(255, 215, 0, 180));
        for (Map.Entry<Integer, Integer> entry : shortcuts.entrySet()) {
            Point startPt = getNodeCoordinates(entry.getKey());
            Point endPt = getNodeCoordinates(entry.getValue());
            if (startPt != null && endPt != null) g2.drawLine(startPt.x, startPt.y, endPt.x, endPt.y);
        }
        if (shoeImage != null) {
            for (Map.Entry<Integer, Integer> entry : shortcuts.entrySet()) {
                drawShoeAtNode(g2, entry.getKey()); drawShoeAtNode(g2, entry.getValue());
            }
        }

        // DRAW PLAYERS
        Map<Integer, java.util.List<Player>> mapPos = new HashMap<>();
        for (Player p : gameController.getPlayerQueue()) {
            int drawPos = p.getCurrentPosition();
            if (p == animatingPlayer && animationPath != null) drawPos = visualCurrentNode;
            mapPos.computeIfAbsent(drawPos, k -> new ArrayList<>()).add(p);
        }
        for (Map.Entry<Integer, java.util.List<Player>> entry : mapPos.entrySet()) {
            int pos = entry.getKey();
            Point pt = getNodeCoordinates(pos);
            if (pt == null) continue;

            java.util.List<Player> playersHere = entry.getValue();
            int count = playersHere.size();
            for (int i = 0; i < count; i++) {
                Player p = playersHere.get(i);
                int offsetX = (count > 1) ? (i * 15) - ((count - 1) * 7) : 0;
                int px = pt.x - 20 + offsetX;
                int py = pt.y - 35; // Player di atas titik tengah

                BufferedImage img = loadedImages.get(p.getImagePath());
                g2.setColor(new Color(0,0,0,150)); g2.fillOval(px+5, py+35, 30, 8);
                if (img != null) g2.drawImage(img, px, py, 40, 40, null);
                else { g2.setColor(Color.RED); g2.fillOval(px, py, 40, 40); }

                if (p == gameController.getCurrentPlayer() || p == animatingPlayer) {
                    g2.setColor(Color.YELLOW);
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    int nameW = g2.getFontMetrics().stringWidth(p.getName());
                    g2.drawString(p.getName(), pt.x - (nameW/2) + offsetX, py - 5);
                }
            }
        }
    }

    // Helper: Koordinat STATIS (tanpa scaling)
    private Point getNodeCoordinates(int nodeNum) {
        if (nodeNum < 1 || nodeNum > 64) return null;
        int row = (nodeNum - 1) / GRID_COLS;
        int col;
        if (row % 2 == 0) col = (nodeNum - 1) % GRID_COLS;
        else col = GRID_COLS - 1 - ((nodeNum - 1) % GRID_COLS);

        int x = GRID_START_X + (col * CELL_STEP_X);
        int y = GRID_START_Y + (row * CELL_STEP_Y);
        return new Point(x, y);
    }

    private void drawShoeAtNode(Graphics2D g2, int nodeNum) {
        Point pt = getNodeCoordinates(nodeNum);
        if (pt != null) g2.drawImage(shoeImage, pt.x - 25, pt.y - 25, 20, 20, null);
    }

    private void drawDiceVisual(Graphics2D g2, int x, int y) {
        int size = 80;
        if (isRolling) g2.setColor(Color.GRAY);
        else if (currentDiceColor.equals("GREEN")) g2.setColor(new Color(46, 204, 113));
        else if (currentDiceColor.equals("RED")) g2.setColor(new Color(231, 76, 60));
        else g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect(x, y, size, size, 20, 20);
        g2.setColor(new Color(0,0,0,50));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, size, size, 20, 20);
        g2.setColor(Color.WHITE);
        int center = size / 2; int q1 = size / 4; int q3 = q1 * 3;
        int n = currentDiceNumber;
        if(n%2 != 0) g2.fillOval(x+center-6, y+center-6, 12, 12);
        if(n > 1) { g2.fillOval(x+q1-6, y+q1-6, 12, 12); g2.fillOval(x+q3-6, y+q3-6, 12, 12); }
        if(n > 3) { g2.fillOval(x+q3-6, y+q1-6, 12, 12); g2.fillOval(x+q1-6, y+q3-6, 12, 12); }
        if(n == 6) { g2.fillOval(x+q1-6, y+center-6, 12, 12); g2.fillOval(x+q3-6, y+center-6, 12, 12); }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String txt = isRolling ? "Rolling..." : (currentDiceColor.equals("RED") ? "MUNDUR" : "MAJU");
        int txtW = g2.getFontMetrics().stringWidth(txt);
        g2.drawString(txt, x + (size/2) - (txtW/2), y + size + 20);
    }

    private void handleRoll() {
        if(isRolling || animationTimer != null && animationTimer.isRunning()) return;
        isRolling = true; rollDiceButton.setEnabled(false); newGameButton.setEnabled(false);

        soundManager.play("roll");

        Timer rollTimer = new Timer(100, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                currentDiceNumber = new Random().nextInt(6) + 1;
                rightPanel.repaint();
                count++;
                if (count > 8) {
                    ((Timer)e.getSource()).stop();
                    executeTurnLogic();
                }
            }
        });
        rollTimer.start();
    }

    private void executeTurnLogic() {
        GameController.TurnResult result = gameController.executeTurn();
        if (result == null) return;
        isRolling = false;
        currentDiceNumber = result.getDiceResult().getNumber();
        currentDiceColor = result.getDiceResult().getColor();
        if (currentDiceColor.equals("RED")) soundManager.play("slide");

        String act = currentDiceColor.equals("GREEN") ? "MAJU" : "MUNDUR";
        String foodName = (result.getFood() != null) ? result.getFood().getName() :("-");

        gameLogArea.append(result.getPlayer().getName() + ": " + act + " " + result.getStepsMoved() + " -> " + foodName + "\n");
        gameLogArea.setCaretPosition(gameLogArea.getDocument().getLength());

        animatingPlayer = result.getPlayer();
        visualCurrentNode = result.getOldPosition();
        animationPath = result.getMovementPath();

        if (animationPath == null || animationPath.isEmpty()) {
            animationPath = new ArrayList<>();
            animationPath.add(result.getNewPosition());
        }

        int firstDelay = SPEED_NORMAL;
        if (!animationPath.isEmpty()) {
            int nextNode = animationPath.get(0);
            if (Math.abs(nextNode - visualCurrentNode) > 1) {
                firstDelay = SPEED_FAST;
            }
        }

        startMovementAnimation(result, firstDelay);
        rightPanel.repaint();
    }

    private void startMovementAnimation(GameController.TurnResult result, int initialDelay) {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        animationTimer = new Timer(initialDelay, null);

        animationTimer.addActionListener(new ActionListener() {
            int index = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index < animationPath.size()) {
                    int targetNode = animationPath.get(index);
                    int distance = Math.abs(targetNode - visualCurrentNode);

                    if (distance > 1) {
                        soundManager.play("dash");
                    } else {
                        soundManager.playStep();
                    }

                    visualCurrentNode = targetNode;
                    boardPanel.repaint();
                    index++;

                    if (index < animationPath.size()) {
                        int nextTarget = animationPath.get(index);
                        int nextDistance = Math.abs(nextTarget - visualCurrentNode);
                        if (nextDistance > 1) {
                            animationTimer.setDelay(SPEED_FAST);
                        } else {
                            animationTimer.setDelay(SPEED_NORMAL);
                        }
                    }
                } else {
                    ((Timer)e.getSource()).stop();
                    endTurn(result);
                }
            }
        });

        animationTimer.setInitialDelay(initialDelay);
        animationTimer.start();
    }

    // --- REVISI DI SINI (POP-UP TEXT & VISUAL) ---
    private void endTurn(GameController.TurnResult result) {
        animatingPlayer = null;
        animationPath = null;
        boardPanel.repaint();
        updatePlayerListPanel((JPanel) rightPanel.getComponent(2));
        newGameButton.setEnabled(true);

        if (gameController.isGameEnded()) {
            soundManager.stop("game_bgm");
            soundManager.play("win");

            showLeaderboardDialog();

            rollDiceButton.setEnabled(false);
        } else {
            rollDiceButton.setEnabled(true);

            int coinEffect = result.getCoinEffect();
            if (coinEffect > 0) {
                soundManager.play("point_plus");
            } else if (coinEffect < 0) {
                soundManager.play("point_minus");
            }

            if (result.isBonusTurn()) {
                soundManager.play("bonus");

                // FIX: Teks bahasa Inggris yang lebih baik
                JOptionPane.showMessageDialog(this,
                        "✨ LUCKY SPOT! ✨\n" +
                                "You found a Lunch Box! 🍱\n" +
                                "Roll the dice again to get your energy reward!",
                        "Double Turn", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // --- REVISI DI SINI (LEADERBOARD PREMIUM & EMOJI FIX) ---
    private void showLeaderboardDialog() {
        JDialog dialog = new JDialog(this, "GAME OVER - RESULTS", true);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 20, 15));
        JLabel header = new JLabel("🏆 CONGRATULATIONS! 🏆", SwingConstants.CENTER);
        header.setFont(EMOJI_FONT.deriveFont(28f)); // Pakai EMOJI_FONT
        header.setForeground(new Color(255, 215, 0)); // Gold
        headerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        headerPanel.add(header);
        dialog.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane Styling
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(50, 45, 40));
        tabbedPane.setForeground(Color.BLACK);

        // --- TAB 1: CURRENT MATCH ---
        JPanel rankingPanel = new JPanel();
        rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));
        rankingPanel.setBackground(new Color(40, 35, 30));
        rankingPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel subTitle = new JLabel("📊 RANKING BASED ON POINTS");
        subTitle.setFont(EMOJI_FONT);
        subTitle.setForeground(new Color(135, 206, 250));
        subTitle.setAlignmentX(CENTER_ALIGNMENT);
        rankingPanel.add(subTitle);
        rankingPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        PriorityQueue<Player> pq = gameController.getScoreLeaderboard();
        int rank = 1;

        PriorityQueue<Player> pqClone = new PriorityQueue<>(pq);

        while (!pqClone.isEmpty()) {
            Player p = pqClone.poll();
            JPanel row = createRankRow(rank, p.getName(), p.getCoins(), true);
            rankingPanel.add(row);
            rankingPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            rank++;
        }
        tabbedPane.addTab("Current Match", rankingPanel);

        // --- TAB 2: HALL OF FAME ---
        JPanel hallOfFamePanel = new JPanel();
        hallOfFamePanel.setLayout(new BoxLayout(hallOfFamePanel, BoxLayout.Y_AXIS));
        hallOfFamePanel.setBackground(new Color(30, 25, 35));
        hallOfFamePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel winTitle = new JLabel("👑 TOP WINNERS (All Time)");
        winTitle.setForeground(Color.ORANGE);
        winTitle.setFont(EMOJI_FONT);
        winTitle.setAlignmentX(CENTER_ALIGNMENT);
        hallOfFamePanel.add(winTitle);
        hallOfFamePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        gameController.getGlobalWinCounts().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    JPanel row = createRankRow(0, entry.getKey(), entry.getValue(), false);
                    hallOfFamePanel.add(row);
                    hallOfFamePanel.add(Box.createRigidArea(new Dimension(0, 5)));
                });

        hallOfFamePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel scoreTitle = new JLabel("🔥 LEGENDARY HIGH SCORES");
        scoreTitle.setForeground(Color.MAGENTA);
        scoreTitle.setFont(EMOJI_FONT);
        scoreTitle.setAlignmentX(CENTER_ALIGNMENT);
        hallOfFamePanel.add(scoreTitle);
        hallOfFamePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        gameController.getGlobalHighScores().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    JPanel row = createRankRow(0, entry.getKey(), entry.getValue(), true);
                    hallOfFamePanel.add(row);
                    hallOfFamePanel.add(Box.createRigidArea(new Dimension(0, 5)));
                });

        tabbedPane.addTab("Hall of Fame", hallOfFamePanel);
        dialog.add(tabbedPane, BorderLayout.CENTER);

        // Footer Button
        JButton closeBtn = new JButton("CLOSE & RESET");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setBackground(new Color(52, 152, 219));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> {
            dialog.dispose();
            handleNewGame();
        });

        JPanel footer = new JPanel();
        footer.setBackground(new Color(25, 20, 15));
        footer.setBorder(new EmptyBorder(10,0,10,0));
        footer.add(closeBtn);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createRankRow(int rank, String name, int value, boolean isScore) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(500, 45));

        // Warna Background Baris (Dark Grey)
        row.setBackground(new Color(60, 55, 50));

        // Border berwarna sesuai Rank
        Color borderColor = Color.DARK_GRAY;
        String iconStr = "⭐ ";
        if (rank == 1) { borderColor = new Color(255, 215, 0); iconStr = "🥇 "; } // Emas
        else if (rank == 2) { borderColor = new Color(192, 192, 192); iconStr = "🥈 "; } // Perak
        else if (rank == 3) { borderColor = new Color(205, 127, 50); iconStr = "🥉 "; } // Perunggu

        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, borderColor),
                new EmptyBorder(5, 10, 5, 10)
        ));

        // FIX: Pakai EMOJI_FONT di sini
        String rankStr = (rank > 0) ? iconStr : "👤 ";
        JLabel nameLbl = new JLabel(rankStr + name);
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setFont(EMOJI_FONT);

        String suffix = isScore ? " Coins" : " Wins";
        JLabel valLbl = new JLabel(value + suffix);
        valLbl.setForeground(new Color(255, 223, 0)); // Gold text
        valLbl.setFont(new Font("Monospaced", Font.BOLD, 14));

        row.add(nameLbl, BorderLayout.WEST);
        row.add(valLbl, BorderLayout.EAST);
        return row;
    }
}
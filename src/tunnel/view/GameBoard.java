package tunnel.view;

import tunnel.controller.GameController;
import tunnel.model.Player;
import tunnel.model.Node;
import main.MainLauncher;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
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
    private JButton backButton;
    private JTextArea gameLogArea;

    private Map<String, BufferedImage> loadedImages;
    private BufferedImage backgroundImage;
    private BufferedImage shoeImage;

    private SoundManager soundManager;

    private static final int GRID_START_X = 155;
    private static final int GRID_START_Y = 137;
    private static final int CELL_STEP_X = 73;
    private static final int CELL_STEP_Y = 75;
    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;
    private static final int SPEED_NORMAL = 300;
    private static final int SPEED_FAST = 100;

    private Timer animationTimer;
    private Player animatingPlayer;
    private int visualCurrentNode;
    private List<Integer> animationPath;

    private int currentDiceNumber = 1;
    private String currentDiceColor = "WHITE";
    private boolean isRolling = false;

    // FONT KHUSUS AGAR EMOJI TERBACA
    private static final Font UI_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font EMOJI_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 12);
    private static final Color GOLD_COLOR = new Color(255, 215, 0);

    public GameBoard(GameController gameController) {
        this.gameController = gameController;
        this.loadedImages = new HashMap<>();
        this.soundManager = new SoundManager();
        this.soundManager.playLoop("game_bgm");

        loadResources();
        initComponents();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                soundManager.stopAllBGM();
            }
        });
    }

    private void loadResources() {
        try {
            File bgFile = new File("resources/tunnel/images/bg.png");
            if (bgFile.exists()) backgroundImage = ImageIO.read(bgFile);

            File shoeFile = new File("resources/tunnel/images/shoe.png");
            if (shoeFile.exists()) shoeImage = ImageIO.read(shoeFile);

            for (Player p : gameController.getPlayerQueue()) {
                if (!loadedImages.containsKey(p.getImagePath())) {
                    File pFile = new File(p.getImagePath());
                    if (pFile.exists()) loadedImages.put(p.getImagePath(), ImageIO.read(pFile));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setTitle("Tunnel Escape - Gameplay");
        setSize(1150, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(15, 10, 5));

        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGameGraphics((Graphics2D) g);
            }
        };
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

        rollDiceButton = createStyledButton("ROLL DICE", new Color(230, 126, 34), Color.BLACK);
        rollDiceButton.addActionListener(e -> handleRoll());

        newGameButton = createStyledButton("NEW GAME", new Color(52, 152, 219), Color.WHITE);
        newGameButton.addActionListener(e -> handleNewGame());

        backButton = createStyledButton("MENU", new Color(192, 57, 43), Color.WHITE);
        backButton.addActionListener(e -> {
            soundManager.stopAllBGM();
            this.dispose();
            new MainLauncher().setVisible(true);
        });

        gameLogArea = new JTextArea();
        gameLogArea.setEditable(false);
        gameLogArea.setBackground(new Color(40, 30, 20));
        gameLogArea.setForeground(new Color(200, 255, 200));

        // PENTING: Gunakan Font Emoji agar 🍬🍜 terbaca
        gameLogArea.setFont(EMOJI_FONT);

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

        JPanel subBtnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        subBtnPanel.setOpaque(false);
        subBtnPanel.setMaximumSize(new Dimension(250, 45));
        newGameButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subBtnPanel.add(newGameButton);
        subBtnPanel.add(backButton);
        panel.add(subBtnPanel);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scrollLog);

        return panel;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if(soundManager != null) soundManager.playClick();
        });

        return btn;
    }

    private void handleNewGame() {
        soundManager.stopAllBGM();
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

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int nodeNum;
                if (row % 2 == 0) nodeNum = (row * GRID_COLS) + col + 1;
                else nodeNum = (row * GRID_COLS) + (GRID_COLS - 1 - col) + 1;

                Point centerPt = getNodeCoordinates(nodeNum);
                if (centerPt == null) continue;
                int centerX = centerPt.x;
                int centerY = centerPt.y;

                String numStr = String.valueOf(nodeNum);
                int textW = fm.stringWidth(numStr);
                int textH = fm.getAscent() - fm.getDescent();
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawString(numStr, centerX - (textW / 2), centerY + (textH / 2));

                Node node = gameController.getGraph().getNode(nodeNum);
                if (node != null && node.getCoinValue() != 0) {
                    int val = node.getCoinValue();
                    String coinText = (val > 0 ? "+" : "") + val;

                    Font originalFont = g2.getFont();
                    g2.setFont(new Font("Arial", Font.BOLD, 12));

                    int coinX = centerX + 12;
                    int coinY = centerY - 12;

                    g2.setColor(Color.BLACK);
                    g2.drawString(coinText, coinX + 1, coinY + 1);

                    if (val > 0) g2.setColor(new Color(255, 215, 0));
                    else g2.setColor(new Color(255, 80, 80));
                    g2.drawString(coinText, coinX, coinY);

                    g2.setFont(originalFont);
                }
            }
        }

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
                drawShoeAtNode(g2, entry.getKey());
                drawShoeAtNode(g2, entry.getValue());
            }
        }

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
                int py = pt.y - 35;

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

        // PERBAIKAN: JANGAN PAKE REPLACE ALL AGAR EMOJI MUNCUL
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

                    if (distance > 1) soundManager.play("dash");
                    else soundManager.playStep();

                    visualCurrentNode = targetNode;
                    boardPanel.repaint();
                    index++;

                    if (index < animationPath.size()) {
                        int nextTarget = animationPath.get(index);
                        int nextDistance = Math.abs(nextTarget - visualCurrentNode);
                        if (nextDistance > 1) animationTimer.setDelay(SPEED_FAST);
                        else animationTimer.setDelay(SPEED_NORMAL);
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
            if (coinEffect > 0) soundManager.play("point_plus");
            else if (coinEffect < 0) soundManager.play("point_minus");

            if (result.isBonusTurn()) {
                soundManager.play("bonus");

                // --- POPUP LUCKY SPOT ---
                JDialog bonusDialog = new JDialog(this, "Lucky Spot!", true);
                bonusDialog.setUndecorated(true);
                bonusDialog.setSize(400, 220);
                bonusDialog.setLocationRelativeTo(this);

                JPanel p = new JPanel(new GridBagLayout());
                p.setBackground(new Color(25, 20, 15));
                p.setBorder(BorderFactory.createLineBorder(GOLD_COLOR, 2));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0; gbc.gridy = 0;
                gbc.insets = new Insets(10, 10, 10, 10);
                gbc.anchor = GridBagConstraints.CENTER;

                JLabel l1 = new JLabel("LUCKY PATH");
                l1.setFont(new Font("Impact", Font.PLAIN, 32));
                l1.setForeground(Color.YELLOW);
                l1.setHorizontalAlignment(SwingConstants.CENTER);
                p.add(l1, gbc);

                gbc.gridy = 1;
                JLabel l2 = new JLabel("<html><center>You found a hidden lunchbox!<br>Roll the dice again to see<br>what food you get!</center></html>");
                l2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                l2.setForeground(Color.WHITE);
                l2.setHorizontalAlignment(SwingConstants.CENTER);
                p.add(l2, gbc);

                gbc.gridy = 2;
                gbc.insets = new Insets(20, 10, 10, 10);

                // TOMBOL ROLL AGAIN
                JButton btnOK = new JButton("ROLL AGAIN");
                btnOK.setPreferredSize(new Dimension(150, 40));
                btnOK.setBackground(new Color(230, 126, 34)); // Orange Terang
                btnOK.setForeground(Color.WHITE);             // Teks Putih
                btnOK.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btnOK.setFocusPainted(false);
                btnOK.setCursor(new Cursor(Cursor.HAND_CURSOR));

                btnOK.addActionListener(e -> {
                    soundManager.playClick();
                    bonusDialog.dispose();
                });

                p.add(btnOK, gbc);

                bonusDialog.add(p);
                bonusDialog.setVisible(true);
            }
        }
    }

    private void showLeaderboardDialog() {
        JDialog dialog = new JDialog(this, "Game Results", true);
        dialog.setUndecorated(true);
        dialog.setSize(550, 700);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 15, 10)); // Darker Brown

        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_COLOR, 2),
                new EmptyBorder(5, 5, 5, 5)
        ));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 20, 15));
        headerPanel.setBorder(new EmptyBorder(25, 0, 20, 0));

        JLabel titleLbl = new JLabel("MISSION COMPLETE", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Impact", Font.BOLD, 36));
        titleLbl.setForeground(GOLD_COLOR);

        JLabel subtitleLbl = new JLabel("The tunnel has been conquered!", SwingConstants.CENTER);
        subtitleLbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLbl.setForeground(new Color(180, 180, 180));

        headerPanel.add(titleLbl, BorderLayout.CENTER);
        headerPanel.add(subtitleLbl, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // TABS
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setUI(new BasicTabbedPaneUI());

        // Tab 1: Current Match
        JPanel currentMatchPanel = createTabContentPanel();
        JLabel rankHeader = new JLabel("FINAL STANDINGS");
        rankHeader.setFont(UI_FONT);
        rankHeader.setForeground(new Color(135, 206, 250));
        rankHeader.setAlignmentX(CENTER_ALIGNMENT);
        currentMatchPanel.add(rankHeader);
        currentMatchPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        PriorityQueue<Player> pq = gameController.getScoreLeaderboard();
        PriorityQueue<Player> pqClone = new PriorityQueue<>(pq);
        int rank = 1;
        while (!pqClone.isEmpty()) {
            Player p = pqClone.poll();
            currentMatchPanel.add(createRankRow(rank, p.getName(), p.getCoins(), true));
            currentMatchPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            rank++;
        }
        tabbedPane.addTab(" Current Match ", createDarkScrollPane(currentMatchPanel));

        // Tab 2: Hall of Fame
        JPanel hofPanel = createTabContentPanel();
        JLabel winTitle = new JLabel("TOP CONQUERORS (Wins)");
        winTitle.setForeground(GOLD_COLOR);
        winTitle.setFont(UI_FONT);
        winTitle.setAlignmentX(CENTER_ALIGNMENT);
        hofPanel.add(winTitle);
        hofPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        gameController.getGlobalWinCounts().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    hofPanel.add(createRankRow(0, entry.getKey(), entry.getValue(), false));
                    hofPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                });

        hofPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel scoreTitle = new JLabel("LEGENDARY HOARDERS (Score)");
        scoreTitle.setForeground(new Color(46, 204, 113));
        scoreTitle.setFont(UI_FONT);
        scoreTitle.setAlignmentX(CENTER_ALIGNMENT);
        hofPanel.add(scoreTitle);
        hofPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        gameController.getGlobalHighScores().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    hofPanel.add(createRankRow(0, entry.getKey(), entry.getValue(), true));
                    hofPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                });
        tabbedPane.addTab(" Hall of Fame ", createDarkScrollPane(hofPanel));

        // WARNAI TAB
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, new Color(60, 50, 40));
            tabbedPane.setForegroundAt(i, Color.WHITE);
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // FOOTER
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footerPanel.setBackground(new Color(20, 15, 10)); // Samakan dengan mainPanel
        footerPanel.setBorder(new EmptyBorder(15, 0, 20, 0));

        // TOMBOL CLOSE (VISIBLE & SOUND)
        JButton closeBtn = new JButton("CLOSE & RESET");
        closeBtn.setPreferredSize(new Dimension(200, 50));
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setBackground(new Color(52, 152, 219)); // Biru Terang
        closeBtn.setForeground(Color.WHITE);             // Teks Putih
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 2));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeBtn.addActionListener(e -> {
            soundManager.playClick();
            dialog.dispose();
            handleNewGame();
        });

        footerPanel.add(closeBtn);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createTabContentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(30, 25, 20)); // Coklat gelap
        p.setBorder(new EmptyBorder(15, 15, 15, 15));
        return p;
    }

    private JPanel createRankRow(int rank, String name, int value, boolean isScore) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(480, 50));
        row.setBackground(new Color(50, 45, 40));

        Color borderColor = Color.GRAY;
        String rankText = "#" + rank;

        if (rank == 1) {
            borderColor = GOLD_COLOR;
            rankText = "1st";
            row.setBackground(new Color(60, 50, 30));
        } else if (rank == 2) {
            borderColor = new Color(192, 192, 192);
            rankText = "2nd";
        } else if (rank == 3) {
            borderColor = new Color(205, 127, 50);
            rankText = "3rd";
        } else if (rank == 0) {
            borderColor = new Color(70, 60, 50);
            rankText = "-";
        }

        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, borderColor),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel nameLbl = new JLabel(rankText + "  " + name);
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setFont(UI_FONT);

        String suffix = isScore ? " Coins" : " Wins";
        JLabel valLbl = new JLabel(value + suffix);

        valLbl.setForeground(GOLD_COLOR);
        valLbl.setFont(new Font("Monospaced", Font.BOLD, 15));

        row.add(nameLbl, BorderLayout.WEST);
        row.add(valLbl, BorderLayout.EAST);

        return row;
    }

    private JScrollPane createDarkScrollPane(JPanel content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(new Color(30, 25, 20));
        return scroll;
    }
}
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
    private static final int CELL_STEP_X = 78;
    private static final int CELL_STEP_Y = 74;

    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 8;

    private Timer animationTimer;
    private Player animatingPlayer;
    private int visualCurrentNode;
    private List<Integer> animationPath;

    private int currentDiceNumber = 1;
    private String currentDiceColor = "WHITE";
    private boolean isRolling = false;

    public GameBoard(GameController gameController) {
        this.gameController = gameController;
        this.loadedImages = new HashMap<>();
        this.soundManager = new SoundManager();
        this.soundManager.playLoop("bgm");

        loadResources();
        initComponents();
    }

    private void loadResources() {
        try {
            File bgFile = new File("resources/images/background.png");
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
        soundManager.stop("bgm");
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
        if (currentDiceColor.equals("RED")) soundManager.play("wind");

        String act = currentDiceColor.equals("GREEN") ? "MAJU" : "MUNDUR";
        String foodName = (result.getFood() != null) ? result.getFood().getName() :("-");

        gameLogArea.append(result.getPlayer().getName() + ": " + act + " " + result.getStepsMoved() + " -> " + foodName + "\n");
        gameLogArea.setCaretPosition(gameLogArea.getDocument().getLength());

        animatingPlayer = result.getPlayer();
        visualCurrentNode = result.getOldPosition();
        animationPath = new ArrayList<>();
        int start = result.getOldPosition();
        int end = result.getNewPosition();
        if (start < end) for (int i = start + 1; i <= end; i++) animationPath.add(i);
        else for (int i = start - 1; i >= end; i--) animationPath.add(i);

        int speedDelay = result.isUsedShortcut() ? 50 : 300;
        if (animationPath.isEmpty()) endTurn(result);
        else startMovementAnimation(result, speedDelay);
        rightPanel.repaint();
    }

    private void startMovementAnimation(GameController.TurnResult result, int delay) {
        animationTimer = new Timer(delay, new ActionListener() {
            int index = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index < animationPath.size()) {
                    visualCurrentNode = animationPath.get(index);
                    soundManager.play("step");
                    boardPanel.repaint();
                    index++;
                } else {
                    ((Timer)e.getSource()).stop();
                    endTurn(result);
                }
            }
        });
        animationTimer.start();
    }

    private void endTurn(GameController.TurnResult result) {
        animatingPlayer = null; animationPath = null;

        if (result.getCoinEffect() != 0) {
            String msg = (result.getCoinEffect() > 0)
                    ? "Found " + result.getCoinEffect() + " coins!"
                    : "Lost " + Math.abs(result.getCoinEffect()) + " coins!";
            gameLogArea.append(">> " + msg + "\n");
        }

        boardPanel.repaint();
        updatePlayerListPanel((JPanel) rightPanel.getComponent(2));
        newGameButton.setEnabled(true);

        if (gameController.isGameEnded()) {
            soundManager.stop("bgm");
            soundManager.play("win");
            showLeaderboardDialog(result.getPlayer());
            rollDiceButton.setEnabled(false);
        } else {
            rollDiceButton.setEnabled(true);
            if (result.isBonusTurn()) {
                JOptionPane.showMessageDialog(this,
                        " LUCKY SPOT! \n" +
                                result.getPlayer().getName() + " mendarat di Node " + result.getNewPosition() +
                                "\n(Kelipatan 5). Roll Dadu Sekali Lagi!",
                        "Double Turn", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void showLeaderboardDialog(Player winner) {
        JDialog dialog = new JDialog(this, "GAME OVER", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 40));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("🏆 VICTORY! 🏆");
        title.setFont(new Font("Impact", Font.PLAIN, 32));
        title.setForeground(new Color(255, 215, 0));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel(winner.getName() + " Reached the Exit!");
        subTitle.setForeground(Color.WHITE);
        subTitle.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(subTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel rankTitle = new JLabel("--- SCORE LEADERBOARD ---");
        rankTitle.setForeground(Color.CYAN);
        rankTitle.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(rankTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        PriorityQueue<Player> pq = gameController.getScoreLeaderboard();
        int rank = 1;
        while (!pq.isEmpty()) {
            Player p = pq.poll();
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(new Color(50, 50, 60));
            row.setMaximumSize(new Dimension(350, 40));
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            JLabel left = new JLabel(" #" + rank + " " + p.getName());
            left.setForeground(Color.WHITE);
            left.setFont(new Font("Segoe UI", Font.BOLD, 14));
            left.setBorder(new EmptyBorder(0,10,0,0));

            JLabel right = new JLabel("Coins: " + p.getCoins() + "  ");
            right.setForeground(new Color(255, 215, 0));

            row.add(left, BorderLayout.WEST);
            row.add(right, BorderLayout.EAST);
            panel.add(row);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            rank++;
        }

        JButton closeBtn = new JButton("CLOSE");
        closeBtn.setAlignmentX(CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());

        panel.add(Box.createVerticalGlue());
        panel.add(closeBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
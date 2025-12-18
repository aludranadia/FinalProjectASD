package maze.view;

import maze.controller.MazeSolver;
import maze.model.Cell;
import maze.model.CellType;
import maze.model.MazeGraph;
import main.MainLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class MazePanel extends JPanel {

    private static final Color[] PATH_COLORS = {
            new Color(255, 50, 50),   // Merah Terang
            new Color(0, 255, 255),   // Cyan
            new Color(255, 255, 0),   // Kuning
            new Color(255, 0, 255),   // Magenta
            new Color(50, 255, 100),  // Hijau Neon
            new Color(255, 165, 0)    // Oranye
    };

    private MazeGraph mazeGraph;
    private final MazeSolver solver;

    private List<Cell> visitedAnimation;
    private List<List<Cell>> allSolutionPaths;

    private int animationIndex = 0;
    private Timer timer;
    private boolean isScanning = false;
    private boolean isPathing = false;
    private boolean isFinished = false; // Flag status selesai

    private SoundManagerMaze soundManager;
    private JPanel controlPanel;

    // Variabel untuk HUD Info
    private String lastAlgorithm = "-";
    private String statusText = "Ready";
    private double totalPathCost = 0;

    public MazePanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);
        this.solver = new MazeSolver();

        this.soundManager = new SoundManagerMaze();
        this.soundManager.playBGM("game");

        generateNewMaze(25, 45);

        controlPanel = createControlPanel();
        this.add(controlPanel, BorderLayout.SOUTH);
    }

    public void stopMusic() {
        if (soundManager != null) {
            soundManager.stopAll();
        }
    }

    private void generateNewMaze(int rows, int cols) {
        if (timer != null && timer.isRunning()) timer.stop();

        visitedAnimation = new ArrayList<>();
        allSolutionPaths = new ArrayList<>();
        isScanning = false;
        isPathing = false;
        isFinished = false;

        // Reset Info
        statusText = "Ready";
        totalPathCost = 0;
        lastAlgorithm = "-";

        mazeGraph = new MazeGraph(rows, cols);
        mazeGraph.generateMaze();

        repaint();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 5, 5));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        algoPanel.setOpaque(false);

        algoPanel.add(createAlgoButton("BFS", new Color(52, 152, 219)));
        algoPanel.add(createAlgoButton("DFS", new Color(155, 89, 182)));
        algoPanel.add(createAlgoButton("Dijkstra", new Color(46, 204, 113)));
        algoPanel.add(createAlgoButton("A* (Smart)", new Color(241, 196, 15)));
        algoPanel.add(createAlgoButton("Prim", new Color(155, 89, 182)));
        algoPanel.add(createAlgoButton("Kruskal", new Color(231, 76, 60)));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnGen = createStyledButton("New Maze", new Color(230, 126, 34));
        btnGen.addActionListener(e -> generateNewMaze(25, 45));

        JButton btnBack = createStyledButton("Back to Menu", new Color(192, 57, 43));
        btnBack.addActionListener(e -> {
            soundManager.playSFX("click");
            stopMusic();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        });

        actionPanel.add(btnGen);
        actionPanel.add(btnBack);

        panel.add(algoPanel);
        panel.add(actionPanel);

        return panel;
    }

    private JButton createAlgoButton(String text, Color bg) {
        JButton btn = createStyledButton(text, bg);
        btn.addActionListener(e -> runSolver(text.split(" ")[0]));
        btn.setPreferredSize(new Dimension(100, 35));
        return btn;
    }

    private void runSolver(String type) {
        soundManager.playSFX("click");

        if (timer != null && timer.isRunning()) return;

        visitedAnimation.clear();
        allSolutionPaths.clear();
        isScanning = true;
        isPathing = false;
        isFinished = false;
        animationIndex = 0;

        // Update Info Awal
        lastAlgorithm = type;
        statusText = "Scanning...";
        totalPathCost = 0;

        MazeSolver.SolverResult result = null;

        switch (type) {
            case "BFS": result = solver.solveBFS(mazeGraph); break;
            case "DFS": result = solver.solveDFS(mazeGraph); break;
            case "Dijkstra": result = solver.solveDijkstra(mazeGraph); break;
            case "A*": result = solver.solveAStar(mazeGraph); break;
            case "Prim": result = solver.solvePrim(mazeGraph); break;
            case "Kruskal": result = solver.solveKruskal(mazeGraph); break;
        }

        if (result != null) {
            final List<Cell> order = result.getVisitedOrder();
            final List<List<Cell>> paths = result.getPaths();
            final double finalCost = result.getTotalCost(); // Ambil Cost

            soundManager.playSFX("scan");

            timer = new Timer(15, e -> {
                if (isScanning) {
                    for (int i = 0; i < 5; i++) {
                        if (animationIndex < order.size()) {
                            visitedAnimation.add(order.get(animationIndex));
                            animationIndex++;
                        } else {
                            isScanning = false;
                            isPathing = true;
                            animationIndex = 0;
                            allSolutionPaths = paths;

                            // Update Status saat pathing dimulai
                            statusText = "Found Path!";
                            totalPathCost = finalCost;
                            break;
                        }
                    }
                } else if (isPathing) {
                    ((Timer)e.getSource()).stop();
                    soundManager.playSFX("success");
                    isFinished = true; // Tandai selesai
                }
                repaint();
            });
            timer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (mazeGraph == null) return;

        int panelW = getWidth();
        int panelH = getHeight() - (controlPanel != null ? controlPanel.getHeight() : 100) - 20;

        int rows = mazeGraph.getRows();
        int cols = mazeGraph.getCols();

        int cellW = panelW / cols;
        int cellH = panelH / rows;
        int cellSize = Math.min(cellW, cellH);
        cellSize = Math.max(cellSize, 5);

        int totalMazeW = cols * cellSize;
        int totalMazeH = rows * cellSize;
        int startX = (panelW - totalMazeW) / 2;
        int startY = (panelH - totalMazeH) / 2 + 30; // Turun dikit buat HUD

        Cell[][] grid = mazeGraph.getGrid();

        // 1. GRID
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                drawCustomCell(g2, grid[r][c], startX + (c * cellSize), startY + (r * cellSize), cellSize);
            }
        }

        // 2. ANIMASI VISITED
        g2.setColor(new Color(150, 150, 150, 120));
        for (Cell cell : visitedAnimation) {
            if (cell.getType() != CellType.WALL) {
                int x = startX + (cell.getCol() * cellSize);
                int y = startY + (cell.getRow() * cellSize);
                g2.fillRect(x, y, cellSize, cellSize);
            }
        }

        // 3. JALUR (MULTIPLE PATHS)
        if (isPathing && allSolutionPaths != null && !allSolutionPaths.isEmpty()) {
            int colorIndex = 0;
            for (List<Cell> path : allSolutionPaths) {
                if (path.size() < 2) continue;
                Color pathColor = PATH_COLORS[colorIndex % PATH_COLORS.length];
                drawPathLine(g2, path, startX, startY, pathColor, cellSize);
                colorIndex++;
            }
        }

        // 4. MARKER
        drawStyledMarker(g2, mazeGraph.getStart(), new Color(46, 204, 113), "S", startX, startY, cellSize);
        drawStyledMarker(g2, mazeGraph.getEnd(), new Color(155, 89, 182), "E", startX, startY, cellSize);

        // 5. DRAW HUD (INFO COST)
        drawStatusHUD(g2, panelW);
    }

    private void drawStatusHUD(Graphics2D g2, int panelWidth) {
        int hudHeight = 35;
        // Background transparan
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, panelWidth, hudHeight);
        // Garis pemisah bawah
        g2.setColor(new Color(255, 255, 255, 50));
        g2.drawLine(0, hudHeight, panelWidth, hudHeight);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(Color.WHITE);

        String textAlgo = "Algorithm: " + lastAlgorithm;
        String textStatus = "Status: " + statusText;
        String textCost = "Total Cost: " + (isFinished || isPathing ? String.format("%.0f", totalPathCost) : "-");

        FontMetrics fm = g2.getFontMetrics();
        int padding = 20;

        // Kiri
        g2.drawString(textAlgo, padding, 23);

        // Tengah
        int wStatus = fm.stringWidth(textStatus);
        g2.drawString(textStatus, (panelWidth - wStatus) / 2, 23);

        // Kanan (Warna Emas untuk Cost)
        int wCost = fm.stringWidth(textCost);
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(textCost, panelWidth - wCost - padding, 23);
    }

    private void drawCustomCell(Graphics2D g2, Cell cell, int x, int y, int cellSize) {
        if (cell.getType() == CellType.WALL) {
            g2.setColor(Color.BLACK);
            g2.fillRect(x, y, cellSize, cellSize);
        } else if (cell.getType() == CellType.TERRACE) {
            g2.setColor(new Color(60, 60, 60));
            g2.fillRect(x, y, cellSize, cellSize);
            g2.setColor(new Color(40, 40, 40));
            g2.drawRect(x, y, cellSize, cellSize);
        } else {
            g2.setColor(cell.getType().getColor());
            g2.fillRect(x, y, cellSize, cellSize);
            g2.setColor(new Color(0, 0, 0, 50));
            g2.drawRect(x, y, cellSize, cellSize);
        }
    }

    private void drawPathLine(Graphics2D g2, List<Cell> path, int startX, int startY, Color color, int cellSize) {
        Path2D polyline = new Path2D.Float();
        Cell first = path.get(0);
        polyline.moveTo(startX + first.getCol() * cellSize + cellSize / 2.0,
                startY + first.getRow() * cellSize + cellSize / 2.0);

        for (int i = 1; i < path.size(); i++) {
            Cell next = path.get(i);
            polyline.lineTo(startX + next.getCol() * cellSize + cellSize / 2.0,
                    startY + next.getRow() * cellSize + cellSize / 2.0);
        }

        float strokeWidth = Math.max(2f, cellSize / 3.5f);
        g2.setStroke(new BasicStroke(strokeWidth * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g2.draw(polyline);

        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        g2.draw(polyline);
    }

    private void drawStyledMarker(Graphics2D g2, Cell cell, Color bg, String text, int dx, int dy, int cellSize) {
        if (cell == null) return;
        int x = dx + (cell.getCol() * cellSize);
        int y = dy + (cell.getRow() * cellSize);
        int offset = Math.max(2, cellSize / 6);
        int size = cellSize - offset * 2;

        g2.setColor(bg);
        g2.fillOval(x + offset, y + offset, size, size);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(Math.max(1, cellSize / 10)));
        g2.drawOval(x + offset, y + offset, size, size);

        g2.setColor(Color.WHITE);
        int fontSize = Math.max(10, cellSize / 2);
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int txtW = fm.stringWidth(text);
        int txtH = fm.getAscent() - fm.getDescent();
        g2.drawString(text, x + cellSize / 2 - txtW / 2, y + cellSize / 2 + txtH / 2 + 1);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 35));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bg.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bg); }
        });
        btn.addActionListener(e -> {
            if(soundManager != null) soundManager.playSFX("click");
        });
        return btn;
    }
}
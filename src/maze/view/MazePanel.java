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
    // CELL_SIZE dihapus, kita hitung dinamis

    private static final Color[] PATH_COLORS = {
            new Color(255, 50, 50),
            new Color(0, 255, 255),
            new Color(255, 255, 0),
            new Color(255, 0, 255),
            new Color(50, 255, 100),
            new Color(255, 165, 0)
    };

    private MazeGraph mazeGraph;
    private final MazeSolver solver;

    private List<Cell> visitedAnimation;
    private List<List<Cell>> allSolutionPaths;

    private int animationIndex = 0;
    private Timer timer;
    private boolean isScanning = false;
    private boolean isPathing = false;

    // SoundManager instance
    private SoundManagerMaze soundManager;

    // Panel Kontrol dijadikan variabel class agar bisa diakses height-nya
    private JPanel controlPanel;

    public MazePanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);
        this.solver = new MazeSolver();

        this.soundManager = new SoundManagerMaze();
        this.soundManager.playBGM("game"); // Play Game BGM

        // Generate maze dengan ukuran agak besar karena layar full screen
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
            stopMusic();

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }

            new MainLauncher().setVisible(true);
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
        animationIndex = 0;

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
                            break;
                        }
                    }
                } else if (isPathing) {
                    ((Timer)e.getSource()).stop();
                    soundManager.playSFX("success");
                }
                repaint();
            });
            timer.start();
        }
    }

    // --- LOGIKA RESPONSIF (FULL SCREEN) ADA DI SINI ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (mazeGraph == null) return;

        // 1. Hitung area yang tersedia untuk menggambar maze
        int panelW = getWidth();
        // Kurangi tinggi control panel agar maze tidak tertutup tombol di bawah
        int panelH = getHeight() - (controlPanel != null ? controlPanel.getHeight() : 100) - 20;

        // 2. Hitung ukuran kotak (Cell Size) secara dinamis
        int rows = mazeGraph.getRows();
        int cols = mazeGraph.getCols();

        // Pilih ukuran terkecil antara lebar/tinggi agar kotak tetap proporsional (persegi)
        int cellW = panelW / cols;
        int cellH = panelH / rows;
        int cellSize = Math.min(cellW, cellH);

        // Batasi minimal 5 pixel agar tidak error saat window diminimize sangat kecil
        cellSize = Math.max(cellSize, 5);

        // 3. Hitung posisi awal (Start X & Y) agar Maze selalu di tengah (Center)
        int totalMazeW = cols * cellSize;
        int totalMazeH = rows * cellSize;
        int startX = (panelW - totalMazeW) / 2;
        int startY = (panelH - totalMazeH) / 2 + 10; // +10 padding atas

        Cell[][] grid = mazeGraph.getGrid();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];
                int x = startX + (c * cellSize);
                int y = startY + (r * cellSize);
                drawCustomCell(g2, cell, x, y, cellSize);
            }
        }

        g2.setColor(new Color(150, 150, 150, 120));
        for (Cell cell : visitedAnimation) {
            if (cell.getType() != CellType.WALL) {
                int x = startX + (cell.getCol() * cellSize);
                int y = startY + (cell.getRow() * cellSize);
                g2.fillRect(x, y, cellSize, cellSize);
                g2.setColor(new Color(200, 200, 200, 50));
                g2.drawRect(x, y, cellSize, cellSize);
                g2.setColor(new Color(150, 150, 150, 120));
            }
        }

        if (isPathing && allSolutionPaths != null && !allSolutionPaths.isEmpty()) {
            int colorIndex = 0;
            for (List<Cell> path : allSolutionPaths) {
                if (path.size() < 2) continue;
                Color pathColor = PATH_COLORS[colorIndex % PATH_COLORS.length];
                drawPathLine(g2, path, startX, startY, pathColor, cellSize);
                colorIndex++;
            }
        }

        drawStyledMarker(g2, mazeGraph.getStart(), new Color(46, 204, 113), "S", startX, startY, cellSize);
        drawStyledMarker(g2, mazeGraph.getEnd(), new Color(155, 89, 182), "E", startX, startY, cellSize);
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
        polyline.moveTo(startX + first.getCol() * cellSize + cellSize / 2.0, startY + first.getRow() * cellSize + cellSize / 2.0);

        for (int i = 1; i < path.size(); i++) {
            Cell next = path.get(i);
            polyline.lineTo(startX + next.getCol() * cellSize + cellSize / 2.0, startY + next.getRow() * cellSize + cellSize / 2.0);
        }

        // Ketebalan garis menyesuaikan ukuran cell
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
        // Font size menyesuaikan ukuran cell
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
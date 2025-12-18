package maze.view;

import maze.controller.MazeSolver;
import maze.model.Cell;
import maze.model.CellType;
import maze.model.MazeGraph;
import main.MainLauncher; // Import Main Launcher

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MazePanel extends JPanel {
    private static final int CELL_SIZE = 25;
    private MazeGraph mazeGraph;
    private final MazeSolver solver;

    // Data Animasi
    private List<Cell> visitedAnimation;
    private List<Cell> pathSolution;
    private int animationIndex = 0;
    private Timer timer;
    private boolean isScanning = false;
    private boolean isPathing = false;

    public MazePanel() {
        this.setLayout(new BorderLayout());
        this.solver = new MazeSolver();

        // Generate Maze Awal
        generateNewMaze(21, 35);

        // Panel Kontrol (Tombol)
        JPanel controlPanel = createControlPanel();
        this.add(controlPanel, BorderLayout.SOUTH);
    }

    private void generateNewMaze(int rows, int cols) {
        if (timer != null && timer.isRunning()) timer.stop();
        visitedAnimation = new ArrayList<>();
        pathSolution = new ArrayList<>();
        isScanning = false;
        isPathing = false;
        mazeGraph = new MazeGraph(rows, cols);
        mazeGraph.generateMaze();
        repaint();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        // Menggunakan GridLayout agar tombol rapi dan tidak terpotong
        panel.setLayout(new GridLayout(2, 1, 5, 5));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Baris 1: Algoritma
        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        algoPanel.setOpaque(false);

        JButton btnBFS = createStyledButton("BFS", new Color(52, 152, 219));
        btnBFS.addActionListener(e -> runSolver("BFS"));

        JButton btnDFS = createStyledButton("DFS", new Color(155, 89, 182));
        btnDFS.addActionListener(e -> runSolver("DFS"));

        JButton btnDijkstra = createStyledButton("Dijkstra", new Color(46, 204, 113));
        btnDijkstra.addActionListener(e -> runSolver("Dijkstra"));

        JButton btnAStar = createStyledButton("A* (Smart)", new Color(241, 196, 15));
        btnAStar.addActionListener(e -> runSolver("A*"));

        algoPanel.add(btnBFS);
        algoPanel.add(btnDFS);
        algoPanel.add(btnDijkstra);
        algoPanel.add(btnAStar);

        // Baris 2: Generate & Back
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnGen = createStyledButton("New Maze", new Color(230, 126, 34));
        btnGen.addActionListener(e -> generateNewMaze(21, 35));

        JButton btnBack = createStyledButton("Back to Menu", new Color(192, 57, 43));
        btnBack.addActionListener(e -> {
            // Tutup Window saat ini
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
            // Buka Main Menu
            new MainLauncher().setVisible(true);
        });

        actionPanel.add(btnGen);
        actionPanel.add(btnBack);

        panel.add(algoPanel);
        panel.add(actionPanel);

        return panel;
    }

    private void runSolver(String type) {
        if (timer != null && timer.isRunning()) return;
        visitedAnimation.clear();
        pathSolution.clear();
        isScanning = true;
        isPathing = false;
        animationIndex = 0;

        MazeSolver.SolverResult result = null;
        switch (type) {
            case "BFS": result = solver.solveBFS(mazeGraph); break;
            case "DFS": result = solver.solveDFS(mazeGraph); break;
            case "Dijkstra": result = solver.solveDijkstra(mazeGraph); break;
            case "A*": result = solver.solveAStar(mazeGraph); break;
        }

        if (result != null) {
            final List<Cell> order = result.getVisitedOrder();
            final List<Cell> path = result.getPath();

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
                            pathSolution = path;
                            break;
                        }
                    }
                } else if (isPathing) {
                    ((Timer)e.getSource()).stop();
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

        // Background Gelap
        g2.setColor(new Color(20, 20, 20));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (mazeGraph == null) return;

        int totalW = mazeGraph.getCols() * CELL_SIZE;
        int totalH = mazeGraph.getRows() * CELL_SIZE;
        int startX = (getWidth() - totalW) / 2;
        int startY = (getHeight() - totalH) / 2;

        // 1. Grid Dasar
        Cell[][] grid = mazeGraph.getGrid();
        for (int r = 0; r < mazeGraph.getRows(); r++) {
            for (int c = 0; c < mazeGraph.getCols(); c++) {
                Cell cell = grid[r][c];
                int x = startX + (c * CELL_SIZE);
                int y = startY + (r * CELL_SIZE);

                g2.setColor(cell.getType().getColor());
                g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                g2.setColor(new Color(0,0,0, 50));
                g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // 2. Animasi Scanning
        g2.setColor(new Color(0, 255, 255, 120));
        for (Cell cell : visitedAnimation) {
            if (cell.getType() != CellType.WALL) {
                int x = startX + (cell.getCol() * CELL_SIZE);
                int y = startY + (cell.getRow() * CELL_SIZE);
                g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // 3. Jalur Solusi
        if (isPathing && pathSolution != null) {
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.RED);
            for (int i = 0; i < pathSolution.size() - 1; i++) {
                Cell curr = pathSolution.get(i);
                Cell next = pathSolution.get(i+1);
                int x1 = startX + (curr.getCol() * CELL_SIZE) + CELL_SIZE/2;
                int y1 = startY + (curr.getRow() * CELL_SIZE) + CELL_SIZE/2;
                int x2 = startX + (next.getCol() * CELL_SIZE) + CELL_SIZE/2;
                int y2 = startY + (next.getRow() * CELL_SIZE) + CELL_SIZE/2;
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // 4. Start & End
        drawMarker(g2, mazeGraph.getStart(), Color.GREEN, "S", startX, startY);
        drawMarker(g2, mazeGraph.getEnd(), Color.MAGENTA, "E", startX, startY);
    }

    private void drawMarker(Graphics2D g2, Cell cell, Color color, String text, int dx, int dy) {
        if (cell == null) return;
        int x = dx + (cell.getCol() * CELL_SIZE);
        int y = dy + (cell.getRow() * CELL_SIZE);
        g2.setColor(color);
        g2.fillOval(x + 4, y + 4, CELL_SIZE - 8, CELL_SIZE - 8);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString(text, x + 8, y + 17);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE); // Teks Putih
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);    // Hilangkan border 3D default
        btn.setOpaque(true);            // Pastikan warna background muncul
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 35)); // Ukuran fix agar seragam
        return btn;
    }
}
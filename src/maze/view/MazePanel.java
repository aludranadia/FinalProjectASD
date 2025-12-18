package maze.view;

import maze.controller.MazeSolver;
import maze.model.Cell;
import maze.model.CellType;
import maze.model.MazeGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MazePanel extends JPanel {
    // Ukuran Cell (Pixel)
    private static final int CELL_SIZE = 25;

    // Model & Controller
    private MazeGraph mazeGraph;
    private final MazeSolver solver;

    // Data untuk Animasi
    private List<Cell> visitedAnimation; // Daftar node yang sedang "discan"
    private List<Cell> pathSolution;     // Jalur solusi akhir
    private int animationIndex = 0;
    private Timer timer;
    private boolean isScanning = false;  // Sedang fase animasi scanning?
    private boolean isPathing = false;   // Sedang fase animasi jalur akhir?

    public MazePanel() {
        this.setLayout(new BorderLayout());
        this.solver = new MazeSolver();

        // Inisialisasi Graph (Default 21x21 agar pas di layar)
        // Ukuran ganjil disarankan untuk Prim's Algorithm
        generateNewMaze(21, 35);

        // Panel Kontrol (Tombol)
        JPanel controlPanel = createControlPanel();
        this.add(controlPanel, BorderLayout.SOUTH);
    }

    private void generateNewMaze(int rows, int cols) {
        // Hentikan animasi jika sedang berjalan
        if (timer != null && timer.isRunning()) timer.stop();

        // Reset Data Visual
        visitedAnimation = new ArrayList<>();
        pathSolution = new ArrayList<>();
        isScanning = false;
        isPathing = false;

        // Generate Graph Baru
        mazeGraph = new MazeGraph(rows, cols);
        mazeGraph.generateMaze();

        repaint();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setBackground(new Color(40, 40, 40));

        JButton btnGen = createStyledButton("Generate New Maze", new Color(230, 126, 34));
        btnGen.addActionListener(e -> generateNewMaze(21, 35));

        JButton btnBFS = createStyledButton("BFS", new Color(52, 152, 219));
        btnBFS.addActionListener(e -> runSolver("BFS"));

        JButton btnDFS = createStyledButton("DFS", new Color(155, 89, 182));
        btnDFS.addActionListener(e -> runSolver("DFS"));

        JButton btnDijkstra = createStyledButton("Dijkstra (Cost)", new Color(46, 204, 113));
        btnDijkstra.addActionListener(e -> runSolver("Dijkstra"));

        JButton btnAStar = createStyledButton("A* (Smart)", new Color(241, 196, 15));
        btnAStar.addActionListener(e -> runSolver("A*"));

        panel.add(btnGen);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(btnBFS);
        panel.add(btnDFS);
        panel.add(btnDijkstra);
        panel.add(btnAStar);

        return panel;
    }

    private void runSolver(String type) {
        if (timer != null && timer.isRunning()) return; // Cegah spam tombol saat animasi

        // Reset visual sebelumnya
        visitedAnimation.clear();
        pathSolution.clear();
        isScanning = true;
        isPathing = false;
        animationIndex = 0;

        // Jalankan Algoritma dari Controller
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

            // Setup Timer Animasi
            timer = new Timer(10, e -> { // 10ms speed
                // FASE 1: SCANNING (Kuning/Scanning Effect)
                if (isScanning) {
                    // Tambahkan beberapa node sekaligus agar animasi lebih cepat
                    for (int i = 0; i < 5; i++) {
                        if (animationIndex < order.size()) {
                            visitedAnimation.add(order.get(animationIndex));
                            animationIndex++;
                        } else {
                            // Selesai scanning, pindah ke fase Path
                            isScanning = false;
                            isPathing = true;
                            animationIndex = 0;
                            pathSolution = path; // Siapkan path untuk digambar
                            break;
                        }
                    }
                }
                // FASE 2: DRAW PATH (Garis Merah Solusi)
                else if (isPathing) {
                    // Path langsung digambar sekaligus atau bertahap (opsional)
                    // Di sini kita langsung selesaikan timer agar path muncul solid
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

        // Background
        g2.setColor(new Color(20, 20, 20));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (mazeGraph == null) return;

        // Centering Maze di Panel
        int totalW = mazeGraph.getCols() * CELL_SIZE;
        int totalH = mazeGraph.getRows() * CELL_SIZE;
        int startX = (getWidth() - totalW) / 2;
        int startY = (getHeight() - totalH) / 2;

        // 1. GAMBAR GRID DASAR
        Cell[][] grid = mazeGraph.getGrid();
        for (int r = 0; r < mazeGraph.getRows(); r++) {
            for (int c = 0; c < mazeGraph.getCols(); c++) {
                Cell cell = grid[r][c];
                int x = startX + (c * CELL_SIZE);
                int y = startY + (r * CELL_SIZE);

                // Warna Dasar berdasarkan Tipe (Wall, Grass, Water, dll)
                g2.setColor(cell.getType().getColor());
                g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Garis Grid Tipis
                g2.setColor(new Color(0,0,0, 50));
                g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // 2. GAMBAR ANIMASI SCANNING (Visited Nodes)
        // Kita beri warna transparan kuning/biru muda untuk area yang sudah dicek algoritma
        g2.setColor(new Color(0, 255, 255, 100)); // Cyan transparan
        for (Cell cell : visitedAnimation) {
            if (cell.getType() != CellType.WALL) { // Jangan warnai tembok
                int x = startX + (cell.getCol() * CELL_SIZE);
                int y = startY + (cell.getRow() * CELL_SIZE);
                g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // 3. GAMBAR JALUR SOLUSI (Path)
        if (isPathing && pathSolution != null) {
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.RED);

            // Gambar garis menghubungkan titik tengah cell
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

        // 4. GAMBAR START & END POINT
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
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }
}
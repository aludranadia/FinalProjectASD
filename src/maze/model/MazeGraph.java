package maze.model;

import java.util.*;

public class MazeGraph {
    private final int rows;
    private final int cols;
    private final Cell[][] grid;
    private final Random random;
    private Cell start;
    private Cell end;

    public MazeGraph(int rows, int cols) {
        // Pastikan ukuran ganjil agar tembok dan jalan bisa tergenerate dengan rapi di Grid
        this.rows = (rows % 2 == 0) ? rows + 1 : rows;
        this.cols = (cols % 2 == 0) ? cols + 1 : cols;
        this.grid = new Cell[this.rows][this.cols];
        this.random = new Random();

        initializeGrid();
    }

    private void initializeGrid() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(r, c);
                grid[r][c].setType(CellType.WALL); // Awalnya semua tembok
            }
        }
    }

    // --- STEP 1: GENERATE MAZE (PRIM'S ALGORITHM) ---
    public void generateMaze() {
        // 1. Start Point untuk Algoritma (biasanya (1,1))
        int startR = 1;
        int startC = 1;
        grid[startR][startC].setType(CellType.TERRACE);

        // List untuk menyimpan tembok yang akan dicek (Frontier)
        ArrayList<Cell> walls = new ArrayList<>();
        addWallsToList(startR, startC, walls);

        // Loop Prim's Algorithm
        while (!walls.isEmpty()) {
            // Ambil tembok secara acak
            int index = random.nextInt(walls.size());
            Cell wall = walls.remove(index);

            // Cek tetangga yang dipisahkan oleh tembok ini
            List<Cell> neighbors = getVisitedNeighbors(wall.getRow(), wall.getCol());

            if (!neighbors.isEmpty()) {
                // Ambil salah satu tetangga yang sudah menjadi jalan (Visited)
                Cell visitedNeighbor = neighbors.get(random.nextInt(neighbors.size()));

                // Hitung posisi di seberang tembok
                int rDiff = wall.getRow() - visitedNeighbor.getRow();
                int cDiff = wall.getCol() - visitedNeighbor.getCol();
                int targetR = wall.getRow() + rDiff;
                int targetC = wall.getCol() + cDiff;

                // Jika target di dalam grid dan masih TEMBOK, kita tembus!
                if (isValid(targetR, targetC) && grid[targetR][targetC].getType() == CellType.WALL) {
                    // Jadikan tembok ini jalan
                    grid[wall.getRow()][wall.getCol()].setType(CellType.TERRACE);
                    // Jadikan target seberang jalan juga
                    grid[targetR][targetC].setType(CellType.TERRACE);

                    // Masukkan tembok-tembok baru dari target ke daftar
                    addWallsToList(targetR, targetC, walls);
                }
            }
        }

        // --- STEP 1b: CREATE MULTIPLE PATHS (CYCLES) ---
        // Hapus beberapa tembok tambahan secara acak agar ada looping/banyak jalan
        createMultiplePaths(50); // Angka bisa disesuaikan (makin besar makin banyak bolong)

        // --- STEP 3: APPLY WEIGHTS (GRASS, MUD, WATER) ---
        applyWeights();

        // Tentukan Entry (Start) dan Exit (End)
        setStartAndEnd();

        // --- STEP Akhir: BUILD GRAPH CONNECTIONS ---
        // Menghubungkan node-node yang bisa dilewati (Adjacency List)
        buildAdjacencyList();
    }

    private void addWallsToList(int r, int c, ArrayList<Cell> walls) {
        // Cek Atas, Bawah, Kiri, Kanan
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (isValid(nr, nc) && grid[nr][nc].getType() == CellType.WALL) {
                // Hindari duplikasi di list walls (opsional, tapi bagus untuk performa)
                if (!walls.contains(grid[nr][nc])) {
                    walls.add(grid[nr][nc]);
                }
            }
        }
    }

    private List<Cell> getVisitedNeighbors(int r, int c) {
        List<Cell> list = new ArrayList<>();
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            // Visited neighbor artinya dia BUKAN Wall (sudah jadi jalan/terrace)
            if (isValid(nr, nc) && grid[nr][nc].getType() != CellType.WALL) {
                list.add(grid[nr][nc]);
            }
        }
        return list;
    }

    private void createMultiplePaths(int numberOfWallsToRemove) {
        int count = 0;
        int attempts = 0;
        while (count < numberOfWallsToRemove && attempts < 1000) {
            int r = random.nextInt(rows - 2) + 1;
            int c = random.nextInt(cols - 2) + 1;

            if (grid[r][c].getType() == CellType.WALL) {
                // Cek apakah menghapus tembok ini menghubungkan dua jalan terpisah
                // (Logika sederhana: jika dia tembok, tapi punya tetangga jalan di kiri-kanan ATAU atas-bawah)
                boolean hasVerticalPath = (grid[r-1][c].getType() != CellType.WALL && grid[r+1][c].getType() != CellType.WALL);
                boolean hasHorizontalPath = (grid[r][c-1].getType() != CellType.WALL && grid[r][c+1].getType() != CellType.WALL);

                if (hasVerticalPath || hasHorizontalPath) {
                    grid[r][c].setType(CellType.TERRACE);
                    count++;
                }
            }
            attempts++;
        }
    }

    private void applyWeights() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Hanya ubah jika ini adalah jalan (TERRACE hasil generate tadi)
                if (grid[r][c].getType() == CellType.TERRACE) {
                    double chance = random.nextDouble();

                    // Probabilitas Weighted Cells
                    if (chance < 0.1) {
                        grid[r][c].setType(CellType.WATER); // 10% Water (Cost 10)
                    } else if (chance < 0.3) {
                        grid[r][c].setType(CellType.MUD);   // 20% Mud (Cost 5)
                    } else if (chance < 0.6) {
                        grid[r][c].setType(CellType.GRASS); // 30% Grass (Cost 1)
                    }
                    // Sisanya 40% tetap TERRACE (Cost 0)
                }
            }
        }
    }

    private void setStartAndEnd() {
        // Entry Point (Pojok Kiri Atas)
        start = grid[1][1];
        start.setType(CellType.TERRACE); // Pastikan Start enak jalannya (Cost 0)

        // Exit Point (Pojok Kanan Bawah)
        // Cari titik valid terdekat dari pojok kanan bawah
        int endR = rows - 2;
        int endC = cols - 2;
        end = grid[endR][endC];
        end.setType(CellType.TERRACE);   // Pastikan End enak jalannya
    }

    // --- REPRESENTASI GRAPH (ADJACENCY LIST) ---
    private void buildAdjacencyList() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell current = grid[r][c];

                // Tembok tidak punya tetangga (tidak bisa dilewati)
                if (current.getType() == CellType.WALL) continue;

                // Cek 4 Arah
                int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] d : dirs) {
                    int nr = r + d[0];
                    int nc = c + d[1];

                    // Jika tetangga valid dan BUKAN tembok, tambahkan ke adjacency list
                    if (isValid(nr, nc) && grid[nr][nc].getType() != CellType.WALL) {
                        current.addNeighbor(grid[nr][nc]);
                    }
                }
            }
        }
    }

    private boolean isValid(int r, int c) {
        return r > 0 && r < rows - 1 && c > 0 && c < cols - 1;
    }

    // --- Getters ---
    public Cell[][] getGrid() { return grid; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Cell getStart() { return start; }
    public Cell getEnd() { return end; }

    // Method untuk reset state graph sebelum algoritma searching dijalankan ulang
    public void resetGraphState() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].resetAlgorithmData();
            }
        }
    }
}
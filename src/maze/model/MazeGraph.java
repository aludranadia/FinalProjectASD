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

    // GENERATE MAZE (PRIM'S ALGORITHM)
    public void generateMaze() {
        int startR = 1;
        int startC = 1;
        grid[startR][startC].setType(CellType.TERRACE);

        ArrayList<Cell> walls = new ArrayList<>();
        addWallsToList(startR, startC, walls);

        // Loop Prim's Algorithm
        while (!walls.isEmpty()) {
            int index = random.nextInt(walls.size());
            Cell wall = walls.remove(index);

            List<Cell> neighbors = getVisitedNeighbors(wall.getRow(), wall.getCol());

            if (!neighbors.isEmpty()) {
                Cell visitedNeighbor = neighbors.get(random.nextInt(neighbors.size()));

                int rDiff = wall.getRow() - visitedNeighbor.getRow();
                int cDiff = wall.getCol() - visitedNeighbor.getCol();
                int targetR = wall.getRow() + rDiff;
                int targetC = wall.getCol() + cDiff;

                if (isValid(targetR, targetC) && grid[targetR][targetC].getType() == CellType.WALL) {
                    grid[wall.getRow()][wall.getCol()].setType(CellType.TERRACE);
                    grid[targetR][targetC].setType(CellType.TERRACE);
                    addWallsToList(targetR, targetC, walls);
                }
            }
        }

        // CREATE MULTIPLE PATHS (CYCLES)
        createMultiplePaths(50);
        applyWeights();
        setStartAndEnd();
        buildAdjacencyList();
    }

    private void addWallsToList(int r, int c, ArrayList<Cell> walls) {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (isValid(nr, nc) && grid[nr][nc].getType() == CellType.WALL) {
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
                if (grid[r][c].getType() == CellType.TERRACE) {
                    double chance = random.nextDouble();

                    if (chance < 0.1) {
                        grid[r][c].setType(CellType.WATER); // 10% Water (Cost 10)
                    } else if (chance < 0.3) {
                        grid[r][c].setType(CellType.MUD);   // 20% Mud (Cost 5)
                    } else if (chance < 0.6) {
                        grid[r][c].setType(CellType.GRASS); // 30% Grass (Cost 1)
                    }
                }
            }
        }
    }

    private void setStartAndEnd() {
        start = grid[1][1];
        start.setType(CellType.TERRACE);
        int endR = rows - 2;
        int endC = cols - 2;
        end = grid[endR][endC];
        end.setType(CellType.TERRACE);
    }

    private void buildAdjacencyList() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell current = grid[r][c];

                if (current.getType() == CellType.WALL) continue;

                int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] d : dirs) {
                    int nr = r + d[0];
                    int nc = c + d[1];

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

    public Cell[][] getGrid() { return grid; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Cell getStart() { return start; }
    public Cell getEnd() { return end; }

    public void resetGraphState() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].resetAlgorithmData();
            }
        }
    }
}
package maze.controller;

import maze.model.Cell;
import maze.model.MazeGraph;
import java.util.*;

public class MazeSolver {

    // Class pembungkus hasil agar bisa dikirim ke View (Animasi + Jalur Akhir)
    public static class SolverResult {
        private final List<Cell> visitedOrder; // Urutan eksplorasi (untuk animasi)
        private final List<Cell> path;         // Jalur solusi akhir (garis tebal)

        public SolverResult(List<Cell> visitedOrder, List<Cell> path) {
            this.visitedOrder = visitedOrder;
            this.path = path;
        }

        public List<Cell> getVisitedOrder() { return visitedOrder; }
        public List<Cell> getPath() { return path; }
    }

    // --- 1. BFS (Breadth-First Search) ---
    // Mencari jalur dengan JUMLAH LANGKAH paling sedikit (mengabaikan bobot/cost)
    public SolverResult solveBFS(MazeGraph graph) {
        graph.resetGraphState(); // Reset visited & parent
        Cell start = graph.getStart();
        Cell end = graph.getEnd();

        List<Cell> visitedOrder = new ArrayList<>();
        Queue<Cell> queue = new LinkedList<>();

        start.setVisited(true);
        queue.add(start);

        boolean found = false;
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            visitedOrder.add(current);

            if (current == end) {
                found = true;
                break;
            }

            // MENGGUNAKAN GRAPH ADJACENCY LIST
            for (Cell neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    neighbor.setParent(current);
                    queue.add(neighbor);
                }
            }
        }

        return new SolverResult(visitedOrder, found ? reconstructPath(end) : new ArrayList<>());
    }

    // --- 2. DFS (Depth-First Search) ---
    // Mencari jalur dengan mengebor sedalam mungkin (seringkali pathnya jelek/memutar)
    public SolverResult solveDFS(MazeGraph graph) {
        graph.resetGraphState();
        Cell start = graph.getStart();
        Cell end = graph.getEnd();

        List<Cell> visitedOrder = new ArrayList<>();
        Stack<Cell> stack = new Stack<>();

        start.setVisited(true);
        stack.push(start);

        boolean found = false;
        while (!stack.isEmpty()) {
            Cell current = stack.pop();
            visitedOrder.add(current);

            if (current == end) {
                found = true;
                break;
            }

            // GRAPH TRAVERSAL
            for (Cell neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    neighbor.setParent(current);
                    stack.push(neighbor);
                }
            }
        }

        return new SolverResult(visitedOrder, found ? reconstructPath(end) : new ArrayList<>());
    }

    // --- 3. DIJKSTRA ---
    // Mencari jalur dengan TOTAL COST TERENDAH (Weighted)
    public SolverResult solveDijkstra(MazeGraph graph) {
        return solveWeighted(graph, false); // false = tanpa heuristic
    }

    // --- 4. A* (A-Star) ---
    // Mencari jalur terendah tapi lebih pintar (menggunakan Heuristic arah target)
    public SolverResult solveAStar(MazeGraph graph) {
        return solveWeighted(graph, true);  // true = pakai heuristic
    }

    // Logika Inti Weighted Search (Dijkstra & A*)
    private SolverResult solveWeighted(MazeGraph graph, boolean useHeuristic) {
        graph.resetGraphState();
        Cell start = graph.getStart();
        Cell end = graph.getEnd();

        List<Cell> visitedOrder = new ArrayList<>();

        // PriorityQueue menggunakan compareTo di class Cell (berdasarkan fScore)
        PriorityQueue<Cell> pq = new PriorityQueue<>();

        start.setDistance(0); // g-score
        start.setFScore(0);   // f-score
        pq.add(start);

        boolean found = false;
        while (!pq.isEmpty()) {
            Cell current = pq.poll();

            // Jika node sudah dikunjungi dengan cost lebih rendah, skip
            if (current.isVisited()) continue;
            current.setVisited(true);
            visitedOrder.add(current);

            if (current == end) {
                found = true;
                break;
            }

            for (Cell neighbor : current.getNeighbors()) {
                if (neighbor.isVisited()) continue;

                // Hitung tentative g-score: Jarak saat ini + Cost Tipe Cell Tetangga
                double newDist = current.getDistance() + neighbor.getType().getCost();

                if (newDist < neighbor.getDistance()) {
                    neighbor.setDistance(newDist);
                    neighbor.setParent(current);

                    double heuristic = 0;
                    if (useHeuristic) {
                        // Manhattan Distance Heuristic
                        heuristic = Math.abs(neighbor.getRow() - end.getRow()) +
                                Math.abs(neighbor.getCol() - end.getCol());
                    }

                    // fScore = gScore + hScore
                    // Untuk Dijkstra, hScore = 0, jadi fScore murni jarak.
                    // Untuk A*, fScore kombinasi jarak dan taksiran.
                    neighbor.setFScore(newDist + heuristic);

                    // Hapus & add ulang untuk update posisi di PriorityQueue (re-sort)
                    pq.remove(neighbor);
                    pq.add(neighbor);
                }
            }
        }

        return new SolverResult(visitedOrder, found ? reconstructPath(end) : new ArrayList<>());
    }

    // Helper: Backtracking dari Finish ke Start untuk membentuk garis jalur
    private List<Cell> reconstructPath(Cell end) {
        List<Cell> path = new ArrayList<>();
        Cell current = end;
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        Collections.reverse(path); // Balik agar urut dari Start -> End
        return path;
    }
}
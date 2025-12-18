package maze.controller;

import maze.model.Cell;
import maze.model.MazeGraph;
import java.util.*;

public class MazeSolver {

    public static class SolverResult {
        private final List<Cell> visitedOrder;
        private final List<List<Cell>> paths;

        public SolverResult(List<Cell> visitedOrder, List<List<Cell>> paths) {
            this.visitedOrder = visitedOrder;
            this.paths = paths;
        }

        public List<Cell> getVisitedOrder() { return visitedOrder; }
        public List<List<Cell>> getPaths() { return paths; }
    }

    // --- 1. BFS ---
    public SolverResult solveBFS(MazeGraph graph) {
        graph.resetGraphState();
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

            for (Cell neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    neighbor.setParent(current);
                    queue.add(neighbor);
                }
            }
        }

        List<List<Cell>> resultPaths = new ArrayList<>();
        if (found) {
            resultPaths.add(reconstructSinglePath(end));
        }
        return new SolverResult(visitedOrder, resultPaths);
    }

    // --- 2. DFS ---
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

            for (Cell neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    neighbor.setParent(current);
                    stack.push(neighbor);
                }
            }
        }

        List<List<Cell>> resultPaths = new ArrayList<>();
        if (found) {
            resultPaths.add(reconstructSinglePath(end));
        }
        return new SolverResult(visitedOrder, resultPaths);
    }

    // --- 3. DIJKSTRA ---
    public SolverResult solveDijkstra(MazeGraph graph) {
        return solveWeighted(graph, false);
    }

    // --- 4. A* STAR ---
    public SolverResult solveAStar(MazeGraph graph) {
        return solveWeighted(graph, true);
    }

    // --- WEIGHTED LOGIC (Fix Cycle Issue) ---
    private SolverResult solveWeighted(MazeGraph graph, boolean useHeuristic) {
        graph.resetGraphState();
        Cell start = graph.getStart();
        Cell end = graph.getEnd();

        List<Cell> visitedOrder = new ArrayList<>();
        PriorityQueue<Cell> pq = new PriorityQueue<>();

        start.setDistance(0);
        start.setFScore(0);
        pq.add(start);

        while (!pq.isEmpty()) {
            Cell current = pq.poll();

            if (current.isVisited()) continue;
            current.setVisited(true);
            visitedOrder.add(current);

            if (current == end) {
                continue; // Lanjut cari path alternatif lain yang cost-nya sama
            }

            for (Cell neighbor : current.getNeighbors()) {
                double newDist = current.getDistance() + neighbor.getType().getCost();
                double heuristic = 0;
                if (useHeuristic) {
                    heuristic = Math.abs(neighbor.getRow() - end.getRow()) +
                            Math.abs(neighbor.getCol() - end.getCol());
                }

                if (newDist < neighbor.getDistance()) {
                    neighbor.setDistance(newDist);
                    neighbor.setFScore(newDist + heuristic);
                    neighbor.setParent(current); // Reset parent list
                    pq.add(neighbor);
                }
                else if (newDist == neighbor.getDistance()) {
                    neighbor.addParent(current); // Tambah parent alternatif
                }
            }
        }

        List<List<Cell>> allPaths = new ArrayList<>();
        if (end.getDistance() != Double.MAX_VALUE) {
            // Mulai rekonstruksi rekursif
            reconstructAllPathsRecursive(end, new ArrayList<>(), allPaths);
        }

        return new SolverResult(visitedOrder, allPaths);
    }

    private List<Cell> reconstructSinglePath(Cell end) {
        List<Cell> path = new ArrayList<>();
        Cell current = end;
        while (current != null) {
            path.add(current);
            if (!current.getParents().isEmpty()) {
                current = current.getParents().get(0);
            } else {
                current = null;
            }
        }
        Collections.reverse(path);
        return path;
    }

    // --- PERBAIKAN UTAMA ADA DI SINI ---
    private void reconstructAllPathsRecursive(Cell current, List<Cell> currentPath, List<List<Cell>> allPaths) {
        // Cek Cycle: Jika node ini sudah ada di path yang sedang kita bangun, berhenti (Dead End / Loop)
        if (currentPath.contains(current)) {
            return;
        }

        currentPath.add(current);

        // Base Case: Distance 0 berarti Start Node
        if (current.getDistance() == 0) {
            List<Cell> validPath = new ArrayList<>(currentPath);
            Collections.reverse(validPath);
            allPaths.add(validPath);
        } else {
            // Recurse ke semua parent
            for (Cell parent : current.getParents()) {
                // Kirim copy path agar tidak bentrok antar cabang
                reconstructAllPathsRecursive(parent, new ArrayList<>(currentPath), allPaths);
            }
        }
    }
}
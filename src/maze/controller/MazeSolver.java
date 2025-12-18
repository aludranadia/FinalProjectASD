package maze.controller;

import maze.model.Cell;
import maze.model.CellType;
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
            if (current == end) { found = true; break; }

            for (Cell neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    neighbor.setParent(current);
                    queue.add(neighbor);
                }
            }
        }
        List<List<Cell>> paths = new ArrayList<>();
        if (found) paths.add(reconstructSinglePath(end));
        return new SolverResult(visitedOrder, paths);
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
            if (current == end) { found = true; break; }

            for (Cell neighbor : current.getNeighbors()) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    neighbor.setParent(current);
                    stack.push(neighbor);
                }
            }
        }
        List<List<Cell>> paths = new ArrayList<>();
        if (found) paths.add(reconstructSinglePath(end));
        return new SolverResult(visitedOrder, paths);
    }

    // --- 3. DIJKSTRA ---
    public SolverResult solveDijkstra(MazeGraph graph) {
        return solveWeighted(graph, false);
    }

    // --- 4. A* STAR ---
    public SolverResult solveAStar(MazeGraph graph) {
        return solveWeighted(graph, true);
    }

    // --- 5. PRIM'S ALGORITHM (Modified for Pathfinding) ---
    // Mencari jalur dengan meminimalkan bobot edge maximum yang dilewati (Minimax Path)
    public SolverResult solvePrim(MazeGraph graph) {
        graph.resetGraphState();
        Cell start = graph.getStart();
        Cell end = graph.getEnd();
        List<Cell> visitedOrder = new ArrayList<>();

        // Priority Queue menyimpan [Cost Edge, Node Asal, Node Tujuan]
        // Kita butuh wrapper class kecil untuk Edge
        class Edge implements Comparable<Edge> {
            Cell source, target;
            int weight;
            public Edge(Cell s, Cell t, int w) { source=s; target=t; weight=w; }
            @Override public int compareTo(Edge o) { return Integer.compare(this.weight, o.weight); }
        }

        PriorityQueue<Edge> pq = new PriorityQueue<>();

        // Mulai dari start
        start.setVisited(true);
        visitedOrder.add(start);

        // Masukkan semua edge dari start ke PQ
        for(Cell n : start.getNeighbors()) {
            // Bobot edge = Cost tujuan (karena node-based weight)
            pq.add(new Edge(start, n, n.getType().getCost()));
        }

        boolean found = false;
        while(!pq.isEmpty()) {
            Edge e = pq.poll();
            Cell curr = e.target;

            if(curr.isVisited()) continue; // Skip jika sudah masuk MST

            curr.setVisited(true);
            visitedOrder.add(curr);
            curr.setParent(e.source); // Simpan parent untuk tracking path

            if(curr == end) {
                found = true;
                break; // Prim selesai saat End point terhubung ke Tree
            }

            for(Cell n : curr.getNeighbors()) {
                if(!n.isVisited()) {
                    pq.add(new Edge(curr, n, n.getType().getCost()));
                }
            }
        }

        List<List<Cell>> paths = new ArrayList<>();
        if(found) paths.add(reconstructSinglePath(end));
        return new SolverResult(visitedOrder, paths);
    }

    // --- 6. KRUSKAL'S ALGORITHM (Pathfinding Variant) ---
    // Menggunakan Disjoint Set Union (DSU)
    public SolverResult solveKruskal(MazeGraph graph) {
        graph.resetGraphState();
        Cell start = graph.getStart();
        Cell end = graph.getEnd();
        List<Cell> visitedOrder = new ArrayList<>(); // Untuk animasi, kita isi saat edge diproses

        // 1. Kumpulkan semua Edge di Graph
        class Edge implements Comparable<Edge> {
            Cell u, v;
            int weight;
            public Edge(Cell u, Cell v, int w) { this.u=u; this.v=v; this.weight=w; }
            @Override public int compareTo(Edge o) { return Integer.compare(this.weight, o.weight); }
        }
        List<Edge> allEdges = new ArrayList<>();

        // Scan grid untuk collect edges (hindari duplikasi u-v dan v-u)
        Cell[][] grid = graph.getGrid();
        for(int r=0; r<graph.getRows(); r++){
            for(int c=0; c<graph.getCols(); c++){
                Cell curr = grid[r][c];
                if(curr.getType() == CellType.WALL) continue;
                for(Cell neighbor : curr.getNeighbors()) {
                    // Masukkan edge hanya sekali (misal: jika hashcode u < hashcode v)
                    if(curr.hashCode() < neighbor.hashCode()) {
                        // Bobot edge = rata-rata atau max dari kedua node (simplifikasi: cost neighbor)
                        allEdges.add(new Edge(curr, neighbor, neighbor.getType().getCost()));
                    }
                }
            }
        }
        Collections.sort(allEdges);

        // 2. DSU Initialization
        Map<Cell, Cell> parentMap = new HashMap<>(); // DSU Parent
        for(int r=0; r<graph.getRows(); r++) {
            for(int c=0; c<graph.getCols(); c++) {
                parentMap.put(grid[r][c], grid[r][c]); // Make Set
            }
        }

        // Helper DSU Find
        // (Perlu effectively final wrapper untuk lambda/inner class, jadi pakai method helper di bawah)

        // 3. Process Edges
        // Kruskal membangun MST global, tapi untuk pathfinding kita bisa berhenti saat Start & End terhubung.
        // Tapi agar path terbentuk sempurna (backtrackable), kita perlu menyimpan adjacency list khusus MST.
        Map<Cell, List<Cell>> mstAdj = new HashMap<>();

        for(Edge e : allEdges) {
            Cell rootU = findSet(parentMap, e.u);
            Cell rootV = findSet(parentMap, e.v);

            if(rootU != rootV) {
                // Union
                parentMap.put(rootU, rootV);

                // Tambahkan ke MST Adjacency untuk rekonstruksi path nanti
                mstAdj.computeIfAbsent(e.u, k -> new ArrayList<>()).add(e.v);
                mstAdj.computeIfAbsent(e.v, k -> new ArrayList<>()).add(e.u);

                // Animasi: tandai node yang terlibat
                if(!visitedOrder.contains(e.u)) visitedOrder.add(e.u);
                if(!visitedOrder.contains(e.v)) visitedOrder.add(e.v);

                // Cek apakah Start dan End sudah satu set?
                if(findSet(parentMap, start) == findSet(parentMap, end)) {
                    break;
                }
            }
        }

        // 4. BFS di MST yang terbentuk untuk mencari jalur Start -> End
        // Karena MST adalah Tree, hanya ada 1 jalur unik.
        // Kita gunakan BFS sederhana pada `mstAdj` untuk mengisi parent pointer cell.
        Queue<Cell> q = new LinkedList<>();
        Set<Cell> visitedMST = new HashSet<>();
        q.add(start);
        visitedMST.add(start);

        while(!q.isEmpty()){
            Cell curr = q.poll();
            if(curr == end) break;

            if(mstAdj.containsKey(curr)) {
                for(Cell neighbor : mstAdj.get(curr)) {
                    if(!visitedMST.contains(neighbor)) {
                        visitedMST.add(neighbor);
                        neighbor.setParent(curr); // Set parent untuk rekonstruksi
                        q.add(neighbor);
                    }
                }
            }
        }

        List<List<Cell>> paths = new ArrayList<>();
        paths.add(reconstructSinglePath(end));
        return new SolverResult(visitedOrder, paths);
    }

    // DSU Find Helper (Path Compression)
    private Cell findSet(Map<Cell, Cell> parent, Cell i) {
        if(parent.get(i) == i) return i;
        Cell root = findSet(parent, parent.get(i));
        parent.put(i, root);
        return root;
    }

    // --- SHARED WEIGHTED LOGIC (Dijkstra/A*) ---
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

            if (current == end) continue;

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
                    neighbor.setParent(current);
                    pq.add(neighbor);
                }
                else if (newDist == neighbor.getDistance()) {
                    neighbor.addParent(current);
                }
            }
        }

        List<List<Cell>> allPaths = new ArrayList<>();
        if (end.getDistance() != Double.MAX_VALUE) {
            reconstructAllPathsRecursive(end, new ArrayList<>(), allPaths);
        }
        return new SolverResult(visitedOrder, allPaths);
    }

    private List<Cell> reconstructSinglePath(Cell end) {
        List<Cell> path = new ArrayList<>();
        Cell current = end;
        while (current != null) {
            path.add(current);
            if (!current.getParents().isEmpty()) current = current.getParents().get(0);
            else current = null;
        }
        Collections.reverse(path);
        return path;
    }

    private void reconstructAllPathsRecursive(Cell current, List<Cell> currentPath, List<List<Cell>> allPaths) {
        if (currentPath.contains(current)) return;
        currentPath.add(current);

        if (current.getDistance() == 0) {
            List<Cell> validPath = new ArrayList<>(currentPath);
            Collections.reverse(validPath);
            allPaths.add(validPath);
        } else {
            for (Cell parent : current.getParents()) {
                reconstructAllPathsRecursive(parent, new ArrayList<>(currentPath), allPaths);
            }
        }
    }
}
package maze.controller;

import maze.model.Cell;
import maze.model.MazeGraph;
import maze.model.CellType;
import java.util.*;

public class MazeSolver {

    public static class SolverResult {
        private final List<Cell> visitedOrder;
        private final List<List<Cell>> paths;
        private final double totalCost; // Field baru untuk Cost

        public SolverResult(List<Cell> visitedOrder, List<List<Cell>> paths, double totalCost) {
            this.visitedOrder = visitedOrder;
            this.paths = paths;
            this.totalCost = totalCost;
        }

        public List<Cell> getVisitedOrder() { return visitedOrder; }
        public List<List<Cell>> getPaths() { return paths; }
        public double getTotalCost() { return totalCost; }
    }

    // BFS
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
        double cost = 0;
        if (found) {
            List<Cell> path = reconstructSinglePath(end);
            paths.add(path);
            cost = calculatePathCost(path);
        }
        return new SolverResult(visitedOrder, paths, cost);
    }

    // DFS
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
        double cost = 0;
        if (found) {
            List<Cell> path = reconstructSinglePath(end);
            paths.add(path);
            cost = calculatePathCost(path);
        }
        return new SolverResult(visitedOrder, paths, cost);
    }

    // DIJKSTRA
    public SolverResult solveDijkstra(MazeGraph graph) {
        return solveWeighted(graph, false);
    }

    // A* STAR
    public SolverResult solveAStar(MazeGraph graph) {
        return solveWeighted(graph, true);
    }

    // PRIM'S
    public SolverResult solvePrim(MazeGraph graph) {
        graph.resetGraphState();
        Cell start = graph.getStart();
        Cell end = graph.getEnd();
        List<Cell> visitedOrder = new ArrayList<>();

        class Edge implements Comparable<Edge> {
            Cell source, target;
            int weight;
            public Edge(Cell s, Cell t, int w) { source=s; target=t; weight=w; }
            @Override public int compareTo(Edge o) { return Integer.compare(this.weight, o.weight); }
        }

        PriorityQueue<Edge> pq = new PriorityQueue<>();
        start.setVisited(true);
        visitedOrder.add(start);

        for(Cell n : start.getNeighbors()) {
            pq.add(new Edge(start, n, n.getType().getCost()));
        }

        boolean found = false;
        while(!pq.isEmpty()) {
            Edge e = pq.poll();
            Cell curr = e.target;
            if(curr.isVisited()) continue;

            curr.setVisited(true);
            visitedOrder.add(curr);
            curr.setParent(e.source);

            if(curr == end) { found = true; break; }

            for(Cell n : curr.getNeighbors()) {
                if(!n.isVisited()) {
                    pq.add(new Edge(curr, n, n.getType().getCost()));
                }
            }
        }

        List<List<Cell>> paths = new ArrayList<>();
        double cost = 0;
        if(found) {
            List<Cell> path = reconstructSinglePath(end);
            paths.add(path);
            cost = calculatePathCost(path);
        }
        return new SolverResult(visitedOrder, paths, cost);
    }

    // KRUSKAL
    public SolverResult solveKruskal(MazeGraph graph) {
        graph.resetGraphState();
        Cell start = graph.getStart();
        Cell end = graph.getEnd();
        List<Cell> visitedOrder = new ArrayList<>();

        class Edge implements Comparable<Edge> {
            Cell u, v;
            int weight;
            public Edge(Cell u, Cell v, int w) { this.u=u; this.v=v; this.weight=w; }
            @Override public int compareTo(Edge o) { return Integer.compare(this.weight, o.weight); }
        }
        List<Edge> allEdges = new ArrayList<>();
        Cell[][] grid = graph.getGrid();
        for(int r=0; r<graph.getRows(); r++){
            for(int c=0; c<graph.getCols(); c++){
                Cell curr = grid[r][c];
                if(curr.getType() == CellType.WALL) continue;
                for(Cell neighbor : curr.getNeighbors()) {
                    if(curr.hashCode() < neighbor.hashCode()) {
                        allEdges.add(new Edge(curr, neighbor, neighbor.getType().getCost()));
                    }
                }
            }
        }
        Collections.sort(allEdges);

        Map<Cell, Cell> parentMap = new HashMap<>();
        for(int r=0; r<graph.getRows(); r++) {
            for(int c=0; c<graph.getCols(); c++) {
                parentMap.put(grid[r][c], grid[r][c]);
            }
        }

        Map<Cell, List<Cell>> mstAdj = new HashMap<>();
        for(Edge e : allEdges) {
            Cell rootU = findSet(parentMap, e.u);
            Cell rootV = findSet(parentMap, e.v);
            if(rootU != rootV) {
                parentMap.put(rootU, rootV);
                mstAdj.computeIfAbsent(e.u, k -> new ArrayList<>()).add(e.v);
                mstAdj.computeIfAbsent(e.v, k -> new ArrayList<>()).add(e.u);
                if(!visitedOrder.contains(e.u)) visitedOrder.add(e.u);
                if(!visitedOrder.contains(e.v)) visitedOrder.add(e.v);
                if(findSet(parentMap, start) == findSet(parentMap, end)) break;
            }
        }

        Queue<Cell> q = new LinkedList<>();
        Set<Cell> visitedMST = new HashSet<>();
        q.add(start);
        visitedMST.add(start);
        boolean found = false;
        while(!q.isEmpty()){
            Cell curr = q.poll();
            if(curr == end) { found = true; break; }
            if(mstAdj.containsKey(curr)) {
                for(Cell neighbor : mstAdj.get(curr)) {
                    if(!visitedMST.contains(neighbor)) {
                        visitedMST.add(neighbor);
                        neighbor.setParent(curr);
                        q.add(neighbor);
                    }
                }
            }
        }

        List<List<Cell>> paths = new ArrayList<>();
        double cost = 0;
        if (found) {
            List<Cell> path = reconstructSinglePath(end);
            paths.add(path);
            cost = calculatePathCost(path);
        }
        return new SolverResult(visitedOrder, paths, cost);
    }

    private Cell findSet(Map<Cell, Cell> parent, Cell i) {
        if(parent.get(i) == i) return i;
        Cell root = findSet(parent, parent.get(i));
        parent.put(i, root);
        return root;
    }

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

            if (!current.isVisited()) {
                current.setVisited(true);
                visitedOrder.add(current);
            }

            if (current == end) {
                if (current.getDistance() > end.getDistance()) break;
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
                    neighbor.setParent(current);
                    pq.add(neighbor);
                }
                else if (newDist == neighbor.getDistance()) {
                    neighbor.addParent(current);
                }
            }
        }

        List<List<Cell>> allPaths = new ArrayList<>();
        double cost = 0;
        if (end.getDistance() != Double.MAX_VALUE) {
            cost = end.getDistance();
            reconstructAllPathsRecursive(end, new ArrayList<>(), allPaths);
        }
        return new SolverResult(visitedOrder, allPaths, cost);
    }

    private List<Cell> reconstructSinglePath(Cell end) {
        List<Cell> path = new ArrayList<>();
        Cell current = end;
        Set<Cell> visited = new HashSet<>(); // Safety check loop
        while (current != null) {
            if(visited.contains(current)) break;
            visited.add(current);
            path.add(current);
            if (!current.getParents().isEmpty()) current = current.getParents().get(0);
            else current = null;
        }
        Collections.reverse(path);
        return path;
    }

    private void reconstructAllPathsRecursive(Cell current, List<Cell> currentPath, List<List<Cell>> allPaths) {
        if (allPaths.size() >= 50) return;

        if (currentPath.contains(current)) return; // Cegah Cycle

        currentPath.add(current);

        if (current.getDistance() == 0) {
            List<Cell> validPath = new ArrayList<>(currentPath);
            Collections.reverse(validPath);
            allPaths.add(validPath);
        } else {
            List<Cell> parents = current.getParents();
            if (parents.isEmpty()) return;

            for (Cell parent : parents) {
                reconstructAllPathsRecursive(parent, new ArrayList<>(currentPath), allPaths);

                if (allPaths.size() >= 50) return;
            }
        }
    }

    private double calculatePathCost(List<Cell> path) {
        double cost = 0;
        for (Cell cell : path) {
            cost += cell.getType().getCost();
        }
        return cost;
    }
}
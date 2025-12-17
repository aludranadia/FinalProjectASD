package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Graph {
    private static final int SIZE = 64;
    private int[][] adjacencyMatrix;
    private Node[] nodes;
    private Map<Integer, Integer> shortcuts;
    private Random random;

    public Graph() {
        adjacencyMatrix = new int[SIZE][SIZE];
        nodes = new Node[SIZE];
        shortcuts = new HashMap<>();
        random = new Random();

        initializeNodes();
        initializeAdjacencyMatrix();
        generateRandomShortcuts();
    }

    private void initializeNodes() {
        int nodeNumber = 1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                nodes[nodeNumber - 1] = new Node(nodeNumber, row, col);

                // --- GENERATE KOIN ---
                // Node 1 (Start) dan 64 (Finish) tidak ada koin
                if (nodeNumber != 1 && nodeNumber != 64) {
                    int val = 0;
                    // Pastikan nilai tidak 0 (harus ada efek plus atau minus)
                    while (val == 0) {
                        // Random antara -5 sampai +10
                        val = random.nextInt(16) - 5;
                    }
                    nodes[nodeNumber - 1].setCoinValue(val);
                }

                nodeNumber++;
            }
        }
    }

    // --- METHOD LAIN TETAP SAMA ---
    private void initializeAdjacencyMatrix() {
        for (int i = 0; i < SIZE; i++) for (int j = 0; j < SIZE; j++) adjacencyMatrix[i][j] = 0;
        for (int i = 0; i < SIZE - 1; i++) { adjacencyMatrix[i][i + 1] = 1; adjacencyMatrix[i + 1][i] = 1; }
    }
    public void generateRandomShortcuts() {
        shortcuts.clear();
        int count = 0;
        while (count < 5) {
            int startNode = random.nextInt(SIZE - 2) + 2;
            if (shortcuts.containsKey(startNode)) continue;
            int jumpDistance = random.nextInt(11) + 5;
            int endNode = startNode + jumpDistance;
            if (endNode <= SIZE) { shortcuts.put(startNode, endNode); count++; }
        }
    }
    public Map<Integer, Integer> getShortcuts() { return shortcuts; }
    public boolean isPrime(int num) {
        if (num <= 1) return false;
        for (int i = 2; i <= Math.sqrt(num); i++) if (num % i == 0) return false;
        return true;
    }
    public int getShortcutDestination(int startNode) { return shortcuts.getOrDefault(startNode, -1); }
    public Node getNode(int number) { if (number >= 1 && number <= SIZE) return nodes[number - 1]; return null; }
}
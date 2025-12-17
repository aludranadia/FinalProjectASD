package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Graph {
    private static final int SIZE = 64;
    private int[][] adjacencyMatrix;
    private Node[] nodes;
    private Map<Integer, Integer> shortcuts; // Key: Node Awal, Value: Node Tujuan
    private Random random;

    public Graph() {
        adjacencyMatrix = new int[SIZE][SIZE];
        nodes = new Node[SIZE];
        shortcuts = new HashMap<>();
        random = new Random();
        initializeNodes();
        initializeAdjacencyMatrix();
        // Generate shortcuts saat graph dibuat pertama kali
        generateRandomShortcuts();
    }

    // Inisialisasi 64 nodes dengan posisi grid 8x8
    private void initializeNodes() {
        int nodeNumber = 1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                nodes[nodeNumber - 1] = new Node(nodeNumber, row, col);
                nodeNumber++;
            }
        }
    }

    // Inisialisasi adjacency matrix untuk koneksi linear (1→2→3→...→64)
    private void initializeAdjacencyMatrix() {
        // Reset semua koneksi
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                adjacencyMatrix[i][j] = 0;
            }
        }

        // Buat koneksi linear antar node berurutan
        for (int i = 0; i < SIZE - 1; i++) {
            adjacencyMatrix[i][i + 1] = 1;  // Node i terhubung ke node i+1
            adjacencyMatrix[i + 1][i] = 1;  // Bidirectional (untuk mundur)
        }
    }

    public void generateRandomShortcuts() {
        shortcuts.clear();
        int count = 0;
        while (count < 5) {
            // Pilih node awal acak (jangan node terakhir, min node 2 agar tidak langsung loncat dari start)
            int startNode = random.nextInt(SIZE - 2) + 2;

            // Pastikan node awal belum punya shortcut
            if (shortcuts.containsKey(startNode)) continue;

            // Pilih jarak lompatan acak (min 5 langkah, max 15 langkah) agar terasa dampaknya
            int jumpDistance = random.nextInt(11) + 5;
            int endNode = startNode + jumpDistance;

            // Pastikan node tujuan valid (tidak melebihi 64)
            if (endNode <= SIZE) {
                shortcuts.put(startNode, endNode);
                count++;
                // System.out.println("Shortcut created: " + startNode + " -> " + endNode); // Debugging
            }
        }
    }

    public Map<Integer, Integer> getShortcuts() {
        return shortcuts;
    }

    public int getShortcutDestination(int startNode) {
        return shortcuts.getOrDefault(startNode, -1);
    }

    // Cek apakah ada koneksi antara 2 node
    public boolean isConnected(int node1, int node2) {
        return adjacencyMatrix[node1 - 1][node2 - 1] == 1;
    }

    // Get node berdasarkan nomor
    public Node getNode(int number) {
        if (number >= 1 && number <= SIZE) {
            return nodes[number - 1];
        }
        return null;
    }

    // Get total nodes
    public int getTotalNodes() {
        return SIZE;
    }

    // Get adjacency matrix
    public int[][] getAdjacencyMatrix() {
        return adjacencyMatrix;
    }

    // Get all nodes
    public Node[] getNodes() {
        return nodes;
    }

    // Display adjacency matrix (untuk debugging)
    public void displayMatrix() {
        System.out.println("Adjacency Matrix:");
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                System.out.print(adjacencyMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}
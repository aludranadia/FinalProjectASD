package model;

public class Graph {
    private static final int SIZE = 64;
    private int[][] adjacencyMatrix;
    private Node[] nodes;

    public Graph() {
        adjacencyMatrix = new int[SIZE][SIZE];
        nodes = new Node[SIZE];
        initializeNodes();
        initializeAdjacencyMatrix();
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

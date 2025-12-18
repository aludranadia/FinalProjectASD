package maze;

import maze.view.MazePanel;
import javax.swing.*;

public class MainGame2 {
    public static void main(String[] args) {
        // Menjalankan UI di Event Dispatch Thread (Standard Swing)
        SwingUtilities.invokeLater(() -> {
            // 1. Buat Frame (Jendela Aplikasi)
            JFrame frame = new JFrame("Maze Graph Solver - BFS/DFS/Dijkstra/A*");

            // 2. Setting Frame
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 750); // Ukuran window
            frame.setLocationRelativeTo(null); // Posisi tengah layar
            frame.setResizable(false); // Agar layout tidak berantakan

            // 3. Tambahkan Panel Game Maze yang sudah kita buat
            frame.add(new MazePanel());

            // 4. Tampilkan
            frame.setVisible(true);
        });
    }
}
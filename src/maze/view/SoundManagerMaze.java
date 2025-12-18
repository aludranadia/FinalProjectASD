package maze.view;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundManagerMaze {
    private Map<String, Clip> soundMap;
    // Clip khusus untuk BGM agar mudah dikontrol secara spesifik
    private Clip currentBGM;

    private static final float BGM_VOLUME = -10.0f;
    private static final float SFX_VOLUME = -5.0f;

    public SoundManagerMaze() {
        soundMap = new HashMap<>();
        loadSound("intro", "resources/maze/sounds/maze_intro.wav");
        loadSound("game", "resources/maze/sounds/maze_game.wav");
        loadSound("click", "resources/maze/sounds/button_click.wav");
        loadSound("scan", "resources/maze/sounds/scan.wav");
        loadSound("success", "resources/maze/sounds/success.wav");
    }

    private void loadSound(String name, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                soundMap.put(name, clip);
            } else {
                System.err.println("Sound file not found: " + path);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playBGM(String name) {
        stopAll(); // Pastikan tidak ada BGM lain yang jalan

        Clip clip = soundMap.get(name);
        if (clip != null) {
            currentBGM = clip; // Simpan referensi BGM yang sedang aktif
            setVolume(clip, BGM_VOLUME);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    public void playSFX(String name) {
        Clip clip = soundMap.get(name);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            setVolume(clip, SFX_VOLUME);
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void stopAll() {
        // Stop spesifik BGM jika ada
        if (currentBGM != null) {
            if (currentBGM.isRunning()) currentBGM.stop();
            currentBGM.setFramePosition(0);
            currentBGM = null;
        }

        // Stop semua clip lain (SFX) di map untuk keamanan
        for (Clip clip : soundMap.values()) {
            if (clip != null) {
                if (clip.isRunning()) clip.stop();
                clip.setFramePosition(0); // Reset posisi agar siap diputar ulang
            }
        }
    }

    private void setVolume(Clip clip, float db) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(db);
            }
        } catch (Exception ignored) {}
    }
}
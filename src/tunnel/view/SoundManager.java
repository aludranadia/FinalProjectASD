package tunnel.view;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SoundManager {
    private Map<String, Clip> soundMap;
    private Random random;

    private static final float BGM_VOLUME = -10.0f;
    private static final float SFX_VOLUME = -5.0f;

    public SoundManager() {
        soundMap = new HashMap<>();
        random = new Random();

        loadSound("intro_bgm", "resources/tunnel/sounds/intro_bgm.wav");
        loadSound("game_bgm", "resources/tunnel/sounds/game_bgm.wav");
        loadSound("step", "resources/tunnel/sounds/step.wav");
        loadSound("slide", "resources/tunnel/sounds/slide.wav");
        loadSound("win", "resources/tunnel/sounds/win.wav");
        loadSound("bonus", "resources/tunnel/sounds/bonus.wav");
        loadSound("dash", "resources/tunnel/sounds/dash.wav");
        loadSound("point_plus", "resources/tunnel/sounds/coin.wav");
        loadSound("point_minus", "resources/tunnel/sounds/error.wav");
        loadSound("roll", "resources/tunnel/sounds/dice_roll.wav");

        loadSound("click", "resources/maze/sounds/button_click.wav");
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
                System.err.println("Sound file missing: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play(String name) {
        Clip clip = soundMap.get(name);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            setVolume(clip, SFX_VOLUME);
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void playClick() {
        play("click");
    }

    public void playStep() {
        play("step");
    }

    public void playLoop(String name) {
        stopAllBGM();
        Clip clip = soundMap.get(name);
        if (clip != null) {
            setVolume(clip, BGM_VOLUME);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    public void stop(String name) {
        Clip clip = soundMap.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void stopAllBGM() {
        stop("intro_bgm");
        stop("game_bgm");
        stop("win");
    }

    private void setVolume(Clip clip, float db) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(db);
            }
        } catch (Exception e) {
            // Ignore volume error
        }
    }
}
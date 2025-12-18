package main;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManagerMain {
    private Map<String, Clip> soundMap;

    private static final float BGM_VOLUME = -10.0f;
    private static final float SFX_VOLUME = -5.0f;

    public SoundManagerMain() {
        soundMap = new HashMap<>();
        loadSound("bgm", "resources/maze/sounds/menu_bgm.wav");
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playBGM() {
        Clip clip = soundMap.get("bgm");
        if (clip != null) {
            if (clip.isRunning()) return;
            setVolume(clip, BGM_VOLUME);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    public void playClick() {
        Clip clip = soundMap.get("click");
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            setVolume(clip, SFX_VOLUME);
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void stopBGM() {
        Clip clip = soundMap.get("bgm");
        if (clip != null && clip.isRunning()) {
            clip.stop();
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
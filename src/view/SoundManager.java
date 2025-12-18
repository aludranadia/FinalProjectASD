package view;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SoundManager {
    private Map<String, Clip> soundMap;
    private Clip[] stepClips;
    private Random random;

    // KONFIGURASI VOLUME
    private static final float BGM_VOLUME = 0.25f;
    private static final float SFX_VOLUME = 0.90f;

    public SoundManager() {
        soundMap = new HashMap<>();
        random = new Random();

        loadSound("intro_bgm", "resources/sounds/intro_bgm.wav"); // Musik Intro
        loadSound("game_bgm", "resources/sounds/game_bgm.wav");   // Musik Main
        loadSound("step", "resources/sounds/step.wav");
        loadSound("slide", "resources/sounds/slide.wav");
        loadSound("win", "resources/sounds/win.wav");
        loadSound("bonus", "resources/sounds/bonus.wav"); // Suara Kelipatan 5
        loadSound("dash", "resources/sounds/dash.wav");   // Suara Shortest Path

        loadSound("point_plus", "resources/sounds/coin.wav");  // Suara dapat poin
        loadSound("point_minus", "resources/sounds/error.wav");  // Suara kurang poin
        loadSound("roll", "resources/sounds/dice_roll.wav");   // Suara kocok dadu
    }

    private void loadSound(String name, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                AudioFormat baseFormat = audioIn.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );
                AudioInputStream decodedAudioIn = AudioSystem.getAudioInputStream(decodedFormat, audioIn);
                Clip clip = AudioSystem.getClip();
                clip.open(decodedAudioIn);
                soundMap.put(name, clip);
            } else {
                // System.err.println("Sound missing: " + path); //untuk debug
            }
        } catch (Exception e) {
            // Silent catch
        }
    }

    public void play(String name) {
        Clip clip = soundMap.get(name);
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            setClipVolume(clip, SFX_VOLUME);
            clip.start();
        }
    }

    public void playStep() {
        play("step");
    }

    public void playLoop(String name) {
        stopAllBGM();
        Clip clip = soundMap.get(name);
        if (clip != null) {
            clip.setFramePosition(0);
            setClipVolume(clip, BGM_VOLUME);
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

    private void setClipVolume(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume == 0.0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
                if (dB < -80.0f) dB = -80.0f;
                gainControl.setValue(dB);
            }
        } catch (Exception e) {
            // Ignore volume error
        }
    }
}
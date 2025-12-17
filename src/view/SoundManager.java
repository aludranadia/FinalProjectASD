package view;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private Map<String, Clip> soundMap;

    // KONFIGURASI VOLUME
    private static final float BGM_VOLUME = 0.25f; // Musik pelan (25%)
    private static final float SFX_VOLUME = 0.95f; // Efek keras (95%)

    public SoundManager() {
        soundMap = new HashMap<>();
        // Pastikan nama file sesuai dengan yang ada di folder resources/sounds/
        loadSound("bgm", "resources/sounds/bgm.wav");
        loadSound("step", "resources/sounds/step.wav");
        loadSound("wind", "resources/sounds/wind.wav");
        loadSound("win", "resources/sounds/win.wav");
    }

    private void loadSound(String name, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                // 1. Ambil Input Stream asli
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                AudioFormat baseFormat = audioIn.getFormat();

                // 2. Buat format baru: 16-bit PCM Signed (Format Standar Java)
                // Ini mengatasi masalah "24 bit not supported"
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16, // Paksa ke 16-bit
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false // Little Endian
                );

                // 3. Konversi stream asli ke format 16-bit
                AudioInputStream decodedAudioIn = AudioSystem.getAudioInputStream(decodedFormat, audioIn);

                // 4. Masukkan ke Clip
                Clip clip = AudioSystem.getClip();
                clip.open(decodedAudioIn);
                soundMap.put(name, clip);
            } else {
                System.err.println("Sound file missing: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error loading sound (" + name + "): " + e.getMessage());
            // Jangan printStackTrace agar log tidak penuh, cukup pesan errornya
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

    public void playLoop(String name) {
        Clip clip = soundMap.get(name);
        if (clip != null && !clip.isRunning()) {
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
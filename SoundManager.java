package mygame;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

public class SoundManager {
    private Clip clip;
    private FloatControl volumeControl;
    private boolean isLoaded;

    public SoundManager(String resourcePath) {
        try {
            URL soundURL = getClass().getResource(resourcePath);
            if (soundURL == null) {
                System.err.println("Sound resource not found: " + resourcePath);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            isLoaded = true;
            
            // Try to get a volume control (MASTER_GAIN)
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play(boolean loop) {
        if (!isLoaded) return;
        stop(); // stop if already playing
        clip.setFramePosition(0);
        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.loop(0);
        }
        clip.start();
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void close() {
        stop();
        if (clip != null) {
            clip.close();
        }
    }

    /**
     * Set volume in range 0.0 (mute) to 1.0 (max).
     * If the system doesn't support volume control, this does nothing.
     */
    public void setVolume(float volume) {
        if (volumeControl == null) return;  // No volume control
        if (volume < 0f) volume = 0f;
        if (volume > 1f) volume = 1f;

        // Convert 0.0->1.0 range to decibels
        float minDb = -80.0f;  // or whatever suits you
        float maxDb = 6.0f;    // a little boost above 0 dB if desired
        float dbValue = minDb + (maxDb - minDb) * volume;
        volumeControl.setValue(dbValue);
    }
}

package Audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;

// Every sound in the game is synthesized on the fly (short sine-wave blips
// with a decay envelope) so no external audio asset files are needed.
public final class SoundManager {
    private static final int SAMPLE_RATE = 44100;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);

    private static final byte[] CLICK = tone(1100, 30, 0.4);
    private static final byte[] SUCCESS = chime();
    private static final byte[] FAIL = tone(180, 220, 0.35);
    private static final byte[] CARD_TICK = tone(1500, 45, 0.32);
    private static final byte[] JOKER_ACTIVATE = twoTone(700, 60, 1050, 100, 0.35);
    private static final byte[] COMBINE_HIT = twoTone(500, 50, 1400, 160, 0.4);

    private SoundManager() {}

    public static void playClick() {
        play(CLICK);
    }

    public static void playSuccess() {
        play(SUCCESS);
    }

    public static void playFail() {
        play(FAIL);
    }

    // A single scoring card ticking its chip value into the counter.
    public static void playCardTick() {
        play(CARD_TICK);
    }

    // A Joker (or Hero) firing during the scoring sequence.
    public static void playJokerActivate() {
        play(JOKER_ACTIVATE);
    }

    // The final chips x mult combine at the end of a scoring sequence.
    public static void playCombineHit() {
        play(COMBINE_HIT);
    }

    private static void play(byte[] data) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMAT, data, 0, data.length);
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (LineUnavailableException | IllegalArgumentException ex) {
            // No audio device available (e.g. headless run) - fail silently.
        }
    }

    private static byte[] tone(double freqHz, int durationMs, double volume) {
        int samples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] data = new byte[samples];
        for (int i = 0; i < samples; i++) {
            double t = i / (double) SAMPLE_RATE;
            double envelope = 1.0 - (i / (double) samples);
            double sample = Math.sin(2 * Math.PI * freqHz * t) * envelope * volume;
            data[i] = (byte) Math.round(sample * 127);
        }
        return data;
    }

    private static byte[] chime() {
        return twoTone(880, 90, 1320, 160, 0.3);
    }

    private static byte[] twoTone(double freq1, int ms1, double freq2, int ms2, double volume) {
        byte[] a = tone(freq1, ms1, volume);
        byte[] b = tone(freq2, ms2, volume);
        byte[] combined = new byte[a.length + b.length];
        System.arraycopy(a, 0, combined, 0, a.length);
        System.arraycopy(b, 0, combined, a.length, b.length);
        return combined;
    }
}

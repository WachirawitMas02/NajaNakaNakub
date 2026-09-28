package UI;

import java.util.function.Consumer;
import javax.swing.Timer;

// Tiny Swing-Timer-based animation helper: ticks ~60fps for a fixed
// duration, feeding an eased 0->1 progress value to the callback.
public final class Animator {
    private Animator() {}

    public static Timer animate(int durationMs, Consumer<Float> onTick, Runnable onDone) {
        long start = System.currentTimeMillis();
        Timer timer = new Timer(15, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start;
            float t = Math.min(1f, elapsed / (float) durationMs);
            onTick.accept(easeOutCubic(t));
            if (t >= 1f) {
                ((Timer) e.getSource()).stop();
                if (onDone != null) {
                    onDone.run();
                }
            }
        });
        timer.start();
        return timer;
    }

    private static float easeOutCubic(float t) {
        float f = t - 1f;
        return f * f * f + 1f;
    }
}

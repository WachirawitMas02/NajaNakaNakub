package UI;

import java.util.List;
import javax.swing.Timer;

// Runs a list of steps one at a time, each after a fixed delay from the
// previous, then invokes onDone. Used to replay a scoring event timeline
// (base -> card -> card -> joker -> combine) instead of applying it all at
// once, so each step gets its own moment on screen.
public final class Sequencer {
    private Sequencer() {}

    public static void run(List<Runnable> steps, int stepDelayMs, Runnable onDone) {
        runFrom(steps, 0, stepDelayMs, onDone);
    }

    private static void runFrom(List<Runnable> steps, int index, int stepDelayMs, Runnable onDone) {
        if (index >= steps.size()) {
            if (onDone != null) {
                onDone.run();
            }
            return;
        }
        steps.get(index).run();
        Timer timer = new Timer(stepDelayMs, e -> runFrom(steps, index + 1, stepDelayMs, onDone));
        timer.setRepeats(false);
        timer.start();
    }
}

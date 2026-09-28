package Engine;

import GameModel.PokerHandType;
import java.util.ArrayList;
import java.util.List;

// A snapshot of how a played hand's score was built: the final numbers, plus
// the ordered event timeline (base -> cards -> hero -> jokers -> combine) so
// the UI can replay the scoring sequence step by step.
public class ScoreResult {
    private final PokerHandType handType;
    private final int finalChips;
    private final double finalMult;
    private final int finalScore;
    private final List<ScoreEvent> events = new ArrayList<>();

    public ScoreResult(PokerHandType handType, int finalChips, double finalMult, int finalScore) {
        this.handType = handType;
        this.finalChips = finalChips;
        this.finalMult = finalMult;
        this.finalScore = finalScore;
    }

    public void addEvent(ScoreEvent event) {
        events.add(event);
    }

    public PokerHandType getHandType() {
        return handType;
    }

    public int getFinalChips() {
        return finalChips;
    }

    public double getFinalMult() {
        return finalMult;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public List<ScoreEvent> getEvents() {
        return events;
    }
}

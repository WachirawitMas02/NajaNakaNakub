package Engine;

import GameModel.Card;
import Modifiers.Joker;

// One step in a played hand's scoring sequence, in the order it happened -
// base hand value, then each scoring card, then the Hero (if it fired), then
// each Joker (if it fired), then the final combine. The UI replays these one
// at a time (highlighting whichever card/joker triggered it) instead of just
// jumping straight to the final number.
public class ScoreEvent {
    public enum Type { BASE, CARD, HERO, JOKER, COMBINE }

    private final Type type;
    private final String label;
    private final Card card;
    private final Joker joker;
    private final int chips;
    private final double mult;

    public ScoreEvent(Type type, String label, Card card, Joker joker, int chips, double mult) {
        this.type = type;
        this.label = label;
        this.card = card;
        this.joker = joker;
        this.chips = chips;
        this.mult = mult;
    }

    public Type getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public Card getCard() {
        return card;
    }

    public Joker getJoker() {
        return joker;
    }

    // Running chip total immediately after this event is applied.
    public int getChips() {
        return chips;
    }

    // Running mult total immediately after this event is applied (already
    // includes xMult once combined, for the final COMBINE event only).
    public double getMult() {
        return mult;
    }
}

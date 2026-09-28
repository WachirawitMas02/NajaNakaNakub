package GameModel;

// The Balatro-style scoring table: every poker hand has a base chip value
// and a base multiplier before any Joker/Hero modifiers are applied.
public enum PokerHandType {
    HIGH_CARD("High Card", 5, 1),
    PAIR("Pair", 10, 2),
    TWO_PAIR("Two Pair", 20, 2),
    THREE_OF_A_KIND("Three of a Kind", 30, 3),
    STRAIGHT("Straight", 30, 4),
    FLUSH("Flush", 35, 4),
    FULL_HOUSE("Full House", 40, 4),
    FOUR_OF_A_KIND("Four of a Kind", 60, 7),
    STRAIGHT_FLUSH("Straight Flush", 100, 8),
    ROYAL_FLUSH("Royal Flush", 100, 8);

    private final String displayName;
    private final int baseChips;
    private final int baseMult;

    PokerHandType(String displayName, int baseChips, int baseMult) {
        this.displayName = displayName;
        this.baseChips = baseChips;
        this.baseMult = baseMult;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseChips() {
        return baseChips;
    }

    public int getBaseMult() {
        return baseMult;
    }
}

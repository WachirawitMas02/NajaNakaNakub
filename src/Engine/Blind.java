package Engine;

// The three blind stages faced in every ante, in order.
public enum Blind {
    SMALL("Small Blind", 1.0, 3),
    BIG("Big Blind", 1.5, 4),
    BOSS("Boss Blind", 2.0, 5);

    private final String label;
    private final double scoreMultiplier;
    private final int cashReward;

    Blind(String label, double scoreMultiplier, int cashReward) {
        this.label = label;
        this.scoreMultiplier = scoreMultiplier;
        this.cashReward = cashReward;
    }

    public String getLabel() {
        return label;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    public int getCashReward() {
        return cashReward;
    }

    public Blind next() {
        return this == BOSS ? SMALL : values()[ordinal() + 1];
    }
}

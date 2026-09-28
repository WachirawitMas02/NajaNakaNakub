package Modifiers;

import java.util.ArrayList;
import java.util.List;

// Selectable starting characters. The player picks exactly one per run.
public class HeroFactory {

    public static List<Hero> createChoices() {
        List<Hero> heroes = new ArrayList<>();

        heroes.add(new Hero("Nigga", "just pass nig",
                "+100 Mult whenever the played hand contains an Ace",
                (mult, hand, type) -> hand.containsRank(14) ? mult + 100 : mult));

        heroes.add(new Hero("Kub", "Face Collector",
                "+5 Mult for every face card (J/Q/K) in the played hand",
                (mult, hand, type) -> {
                    long faceCount = hand.getSelectedCards().stream()
                            .filter(c -> c.getRankValue() >= 11 && c.getRankValue() <= 13)
                            .count();
                    return mult + (faceCount * 5);
                }));

        heroes.add(new Hero("big sans", "Steady Hand",
                "+150 Mult on any Straight or Flush (or better)",
                (mult, hand, type) -> {
                    switch (type) {
                        case STRAIGHT:
                        case FLUSH:
                        case FULL_HOUSE:
                        case FOUR_OF_A_KIND:
                        case STRAIGHT_FLUSH:
                        case ROYAL_FLUSH:
                            return mult + 150;
                        default:
                            return mult;
                    }
                }));

        heroes.add(new Hero("Mekhala", "Small Blessing",
                "+2 Mult for every card in the played hand",
                (mult, hand, type) -> mult + (hand.getSelectedCards().size() * 2)));

        return heroes;
    }
}

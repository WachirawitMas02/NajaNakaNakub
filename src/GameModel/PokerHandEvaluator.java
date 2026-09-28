package GameModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Determines the best standard poker hand formed by a set of 1-5 selected cards.
public class PokerHandEvaluator {

    public static PokerHandType evaluate(List<Card> selectedCards) {
        if (selectedCards == null || selectedCards.isEmpty()) {
            return PokerHandType.HIGH_CARD;
        }

        Map<Integer, Integer> rankCounts = rankCounts(selectedCards);
        Map<String, Integer> suitCounts = new HashMap<>();
        for (Card card : selectedCards) {
            suitCounts.merge(card.getSuit(), 1, Integer::sum);
        }

        boolean isFlush = selectedCards.size() == 5 && suitCounts.size() == 1;
        boolean isStraight = isStraight(rankCounts.keySet());

        int highestCount = 1;
        int pairCount = 0;
        boolean hasThree = false;
        boolean hasFour = false;
        for (int count : rankCounts.values()) {
            highestCount = Math.max(highestCount, count);
            if (count == 2) pairCount++;
            if (count == 3) hasThree = true;
            if (count == 4) hasFour = true;
        }

        if (isStraight && isFlush) {
            boolean isRoyal = rankCounts.containsKey(10) && rankCounts.containsKey(14)
                    && rankCounts.containsKey(11) && rankCounts.containsKey(12) && rankCounts.containsKey(13);
            return isRoyal ? PokerHandType.ROYAL_FLUSH : PokerHandType.STRAIGHT_FLUSH;
        }
        if (hasFour) return PokerHandType.FOUR_OF_A_KIND;
        if (hasThree && pairCount >= 1) return PokerHandType.FULL_HOUSE;
        if (isFlush) return PokerHandType.FLUSH;
        if (isStraight) return PokerHandType.STRAIGHT;
        if (hasThree) return PokerHandType.THREE_OF_A_KIND;
        if (pairCount >= 2) return PokerHandType.TWO_PAIR;
        if (pairCount == 1) return PokerHandType.PAIR;
        return PokerHandType.HIGH_CARD;
    }

    // Only the cards that actually form the hand contribute chips - e.g. a
    // High Card hand scores just its single best card, and a Pair scores
    // only the paired two, never the kickers alongside them. Straight/Flush/
    // Full House/Straight Flush/Royal Flush require all 5 selected cards to
    // form the hand in the first place, so every card counts for those.
    public static List<Card> selectScoringCards(List<Card> selectedCards, PokerHandType handType) {
        switch (handType) {
            case STRAIGHT:
            case FLUSH:
            case FULL_HOUSE:
            case STRAIGHT_FLUSH:
            case ROYAL_FLUSH:
                return new ArrayList<>(selectedCards);
            case FOUR_OF_A_KIND:
                return cardsWithRankCount(selectedCards, 4);
            case THREE_OF_A_KIND:
                return cardsWithRankCount(selectedCards, 3);
            case PAIR:
            case TWO_PAIR:
                // A lone pair has exactly one rank with count 2 (2 cards); two
                // pair has two such ranks (4 cards) - the same filter covers both.
                return cardsWithRankCount(selectedCards, 2);
            case HIGH_CARD:
            default:
                return highestCard(selectedCards);
        }
    }

    private static Map<Integer, Integer> rankCounts(List<Card> cards) {
        Map<Integer, Integer> counts = new TreeMap<>();
        for (Card card : cards) {
            counts.merge(card.getRankValue(), 1, Integer::sum);
        }
        return counts;
    }

    private static List<Card> cardsWithRankCount(List<Card> selectedCards, int targetCount) {
        Map<Integer, Integer> counts = rankCounts(selectedCards);
        List<Card> result = new ArrayList<>();
        for (Card card : selectedCards) {
            if (counts.get(card.getRankValue()) == targetCount) {
                result.add(card);
            }
        }
        return result;
    }

    private static List<Card> highestCard(List<Card> selectedCards) {
        Card best = null;
        for (Card card : selectedCards) {
            if (best == null || card.getRankValue() > best.getRankValue()) {
                best = card;
            }
        }
        List<Card> result = new ArrayList<>();
        if (best != null) {
            result.add(best);
        }
        return result;
    }

    // Straight requires exactly 5 distinct, consecutive ranks. Ace (14) can also
    // play low to complete A-2-3-4-5 (treated as 5-high).
    private static boolean isStraight(java.util.Set<Integer> distinctRanks) {
        if (distinctRanks.size() != 5) {
            return false;
        }
        int min = java.util.Collections.min(distinctRanks);
        int max = java.util.Collections.max(distinctRanks);
        if (max - min == 4) {
            return true;
        }
        // Ace-low straight: ranks are {2,3,4,5,14}
        return distinctRanks.contains(14) && distinctRanks.contains(2) && distinctRanks.contains(3)
                && distinctRanks.contains(4) && distinctRanks.contains(5);
    }
}

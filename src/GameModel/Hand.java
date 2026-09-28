package GameModel;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    // The maximum cards a player can select to score in Balatro is usually 5
    private static final int MAX_SELECTED = 5;

    // All cards currently in the player's holding hand
    private final List<Card> cards;
    
    // The subset of cards the player clicked to play/score
    private final List<Card> selectedCards;

    public Hand() {
        this.cards = new ArrayList<>();
        this.selectedCards = new ArrayList<>();
    }

    // Add a card drawn from the deck
    public void addCard(Card card) {
        if (card != null) {
            cards.add(card);
        }
    }

    // Toggle selection when player clicks a card
    public boolean selectCard(Card card) {
        if (selectedCards.contains(card)) {
            selectedCards.remove(card);
            return false; // Now deselected
        } else {
            if (selectedCards.size() < MAX_SELECTED) {
                selectedCards.add(card);
                return true; // Now selected
            }
            return false; // Reached selection limit (e.g., 5 cards)
        }
    }

    // Clear selected cards after playing or discarding
    public void removeSelectedCards() {
        cards.removeAll(selectedCards);
        selectedCards.clear();
    }

    // Check if the hand contains a specific rank (useful for Joker/Hero passives)
    public boolean containsRank(int rankValue) {
        for (Card card : selectedCards) {
            if (card.getRankValue() == rankValue) {
                return true;
            }
        }
        return false;
    }

    // Getters
    public List<Card> getCards() {
        return cards;
    }

    public List<Card> getSelectedCards() {
        return selectedCards;
    }

    public int getCardCount() {
        return cards.size();
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameModel;

public class Card {
    // Suit constants or strings: "Hearts", "Diamonds", "Clubs", "Spades"
    private final String suit;
    
    // Rank: "2", "3", ... "10", "Jack", "Queen", "King", "Ace"
    private final String rank;
    
    // Numeric value for poker logic (2 = 2, Jack = 11, Queen = 12, King = 13, Ace = 14)
    private final int rankValue;
    
    // Base chips awarded when scored (e.g., Ace = 11 chips, Face cards = 10 chips)
    private final int chipValue;

    // Constructor
    public Card(String suit, String rank, int rankValue, int chipValue) {
        this.suit = suit;
        this.rank = rank;
        this.rankValue = rankValue;
        this.chipValue = chipValue;
    }

    // Getters (Encapsulation: private variables exposed safely via methods)
    public String getSuit() {
        return suit;
    }

    public String getRank() {
        return rank;
    }

    public int getRankValue() {
        return rankValue;
    }

    public int getChipValue() {
        return chipValue;
    }

    // Helper method to automatically find the image in your assets folder
    // e.g. "ace_of_spades.png" or "hearts_10.png"
    public String getImageFileName() {
        return suit.toLowerCase() + "_" + rank.toLowerCase() + ".png";
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card other = (Card) obj;
        return this.rankValue == other.rankValue &&
               this.suit != null && this.suit.equalsIgnoreCase(other.suit);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(suit != null ? suit.toLowerCase() : "", rankValue);
    }
}

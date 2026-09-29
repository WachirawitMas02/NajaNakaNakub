/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package GameModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards= new ArrayList<>();
    public Deck() {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        
        for (String suit : suits) {
            for (int i = 0; i < ranks.length; i++) {
                int rankValue = i + 2; // '2' is 2, 'Ace' is 14
                int chipValue = (rankValue >= 10 && rankValue <= 13) ? 10 : (rankValue == 14 ? 11 : rankValue);

                cards.add(new Card(suit, ranks[i], rankValue, chipValue));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (!cards.isEmpty()) {
            return cards.remove(0);
        }
        return null; // Deck is empty
    }
    private void SAVE(){
        System.out.println("HI");
    }
}

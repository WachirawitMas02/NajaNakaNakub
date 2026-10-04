package Modifiers;

import GameModel.PokerHandType;
import java.util.ArrayList;
import java.util.List;

// Central pool of Joker definitions the shop draws from.
public class JokerFactory {

    public static List<Joker> createPool() {
        List<Joker> pool = new ArrayList<>();

        // Flat, always-on bonuses
        pool.add(new Joker("Joker", "+4 Mult", 0, 4, 1.0, 3,"/Assets/Jokers/Jokerplaceholder.png"));
        pool.add(new Joker("Chip Stack", "+30 Chips", 30, 0, 1.0, 4,"/Assets/Jokers/badman.png"));
        pool.add(new Joker("Greedy Multiplier", "+8 Mult", 0, 8, 1.0, 6,"/Assets/Jokers/doalonja.png"));
        pool.add(new Joker("Golden Ticket", "x1.5 Mult", 0, 0, 1.5, 6,"/Assets/Jokers/bakambe.png"));
        pool.add(new Joker("Steel Plate", "+50 Chips", 50, 0, 1.0, 5,"/Assets/Jokers/fools.png"));
        pool.add(new Joker("Big Mult", "x2 Mult", 0, 0, 2.0, 8,"/Assets/Jokers/jaian.png"));
        pool.add(new Joker("99", "x99 Bonus&Multi", 99, 9.9, 9.9, 9,"/Assets/Jokers/nai99.png"));
        pool.add(new Joker("J POT", "+888 Chip&Multi", 888, 888, 8.8, 88,"/Assets/Jokers/CJEK.png"));


        // Hand-type conditional bonuses
        pool.add(new Joker("Pair Hunter", "+20 Chips if hand is a Pair", 20, 0, 1.0, 3,"/Assets/Jokers/Jokerplaceholder.png",
                PokerHandType.PAIR));
        pool.add(new Joker("Two Pair Booster", "+15 Mult if hand is Two Pair", 0, 15, 1.0, 5,"/Assets/Jokers/Jokerplaceholder.png",
                PokerHandType.TWO_PAIR));
        pool.add(new Joker("Triplicate", "+25 Mult on Three of a Kind", 0, 25, 1.0, 5,"/Assets/Jokers/Jokerplaceholder.png",
                PokerHandType.THREE_OF_A_KIND));
        pool.add(new Joker("Straight Shooter", "+40 Chips on a Straight", 40, 0, 1.0, 5,"/Assets/Jokers/Jokerplaceholder.png",
                PokerHandType.STRAIGHT));
        pool.add(new Joker("Flush Fanatic", "x2 Mult on a Flush", 0, 0, 2.0, 6,"/Assets/Jokers/Jokerplaceholder.png",
                PokerHandType.FLUSH));
        pool.add(new Joker("Full House Feast", "+50 Chips on a Full House", 50, 0, 1.0, 6,"/Assets/Jokers/Jokerplaceholder.png",
                PokerHandType.FULL_HOUSE));
        pool.add(new Joker("Quad Damage", "x3 Mult on Four of a Kind", 0, 0, 3.0, 8,"/Assets/Jokers/Jokerplaceholder.png",
                PokerHandType.FOUR_OF_A_KIND));
        pool.add(new Joker("Royalty", "x4 Mult on Straight/Royal Flush", 0, 0, 4.0, 10,"/Assets/Jokers/justoo.png",
                PokerHandType.STRAIGHT_FLUSH));

        return pool;
    }
}

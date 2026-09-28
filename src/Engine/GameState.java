package Engine;

import GameModel.Card;
import GameModel.Deck;
import GameModel.Hand;
import GameModel.PokerHandEvaluator;
import GameModel.PokerHandType;
import Modifiers.Hero;
import Modifiers.Joker;
import java.util.ArrayList;
import java.util.List;

// Owns the entire state of one run: money, ante/blind progress, the current
// hand of cards, and the player's Jokers/Hero. This is the single source of
// truth the UI reads from and calls into.
public class GameState {

    public static final int MAX_ANTE = 8;
    public static final int HAND_SIZE = 8;
    public static final int STARTING_HANDS = 4;
    public static final int STARTING_DISCARDS = 3;
    public static final int JOKER_SLOTS = 5;
    public static final int STARTING_MONEY = 4;

    private Deck deck;
    private final Hand hand = new Hand();
    private final List<Joker> jokers = new ArrayList<>();
    private Hero hero;

    private int money;
    private int ante;
    private Blind currentBlind;
    private int handsRemaining;
    private int discardsRemaining;
    private int roundScore;
    private int blindRequirement;

    private boolean gameOver;
    private boolean victory;

    private List<Joker> shopOffers = new ArrayList<>();

    public void startNewRun(Hero chosenHero) {
        this.hero = chosenHero;
        this.jokers.clear();
        this.money = STARTING_MONEY;
        this.ante = 1;
        this.currentBlind = Blind.SMALL;
        this.gameOver = false;
        this.victory = false;
        startBlind();
    }

    public void startBlind() {
        this.deck = new Deck();
        this.deck.shuffle();
        this.hand.getCards().clear();
        this.hand.getSelectedCards().clear();
        this.handsRemaining = STARTING_HANDS;
        this.discardsRemaining = STARTING_DISCARDS;
        this.roundScore = 0;
        this.blindRequirement = computeRequirement(ante, currentBlind);
        refillHand();
    }

    private int computeRequirement(int ante, Blind blind) {
        double base = 300 * Math.pow(1.6, ante - 1);
        double required = base * blind.getScoreMultiplier();
        return (int) (Math.round(required / 10.0) * 10);
    }

    private void refillHand() {
        while (hand.getCardCount() < HAND_SIZE) {
            Card card = deck.drawCard();
            if (card == null) {
                break;
            }
            hand.addCard(card);
        }
    }

    public boolean selectCard(Card card) {
        return hand.selectCard(card);
    }

    // What the currently selected cards evaluate to, before any scoring
    // happens. Deliberately does NOT include card chip values or Joker/Hero
    // bonuses - those only reveal once the player actually commits by
    // pressing Play Hand, via the animated event timeline in playSelected().
    public PokerHandType previewHandType() {
        if (hand.getSelectedCards().isEmpty()) {
            return null;
        }
        return PokerHandEvaluator.evaluate(hand.getSelectedCards());
    }

    // Builds the full scoring timeline for the currently selected cards:
    // base hand value, then each scoring card's chip contribution, then the
    // Hero's effect (if any), then each Joker's effect (if any), then the
    // final combine. Mutates nothing - playSelected() is the only caller
    // that commits the result.
    private ScoreResult computeScore() {
        if (hand.getSelectedCards().isEmpty()) {
            return null;
        }

        PokerHandType handType = PokerHandEvaluator.evaluate(hand.getSelectedCards());
        List<Card> scoringCards = PokerHandEvaluator.selectScoringCards(hand.getSelectedCards(), handType);

        int chips = handType.getBaseChips();
        double mult = handType.getBaseMult();
        double xMult = 1.0;

        List<ScoreEvent> events = new ArrayList<>();
        events.add(new ScoreEvent(ScoreEvent.Type.BASE,
                handType.getDisplayName(), null, null, chips, mult));

        for (Card card : scoringCards) {
            chips += card.getChipValue();
            events.add(new ScoreEvent(ScoreEvent.Type.CARD,
                    "+" + card.getChipValue() + " chips", card, null, chips, mult));
        }

        if (hero != null) {
            double before = mult;
            mult = hero.modifyMult(mult, hand, handType);
            if (mult != before) {
                events.add(new ScoreEvent(ScoreEvent.Type.HERO,
                        hero.getName(), null, null, chips, mult));
            }
        }

        for (Joker joker : jokers) {
            int chipsBefore = chips;
            double multBefore = mult;
            double xMultBefore = xMult;
            chips = joker.modifyChips(chips, hand, handType);
            mult = joker.modifyMult(mult, hand, handType);
            xMult = joker.modifyXMult(xMult, hand, handType);
            if (chips != chipsBefore || mult != multBefore || xMult != xMultBefore) {
                events.add(new ScoreEvent(ScoreEvent.Type.JOKER,
                        joker.getName(), null, joker, chips, mult));
            }
        }

        double finalMult = mult * xMult;
        int finalScore = (int) Math.round(chips * finalMult);
        events.add(new ScoreEvent(ScoreEvent.Type.COMBINE,
                "= " + finalScore, null, null, chips, finalMult));

        ScoreResult result = new ScoreResult(handType, chips, finalMult, finalScore);
        events.forEach(result::addEvent);
        return result;
    }

    // Plays the currently selected cards: scores them, banks the score,
    // spends a hand, then removes/refills the hand.
    public ScoreResult playSelected() {
        if (hand.getSelectedCards().isEmpty() || handsRemaining <= 0) {
            return null;
        }

        ScoreResult result = computeScore();
        roundScore += result.getFinalScore();
        handsRemaining--;

        hand.removeSelectedCards();
        refillHand();

        if (isRoundLost()) {
            gameOver = true;
        }

        return result;
    }

    public boolean discardSelected() {
        if (hand.getSelectedCards().isEmpty() || discardsRemaining <= 0) {
            return false;
        }
        hand.removeSelectedCards();
        refillHand();
        discardsRemaining--;
        return true;
    }

    public boolean isRoundWon() {
        return roundScore >= blindRequirement;
    }

    public boolean isRoundLost() {
        return !isRoundWon() && handsRemaining <= 0;
    }

    // Called when the player has beaten the blind: pays out reward + interest
    // and advances the blind/ante counters. Returns true if this was the last
    // boss blind of the run (victory).
    public boolean collectReward() {
        int interest = Math.min(5, money / 5);
        money += currentBlind.getCashReward() + interest;

        if (currentBlind == Blind.BOSS) {
            ante++;
            currentBlind = Blind.SMALL;
            if (ante > MAX_ANTE) {
                victory = true;
                return true;
            }
        } else {
            currentBlind = currentBlind.next();
        }
        return false;
    }

    public List<Joker> generateShopOffers(int count) {
        List<Joker> pool = Modifiers.JokerFactory.createPool();
        java.util.Collections.shuffle(pool);
        shopOffers = pool.subList(0, Math.min(count, pool.size()));
        return shopOffers;
    }

    public boolean buyJoker(Joker joker) {
        if (joker == null || money < joker.getPrice() || jokers.size() >= JOKER_SLOTS) {
            return false;
        }
        money -= joker.getPrice();
        jokers.add(joker);
        shopOffers.remove(joker);
        return true;
    }

    // --- Getters for the UI ---

    public Hand getHand() {
        return hand;
    }

    public List<Joker> getJokers() {
        return jokers;
    }

    public Hero getHero() {
        return hero;
    }

    public int getMoney() {
        return money;
    }

    public int getAnte() {
        return ante;
    }

    public Blind getCurrentBlind() {
        return currentBlind;
    }

    public int getHandsRemaining() {
        return handsRemaining;
    }

    public int getDiscardsRemaining() {
        return discardsRemaining;
    }

    public int getRoundScore() {
        return roundScore;
    }

    public int getBlindRequirement() {
        return blindRequirement;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isVictory() {
        return victory;
    }

    public List<Joker> getShopOffers() {
        return shopOffers;
    }
}

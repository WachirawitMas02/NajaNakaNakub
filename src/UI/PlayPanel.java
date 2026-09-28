package UI;

import Audio.SoundManager;
import Engine.GameState;
import Engine.ScoreEvent;
import Engine.ScoreResult;
import GameModel.Card;
import GameModel.PokerHandType;
import Modifiers.Joker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

// The main gameplay screen: blind/score header, joker slots, a live score
// calculator, the player's hand of cards, and the play/discard/sort controls.
public class PlayPanel extends BackdropPanel {

    private static final int SCORE_STEP_DELAY_MS = 320;

    private final GameState state;
    private final Runnable onRoundWon;
    private final Runnable onGameLost;

    private final Map<Card, CardView> cardViews = new LinkedHashMap<>();
    private final Map<Joker, JokerSlotView> jokerSlotViews = new LinkedHashMap<>();

    private final JLabel blindLabel = new JLabel();
    private final JLabel targetLabel = new JLabel();
    private final JLabel scoreLabel = new JLabel();
    private final JLabel handsLabel = new JLabel();
    private final JLabel discardsLabel = new JLabel();
    private final JLabel moneyLabel = new JLabel();
    private final JLabel lastPlayLabel = new JLabel(" ");

    private final JLabel handTypeLabel = new JLabel("Select cards");
    private final BadgeLabel chipsBadge = new BadgeLabel("0", Theme.CHIP_BLUE, java.awt.Color.WHITE,
            Theme.HEADER_FONT.deriveFont(18f));
    private final BadgeLabel multBadge = new BadgeLabel("x0", Theme.MULT_RED, java.awt.Color.WHITE,
            Theme.HEADER_FONT.deriveFont(18f));
    private final JLabel totalLabel = new JLabel("0");

    private final JPanel jokerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
    private final JPanel handRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 10));
    private final StyledButton sortRankButton = new StyledButton("Sort by Rank");
    private final StyledButton sortSuitButton = new StyledButton("Sort by Suit");
    private final StyledButton playButton = new StyledButton("Play Hand");
    private final StyledButton discardButton = new StyledButton("Discard");

    // Running chip/mult values as the scoring animation reveals them, so each
    // step can animate a count-up from the previous step's result.
    private int displayedChips;
    private double displayedMult;

    public PlayPanel(GameState state, Runnable onRoundWon, Runnable onGameLost) {
        this.state = state;
        this.onRoundWon = onRoundWon;
        this.onGameLost = onGameLost;

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridLayout(1, 6, 10, 0));
        header.setOpaque(false);
        for (JLabel label : new JLabel[]{blindLabel, targetLabel, scoreLabel, handsLabel, discardsLabel, moneyLabel}) {
            label.setFont(Theme.HEADER_FONT);
            label.setForeground(Theme.TEXT);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            header.add(label);
        }
        scoreLabel.setForeground(Theme.GOLD);
        moneyLabel.setForeground(Theme.GOLD);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        jokerRow.setOpaque(false);
        jokerRow.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.PANEL_BORDER), "Jokers"));
        ((javax.swing.border.TitledBorder) jokerRow.getBorder()).setTitleColor(Theme.TEXT_DIM);
        jokerRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel calculator = buildCalculator();
        calculator.setAlignmentX(Component.CENTER_ALIGNMENT);

        lastPlayLabel.setFont(Theme.BODY_FONT);
        lastPlayLabel.setForeground(Theme.TEXT_DIM);
        lastPlayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        handRow.setOpaque(false);
        handRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(jokerRow);
        center.add(javax.swing.Box.createVerticalStrut(8));
        center.add(calculator);
        center.add(lastPlayLabel);
        center.add(javax.swing.Box.createVerticalGlue());
        center.add(handRow);
        return center;
    }

    private JPanel buildCalculator() {
        JPanel calc = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        calc.setOpaque(false);

        handTypeLabel.setFont(Theme.HEADER_FONT);
        handTypeLabel.setForeground(Theme.TEXT);

        totalLabel.setFont(Theme.SCORE_FONT);
        totalLabel.setForeground(Theme.GOLD);

        JLabel times = new JLabel("x");
        times.setFont(Theme.SCORE_FONT.deriveFont(20f));
        times.setForeground(Theme.TEXT_DIM);

        JLabel equals = new JLabel("=");
        equals.setFont(Theme.SCORE_FONT.deriveFont(20f));
        equals.setForeground(Theme.TEXT_DIM);

        calc.add(handTypeLabel);
        calc.add(chipsBadge);
        calc.add(times);
        calc.add(multBadge);
        calc.add(equals);
        calc.add(totalLabel);
        return calc;
    }

    private JPanel buildControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        controls.setOpaque(false);

        sortRankButton.addActionListener(e -> {
            state.getHand().getCards().sort(Comparator.comparingInt(Card::getRankValue));
            refresh();
        });

        sortSuitButton.addActionListener(e -> {
            state.getHand().getCards().sort(Comparator.comparing(Card::getSuit)
                    .thenComparingInt(Card::getRankValue));
            refresh();
        });

        discardButton.addActionListener(e -> {
            if (!discardButton.isEnabled()) {
                return;
            }
            setControlsEnabled(false);
            List<Card> toDiscard = new ArrayList<>(state.getHand().getSelectedCards());
            animateExitFor(toDiscard, () -> {
                state.discardSelected();
                lastPlayLabel.setText(" ");
                refresh();
            });
        });

        playButton.addActionListener(e -> {
            if (!playButton.isEnabled()) {
                return;
            }
            setControlsEnabled(false);
            startScoringSequence();
        });

        controls.add(sortRankButton);
        controls.add(sortSuitButton);
        controls.add(discardButton);
        controls.add(playButton);
        return controls;
    }

    private void setControlsEnabled(boolean enabled) {
        sortRankButton.setEnabled(enabled);
        sortSuitButton.setEnabled(enabled);
        playButton.setEnabled(enabled && !state.getHand().getSelectedCards().isEmpty()
                && state.getHandsRemaining() > 0);
        discardButton.setEnabled(enabled && !state.getHand().getSelectedCards().isEmpty()
                && state.getDiscardsRemaining() > 0);
    }

    // Captures the ScoreEvent timeline up front (the actual scoring + hand
    // mutation already happened), then replays it one step at a time -
    // base hand value, each scoring card ticking in, each Joker/Hero firing,
    // then the final combine - before the played cards visually leave and
    // the next hand deals in.
    private void startScoringSequence() {
        List<Card> playedCards = new ArrayList<>(state.getHand().getSelectedCards());
        ScoreResult result = state.playSelected();
        if (result == null) {
            setControlsEnabled(true);
            return;
        }

        lastPlayLabel.setText(" ");
        displayedChips = 0;
        displayedMult = 0;

        List<Runnable> steps = new ArrayList<>();
        for (ScoreEvent event : result.getEvents()) {
            steps.add(() -> applyScoreEvent(event));
        }
        Sequencer.run(steps, SCORE_STEP_DELAY_MS, () -> finishPlay(result, playedCards));
    }

    private void applyScoreEvent(ScoreEvent event) {
        int fromChips = displayedChips;
        double fromMult = displayedMult;

        switch (event.getType()) {
            case BASE:
                handTypeLabel.setText(event.getLabel());
                break;
            case CARD: {
                CardView cv = cardViews.get(event.getCard());
                int added = event.getChips() - fromChips;
                if (cv != null) {
                    cv.pulseScore("+" + added);
                }
                SoundManager.playCardTick();
                break;
            }
            case HERO:
                SoundManager.playJokerActivate();
                break;
            case JOKER: {
                JokerSlotView slot = jokerSlotViews.get(event.getJoker());
                if (slot != null) {
                    slot.pulseActivate();
                }
                SoundManager.playJokerActivate();
                break;
            }
            case COMBINE: {
                SoundManager.playCombineHit();
                int finalScore = (int) Math.round(event.getChips() * event.getMult());
                totalLabel.setText(String.valueOf(finalScore));
                punch(totalLabel, Theme.SCORE_FONT);
                break;
            }
            default:
                break;
        }

        animateChipsBadge(fromChips, event.getChips());
        animateMultBadge(fromMult, event.getMult());
        displayedChips = event.getChips();
        displayedMult = event.getMult();
    }

    private void animateChipsBadge(int from, int to) {
        if (from == to) {
            chipsBadge.setValueQuiet(String.valueOf(to));
            return;
        }
        Animator.animate(220, t -> chipsBadge.setValueQuiet(String.valueOf(Math.round(from + (to - from) * t))),
                chipsBadge::pulse);
    }

    private void animateMultBadge(double from, double to) {
        if (from == to) {
            multBadge.setValueQuiet("x" + trim(to));
            return;
        }
        Animator.animate(220, t -> multBadge.setValueQuiet("x" + trim(from + (to - from) * t)),
                multBadge::pulse);
    }

    // Brief font-size punch used for the final total reveal.
    private void punch(JLabel label, Font baseFont) {
        Animator.animate(240, t -> {
            float scale = 1f + 0.35f * (1f - t);
            label.setFont(baseFont.deriveFont(baseFont.getSize2D() * scale));
        }, () -> label.setFont(baseFont));
    }

    private void finishPlay(ScoreResult result, List<Card> playedCards) {
        lastPlayLabel.setText(result.getHandType().getDisplayName() + "  ->  "
                + result.getFinalChips() + " chips x " + trim(result.getFinalMult())
                + " mult = " + result.getFinalScore() + " pts");

        animateExitFor(playedCards, () -> {
            if (state.isRoundWon()) {
                SoundManager.playSuccess();
                refresh();
                onRoundWon.run();
            } else if (state.isGameOver()) {
                refresh();
                onGameLost.run();
            } else {
                refresh();
            }
        });
    }

    // Animates the given cards' CardViews popping/fading out, then runs
    // onAllDone once every one finishes (or immediately if none are shown).
    private void animateExitFor(List<Card> cards, Runnable onAllDone) {
        List<CardView> exiting = cards.stream()
                .map(cardViews::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (exiting.isEmpty()) {
            onAllDone.run();
            return;
        }
        AtomicInteger remaining = new AtomicInteger(exiting.size());
        for (CardView view : exiting) {
            view.animateExit(() -> {
                if (remaining.decrementAndGet() == 0) {
                    onAllDone.run();
                }
            });
        }
    }

    private static String trim(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        // Mid count-up-animation values are almost never exact integers -
        // cap at 1 decimal instead of printing full double precision.
        return String.format("%.1f", value);
    }

    // Full rebuild: hand row is redealt with a staggered entrance animation.
    // Used whenever the set of cards in hand actually changes (new blind,
    // after playing, after discarding, after sorting).
    public void refresh() {
        rebuildJokerSlots();
        rebuildHandRow();
        refreshStats();
    }

    private void rebuildJokerSlots() {
        jokerRow.removeAll();
        jokerSlotViews.clear();
        List<Joker> jokers = state.getJokers();
        for (int i = 0; i < GameState.JOKER_SLOTS; i++) {
            Joker joker = i < jokers.size() ? jokers.get(i) : null;
            JokerSlotView slot = new JokerSlotView(joker);
            if (joker != null) {
                jokerSlotViews.put(joker, slot);
            }
            jokerRow.add(slot);
        }
        jokerRow.revalidate();
        jokerRow.repaint();
    }

    private void rebuildHandRow() {
        handRow.removeAll();
        cardViews.clear();

        Consumer<Card> onCardClick = this::handleCardClick;
        int index = 0;
        for (Card card : state.getHand().getCards()) {
            CardView view = new CardView(card, onCardClick);
            view.setSelectedImmediate(state.getHand().getSelectedCards().contains(card));
            cardViews.put(card, view);
            handRow.add(view);
            view.playDealAnimation(index * 55);
            index++;
        }
        handRow.revalidate();
        handRow.repaint();
    }

    private void handleCardClick(Card card) {
        state.selectCard(card);
        CardView view = cardViews.get(card);
        if (view != null) {
            view.setSelected(state.getHand().getSelectedCards().contains(card));
        }
        refreshStats();
    }

    // Lightweight update: header numbers, live score preview, and button
    // enabled state. Never rebuilds the hand row, so selecting a card stays
    // smooth instead of re-triggering the deal animation.
    private void refreshStats() {
        blindLabel.setText(state.getCurrentBlind().getLabel() + " (Ante " + state.getAnte() + ")");
        targetLabel.setText("Target: " + state.getBlindRequirement());
        scoreLabel.setText("Score: " + state.getRoundScore());
        handsLabel.setText("Hands: " + state.getHandsRemaining());
        discardsLabel.setText("Discards: " + state.getDiscardsRemaining());
        moneyLabel.setText("$" + state.getMoney());

        // Deliberately just the hand type's base chips/mult here - card
        // values and Joker/Hero bonuses only reveal in the animated
        // sequence once the player actually presses Play Hand.
        PokerHandType previewType = state.previewHandType();
        if (previewType == null) {
            handTypeLabel.setText("Select cards");
            chipsBadge.setValue("0");
            multBadge.setValue("x0");
            totalLabel.setText("0");
        } else {
            handTypeLabel.setText(previewType.getDisplayName());
            chipsBadge.setValue(String.valueOf(previewType.getBaseChips()));
            multBadge.setValue("x" + trim(previewType.getBaseMult()));
            totalLabel.setText(String.valueOf(previewType.getBaseChips() * previewType.getBaseMult()));
        }

        setControlsEnabled(true);
    }
}

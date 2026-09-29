package UI;

import Engine.GameState;
import Engine.ScoreEvent;
import Engine.ScoreResult;
import GameModel.Card;
import GameModel.PokerHandType;
import Modifiers.Joker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class PlayPanel extends BackdropPanel {

    private final GameState state;
    private final Runnable onBlindCleared;
    private final Runnable onGameOver;

    // Layout Panels
    private JPanel leftDashboard;
    private JPanel greenTableFelt;
    private JPanel jokerShelfArea;
    private JPanel handCardArea;
    private JPanel feltBottomArea;

    // Badges (BadgeLabel)
    private BadgeLabel anteBadge;
    private BadgeLabel moneyBadge;
    private BadgeLabel targetScoreBadge;
    private BadgeLabel currentScoreBadge;
    private BadgeLabel liveChipsBadge;
    private BadgeLabel liveMultBadge;
    private BadgeLabel handsBadge;
    private BadgeLabel discardsBadge;

    private JLabel handTitleLabel;

    private final List<CardView> activeCardViews = new ArrayList<>();
    private final List<JokerSlotView> activeJokerSlots = new ArrayList<>();
    private boolean isBusy = false;

    public PlayPanel(GameState state, Runnable onBlindCleared, Runnable onGameOver) {
        this.state = state;
        this.onBlindCleared = onBlindCleared;
        this.onGameOver = onGameOver;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        buildLeftDashboard();
        buildCenterFeltTable();

        add(leftDashboard, BorderLayout.WEST);
        add(greenTableFelt, BorderLayout.CENTER);

        refresh();
        playDealSequence();
    }

    // 1. LEFT DASHBOARD (จัดเรียงแนวตั้งลงมาจริง ๆ ไม่แบนเป็นแถวนอน)
    private void buildLeftDashboard() {
        leftDashboard = new JPanel();
        leftDashboard.setPreferredSize(new Dimension(280, 0));
        leftDashboard.setLayout(new BoxLayout(leftDashboard, BoxLayout.Y_AXIS));
        leftDashboard.setOpaque(false);
        leftDashboard.setBorder(new EmptyBorder(10, 10, 10, 10));

        // กล่อง Ante & Money
        JPanel metaRow = new JPanel(new GridLayout(1, 2, 6, 0));
        metaRow.setOpaque(false);
        metaRow.setMaximumSize(new Dimension(260, 40));
        anteBadge = new BadgeLabel("ANTE 1/8", new Color(130, 70, 200), Color.WHITE, Theme.FONT_BADGE);
        moneyBadge = new BadgeLabel("$4", Theme.GOLD, Color.BLACK, Theme.FONT_BADGE);
        metaRow.add(anteBadge);
        metaRow.add(moneyBadge);
        leftDashboard.add(metaRow);
        leftDashboard.add(Box.createVerticalStrut(15));

        // Target Score
        JLabel goalTitle = new JLabel("TARGET SCORE", SwingConstants.CENTER);
        goalTitle.setFont(Theme.HEADER_FONT);
        goalTitle.setForeground(Theme.TEXT_DIM);
        goalTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftDashboard.add(goalTitle);
        leftDashboard.add(Box.createVerticalStrut(4));

        targetScoreBadge = new BadgeLabel("300", Theme.MULT_RED, Color.WHITE, Theme.FONT_SCORE);
        targetScoreBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        targetScoreBadge.setMaximumSize(new Dimension(260, 50));
        leftDashboard.add(targetScoreBadge);
        leftDashboard.add(Box.createVerticalStrut(15));

        // Current Score
        JLabel currentTitle = new JLabel("CURRENT SCORE", SwingConstants.CENTER);
        currentTitle.setFont(Theme.HEADER_FONT);
        currentTitle.setForeground(Theme.TEXT_DIM);
        currentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftDashboard.add(currentTitle);
        leftDashboard.add(Box.createVerticalStrut(4));

        currentScoreBadge = new BadgeLabel("0", Theme.CHIP_BLUE, Color.WHITE, Theme.FONT_SCORE);
        currentScoreBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentScoreBadge.setMaximumSize(new Dimension(260, 50));
        leftDashboard.add(currentScoreBadge);
        leftDashboard.add(Box.createVerticalStrut(20));

        // Hand Preview Box
        JPanel handBox = new JPanel();
        handBox.setLayout(new BoxLayout(handBox, BoxLayout.Y_AXIS));
        handBox.setBackground(Theme.PANEL_BG);
        handBox.setBorder(BorderFactory.createLineBorder(Theme.PANEL_BORDER, 2));
        handBox.setMaximumSize(new Dimension(260, 95));

        handTitleLabel = new JLabel("SELECT CARDS", SwingConstants.CENTER);
        handTitleLabel.setFont(Theme.FONT_HEADER);
        handTitleLabel.setForeground(Theme.GOLD);
        handTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel chipMultRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        chipMultRow.setOpaque(false);
        liveChipsBadge = new BadgeLabel("0", Theme.CHIP_BLUE, Color.WHITE, Theme.FONT_BADGE);
        JLabel xLabel = new JLabel("X");
        xLabel.setFont(Theme.FONT_BADGE);
        xLabel.setForeground(Theme.TEXT);
        liveMultBadge = new BadgeLabel("0", Theme.MULT_RED, Color.WHITE, Theme.FONT_BADGE);

        chipMultRow.add(liveChipsBadge);
        chipMultRow.add(xLabel);
        chipMultRow.add(liveMultBadge);

        handBox.add(Box.createVerticalStrut(6));
        handBox.add(handTitleLabel);
        handBox.add(chipMultRow);
        handBox.add(Box.createVerticalStrut(4));

        leftDashboard.add(handBox);
        leftDashboard.add(Box.createVerticalGlue()); // ดันส่วน Hands/Discards ลงล่างสุด

        // Hands & Discards
        JPanel badgeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        badgeRow.setOpaque(false);
        badgeRow.setMaximumSize(new Dimension(260, 50));
        handsBadge = new BadgeLabel("HANDS: 4", Theme.CHIP_BLUE, Color.WHITE, Theme.FONT_BADGE);
        discardsBadge = new BadgeLabel("DISC: 3", Theme.DISCARD_ORANGE, Color.WHITE, Theme.FONT_BADGE);
        badgeRow.add(handsBadge);
        badgeRow.add(discardsBadge);
        leftDashboard.add(badgeRow);
    }

    // 2. CENTER FELT TABLE (โต๊ะสักหลาดสีเขียวเต็มจอขวา)
    // In UI/PlayPanel.java:
    // In UI/PlayPanel.java:
    // 2. CENTER FELT TABLE: Single arena where hand and played cards share the space
    // 2. CENTER FELT TABLE
    private void buildCenterFeltTable() {
        greenTableFelt = new JPanel(new BorderLayout(0, 0));
        greenTableFelt.setBackground(Theme.FELT_GREEN);
        greenTableFelt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.FELT_BORDER, 3),
            new EmptyBorder(8, 10, 10, 10)
        ));

        // 1. TOP: Jokers
        jokerShelfArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        jokerShelfArea.setOpaque(false);
        greenTableFelt.add(jokerShelfArea, BorderLayout.NORTH);

        // 2. CENTER: Main Table where hand cards live and fly
        // We use GridBagLayout to pin the cards to the bottom of the center arena
        JPanel tableCenter = new JPanel(new GridBagLayout());
        tableCenter.setOpaque(false);

        // Inside buildCenterFeltTable() in PlayPanel.java:
        handCardArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0)) {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        handCardArea.setOpaque(false);
        // Expand preferred size to match FULL_H (480px) and wide enough for 8 large cards
        handCardArea.setPreferredSize(new Dimension(1100, 480));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(0, 0, 4, 0);

        tableCenter.add(handCardArea, gbc);
        greenTableFelt.add(tableCenter, BorderLayout.CENTER);

        // 3. SOUTH: Buttons
        feltBottomArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        feltBottomArea.setOpaque(false);

        StyledButton discardBtn = new StyledButton("DISCARD", Theme.DISCARD_ORANGE);
        discardBtn.setPreferredSize(new Dimension(160, 46));
        discardBtn.addActionListener(e -> {
            if (!isBusy) handleDiscard();
        });

        StyledButton playHandBtn = new StyledButton("PLAY HAND", Theme.MULT_RED);
        playHandBtn.setPreferredSize(new Dimension(190, 46));
        playHandBtn.addActionListener(e -> {
            if (!isBusy) handlePlayHand();
        });

        feltBottomArea.add(discardBtn);
        feltBottomArea.add(playHandBtn);

        greenTableFelt.add(feltBottomArea, BorderLayout.SOUTH);
    }

    public void refresh() {
        if (state == null) return;

        targetScoreBadge.setValueQuiet(String.valueOf(state.getBlindRequirement()));
        currentScoreBadge.setValueQuiet(String.valueOf(state.getRoundScore()));
        anteBadge.setValueQuiet("ANTE " + state.getAnte() + "/" + GameState.MAX_ANTE);
        moneyBadge.setValueQuiet("$" + state.getMoney());
        handsBadge.setValueQuiet("HANDS: " + state.getHandsRemaining());
        discardsBadge.setValueQuiet("DISC: " + state.getDiscardsRemaining());

        renderJokers();
        renderHandCards();
        updateSelectedHandPreview();

        revalidate();
        repaint();
    }

    private void renderJokers() {
        jokerShelfArea.removeAll();
        activeJokerSlots.clear();

        List<Joker> jokers = state.getJokers();
        for (int i = 0; i < GameState.JOKER_SLOTS; i++) {
            Joker joker = (i < jokers.size()) ? jokers.get(i) : null;
            JokerSlotView slot = new JokerSlotView(joker);
            activeJokerSlots.add(slot);
            jokerShelfArea.add(slot);
        }
    }

    private void renderHandCards() {
        handCardArea.removeAll();
        activeCardViews.clear();

        for (Card card : state.getHand().getCards()) {
            CardView cv = new CardView(card, c -> {
                if (isBusy) return;
                boolean isNowSelected = state.selectCard(c);
                for (CardView view : activeCardViews) {
                    if (view.getCard().equals(c)) {
                        view.setSelected(isNowSelected);
                        break;
                    }
                }
                updateSelectedHandPreview();
            });

            if (state.getHand().getSelectedCards().contains(card)) {
                cv.setSelectedImmediate(true);
            }

            activeCardViews.add(cv);
            handCardArea.add(cv);
        }
        
    }

    private void playDealSequence() {
        int delay = 0;
        for (CardView cv : activeCardViews) {
            cv.playDealAnimation(delay);
            delay += 45;
        }
    }

    private void updateSelectedHandPreview() {
        PokerHandType handType = state.previewHandType();
        if (handType == null) {
            handTitleLabel.setText("SELECT CARDS");
            liveChipsBadge.setValueQuiet("0");
            liveMultBadge.setValueQuiet("0");
        } else {
            handTitleLabel.setText(handType.getDisplayName().toUpperCase());
            liveChipsBadge.setValueQuiet(String.valueOf(handType.getBaseChips()));
            liveMultBadge.setValueQuiet(String.valueOf((int) handType.getBaseMult()));
        }
    }

// PLAY HAND SEQUENCE: พุ่งเข้ากลางโต๊ะ -> Pulse ทีละใบ -> พุ่งออกจากโต๊ะ
    private void handlePlayHand() {
        if (state.getHand().getSelectedCards().isEmpty() || state.getHandsRemaining() <= 0) {
            return;
        }

        isBusy = true;

        // 1. ดึง CardView ที่มีสถานะถูกเลือกอยู่ ณ ปัจจุบันโดยตรง
        List<CardView> playedViews = new ArrayList<>();
        for (CardView cv : activeCardViews) {
            if (cv.isCardSelected()) {
                playedViews.add(cv);
            }
        }

        ScoreResult result = state.playSelected();
        if (result == null) {
            isBusy = false;
            return;
        }

        // 2. สั่งให้การ์ดที่ถูกเลือก พุ่งเข้าสู่กึ่งกลางโต๊ะทันที
        for (CardView cv : playedViews) {
            cv.animatePlayToCenter(null);
        }

        // 3. เตรียม Step การคิดคะแนน
        List<Runnable> steps = new ArrayList<>();

        for (ScoreEvent event : result.getEvents()) {
            steps.add(() -> {
                if (event.getType() == ScoreEvent.Type.CARD && event.getCard() != null) {
                    for (CardView cv : playedViews) {
                        // เปรียบเทียบ rank และ suit ตรงๆ ป้องกัน equals() ทำงานพลาด
                        if (cv.getCard().getRank().equals(event.getCard().getRank()) &&
                            cv.getCard().getSuit().equals(event.getCard().getSuit())) {
                            cv.pulseScore(event.getLabel());
                            break;
                        }
                    }
                } else if (event.getType() == ScoreEvent.Type.JOKER && event.getJoker() != null) {
                    int idx = state.getJokers().indexOf(event.getJoker());
                    if (idx >= 0 && idx < activeJokerSlots.size()) {
                        activeJokerSlots.get(idx).pulseActivate();
                    }
                }

                liveChipsBadge.setValue(String.valueOf(event.getChips()));
                liveMultBadge.setValue(String.valueOf((int) event.getMult()));
            });
        }

        // 4. เมื่อคิดคะแนนครบ ให้การ์ดพุ่งออกจากหน้าจอ
        steps.add(() -> {
            for (CardView cv : playedViews) {
                cv.animateScoreExit(null);
            }
        });

        // 5. หน่วงเวลาให้การ์ดบินเข้าตรงกลางเรียบร้อยก่อน (300ms) แล้วค่อยเริ่มนับแต้มทีละใบ
        Animator.animate(300, t -> {}, () -> {
            Sequencer.run(steps, 240, () -> {
                currentScoreBadge.setValue(String.valueOf(state.getRoundScore()));
                refresh();
                playDealSequence();
                isBusy = false;

                if (state.isRoundWon()) {
                    onBlindCleared.run();
                } else if (state.isGameOver()) {
                    onGameOver.run();
                }
            });
        });
    }

    // DISCARD SEQUENCE: ไพ่หมุนเคว้งและโยนตกลงข้างล่างออกจากโต๊ะ
    private void handleDiscard() {
        if (state.getHand().getSelectedCards().isEmpty() || state.getDiscardsRemaining() <= 0) {
            return;
        }

        isBusy = true;
        List<Card> discarding = new ArrayList<>(state.getHand().getSelectedCards());

        // สั่งให้แต่ละใบหมุนเคว้งด้วยทิศทางสุ่มซ้าย/ขวาเบาๆ
        float dir = -0.6f;
        for (CardView cv : activeCardViews) {
            if (discarding.contains(cv.getCard())) {
                cv.animateDiscardToss(dir, null);
                dir += 0.4f; // ไพ่แต่ละใบจะกระเด็นกระจายทิศทางกันอย่างเป็นธรรมชาติ
            }
        }

        List<Runnable> steps = List.of(
            () -> state.discardSelected()
        );

        // รอให้แอนิเมชันเหวี่ยงไพ่ทิ้งจบแล้วค่อยแจกใบใหม่
        Sequencer.run(steps, 280, () -> {
            refresh();
            playDealSequence();
            isBusy = false;
        });
    }
    private void SAVE(){
        System.out.println("HI");
    }
}
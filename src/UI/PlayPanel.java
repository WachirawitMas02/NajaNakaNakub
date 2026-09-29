package UI;

import Engine.GameState;
import Engine.ScoreResult;
import GameModel.Card;
import GameModel.PokerHandType;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class PlayPanel extends JPanel {
    private final GameState state;
    private final Runnable onBlindCleared;
    private final Runnable onGameOver;

    // Sub-panels
    private JPanel leftDashboard;
    private JPanel topJokerRail;
    private JPanel centerCardArea;
    private JPanel bottomControlDock;

    // Dynamic UI Labels
    private JLabel targetScoreLabel;
    private JLabel currentScoreLabel;
    private JLabel handsLeftBadge;
    private JLabel discardsLeftBadge;
    private JLabel moneyLabel;
    private JLabel anteLabel;
    private JLabel pokerHandPreviewLabel;

    private final List<CardView> activeCardViews = new ArrayList<>();

    public PlayPanel(GameState state, Runnable onBlindCleared, Runnable onGameOver) {
        this.state = state;
        this.onBlindCleared = onBlindCleared;
        this.onGameOver = onGameOver;

        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        buildLeftDashboard();
        buildTopJokerRail();
        buildCenterCardArea();
        buildBottomDock();

        refresh();
    }

    // 1. LEFT DASHBOARD (Score requirement, current score, Hands & Discards)
    private void buildLeftDashboard() {
        leftDashboard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Theme.PANEL_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 22, 22));
                g2d.setColor(Theme.PANEL_BORDER);
                g2d.setStroke(new BasicStroke(3f));
                g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 22, 22));
                g2d.dispose();
            }
        };
        leftDashboard.setPreferredSize(new Dimension(280, 0));
        leftDashboard.setLayout(new BoxLayout(leftDashboard, BoxLayout.Y_AXIS));
        leftDashboard.setOpaque(false);
        leftDashboard.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        // Ante & Money
        JPanel metaRow = new JPanel(new GridLayout(1, 2, 8, 0));
        metaRow.setOpaque(false);
        anteLabel = createStatPill("ANTE 1/8", new Color(150, 80, 220));
        moneyLabel = createStatPill("$4", Theme.GOLD);
        metaRow.add(anteLabel);
        metaRow.add(moneyLabel);
        leftDashboard.add(metaRow);
        leftDashboard.add(Box.createVerticalStrut(25));

        // Target Requirement Box
        JLabel goalTitle = new JLabel("SCORE AT LEAST", SwingConstants.CENTER);
        goalTitle.setFont(Theme.FONT_LABEL);
        goalTitle.setForeground(new Color(200, 200, 200));
        goalTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftDashboard.add(goalTitle);

        targetScoreLabel = new JLabel("300", SwingConstants.CENTER);
        targetScoreLabel.setFont(Theme.FONT_SCORE);
        targetScoreLabel.setForeground(Theme.MULT_RED);
        targetScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftDashboard.add(targetScoreLabel);
        leftDashboard.add(Box.createVerticalStrut(20));

        // Current Score Box
        JLabel currentTitle = new JLabel("ROUND SCORE", SwingConstants.CENTER);
        currentTitle.setFont(Theme.FONT_LABEL);
        currentTitle.setForeground(new Color(200, 200, 200));
        currentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftDashboard.add(currentTitle);

        currentScoreLabel = new JLabel("0", SwingConstants.CENTER);
        currentScoreLabel.setFont(Theme.FONT_SCORE);
        currentScoreLabel.setForeground(Theme.CHIP_BLUE);
        currentScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftDashboard.add(currentScoreLabel);
        leftDashboard.add(Box.createVerticalGlue());

        // Hands & Discards Badges
        JPanel badgeRow = new JPanel(new GridLayout(1, 2, 12, 0));
        badgeRow.setOpaque(false);

        handsLeftBadge = createBadge("HANDS", "4", Theme.CHIP_BLUE);
        discardsLeftBadge = createBadge("DISCARDS", "3", Theme.DISCARD_ORANGE);

        badgeRow.add(handsLeftBadge);
        badgeRow.add(discardsLeftBadge);
        leftDashboard.add(badgeRow);

        add(leftDashboard, BorderLayout.WEST);
    }

    // 2. TOP JOKER RAIL
    private void buildTopJokerRail() {
        topJokerRail = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(28, 32, 42));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2d.dispose();
            }
        };
        topJokerRail.setPreferredSize(new Dimension(0, 130));
        topJokerRail.setOpaque(false);
        add(topJokerRail, BorderLayout.NORTH);
    }

    // 3. CENTER CASINO TABLE (Poker Hand title & cards)
    private void buildCenterCardArea() {
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);

        // Preview Label
        pokerHandPreviewLabel = new JLabel("SELECT CARDS", SwingConstants.CENTER);
        pokerHandPreviewLabel.setFont(new Font("Impact", Font.PLAIN, 24));
        pokerHandPreviewLabel.setForeground(Theme.GOLD);
        pokerHandPreviewLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        centerContainer.add(pokerHandPreviewLabel, BorderLayout.NORTH);

        // Green Felt Arena
        centerCardArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 35)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Theme.FELT_GREEN);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2d.setColor(new Color(45, 80, 60));
                g2d.setStroke(new BasicStroke(4f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2d.dispose();
            }
        };
        centerCardArea.setOpaque(false);
        centerContainer.add(centerCardArea, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    // 4. BOTTOM DOCK (Action Buttons)
    private void buildBottomDock() {
        bottomControlDock = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        bottomControlDock.setOpaque(false);

        StyledButton discardBtn = new StyledButton("DISCARD", Theme.DISCARD_ORANGE);
        discardBtn.setPreferredSize(new Dimension(170, 54));
        discardBtn.addActionListener(e -> handleDiscard());

        StyledButton playHandBtn = new StyledButton("PLAY HAND", Theme.MULT_RED);
        playHandBtn.setPreferredSize(new Dimension(200, 54));
        playHandBtn.addActionListener(e -> handlePlayHand());

        bottomControlDock.add(discardBtn);
        bottomControlDock.add(playHandBtn);
        add(bottomControlDock, BorderLayout.SOUTH);
    }

    public void refresh() {
        if (state == null) return;

        targetScoreLabel.setText(String.valueOf(state.getBlindRequirement()));
        currentScoreLabel.setText(String.valueOf(state.getRoundScore()));
        anteLabel.setText("ANTE " + state.getAnte() + "/" + GameState.MAX_ANTE);
        moneyLabel.setText("$" + state.getMoney());
        handsLeftBadge.setText("<html><center>HANDS<br><b>" + state.getHandsRemaining() + "</b></center></html>");
        discardsLeftBadge.setText("<html><center>DISCARDS<br><b>" + state.getDiscardsRemaining() + "</b></center></html>");

        renderHandCards();
        renderJokers();
        updateSelectedHandPreview();

        revalidate();
        repaint();
    }

    private void renderHandCards() {
        centerCardArea.removeAll();
        activeCardViews.clear();

        for (Card card : state.getHand().getCards()) {
            // ส่ง Lambda แบบ Consumer<Card> เพื่อไม่ให้เกิด error
            CardView cv = new CardView(card, c -> {
                boolean isNowSelected = state.selectCard(c);
                // ค้นหา CardView ที่ถูกคลิกแล้วสั่งให้การ์ดยกตัวขึ้น/ลง พร้อมอัปเดตการแสดงผล
                for (CardView view : activeCardViews) {
                    if (view.getCard().equals(c)) {
                        view.setSelected(isNowSelected);
                        break;
                    }
                }
                updateSelectedHandPreview();
            });

            // ตรวจสอบว่าถ้าการ์ดใบนี้ถูกเลือกอยู่แล้ว ให้ยกตัวค้างไว้
            if (state.getHand().getSelectedCards().contains(card)) {
                cv.setSelectedImmediate(true);
            }

            activeCardViews.add(cv);
            centerCardArea.add(cv);
        }
    }

    private void renderJokers() {
        topJokerRail.removeAll();
        int activeJokers = state.getJokers().size();
        int jokerBadgeSize = 80; // กำหนดขนาด size ของ Joker icon

        for (int i = 0; i < GameState.JOKER_SLOTS; i++) {
            if (i < activeJokers) {
                // ส่งทั้ง joker และ size เข้าไปตาม constructor
                topJokerRail.add(new JokerIconView(state.getJokers().get(i), jokerBadgeSize));
            } else {
                JPanel emptySlot = new JPanel();
                emptySlot.setPreferredSize(new Dimension(jokerBadgeSize, jokerBadgeSize));
                emptySlot.setOpaque(false);
                emptySlot.setBorder(BorderFactory.createDashedBorder(new Color(60, 70, 90), 3f, 5f, 3f, true));
                topJokerRail.add(emptySlot);
            }
        }
    }

    private void updateSelectedHandPreview() {
        PokerHandType handType = state.previewHandType();
        if (handType == null) {
            pokerHandPreviewLabel.setText("SELECT UP TO 5 CARDS");
        } else {
            pokerHandPreviewLabel.setText(handType.getDisplayName().toUpperCase());
        }
    }

    private void handlePlayHand() {
        ScoreResult result = state.playSelected();
        if (result == null) return;

        refresh();

        if (state.isRoundWon()) {
            onBlindCleared.run();
        } else if (state.isGameOver()) {
            onGameOver.run();
        }
    }

    private void handleDiscard() {
        boolean discarded = state.discardSelected();
        if (discarded) {
            refresh();
        }
    }

    // Helpers
    private JLabel createStatPill(String text, Color fg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(Theme.FONT_BADGE);
        l.setForeground(fg);
        l.setBackground(new Color(20, 24, 30));
        l.setOpaque(true);
        l.setBorder(BorderFactory.createLineBorder(fg.darker(), 2));
        return l;
    }

    private JLabel createBadge(String title, String val, Color color) {
        JLabel b = new JLabel("<html><center>" + title + "<br><b>" + val + "</b></center></html>", SwingConstants.CENTER);
        b.setFont(new Font("Impact", Font.PLAIN, 18));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return b;
    }
}
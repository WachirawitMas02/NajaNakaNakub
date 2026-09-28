package UI;

import Audio.SoundManager;
import GameModel.Card;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.Timer;

// A single playing card, entirely hand-drawn (no image assets). Supports a
// smooth selection lift, a hover glow, a staggered "deal in" entrance
// animation, and a pop-and-fade exit animation used when a hand is played.
public class CardView extends JPanel {
    private static final int WIDTH = 78;
    private static final int HEIGHT = 108;
    private static final int TOP_PAD = 60;

    private final Card card;
    private boolean selected;
    private boolean hovering;

    private float liftOffset = 0f;
    private float scale = 1f;
    private float alpha = 1f;
    private boolean scoringGlow;
    private String popupText;
    private float popupAlpha;
    private float popupOffsetY;

    private Timer activeAnim;

    public CardView(Card card, Consumer<Card> onClick) {
        this.card = card;
        setPreferredSize(new Dimension(WIDTH + 10, HEIGHT + TOP_PAD));
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SoundManager.playClick();
                onClick.accept(card);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    public Card getCard() {
        return card;
    }

    public void setSelected(boolean selected) {
        if (this.selected == selected) {
            return;
        }
        this.selected = selected;
        animateLiftTo(selected ? -22f : 0f);
    }

    // Sets selection state without animating - used right after a fresh
    // CardView is created (e.g. on rebuild after a sort) so it starts in
    // sync with the model instead of momentarily looking deselected.
    public void setSelectedImmediate(boolean selected) {
        this.selected = selected;
    }

    private void animateLiftTo(float target) {
        stopActiveAnim();
        float start = liftOffset;
        activeAnim = Animator.animate(140, t -> {
            liftOffset = start + (target - start) * t;
            repaint();
        }, null);
    }

    // Cards start below the hand, faded out and slightly small, then rise
    // into place. delayMs staggers each card in the hand for a dealt effect.
    public void playDealAnimation(int delayMs) {
        scale = 0.6f;
        alpha = 0f;
        liftOffset = 46f;
        Timer delay = new Timer(delayMs, null);
        delay.setRepeats(false);
        delay.addActionListener(e -> {
            float targetLift = selected ? -22f : 0f;
            float startLift = liftOffset;
            activeAnim = Animator.animate(280, t -> {
                scale = 0.6f + 0.4f * t;
                alpha = t;
                liftOffset = startLift + (targetLift - startLift) * t;
                repaint();
            }, null);
        });
        delay.start();
    }

    // Used when a played card leaves the hand: a quick pop + fade, then
    // invokes onDone so the caller can safely mutate game state afterward.
    public void animateExit(Runnable onDone) {
        stopActiveAnim();
        float startLift = liftOffset;
        activeAnim = Animator.animate(200, t -> {
            scale = 1f + 0.2f * t;
            alpha = 1f - t;
            liftOffset = startLift - 26f * t;
            repaint();
        }, onDone);
    }

    // Played during the scoring sequence when this card contributes its
    // chip value: a quick pulse/bounce with a floating "+N" that rises and
    // fades, so "this card added chips" reads clearly on screen.
    public void pulseScore(String label) {
        stopActiveAnim();
        scoringGlow = true;
        popupText = label;
        popupAlpha = 1f;
        popupOffsetY = 0f;
        float baseLift = liftOffset;
        activeAnim = Animator.animate(420, t -> {
            scale = 1f + 0.18f * (float) Math.sin(t * Math.PI);
            liftOffset = baseLift - 6f * (float) Math.sin(t * Math.PI);
            popupAlpha = 1f - t;
            popupOffsetY = -26f * t;
            repaint();
        }, () -> {
            scale = 1f;
            liftOffset = baseLift;
            scoringGlow = false;
            popupText = null;
            repaint();
        });
    }

    private void stopActiveAnim() {
        if (activeAnim != null) {
            activeAnim.stop();
        }
    }

    private boolean isRed() {
        return "Hearts".equals(card.getSuit()) || "Diamonds".equals(card.getSuit());
    }

    private String suitSymbol() {
        switch (card.getSuit()) {
            case "Hearts": return "♥";
            case "Diamonds": return "♦";
            case "Clubs": return "♣";
            case "Spades": return "♠";
            default: return "?";
        }
    }

    private String rankLabel() {
        switch (card.getRank()) {
            case "Jack": return "J";
            case "Queen": return "Q";
            case "King": return "K";
            case "Ace": return "A";
            default: return card.getRank();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = (WIDTH + 10) / 2;
        int topY = TOP_PAD + (int) liftOffset;

        g2.translate(cx, topY + HEIGHT / 2f);
        g2.scale(scale, scale);
        g2.translate(-cx, -(topY + HEIGHT / 2f));

        Composite oldComposite = g2.getComposite();
        if (alpha < 1f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
        }

        int arc = 14;
        int x = 4;
        int w = WIDTH - 8;

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(x + 2, topY + 4, w, HEIGHT, arc, arc);

        // Glow ring while selected, hovered, or actively contributing to a score
        if (scoringGlow) {
            g2.setColor(Theme.CHIP_BLUE);
            g2.fillRoundRect(x - 4, topY - 4, w + 8, HEIGHT + 8, arc + 4, arc + 4);
        } else if (selected || hovering) {
            g2.setColor(selected ? Theme.GOLD : new Color(255, 255, 255, 120));
            g2.fillRoundRect(x - 3, topY - 3, w + 6, HEIGHT + 6, arc + 3, arc + 3);
        }

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, topY, w, HEIGHT, arc, arc);
        g2.setColor(new Color(0xD8D8E8));
        g2.drawRoundRect(x, topY, w, HEIGHT, arc, arc);

        Color suitColor = isRed() ? Theme.SUIT_RED : Theme.SUIT_BLACK;
        g2.setColor(suitColor);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g2.drawString(rankLabel(), x + 8, topY + 22);

        g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 32));
        String suit = suitSymbol();
        int textWidth = g2.getFontMetrics().stringWidth(suit);
        g2.drawString(suit, x + (w - textWidth) / 2, topY + HEIGHT / 2 + 16);

        if (popupText != null) {
            Composite before = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, popupAlpha)));
            g2.setColor(Theme.CHIP_BLUE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            int tw = g2.getFontMetrics().stringWidth(popupText);
            g2.drawString(popupText, x + (w - tw) / 2, topY + (int) popupOffsetY - 6);
            g2.setComposite(before);
        }

        g2.setComposite(oldComposite);
        g2.dispose();
    }
}

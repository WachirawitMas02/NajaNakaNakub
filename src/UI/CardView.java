package UI;

import GameModel.Card;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.function.Consumer;
import javax.swing.JPanel;

public class CardView extends JPanel {
    public static final int CARD_W = 129;
    public static final int CARD_H = 171;
    public static final int FULL_H = 480; // Headroom for center table flight
    private static final int ARC = 12;

    private final Card card;
    private final Consumer<Card> onClick;

    private boolean isSelected = false;
    private boolean isHovered = false;

    // Transformation variables
    private float offsetY = 0f;
    private float offsetX = 0f;
    private float rotationDeg = 0f;
    private float scale = 1f;
    private float alpha = 1f;

    // Score Popup
    private String floatingText = null;
    private float floatY = 0f;
    private float floatAlpha = 0f;

    public CardView(Card card, Consumer<Card> onClick) {
        this.card = card;
        this.onClick = onClick;
        setPreferredSize(new Dimension(CARD_W, FULL_H));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                isHovered = true;
                if (!isSelected) animateLift(-8f);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                isHovered = false;
                if (!isSelected) animateLift(0f);
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (onClick != null) onClick.accept(card);
            }
        });
    }

    public Card getCard() {
        return card;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        animateLift(selected ? -24f : (isHovered ? -8f : 0f));
    }
    // เพิ่มเมธอดนี้ใน UI/CardView.java เพื่อให้เช็คสถานะการเลือกได้โดยตรง ไม่ต้องพึ่ง equals() ของ Card
    public boolean isCardSelected() {
        return isSelected;
    }

    public void setSelectedImmediate(boolean selected) {
        this.isSelected = selected;
        this.offsetY = selected ? -24f : 0f;
        repaint();
    }

    private void animateLift(float targetY) {
        float startY = this.offsetY;
        Animator.animate(140, t -> {
            offsetY = startY + (targetY - startY) * t;
            if (getParent() != null) {
                getParent().repaint();
            }
            repaint();
        }, null);
    }

    // 1. Move to Center Table (Travels from bottom hand area to felt center)
    public void animatePlayToCenter(Runnable onDone) {
        float startY = this.offsetY;
        // Travel -170px up (stays safely within 480px canvas without breaching top border)
        Animator.animate(260, t -> {
            offsetY = startY + (-170f - startY) * t;
            scale = 1f + 0.08f * (float) Math.sin(t * Math.PI);
            if (getParent() != null) {
                getParent().repaint();
                if (getParent().getParent() != null) {
                    getParent().getParent().repaint();
                }
            }
            repaint();
        }, onDone);
    }

    // 2. Pulse when card scores (+Chips tag)
    public void pulseScore(String text) {
        this.floatingText = text;
        this.floatY = 0f;
        this.floatAlpha = 1f;

        Animator.animate(200, t -> {
            scale = 1.08f + 0.16f * (1f - t);
            repaint();
        }, () -> scale = 1.08f);

        Animator.animate(380, t -> {
            floatY = -35f * t;
            floatAlpha = 1f - t;
            repaint();
        }, () -> {
            floatingText = null;
            repaint();
        });
    }

    // 3. Exit table after scoring completes (Flies up past top edge)
    public void animateScoreExit(Runnable onDone) {
        float startY = this.offsetY;
        Animator.animate(220, t -> {
            offsetY = startY - 140f * t;
            alpha = Math.max(0f, 1f - t);
            repaint();
        }, onDone);
    }

    // 4. Discard Toss (Tumbles down off table)
    public void animateDiscardToss(float directionRandomness, Runnable onDone) {
        float startY = this.offsetY;
        Animator.animate(260, t -> {
            offsetY = startY + 180f * t;
            offsetX = directionRandomness * 80f * t;
            rotationDeg = directionRandomness * 35f * t;
            scale = Math.max(0.2f, 1f - 0.4f * t);
            alpha = Math.max(0f, 1f - t * 1.2f);
            repaint();
        }, onDone);
    }

    // 5. Deal In (Enters smoothly from bottom)
    public void playDealAnimation(int delayMs) {
        this.offsetY = 80f;
        this.alpha = 0f;
        this.scale = 0.88f;
        this.rotationDeg = 0f;
        this.offsetX = 0f;

        if (delayMs > 0) {
            Animator.animate(delayMs, t -> {}, this::runDealEntrance);
        } else {
            runDealEntrance();
        }
    }

    private void runDealEntrance() {
        Animator.animate(240, t -> {
            offsetY = 80f * (1f - t);
            alpha = t;
            scale = 0.88f + 0.12f * t;
            repaint();
        }, () -> {
            offsetY = 0f;
            alpha = 1f;
            scale = 1f;
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha <= 0.01f) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        // Rest near bottom with 12px cushion
        int baseBottomY = getHeight() - CARD_H - 12;

        AffineTransform oldTx = g2.getTransform();
        g2.translate(w / 2.0 + offsetX, baseBottomY + CARD_H / 2.0 + offsetY);
        g2.rotate(Math.toRadians(rotationDeg));
        g2.scale(scale, scale);
        g2.translate(-CARD_W / 2.0, -CARD_H / 2.0);

        if (alpha < 1f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(3, 5, CARD_W - 6, CARD_H - 6, ARC, ARC);

        // Face
        g2.setColor(new Color(248, 249, 252));
        g2.fillRoundRect(0, 0, CARD_W - 1, CARD_H - 1, ARC, ARC);

        // Border
        g2.setColor(isSelected ? Theme.GOLD : new Color(180, 185, 200));
        g2.setStroke(new BasicStroke(isSelected ? 3.5f : 1.8f));
        g2.drawRoundRect(0, 0, CARD_W - 1, CARD_H - 1, ARC, ARC);

        // Suit & Rank strings (scaled for 1.5x)
        String s = card.getSuit().toLowerCase();
        boolean isRed = s.contains("heart") || s.contains("diamond");
        Color suitColor = isRed ? Theme.SUIT_RED : Theme.SUIT_BLACK;
        g2.setColor(suitColor);

        String suitSymbol = s.contains("heart") ? "♥" : (s.contains("diamond") ? "♦" : (s.contains("club") ? "♣" : "♠"));
        String r = card.getRank();
        String rankLabel;
        if (r.equalsIgnoreCase("Ace")) rankLabel = "A";
        else if (r.equalsIgnoreCase("King")) rankLabel = "K";
        else if (r.equalsIgnoreCase("Queen")) rankLabel = "Q";
        else if (r.equalsIgnoreCase("Jack")) rankLabel = "J";
        else rankLabel = r;

        // Scaled typography
        g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        g2.drawString(rankLabel, 9, 28);

        g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        g2.drawString(suitSymbol, 9, 52);

        g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 52));
        FontMetrics fm = g2.getFontMetrics();
        int symW = fm.stringWidth(suitSymbol);
        g2.drawString(suitSymbol, (CARD_W - symW) / 2, CARD_H / 2 + 18);

        g2.setTransform(oldTx);

        // Floating score tag
        if (floatingText != null && floatAlpha > 0.05f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, floatAlpha));
            g2.setFont(Theme.FONT_BADGE);
            FontMetrics tfm = g2.getFontMetrics();
            int tw = tfm.stringWidth(floatingText);
            int bx = (w - tw) / 2 - 8;
            int by = (int) (baseBottomY + offsetY + floatY);
            g2.setColor(Theme.CHIP_BLUE);
            g2.fillRoundRect(bx, by, tw + 16, tfm.getHeight() + 4, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(floatingText, bx + 8, by + tfm.getAscent());
        }

        g2.dispose();
    }
    private void SAVE(){
        System.out.println("HI");
    }
}
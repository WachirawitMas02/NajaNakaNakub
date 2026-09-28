package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

// A small pill-shaped value readout (used for the chip/mult counters), so the
// live calculator reads like Balatro's rounded score chips instead of plain text.
public class BadgeLabel extends JPanel {
    private String text;
    private final Color bg;
    private final Color fg;
    private final Font font;
    private float punchScale = 1f;

    public BadgeLabel(String initialText, Color bg, Color fg, Font font) {
        this.text = initialText;
        this.bg = bg;
        this.fg = fg;
        this.font = font;
        setOpaque(false);
    }

    // Sets the value and gives it a little punch/bounce - use for a value
    // that just changed and should draw the eye (e.g. the end of a count-up).
    public void setValue(String text) {
        setValueQuiet(text);
        pulse();
    }

    // Sets the value with no animation - used for the in-between frames of a
    // count-up animation, where each individual frame shouldn't re-punch.
    public void setValueQuiet(String text) {
        this.text = text;
        revalidate();
        repaint();
    }

    public void pulse() {
        Animator.animate(200, t -> {
            punchScale = 1f + 0.3f * (1f - t);
            repaint();
        }, () -> {
            punchScale = 1f;
            repaint();
        });
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(font);
        // Reserve width for "x888.8"-sized content so a count-up animation
        // passing through different digit lengths doesn't jiggle the row
        // layout on every frame; still grows further for genuinely long text.
        int contentWidth = Math.max(fm.stringWidth(text), fm.stringWidth("x888.8"));
        int w = contentWidth + 30;
        int h = fm.getHeight() + 14;
        return new Dimension(Math.max(w, 56), h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        if (punchScale != 1f) {
            g2.translate(w / 2f, h / 2f);
            g2.scale(punchScale, punchScale);
            g2.translate(-w / 2f, -h / 2f);
        }

        g2.setColor(bg.darker());
        g2.fillRoundRect(0, 2, w, h - 2, h, h);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h - 2, h, h);

        g2.setColor(fg);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        g2.drawString(text, (w - tw) / 2, (h - fm.getHeight()) / 2 + fm.getAscent() - 1);
        g2.dispose();
    }
}

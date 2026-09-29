package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class BadgeLabel extends JPanel {
    private String text;
    private final Color bg;
    private final Color fg;
    private final Font font;
    private float punchScale = 1f;

    private static final int CORNER_RADIUS = 6; // Small round edge instead of circular pill

    public BadgeLabel(String initialText, Color bg, Color fg, Font font) {
        this.text = initialText;
        this.bg = bg;
        this.fg = fg;
        this.font = font;
        setOpaque(false);
    }

    public void setValue(String text) {
        setValueQuiet(text);
        pulse();
    }

    public void setValueQuiet(String text) {
        this.text = text;
        revalidate();
        repaint();
    }

    public void pulse() {
        Animator.animate(200, t -> {
            punchScale = 1f + 0.22f * (1f - t);
            repaint();
        }, () -> {
            punchScale = 1f;
            repaint();
        });
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(font);
        int contentWidth = Math.max(fm.stringWidth(text), fm.stringWidth("x888.8"));
        int w = contentWidth + 24;
        int h = fm.getHeight() + 10;
        return new Dimension(Math.max(w, 52), h);
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

        // Drop bevel shadow
        g2.setColor(bg.darker().darker());
        g2.fillRoundRect(0, 3, w, h - 3, CORNER_RADIUS, CORNER_RADIUS);

        // Face
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h - 3, CORNER_RADIUS, CORNER_RADIUS);

        // Subtle top rim highlight
        g2.setColor(new Color(255, 255, 255, 35));
        g2.drawRoundRect(0, 0, w - 1, h - 4, CORNER_RADIUS, CORNER_RADIUS);

        // Text
        g2.setColor(fg);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        g2.drawString(text, (w - tw) / 2, (h - fm.getHeight()) / 2 + fm.getAscent() - 2);

        g2.dispose();
    }
}
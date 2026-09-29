package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;

public final class Theme {
    private Theme() {}

    // Dimmed CRT Arcade Palette
    public static final Color BG_DARK = new Color(14, 16, 22);
    public static final Color BG_TOP = new Color(18, 20, 28);
    public static final Color BG_BOTTOM = new Color(10, 12, 16);

    // Dimmed Surfaces & Borders
    public static final Color FELT_GREEN = new Color(19, 34, 26);
    public static final Color FELT_BORDER = new Color(28, 52, 38);
    public static final Color PANEL_BG = new Color(24, 28, 38);
    public static final Color PANEL_BORDER = new Color(42, 48, 64);

    public static final Color PANEL = new Color(24, 28, 38);
    public static final Color PANEL_LIGHT = new Color(32, 38, 52);

    // Dimmed Accents & Scores
    public static final Color CHIP_BLUE = new Color(0, 115, 205);
    public static final Color MULT_RED = new Color(205, 52, 50);
    public static final Color GOLD = new Color(215, 170, 35);
    public static final Color GOLD_DIM = new Color(160, 125, 25);
    public static final Color DISCARD_ORANGE = new Color(185, 75, 30);
    public static final Color PURPLE_ANTE = new Color(105, 55, 155);

    // Text & Suits
    public static final Color TEXT = new Color(210, 212, 220);
    public static final Color TEXT_DIM = new Color(130, 134, 150);
    public static final Color SUIT_RED = new Color(195, 45, 50);
    public static final Color SUIT_BLACK = new Color(25, 25, 32);

    // Fonts
    public static final Font FONT_HEADER = new Font("Impact", Font.PLAIN, 24);
    public static final Font FONT_SCORE = new Font("Impact", Font.PLAIN, 32);
    public static final Font FONT_BADGE = new Font("Impact", Font.PLAIN, 20);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 14);
    public static final Font TITLE_FONT = new Font("Impact", Font.PLAIN, 36);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.BOLD, 11);

    public static Color colorFor(String name) {
        if (name == null) return new Color(90, 95, 110);
        int hash = Math.abs(name.hashCode());
        Color[] pool = {
            new Color(175, 50, 50),
            new Color(45, 105, 175),
            new Color(170, 130, 30),
            new Color(110, 55, 165),
            new Color(35, 135, 85)
        };
        return pool[hash % pool.length];
    }

    // Paints dark background + CRT horizontal scanlines + vignette edge shadow
    public static void paintBackdrop(Graphics2D g2, int width, int height) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Base Gradient
        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, height, BG_BOTTOM);
        g2.setPaint(gp);
        g2.fillRect(0, 0, width, height);

        // 2. CRT Scanlines (Every 3 pixels, faint dark line)
        g2.setColor(new Color(0, 0, 0, 40));
        g2.setStroke(new BasicStroke(1f));
        for (int y = 0; y < height; y += 3) {
            g2.drawLine(0, y, width, y);
        }

        // 3. Radial Vignette (Dark edges simulating curved monitor tube)
        Point2D center = new Point2D.Float(width / 2f, height / 2f);
        float radius = (float) Math.hypot(width / 2.0, height / 2.0);
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 60),
            new Color(0, 0, 0, 190)
        };
        RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(rgp);
        g2.fillRect(0, 0, width, height);
    }

    public static void paintVerticalPanelGradient(Graphics2D g2, Shape shape, Color top, Color bottom) {
        java.awt.Rectangle b = shape.getBounds();
        LinearGradientPaint lg = new LinearGradientPaint(
                0, b.y, 0, b.y + Math.max(1, b.height),
                new float[]{0f, 1f}, new Color[]{top, bottom});
        g2.setPaint(lg);
        g2.fill(shape);
    }
}
package UI;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Shape;

// Shared look and feel so every screen reads as one system without needing
// any image assets: a dark, slightly purple gradient background with gold
// accents, plus small paint helpers used across the custom-drawn components.
public final class Theme {
    private Theme() {}

    public static final Color BG_TOP = new Color(0x1A1330);
    public static final Color BG_BOTTOM = new Color(0x2B1E4A);
    public static final Color PANEL = new Color(0x2A2350);
    public static final Color PANEL_LIGHT = new Color(0x3B3270);
    public static final Color PANEL_BORDER = new Color(0x5A4A9A);
    public static final Color GOLD = new Color(0xF4C542);
    public static final Color GOLD_DIM = new Color(0xC79A2E);
    public static final Color CHIP_BLUE = new Color(0x4FA3F7);
    public static final Color MULT_RED = new Color(0xE94F4F);
    public static final Color TEXT = new Color(0xF2F0FA);
    public static final Color TEXT_DIM = new Color(0xA79FC7);
    public static final Color SUIT_RED = new Color(0xD7263D);
    public static final Color SUIT_BLACK = new Color(0x201830);
    public static final Color GREEN = new Color(0x59C97A);

    private static final String FAMILY = "Segoe UI";

    public static final Font TITLE_FONT = new Font(FAMILY, Font.BOLD, 42);
    public static final Font HEADER_FONT = new Font(FAMILY, Font.BOLD, 18);
    public static final Font BODY_FONT = new Font(FAMILY, Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font SCORE_FONT = new Font(FAMILY, Font.BOLD, 30);

    // Deterministic pastel accent color per name, used for Joker badges so
    // every joker reads as visually distinct without needing real art yet.
    public static Color colorFor(String name) {
        int hash = Math.abs(name.hashCode());
        float hue = (hash % 360) / 360f;
        return Color.getHSBColor(hue, 0.55f, 0.85f);
    }

    public static void paintBackdrop(Graphics2D g2, int width, int height) {
        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, width, height, BG_BOTTOM);
        g2.setPaint(gp);
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

package UI;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public final class Theme {
    private Theme() {}

    // Dark Arcade Palette
    public static final Color BG_DARK = new Color(12, 14, 18);
    public static final Color BG_TOP = new Color(18, 22, 28);
    public static final Color BG_BOTTOM = new Color(8, 10, 14);

    public static final Color FELT_GREEN = new Color(17, 30, 24);
    public static final Color FELT_BORDER = new Color(26, 48, 36);
    public static final Color PANEL_BG = new Color(22, 26, 34);
    public static final Color PANEL_BORDER = new Color(38, 44, 58);

    public static final Color CHIP_BLUE = new Color(0, 115, 205);
    public static final Color MULT_RED = new Color(205, 52, 50);
    public static final Color GOLD = new Color(215, 170, 35);
    public static final Color GOLD_DIM = new Color(160, 125, 25);
    public static final Color DISCARD_ORANGE = new Color(185, 75, 30);

    public static final Color TEXT = new Color(210, 212, 220);
    public static final Color TEXT_DIM = new Color(130, 134, 150);
    public static final Color SUIT_RED = new Color(195, 45, 50);
    public static final Color SUIT_BLACK = new Color(25, 25, 32);

    public static final Font FONT_HEADER = new Font("Impact", Font.PLAIN, 24);
    public static final Font FONT_SCORE = new Font("Impact", Font.PLAIN, 32);
    public static final Font FONT_BADGE = new Font("Impact", Font.PLAIN, 20);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 14);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.BOLD, 11);
        // --- Additional Centralized Styling Constants ---
    public static final Color PANEL_LIGHT = new Color(34, 40, 52);

    // Global rolling phase for animated scanbeam
    private static float crtPhase = 0f;

    public static void advanceCrtPhase() {
        crtPhase = (crtPhase + 1.2f) % 600f;
    }

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

    /**
     * Fills any Shape with a subtle vertical gradient from topColor to bottomColor.
     */
    public static void paintVerticalPanelGradient(Graphics2D g2, Shape shape, Color topColor, Color bottomColor) {
        Rectangle bounds = shape.getBounds();
        GradientPaint gp = new GradientPaint(
            0, bounds.y, topColor,
            0, bounds.y + bounds.height, bottomColor
        );
        Paint oldPaint = g2.getPaint();
        g2.setPaint(gp);
        g2.fill(shape);
        g2.setPaint(oldPaint);
    }

    /**
     * Paints an activation glow halo around a rounded shape.
     */
    public static void paintActivationGlow(Graphics2D g2, int width, int height, int arc, float glowAlpha, Color glowColor) {
        if (glowAlpha <= 0.01f) return;
        
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int alpha = Math.min(255, Math.round(140 * glowAlpha));
        g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), alpha));
        g.setStroke(new BasicStroke(4f));
        
        RoundRectangle2D outerRing = new RoundRectangle2D.Float(0, 0, width, height, arc + 2, arc + 2);
        g.draw(outerRing);
        g.dispose();
    }

    // High-fidelity Balatro CRT Monitor Shader
    public static void paintBackdrop(Graphics2D g2, int width, int height) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // 1. Deep Cabinet Gradient
        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, height, BG_BOTTOM);
        g2.setPaint(gp);
        g2.fillRect(0, 0, width, height);

        // 2. Phosphor Aperture Grille (Subtle RGB column pitch simulation)
        // Alternating faint vertical tint lines every 4 pixels
        g2.setColor(new Color(0, 255, 180, 5));
        for (int x = 0; x < width; x += 4) {
            g2.fillRect(x, 0, 1, height);
        }

        // 3. Horizontal Scanline Grooves (Fine pitch with dark troughs)
        g2.setColor(new Color(0, 0, 0, 55));
        for (int y = 0; y < height; y += 2) {
            g2.drawLine(0, y, width, y);
        }

        // 4. Sweeping Refresh Scanbeam (Luminous rolling beam)
        int beamY = (int) (crtPhase * (height / 600f));
        int beamH = 90;
        GradientPaint beamPaint = new GradientPaint(
            0, beamY - beamH, new Color(255, 255, 255, 0),
            0, beamY, new Color(180, 240, 255, 14)
        );
        g2.setPaint(beamPaint);
        g2.fillRect(0, Math.max(0, beamY - beamH), width, beamH);

        // 5. Heavy Tube Vignette & Corner Spherical Falloff
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Point2D center = new Point2D.Float(width / 2f, height / 2f);
        float radius = (float) Math.hypot(width / 2.0, height / 2.0);
        float[] dist = {0.0f, 0.55f, 0.85f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 30),
            new Color(0, 0, 0, 140),
            new Color(0, 0, 0, 235) // Deep cathode corner blacking
        };
        RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(rgp);
        g2.fillRect(0, 0, width, height);

        // 6. Monitor Glass Rim Shadow (Simulates thick curved bevel)
        g2.setColor(new Color(0, 0, 0, 180));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(1, 1, width - 2, height - 2);
    }
}
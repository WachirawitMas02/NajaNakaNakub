package UI;

import Modifiers.Joker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class JokerSlotView extends JPanel {
    private static final int SLOT_W = 129;
    private static final int SLOT_H = 171;
    private static final int ARC = 8; // Small round edge matching the new theme

    private final boolean empty;
    private float glowAlpha = 0f;
    private float bounceScale = 1f;

    public JokerSlotView(Joker joker) {
        this.empty = joker == null;
        setPreferredSize(new Dimension(SLOT_W, SLOT_H));
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(4, 4, 4, 4));

        if (empty) {
            return;
        }

        setToolTipText("<html><b>" + joker.getName() + "</b><br>" + joker.getDescription() + "</html>");

        JokerIconView icon = new JokerIconView(joker, 100);
        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        iconWrap.setOpaque(false);
        iconWrap.add(icon);
        add(iconWrap, BorderLayout.CENTER);

        JLabel name = new JLabel(shorten(joker.getName()), SwingConstants.CENTER);
        name.setFont(Theme.SMALL_FONT);
        name.setForeground(Theme.TEXT);
        add(name, BorderLayout.SOUTH);
    }

    private static String shorten(String name) {
        return name.length() > 13 ? name.substring(0, 12) + "…" : name;
    }

    public void pulseActivate() {
        if (empty) {
            return;
        }
        Animator.animate(360, t -> {
            glowAlpha = (float) Math.sin(t * Math.PI);
            bounceScale = 1f + 0.18f * (float) Math.sin(t * Math.PI);
            repaint();
        }, () -> {
            glowAlpha = 0f;
            bounceScale = 1f;
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bounceScale != 1f) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            g2.translate(cx, cy);
            g2.scale(bounceScale, bounceScale);
            g2.translate(-cx, -cy);
        }

        RoundRectangle2D shape = new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, ARC, ARC);

        if (empty) {
            // Empty dashed slot
            g2.setColor(Theme.PANEL_BG);
            g2.fill(shape);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,
                    new float[]{5, 4}, 0));
            g2.setColor(Theme.PANEL_BORDER);
            g2.draw(shape);
        } else {
            // Outer golden glow when activating during score
            if (glowAlpha > 0f) {
                g2.setColor(new Color(215, 170, 35, Math.round(130 * glowAlpha)));
                RoundRectangle2D outerGlow = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC + 2, ARC + 2);
                g2.setStroke(new BasicStroke(4f));
                g2.draw(outerGlow);
            }

            // Fill slot background directly
            g2.setColor(Theme.PANEL_BG);
            g2.fill(shape);

            // Subtle top highlight line for bevel effect
            g2.setColor(new Color(255, 255, 255, 22));
            g2.drawLine(5, 3, getWidth() - 6, 3);

            // Slot border
            g2.setColor(glowAlpha > 0f ? Theme.GOLD : Theme.PANEL_BORDER);
            g2.setStroke(new BasicStroke(glowAlpha > 0f ? 2.5f : 1.5f));
            g2.draw(shape);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}

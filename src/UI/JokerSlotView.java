package UI;

import Modifiers.Joker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
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

// One fixed slot in the joker row: either empty (dashed outline) or holding
// a Joker (icon badge + name, with the full description as a tooltip).
public class JokerSlotView extends JPanel {
    private static final int SLOT_W = 84;
    private static final int SLOT_H = 100;
    private static final int ARC = 12;

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

        JokerIconView icon = new JokerIconView(joker, 44);
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

    // Played during the scoring sequence when this joker fires: a gold glow
    // ring plus a quick scale bounce, timed with the activation sound.
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
            g2.setColor(Theme.PANEL);
            g2.fill(shape);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,
                    new float[]{5, 4}, 0));
            g2.setColor(Theme.PANEL_BORDER);
            g2.draw(shape);
        } else {
            Theme.paintVerticalPanelGradient(g2, shape, Theme.PANEL_LIGHT, Theme.PANEL);
            g2.setColor(glowAlpha > 0f ? Theme.GOLD : Theme.PANEL_BORDER);
            g2.setStroke(new BasicStroke(glowAlpha > 0f ? 3f : 1f));
            g2.draw(shape);
            if (glowAlpha > 0f) {
                g2.setColor(new java.awt.Color(244, 197, 66, Math.round(120 * glowAlpha)));
                RoundRectangle2D outerGlow = new RoundRectangle2D.Float(-3, -3, getWidth() + 2, getHeight() + 2,
                        ARC + 3, ARC + 3);
                g2.fill(outerGlow);
            }
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

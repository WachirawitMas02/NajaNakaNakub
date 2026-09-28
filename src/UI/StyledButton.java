package UI;

import Audio.SoundManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.SwingConstants;

// A flat, gradient-filled, rounded button that lights up gold on hover and
// auto-plays the click sound - used everywhere instead of a plain JButton.
public class StyledButton extends JButton {
    private boolean hovering = false;
    private boolean pressed = false;

    public StyledButton(String text) {
        super(text);
        setFont(Theme.HEADER_FONT.deriveFont(16f));
        setForeground(Theme.TEXT);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
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

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    pressed = true;
                    SoundManager.playClick();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int arc = 16;

        Color top;
        Color bottom;
        if (!isEnabled()) {
            top = Theme.PANEL_LIGHT;
            bottom = Theme.PANEL;
        } else if (pressed) {
            top = Theme.GOLD_DIM;
            bottom = Theme.GOLD_DIM.darker();
        } else if (hovering) {
            top = Theme.GOLD;
            bottom = Theme.GOLD_DIM;
        } else {
            top = Theme.PANEL_LIGHT;
            bottom = Theme.PANEL;
        }

        GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 1, w - 1, h - 2, arc, arc);
        g2.setColor(Theme.PANEL_BORDER);
        g2.drawRoundRect(0, 1, w - 2, h - 3, arc, arc);
        g2.dispose();

        setForeground(!isEnabled() ? Theme.TEXT_DIM : (hovering || pressed) ? Theme.SUIT_BLACK : Theme.TEXT);
        super.paintComponent(g);
    }
}

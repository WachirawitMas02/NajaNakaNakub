package UI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

public class StyledButton extends JButton {
    private boolean isHovered = false;
    private boolean isPressed = false;
    private Color baseColor = Theme.MULT_RED;
    private static final int ARC = 6; // Crisp corner edge
    

    public StyledButton(String text) {
        super(text);
        init();
    }

    public StyledButton(String text, Color baseColor) {
        super(text);
        this.baseColor = baseColor;
        init();
    }

    private void init() {
        setFont(Theme.FONT_BADGE);
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(e -> {
            Audio.SoundManager.playClick();
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { isHovered = false; isPressed = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int pressOffset = isPressed ? 4 : (isHovered ? -1 : 0);

        // Bevel base
        g2d.setColor(baseColor.darker().darker());
        g2d.fill(new RoundRectangle2D.Double(0, 5, w, h - 5, ARC, ARC));

        // Face
        Color fill = isHovered ? baseColor.brighter() : baseColor;
        g2d.setColor(fill);
        g2d.fill(new RoundRectangle2D.Double(0, pressOffset, w, h - 5, ARC, ARC));

        // Thin rim
        g2d.setColor(new Color(255, 255, 255, 45));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(new RoundRectangle2D.Double(0, pressOffset, w - 1, h - 6, ARC, ARC));

        // Label
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = ((h - 5) + fm.getAscent() - fm.getDescent()) / 2 + pressOffset;

        // Shadow & text
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.drawString(getText(), tx + 1, ty + 1);

        g2d.setColor(Color.WHITE);
        g2d.drawString(getText(), tx, ty);

        g2d.dispose();
    }
}
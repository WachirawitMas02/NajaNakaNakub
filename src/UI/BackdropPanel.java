package UI;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

// Base panel for every full screen: paints the shared dark gradient backdrop
// so no screen is ever a plain flat color.
public class BackdropPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        // Deliberately skip super.paintComponent(): JPanel is opaque by
        // default, so the default UI delegate would fill the whole area
        // with the plain background color and erase this gradient. Children
        // (labels, buttons, card views) still paint fine afterward via the
        // normal paintChildren() pass regardless of what happens here.
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.paintBackdrop(g2, getWidth(), getHeight());
        g2.dispose();
    }
}

package UI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import javax.swing.*;

public class TitlePanel extends JPanel {
    private final GameFrame frame;
    private float floatPhase = 0f;
    private final Timer animTimer;

    public TitlePanel(GameFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setOpaque(false);

        // Smooth 60 FPS floating title loop
        animTimer = new Timer(16, (ActionEvent e) -> {
            floatPhase += 0.04f;
            repaint();
        });
        animTimer.start();

        buildButtons();
    }

    private void buildButtons() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Push button cluster down under the title banner
        gbc.gridy = 0;
        gbc.insets = new Insets(180, 0, 16, 0);
        add(Box.createVerticalStrut(20), gbc);

        // 1. PLAY BUTTON -> Directs to your existing MenuPanel (Hero select)
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 8, 0);
        StyledButton playBtn = new StyledButton("PLAY");
        playBtn.setPreferredSize(new Dimension(240, 52));
        playBtn.setFont(new Font("SansSerif", Font.BOLD, 22));
        playBtn.addActionListener(e -> frame.showHeroSelect());
        add(playBtn, gbc);

        // 2. SETTINGS / HOW TO PLAY
        gbc.gridy = 2;
        StyledButton optionsBtn = new StyledButton("HOW TO PLAY");
        optionsBtn.setPreferredSize(new Dimension(240, 46));
        optionsBtn.setFont(new Font("SansSerif", Font.BOLD, 17));
        optionsBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Select a Hero, play poker hands to score chips and mult,\nand defeat the Blinds before running out of hands!",
                "Rules", JOptionPane.INFORMATION_MESSAGE);
        });
        add(optionsBtn, gbc);

        // 3. QUIT BUTTON
        gbc.gridy = 3;
        StyledButton quitBtn = new StyledButton("QUIT");
        quitBtn.setPreferredSize(new Dimension(240, 46));
        quitBtn.setFont(new Font("SansSerif", Font.BOLD, 17));
        quitBtn.addActionListener(e -> System.exit(0));
        add(quitBtn, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelWidth = getWidth();

        // Balatro-style subtle CRT Scanlines
        g2d.setColor(new Color(0, 0, 0, 30));
        for (int y = 0; y < getHeight(); y += 3) {
            g2d.drawLine(0, y, panelWidth, y);
        }

        // Floating Title Banner
        String titleText = "NAJA NAKA NAKUB";
        Font titleFont = new Font("Impact", Font.ITALIC, 54);
        g2d.setFont(titleFont);
        FontMetrics fm = g2d.getFontMetrics();

        int textWidth = fm.stringWidth(titleText);
        int titleX = (panelWidth - textWidth) / 2;
        int titleY = 125 + (int) (Math.sin(floatPhase) * 6);

        // Tilt title slightly
        AffineTransform old = g2d.getTransform();
        g2d.rotate(Math.toRadians(-2.5), panelWidth / 2.0, titleY);

        // Title Shadow
        g2d.setColor(new Color(15, 15, 20, 200));
        g2d.drawString(titleText, titleX + 4, titleY + 5);

        // Balatro red/orange accent
        g2d.setColor(new Color(255, 68, 68));
        g2d.drawString(titleText, titleX, titleY);

        // Gold Subtitle
        g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2d.setColor(new Color(245, 200, 70));
        String sub = "POKER ROGUELIKE";
        int subWidth = g2d.getFontMetrics().stringWidth(sub);
        g2d.drawString(sub, (panelWidth - subWidth) / 2, titleY + 30);

        g2d.setTransform(old);
        g2d.dispose();
    }
}
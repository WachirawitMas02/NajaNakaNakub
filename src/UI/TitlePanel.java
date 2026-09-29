package UI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

public class TitlePanel extends JPanel {

    private final Runnable onStartGame;
    private BufferedImage bgImage;
    private float scrollY = 0f; // Current vertical scroll position

    // Scroll speed: increase for a faster roll, decrease for a slower roll
    private static final float SCROLL_SPEED = 1.6f;

    public TitlePanel(Runnable onStartGame) {
        this.onStartGame = onStartGame;

        setLayout(new BorderLayout());
        setOpaque(false);

        loadBackgroundImage("/Assets/Bg/Bgp1.png"); // Adjust path if needed

        // ~60 FPS animation timer driving the continuous vertical film roll
        Timer scrollTimer = new Timer(16, (ActionEvent e) -> {
            scrollY += SCROLL_SPEED;
            repaint();
        });
        scrollTimer.start();

        buildUI();
    }

    private void loadBackgroundImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                bgImage = ImageIO.read(is);
            } else {
                File f = new File(path.startsWith("/") ? path.substring(1) : path);
                if (f.exists()) {
                    bgImage = ImageIO.read(f);
                }
            }
        } catch (Exception ex) {
            System.err.println("[TitlePanel] Could not load background: " + path);
            bgImage = null;
        }
    }

    private void buildUI() {
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        JLabel gameTitle = new JLabel("NajaNakaNakub", SwingConstants.CENTER);
        gameTitle.setFont(new Font("Impact", Font.PLAIN, 72));
        gameTitle.setForeground(Theme.GOLD);
        gameTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("The NNN", SwingConstants.CENTER);
        subtitle.setFont(new Font("Impact", Font.PLAIN, 24));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        StyledButton playButton = new StyledButton("PLAY", Theme.MULT_RED);
        playButton.setPreferredSize(new Dimension(220, 60));
        playButton.setFont(new Font("Impact", Font.PLAIN, 28));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> {
            if (onStartGame != null) {
                onStartGame.run();
            }
        });

        centerBox.add(Box.createVerticalGlue());
        centerBox.add(gameTitle);
        centerBox.add(Box.createVerticalStrut(6));
        centerBox.add(subtitle);
        centerBox.add(Box.createVerticalStrut(36));
        centerBox.add(playButton);
        centerBox.add(Box.createVerticalGlue());

        add(centerBox, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        if (bgImage != null) {
            int imgW = bgImage.getWidth();
            int imgH = bgImage.getHeight();

            // Scale image width to match the screen width while maintaining aspect ratio
            double scale = (double) w / imgW;
            int drawW = w;
            int drawH = (int) Math.round(imgH * scale);

            // Avoid division by zero if drawH is somehow 0
            if (drawH > 0) {
                // Wrap the scroll offset within the height of one image cycle
                int offset = (int) (scrollY % drawH);

                // Draw consecutive stacked tiles along Y to cover the screen entirely
                int startY = offset - drawH;
                for (int y = startY; y < h; y += drawH) {
                    g2.drawImage(bgImage, 0, y, drawW, drawH, null);
                }
            }

            // Dark tint overlay to keep title text readable over the moving art
            g2.setColor(new Color(10, 14, 22, 130));
            g2.fillRect(0, 0, w, h);
        } else {
            Theme.paintBackdrop(g2, w, h);
        }

        g2.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // Scanlines across the screen
        g2.setColor(new Color(0, 0, 0, 24));
        for (int y = 0; y < h; y += 3) {
            g2.drawLine(0, y, w, y);
        }

        // Curved CRT glass vignette
        Point2D center = new Point2D.Float(w / 2f, h / 2f);
        float radius = (float) Math.hypot(w / 2.0, h / 2.0);
        RadialGradientPaint vignette = new RadialGradientPaint(
            center, radius,
            new float[]{0.0f, 0.70f, 1.0f},
            new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 45), new Color(0, 0, 0, 180)}
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
    }
}
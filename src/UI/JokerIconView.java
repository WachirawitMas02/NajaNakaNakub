package UI;

import Modifiers.Joker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

// Placeholder joker art: a colored badge with the joker's initials, deterministic
// per name so every joker looks visually distinct. If a real image later shows up
// at "/Assets/jokers/<slug>.png" on the classpath, it's used instead automatically -
// drop art in there whenever it's ready and no code changes are needed.
public class JokerIconView extends JPanel {
    private final Joker joker;
    private final BufferedImage customArt;

    public JokerIconView(Joker joker, int size) {
        this.joker = joker;
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
        this.customArt = loadCustomArt(joker);
    }

    private static BufferedImage loadCustomArt(Joker joker) {
        String path = joker.getimgpath();
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        try {

            InputStream in = JokerIconView.class.getResourceAsStream(path);
            if (in != null) {
                return ImageIO.read(in);
            } else {

                java.io.File file = new java.io.File(path.startsWith("/") ? path.substring(1) : path);
                if (file.exists()) {
                    return ImageIO.read(file);
                }
            }
        } catch (IOException ignored) {

        }
        return null;
        // String slug = slugify(joker.getName());
        // String path = "/Assets/jokers/" + slug + ".png";
        // try (InputStream in = JokerIconView.class.getResourceAsStream(path)) {
        //     if (in != null) {
        //         return ImageIO.read(in);
        //     }
        // } catch (IOException ignored) {
        //     // Fall back to the drawn badge below.
        // }
        // return null;
    }

    private static String slugify(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    private String monogram() {
        String[] words = joker.getName().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
            }
            if (sb.length() >= 2) {
                break;
            }
        }
        return sb.toString();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int size = Math.min(getWidth(), getHeight());

        if (customArt != null) {
            g2.drawImage(customArt, 0, 0, size, size, null);
            g2.dispose();
            return;
        }

        Color base = Theme.colorFor(joker.getName());
        g2.setColor(base.darker());
        g2.fillOval(0, 0, size, size);
        g2.setColor(base);
        g2.fillOval(2, 2, size - 4, size - 4);

        g2.setColor(new Color(0, 0, 0, 170));
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        String mono = monogram();
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(mono);
        g2.drawString(mono, (size - tw) / 2, (size + fm.getAscent() - fm.getDescent()) / 2 - 1);

        g2.dispose();
    }
}

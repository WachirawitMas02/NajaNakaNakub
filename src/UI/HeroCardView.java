package UI;

import Modifiers.Hero;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class HeroCardView extends JPanel {
    public static final int CARD_W = 280;
    public static final int CARD_H = 430;
    private static final int ARC = 14;

    private Hero hero; // Non-final so setHero can update it
    private final Consumer<Hero> onSelect;
    private BufferedImage portraitImage;

    private boolean isHovered = false;
    private boolean isSelected = false;
    private float liftY = 0f;
    private float floatAngle = 0f; // Declared field

    public HeroCardView(Hero hero, Consumer<Hero> onSelect) {
        this.hero = hero;
        this.onSelect = onSelect;

        setPreferredSize(new Dimension(CARD_W, CARD_H));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        loadHeroGraphic();

        // 60FPS idle sine-wave bobbing
        javax.swing.Timer bobTimer = new javax.swing.Timer(20, e -> {
            floatAngle += 0.045f;
            repaint();
        });
        bobTimer.start();
    }

    public void setHero(Hero hero) {
        this.hero = hero;
        loadHeroGraphic();
        repaint();
    }

    private void loadHeroGraphic() {
        if (hero == null) return;
        String path = null;
        try {
            path = hero.getimgpath();
        } catch (Throwable t) {
            try {
                path = (String) hero.getClass().getMethod("getImgPath").invoke(hero);
            } catch (Throwable ignored) {}
        }

        if (path == null || path.trim().isEmpty()) {
            portraitImage = null;
            return;
        }

        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                portraitImage = ImageIO.read(is);
            } else {
                File file = new File(path.startsWith("/") ? path.substring(1) : path);
                if (file.exists()) {
                    portraitImage = ImageIO.read(file);
                } else {
                    portraitImage = null;
                }
            }
        } catch (Exception ignored) {
            portraitImage = null;
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        animateLift(selected ? -18f : (isHovered ? -12f : 0f));
    }

    public boolean isSelected() {
        return isSelected;
    }

    public Hero getHero() {
        return hero;
    }

    private void animateLift(float targetY) {
        float startY = this.liftY;
        Animator.animate(140, t -> {
            liftY = startY + (targetY - startY) * t;
            if (getParent() != null) getParent().repaint();
            repaint();
        }, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (hero == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Combined hover lift and smooth idle bobbing
        int totalOffsetY = (int) (liftY + Math.sin(floatAngle) * 4);
        g2.translate(0, totalOffsetY);

        // 1. Drop shadow
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(4, 8, w - 8, h - 8, ARC, ARC);

        // 2. Card Base Slate
        g2.setColor(Theme.PANEL_BG);
        g2.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

        // 3. Hero Name Header (Top)
        g2.setColor(Color.WHITE);
        g2.setFont(Theme.FONT_HEADER.deriveFont(24f));
        FontMetrics nfm = g2.getFontMetrics();
        String heroName = hero.getName().toUpperCase();
        g2.drawString(heroName, (w - nfm.stringWidth(heroName)) / 2, 34);

        // 4. Portrait Showcase Window
        int imgPad = 14;
        int imgW = w - (imgPad * 2);
        int imgH = 210;
        int imgY = 46;

        Shape oldClip = g2.getClip();
        java.awt.geom.RoundRectangle2D imageClip =
                new java.awt.geom.RoundRectangle2D.Float(imgPad, imgY, imgW, imgH, 10, 10);
        g2.clip(imageClip);

        g2.setColor(new Color(16, 20, 28));
        g2.fill(imageClip);

        if (portraitImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int srcW = portraitImage.getWidth();
            int srcH = portraitImage.getHeight();

            double scale = Math.min((double) (imgW - 16) / srcW, (double) (imgH - 16) / srcH);
            int finalW = (int) (srcW * scale);
            int finalH = (int) (srcH * scale);
            int drawX = imgPad + (imgW - finalW) / 2;
            int drawY = imgY + (imgH - finalH) / 2;

            g2.drawImage(portraitImage, drawX, drawY, finalW, finalH, null);
        } else {
            Color heroTint = Theme.colorFor(hero.getNName());
            g2.setColor(heroTint.darker());
            g2.fillRect(imgPad, imgY, imgW, imgH);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Impact", Font.PLAIN, 56));
            FontMetrics fm = g2.getFontMetrics();
            String initial = hero.getNName().isEmpty() ? "?" : hero.getNName().substring(0, 1).toUpperCase();
            g2.drawString(initial, imgPad + (imgW - fm.stringWidth(initial)) / 2, imgY + imgH / 2 + 20);
        }
        g2.setClip(oldClip);

        // Inner frame border
        g2.setColor(Theme.PANEL_BORDER);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(imageClip);

        // 5. Subtitle (Tagline)
        g2.setColor(Theme.GOLD);
        g2.setFont(new Font("Impact", Font.PLAIN, 16));
        FontMetrics tfm = g2.getFontMetrics();
        String titleStr = (hero.getTitle() != null && !hero.getTitle().isEmpty())
                ? "THE " + hero.getTitle().toUpperCase()
                : "";
        g2.drawString(titleStr, (w - tfm.stringWidth(titleStr)) / 2, imgY + imgH + 24);

        // 6. Ability Description Inset Box
        int boxY = imgY + imgH + 32;
        int boxH = h - boxY - 14;
        java.awt.geom.RoundRectangle2D descBox =
                new java.awt.geom.RoundRectangle2D.Float(imgPad, boxY, imgW, boxH, 8, 8);

        g2.setColor(new Color(18, 22, 30, 220));
        g2.fill(descBox);
        g2.setColor(new Color(42, 48, 62));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(descBox);

        // Centered ability text
        g2.setColor(new Color(230, 235, 245));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        drawCenteredWrappedText(g2, hero.getDescription(), w / 2, boxY + 22, imgW - 16);

        // 7. Outer Card Border
        Color borderCol = isSelected ? Theme.GOLD : (isHovered ? Theme.CHIP_BLUE : Theme.PANEL_BORDER);
        g2.setColor(borderCol);
        g2.setStroke(new BasicStroke(isSelected ? 3.5f : 1.8f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);

        g2.dispose();
    }

    private void drawCenteredWrappedText(Graphics2D g2, String text, int centerX, int startY, int maxWidth) {
        if (text == null || text.isEmpty()) return;
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (fm.stringWidth(current + (current.length() == 0 ? "" : " ") + word) < maxWidth) {
                if (current.length() > 0) current.append(" ");
                current.append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }

        int y = startY;
        for (String line : lines) {
            int x = centerX - (fm.stringWidth(line) / 2);
            g2.drawString(line, x, y);
            y += fm.getHeight();
        }
    }
}
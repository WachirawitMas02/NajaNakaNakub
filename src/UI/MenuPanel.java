package UI;

import Modifiers.Hero;
import Modifiers.HeroFactory;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public class MenuPanel extends BackdropPanel {

    private final Consumer<Hero> onHeroSelected;
    private final List<Hero> heroes;
    private int currentIndex = 0;
    private final HeroCardView cardPreview;

    public MenuPanel(Consumer<Hero> onHeroSelected) {
        this.onHeroSelected = onHeroSelected;
        this.heroes = HeroFactory.createChoices();

        setLayout(new BorderLayout());

        // Header Title
        JLabel titleLabel = new JLabel("CHOOSE YOUR HERO", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.PLAIN, 56));
        titleLabel.setForeground(new Color(255, 230, 90));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Center Area: Balatro Carousel (<  [ CARD ]  >)
        JPanel carouselPanel = new JPanel(new GridBagLayout());
        carouselPanel.setOpaque(false);

        JButton prevBtn = new StyledButton("<");
        prevBtn.setPreferredSize(new Dimension(55, 55));
        prevBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        prevBtn.addActionListener(e -> cycle(-1));

        JButton nextBtn = new StyledButton(">");
        nextBtn.setPreferredSize(new Dimension(55, 55));
        nextBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
        nextBtn.addActionListener(e -> cycle(1));

        // Use the external UI/HeroCardView.java component directly
        Hero initialHero = (heroes != null && !heroes.isEmpty()) ? heroes.get(0) : null;
        cardPreview = new HeroCardView(initialHero, null);

        carouselPanel.add(prevBtn);
        carouselPanel.add(Box.createHorizontalStrut(32));
        carouselPanel.add(cardPreview);
        carouselPanel.add(Box.createHorizontalStrut(32));
        carouselPanel.add(nextBtn);

        add(carouselPanel, BorderLayout.CENTER);

        // Bottom confirmation button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 25));
        bottomBar.setOpaque(false);

        StyledButton selectButton = new StyledButton("START RUN WITH HERO");
        selectButton.setPreferredSize(new Dimension(290, 52));
        selectButton.setFont(new Font("Impact", Font.PLAIN, 22));
        selectButton.addActionListener(e -> selectCurrentHero());

        bottomBar.add(selectButton);
        add(bottomBar, BorderLayout.SOUTH);

        updateDisplay();
    }

    private void cycle(int direction) {
        if (heroes == null || heroes.isEmpty()) return;
        currentIndex = (currentIndex + direction + heroes.size()) % heroes.size();
        updateDisplay();
    }

    private void updateDisplay() {
        if (heroes == null || heroes.isEmpty()) return;
        Hero hero = heroes.get(currentIndex);
        // Pass the chosen hero instance to update portrait and text together
        cardPreview.setHero(hero);
    }

    private void selectCurrentHero() {
        if (heroes == null || heroes.isEmpty()) return;
        Hero chosen = heroes.get(currentIndex);
        onHeroSelected.accept(chosen);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // Scanlines across entire menu
        g2.setColor(new Color(0, 0, 0, 24));
        for (int y = 0; y < h; y += 3) {
            g2.drawLine(0, y, w, y);
        }

        // Curved tube vignette
        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(w / 2f, h / 2f);
        float radius = (float) Math.hypot(w / 2.0, h / 2.0);
        RadialGradientPaint glassVignette = new RadialGradientPaint(
            center, radius,
            new float[]{0.0f, 0.70f, 1.0f},
            new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 35), new Color(0, 0, 0, 150)}
        );
        g2.setPaint(glassVignette);
        g2.fillRect(0, 0, w, h);

        g2.dispose();
    }
}
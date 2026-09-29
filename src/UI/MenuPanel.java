package UI;

import Modifiers.Hero;
import Modifiers.HeroFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public class MenuPanel extends JPanel {

    private final Consumer<Hero> onHeroSelected;
    private final List<Hero> heroes;
    private int currentIndex = 0;

    // Display components
    private final JLabel heroNameLabel;
    private final JLabel heroTaglineLabel;
    private final JTextArea heroAbilityArea;
    private final HeroCardView cardPreview;

    public MenuPanel(Consumer<Hero> onHeroSelected) {
        this.onHeroSelected = onHeroSelected;
        // เรียกใช้เมธอดตรงตาม HeroFactory.java
        this.heroes = HeroFactory.createChoices();

        setLayout(new BorderLayout());
        setOpaque(false);

        // Header Title
        JLabel titleLabel = new JLabel("CHOOSE YOUR HERO", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.PLAIN, 42));
        titleLabel.setForeground(new Color(255, 230, 90));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Center Area: Balatro Carousel (<  [ CARD ]  >)
        JPanel carouselPanel = new JPanel(new GridBagLayout());
        carouselPanel.setOpaque(false);

        JButton prevBtn = new StyledButton("<");
        prevBtn.setPreferredSize(new Dimension(60, 60));
        prevBtn.setFont(new Font("SansSerif", Font.BOLD, 26));
        prevBtn.addActionListener(e -> cycle(-1));

        JButton nextBtn = new StyledButton(">");
        nextBtn.setPreferredSize(new Dimension(60, 60));
        nextBtn.setFont(new Font("SansSerif", Font.BOLD, 26));
        nextBtn.addActionListener(e -> cycle(1));

        // กล่องแสดงการ์ด Hero
        cardPreview = new HeroCardView();
        cardPreview.setPreferredSize(new Dimension(320, 440));
        cardPreview.setLayout(new BoxLayout(cardPreview, BoxLayout.Y_AXIS));

        heroNameLabel = new JLabel("", SwingConstants.CENTER);
        heroNameLabel.setFont(new Font("Impact", Font.PLAIN, 30));
        heroNameLabel.setForeground(Color.WHITE);
        heroNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        heroTaglineLabel = new JLabel("", SwingConstants.CENTER);
        heroTaglineLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        heroTaglineLabel.setForeground(new Color(255, 215, 0));
        heroTaglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        heroAbilityArea = new JTextArea();
        heroAbilityArea.setWrapStyleWord(true);
        heroAbilityArea.setLineWrap(true);
        heroAbilityArea.setEditable(false);
        heroAbilityArea.setFocusable(false);
        heroAbilityArea.setOpaque(false);
        heroAbilityArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        heroAbilityArea.setForeground(new Color(240, 240, 240));
        heroAbilityArea.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        heroAbilityArea.setMaximumSize(new Dimension(280, 160));
        heroAbilityArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPreview.add(Box.createVerticalStrut(30));
        cardPreview.add(heroNameLabel);
        cardPreview.add(Box.createVerticalStrut(6));
        cardPreview.add(heroTaglineLabel);
        cardPreview.add(Box.createVerticalStrut(25));
        cardPreview.add(heroAbilityArea);
        cardPreview.add(Box.createVerticalGlue());

        // ประกอบร่าง Carousel
        carouselPanel.add(prevBtn);
        carouselPanel.add(Box.createHorizontalStrut(35));
        carouselPanel.add(cardPreview);
        carouselPanel.add(Box.createHorizontalStrut(35));
        carouselPanel.add(nextBtn);

        add(carouselPanel, BorderLayout.CENTER);

        // ปุ่มยืนยันด้านล่าง
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        bottomBar.setOpaque(false);

        StyledButton selectButton = new StyledButton("START RUN WITH HERO");
        selectButton.setPreferredSize(new Dimension(280, 54));
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

        // ดึงค่าตาม getter ของ Hero.java (หากชื่อ getter ต่างจากนี้ให้ปรับตาม field ใน Hero)
        try {
            heroNameLabel.setText(hero.getName().toUpperCase());
        } catch (Exception e) {
            heroNameLabel.setText("HERO");
        }

        try {
            // ดึงฉายา (Tagline / Title)
            heroTaglineLabel.setText(hero.getDescription());
        } catch (Exception e) {
            heroTaglineLabel.setText("HERO ARCHETYPE");
        }

        try {
            // ดึงข้อความ Ability / Description
            heroAbilityArea.setText(hero.getDescription());
        } catch (Exception e) {
            heroAbilityArea.setText("");
        }

        cardPreview.repaint();
    }

    private void selectCurrentHero() {
        if (heroes == null || heroes.isEmpty()) return;
        Hero chosen = heroes.get(currentIndex);
        onHeroSelected.accept(chosen);
    }

    // กล่องการ์ดสไตล์ Balatro พร้อมแอนิเมชันลอยตัว (Floating Card Effect)
    private class HeroCardView extends JPanel {
        private float floatAngle = 0f;

        public HeroCardView() {
            setOpaque(false);
            Timer timer = new Timer(20, (ActionEvent e) -> {
                floatAngle += 0.05f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int offsetY = (int) (Math.sin(floatAngle) * 4);

            // Card Shadow
            g2d.setColor(new Color(10, 12, 16, 180));
            g2d.fill(new RoundRectangle2D.Double(8, 12 + offsetY, w - 16, h - 20, 24, 24));

            // Card Body (Slate Background)
            g2d.setColor(new Color(36, 42, 54));
            g2d.fill(new RoundRectangle2D.Double(6, 6 + offsetY, w - 12, h - 20, 24, 24));

            // Balatro Red Border
            g2d.setColor(new Color(255, 68, 68));
            g2d.setStroke(new BasicStroke(3.5f));
            g2d.draw(new RoundRectangle2D.Double(6, 6 + offsetY, w - 12, h - 20, 24, 24));

            // CRT Scanlines บนตัวการ์ด
            g2d.setColor(new Color(0, 0, 0, 30));
            for (int y = 6 + offsetY; y < h - 14 + offsetY; y += 4) {
                g2d.drawLine(10, y, w - 10, y);
            }

            g2d.dispose();
        }
    }
}
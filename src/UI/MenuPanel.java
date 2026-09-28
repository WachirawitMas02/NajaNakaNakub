package UI;

import Audio.SoundManager;
import Modifiers.Hero;
import Modifiers.HeroFactory;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

// Title screen / character select. Picking a Hero immediately starts a run.
public class MenuPanel extends BackdropPanel {

    public MenuPanel(Consumer<Hero> onHeroChosen) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("NAJA NAKA NAKUB", SwingConstants.CENTER);
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.GOLD);
        add(title, BorderLayout.NORTH);

        JPanel heroGrid = new JPanel(new GridLayout(2, 2, 26, 26));
        heroGrid.setOpaque(false);
        heroGrid.setBorder(new EmptyBorder(40, 90, 40, 90));

        List<Hero> heroes = HeroFactory.createChoices();
        for (Hero hero : heroes) {
            heroGrid.add(new HeroCard(hero, onHeroChosen));
        }
        add(heroGrid, BorderLayout.CENTER);

        JLabel hint = new JLabel("Choose your character to begin the run", SwingConstants.CENTER);
        hint.setFont(Theme.BODY_FONT);
        hint.setForeground(Theme.TEXT_DIM);
        add(hint, BorderLayout.SOUTH);
    }

    private static class HeroCard extends JPanel {
        private boolean hovering = false;

        HeroCard(Hero hero, Consumer<Hero> onHeroChosen) {
            setLayout(new BorderLayout(0, 10));
            setOpaque(false);
            setBorder(new EmptyBorder(18, 18, 18, 18));

            JLabel name = new JLabel(hero.getName(), SwingConstants.CENTER);
            name.setFont(Theme.HEADER_FONT);
            name.setForeground(Theme.GOLD);
            add(name, BorderLayout.NORTH);

            JLabel desc = new JLabel("<html><center>" + hero.getDescription() + "</center></html>",
                    SwingConstants.CENTER);
            desc.setFont(Theme.BODY_FONT);
            desc.setForeground(Theme.TEXT);
            add(desc, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    SoundManager.playClick();
                    onHeroChosen.accept(hero);
                }

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
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 18, 18);
            Theme.paintVerticalPanelGradient(g2, shape,
                    hovering ? Theme.PANEL_LIGHT : Theme.PANEL, Theme.PANEL);
            g2.setStroke(new BasicStroke(hovering ? 2.5f : 1.5f));
            g2.setColor(hovering ? Theme.GOLD : Theme.PANEL_BORDER);
            g2.draw(shape);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

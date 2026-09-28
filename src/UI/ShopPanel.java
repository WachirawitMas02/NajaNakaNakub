package UI;

import Engine.GameState;
import Modifiers.Joker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

// Shown between blinds: spend money on new Jokers before facing the next one.
public class ShopPanel extends BackdropPanel {

    private final GameState state;
    private final Runnable onContinue;
    private final JPanel offersPanel = new JPanel(new GridLayout(1, 3, 20, 0));
    private final JLabel moneyLabel = new JLabel();

    public ShopPanel(GameState state, Runnable onContinue) {
        this.state = state;
        this.onContinue = onContinue;

        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("SHOP", SwingConstants.CENTER);
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.GOLD);
        add(title, BorderLayout.NORTH);

        offersPanel.setOpaque(false);
        add(offersPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setOpaque(false);
        moneyLabel.setFont(Theme.HEADER_FONT);
        moneyLabel.setForeground(Theme.GOLD);
        StyledButton nextButton = new StyledButton("Next Blind  ->");
        nextButton.addActionListener(e -> onContinue.run());
        bottom.add(moneyLabel);
        bottom.add(nextButton);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        moneyLabel.setText("$" + state.getMoney());
        state.generateShopOffers(3);
        renderOffers();
    }

    private void renderOffers() {
        offersPanel.removeAll();
        for (Joker joker : state.getShopOffers()) {
            offersPanel.add(new OfferCard(joker));
        }
        offersPanel.revalidate();
        offersPanel.repaint();
    }

    private class OfferCard extends JPanel {
        OfferCard(Joker joker) {
            setLayout(new BorderLayout(0, 10));
            setOpaque(false);
            setBorder(new EmptyBorder(16, 12, 16, 12));

            JokerIconView icon = new JokerIconView(joker, 56);
            JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
            iconWrap.setOpaque(false);
            iconWrap.add(icon);
            add(iconWrap, BorderLayout.NORTH);

            JPanel textStack = new JPanel();
            textStack.setOpaque(false);
            textStack.setLayout(new javax.swing.BoxLayout(textStack, javax.swing.BoxLayout.Y_AXIS));

            JLabel name = new JLabel("<html><center>" + joker.getName() + "</center></html>", SwingConstants.CENTER);
            name.setFont(Theme.HEADER_FONT);
            name.setForeground(Theme.GOLD);
            name.setAlignmentX(CENTER_ALIGNMENT);
            textStack.add(name);

            JLabel desc = new JLabel("<html><center>" + joker.getDescription() + "</center></html>", SwingConstants.CENTER);
            desc.setFont(Theme.BODY_FONT);
            desc.setForeground(Theme.TEXT);
            desc.setAlignmentX(CENTER_ALIGNMENT);
            textStack.add(javax.swing.Box.createVerticalStrut(6));
            textStack.add(desc);
            add(textStack, BorderLayout.CENTER);

            StyledButton buy = new StyledButton("Buy - $" + joker.getPrice());
            boolean canAfford = state.getMoney() >= joker.getPrice();
            boolean hasSlot = state.getJokers().size() < GameState.JOKER_SLOTS;
            buy.setEnabled(canAfford && hasSlot);
            buy.addActionListener(e -> {
                if (state.buyJoker(joker)) {
                    moneyLabel.setText("$" + state.getMoney());
                    renderOffers();
                }
            });
            JPanel buyWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buyWrap.setOpaque(false);
            buyWrap.add(buy);
            add(buyWrap, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 18, 18);
            Theme.paintVerticalPanelGradient(g2, shape, Theme.PANEL_LIGHT, Theme.PANEL);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(Theme.PANEL_BORDER);
            g2.draw(shape);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

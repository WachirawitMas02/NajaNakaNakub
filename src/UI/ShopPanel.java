package UI;

import Engine.GameState;
import Modifiers.Joker;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ShopPanel extends BackdropPanel {

    private final GameState state;
    private final Runnable onContinue;

    // Green Felt Table Colors
    private static final Color FELT_CENTER = new Color(28, 92, 54);
    private static final Color FELT_EDGE = new Color(14, 46, 28);
    private static final Color FELT_TRIM = new Color(10, 32, 20);

    // Exact card dimensions specified
    public static final int CARD_W = 129;
    public static final int CARD_H = 171;

    private final JPanel offersPanel = new JPanel();
    private final BadgeLabel moneyBadge;
    private float floatPhase = 0f;

    public ShopPanel(GameState state, Runnable onContinue) {
        this.state = state;
        this.onContinue = onContinue;

        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(20, 30, 24, 30));
        setOpaque(false);

        // -----------------------------------------------------------
        // 1. TOP BAR: "SHOP" Banner + Top Cash Badge
        // -----------------------------------------------------------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel shopTitle = new JLabel("SHOP", SwingConstants.LEFT);
        shopTitle.setFont(new Font("Impact", Font.PLAIN, 44));
        shopTitle.setForeground(Theme.GOLD);

        // Top Money Indicator
        moneyBadge = new BadgeLabel("$0", new Color(20, 26, 22), Theme.GOLD, Theme.FONT_SCORE);
        moneyBadge.setPreferredSize(new Dimension(140, 48));

        topBar.add(shopTitle, BorderLayout.WEST);
        topBar.add(moneyBadge, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // -----------------------------------------------------------
        // 2. CENTER: Green Felt Arena with 3 Cards
        // -----------------------------------------------------------
        offersPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 20));
        offersPanel.setOpaque(false);
        add(offersPanel, BorderLayout.CENTER);

        // -----------------------------------------------------------
        // 3. BOTTOM BAR: Next Blind Button
        // -----------------------------------------------------------
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomBar.setOpaque(false);

        StyledButton nextBtn = new StyledButton("NEXT BLIND ->", Theme.MULT_RED);
        nextBtn.setPreferredSize(new Dimension(210, 50));
        nextBtn.setFont(new Font("Impact", Font.PLAIN, 22));
        nextBtn.addActionListener(e -> onContinue.run());
        bottomBar.add(nextBtn);

        add(bottomBar, BorderLayout.SOUTH);

        // Idle sine wave animation for floating cards and placards
        Timer timer = new Timer(18, (ActionEvent e) -> {
            floatPhase += 0.045f;
            offersPanel.repaint();
        });
        timer.start();
    }

    public void refresh() {
        moneyBadge.setValueQuiet("$" + state.getMoney());
        state.generateShopOffers(3);
        renderOffers();
    }

    private void renderOffers() {
        offersPanel.removeAll();
        java.util.List<Joker> offers = state.getShopOffers();
        for (int i = 0; i < offers.size(); i++) {
            offersPanel.add(new ShopSlotView(offers.get(i), i));
        }
        offersPanel.revalidate();
        offersPanel.repaint();
    }

    // --- Green Casino Felt Table Backdrop ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Radial green felt gradient
        Point2D center = new Point2D.Float(w / 2f, h / 2f);
        float radius = (float) Math.hypot(w / 2.0, h / 2.0);
        RadialGradientPaint feltGrad = new RadialGradientPaint(
            center, radius,
            new float[]{0.0f, 0.65f, 1.0f},
            new Color[]{FELT_CENTER, FELT_EDGE, FELT_TRIM}
        );
        g2.setPaint(feltGrad);
        g2.fillRect(0, 0, w, h);

        // 2. Table rail border
        g2.setColor(new Color(255, 255, 255, 12));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(14, 14, w - 28, h - 28, 24, 24);

        g2.dispose();
    }

    // --- CRT Scanlines on the Felt Table ---
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0, 0, 0, 22));
        for (int y = 0; y < h; y += 3) {
            g2.drawLine(0, y, w, y);
        }
        g2.dispose();
    }

    // =========================================================================
    // Card Slot: 129x171 Card + Floating Info Placard Side-by-Side
    // =========================================================================
    private class ShopSlotView extends JPanel {
        private final Joker joker;
        private final int index;

        ShopSlotView(Joker joker, int index) {
            this.joker = joker;
            this.index = index;

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            // Width gives room for: Card (129) + Gap (14) + Placard (160) = ~303px
            setPreferredSize(new Dimension(305, 230));

            // 1. The 129x171 Joker Card Widget
            JokerCardNode cardNode = new JokerCardNode(joker);
            cardNode.setPreferredSize(new Dimension(CARD_W, CARD_H));
            cardNode.setMaximumSize(new Dimension(CARD_W, CARD_H));
            add(cardNode);

            add(Box.createHorizontalStrut(14));

            // 2. Floating Info Placard alongside the card
            JPanel placard = createFloatingPlacard(joker);
            add(placard);
        }

        private JPanel createFloatingPlacard(Joker joker) {
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Drop shadow
                    g2.setColor(new Color(0, 0, 0, 110));
                    g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 8, 12, 12);

                    // Placard dark slate backing
                    g2.setColor(new Color(24, 30, 40, 235));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                    // Red/Gold Balatro accent frame
                    g2.setColor(new Color(45, 54, 72));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);
            panel.setPreferredSize(new Dimension(160, 171));
            panel.setMaximumSize(new Dimension(160, 171));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            // Joker Title
            JLabel nameLbl = new JLabel("<html><center>" + joker.getName().toUpperCase() + "</center></html>", SwingConstants.CENTER);
            nameLbl.setFont(new Font("Impact", Font.PLAIN, 18));
            nameLbl.setForeground(Color.WHITE);
            nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Ability Description
            JLabel descLbl = new JLabel("<html><center>" + joker.getDescription() + "</center></html>", SwingConstants.CENTER);
            descLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            descLbl.setForeground(new Color(215, 222, 235));
            descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Buy Button / Price Pill
            StyledButton buyBtn = new StyledButton("$" + joker.getPrice(), Theme.GOLD);
            buyBtn.setPreferredSize(new Dimension(110, 36));
            buyBtn.setMaximumSize(new Dimension(120, 36));
            buyBtn.setFont(new Font("Impact", Font.PLAIN, 18));
            buyBtn.setForeground(Color.BLACK);
            buyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

            boolean canAfford = state.getMoney() >= joker.getPrice();
            boolean hasSlot = state.getJokers().size() < GameState.JOKER_SLOTS;
            buyBtn.setEnabled(canAfford && hasSlot);

            buyBtn.addActionListener(e -> {
                if (state.buyJoker(joker)) {
                    moneyBadge.setValueQuiet("$" + state.getMoney());
                    renderOffers();
                }
            });

            panel.add(nameLbl);
            panel.add(Box.createVerticalStrut(6));
            panel.add(descLbl);
            panel.add(Box.createVerticalGlue());
            panel.add(buyBtn);

            return panel;
        }

        // Float the entire slot (card + placard) with staggered phase offset
        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int offsetY = (int) (Math.sin(floatPhase + (index * 0.9f)) * 4.5f);
            g2.translate(0, offsetY);
            super.paint(g2);
            g2.dispose();
        }
    }

    // =========================================================================
    // The Exact 129x171 Joker Card Node
    // =========================================================================
    private class JokerCardNode extends JPanel {
        private final Joker joker;
        private java.awt.image.BufferedImage cardImg = null;

        JokerCardNode(Joker joker) {
            this.joker = joker;
            setPreferredSize(new Dimension(CARD_W, CARD_H));
            setMaximumSize(new Dimension(CARD_W, CARD_H));
            setOpaque(false);

            loadJokerImage();
        }

        private void loadJokerImage() {
            String path = joker.getimgpath();
            if (path == null || path.trim().isEmpty()) {
                System.out.println("[ShopPanel] No imgpath specified for joker: " + joker.getName());
                return;
            }

            try {
                // 1. Try Classpath Resource (e.g. /assets/jokers/my_joker.png)
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    cardImg = javax.imageio.ImageIO.read(is);
                    System.out.println("[ShopPanel] Loaded Joker image from classpath: " + path);
                } else {
                    // 2. Try Relative File Path fallback
                    java.io.File file = new java.io.File(path.startsWith("/") ? path.substring(1) : path);
                    if (file.exists()) {
                        cardImg = javax.imageio.ImageIO.read(file);
                        System.out.println("[ShopPanel] Loaded Joker image from file: " + file.getAbsolutePath());
                    } else {
                        System.err.println("[ShopPanel] File not found: " + file.getAbsolutePath());
                    }
                }
            } catch (Exception ex) {
                System.err.println("[ShopPanel] Error reading Joker image from: " + path);
                ex.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Drop shadow onto the felt table
            g2.setColor(new Color(6, 16, 10, 170));
            g2.fillRoundRect(4, 6, w - 8, h - 8, 12, 12);

            // 2. Card Background base
            g2.setColor(Theme.PANEL_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);

            // 3. Card Artwork Window (Rounded Clip)
            Shape oldClip = g2.getClip();
            RoundRectangle2D cardClip = new RoundRectangle2D.Float(0, 0, w, h, 12, 12);
            g2.clip(cardClip);

            if (cardImg != null) {
                // Keep pixel art sharp and prevent blurring
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                int imgW = cardImg.getWidth();
                int imgH = cardImg.getHeight();

                // Scale to fit completely inside 129x171
                double scale = Math.min((double) (w - 8) / imgW, (double) (h - 8) / imgH);
                int finalW = (int) (imgW * scale);
                int finalH = (int) (imgH * scale);
                int drawX = (w - finalW) / 2;
                int drawY = (h - finalH) / 2;

                g2.drawImage(cardImg, drawX, drawY, finalW, finalH, null);
            } else {
                // Fallback slate box if image asset fails or is missing
                g2.setColor(new Color(22, 26, 36));
                g2.fillRect(0, 0, w, h);

                g2.setColor(Theme.GOLD);
                g2.setFont(new Font("Impact", Font.PLAIN, 46));
                FontMetrics fm = g2.getFontMetrics();
                String initial = (joker.getName() != null && !joker.getName().isEmpty())
                        ? joker.getName().substring(0, 1).toUpperCase()
                        : "J";
                g2.drawString(initial, (w - fm.stringWidth(initial)) / 2, (h / 2) + 16);
            }
            g2.setClip(oldClip);

            // 4. Balatro Red Card Rim
            g2.setColor(Theme.MULT_RED);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            g2.dispose();
        }
    }
}
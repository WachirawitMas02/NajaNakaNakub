import Engine.GameState;
import GameModel.Card;
import Modifiers.Hero;
import Modifiers.HeroFactory;
import Modifiers.Joker;
import Modifiers.JokerFactory;
import UI.EndPanel;
import UI.MenuPanel;
import UI.PlayPanel;
import UI.ShopPanel;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;

// Dev tool (not part of the shipped game): renders each real UI screen off
// the actual production classes - not a mockup - and screenshots it to
// tools/screenshots/, so layout/animation can be reviewed/tuned without
// launching the full game and clicking through every screen by hand.
//
// Build & run from the project root:
//   javac -d out -cp src tools/ScreenshotHarness.java $(find src -name "*.java")   (bash)
//   java -cp out ScreenshotHarness
//
// On Windows/PowerShell:
//   javac -d out -cp src tools\ScreenshotHarness.java (Get-ChildItem -Recurse src\*.java).FullName
//   java -cp out ScreenshotHarness
public class ScreenshotHarness {
    private static final String OUT_DIR = "tools/screenshots";

    public static void main(String[] args) throws Exception {
        new File(OUT_DIR).mkdirs();
        Rectangle bounds = screenBounds();

        capture("menu", () -> new MenuPanel(hero -> {}), bounds, 400);
        capture("play", ScreenshotHarness::buildPlay, bounds, 1300);
        capture("shop", ScreenshotHarness::buildShop, bounds, 400);
        capture("end_victory", () -> buildEnd(true), bounds, 400);
        capture("end_gameover", () -> buildEnd(false), bounds, 400);
        captureScoringSequence(bounds);

        System.exit(0);
    }

    private static Rectangle screenBounds() {
        DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        return new Rectangle(0, 0, dm.getWidth(), dm.getHeight());
    }

    // Pre-populates a run in progress (a joker owned, a few cards selected)
    // so the play screen screenshot shows the live calculator and a filled
    // joker slot instead of the empty starting state.
    private static JPanel buildPlay() {
        GameState state = new GameState();
        Hero hero = HeroFactory.createChoices().get(0);
        state.startNewRun(hero);

        List<Joker> pool = JokerFactory.createPool();
        state.buyJoker(pool.get(0));

        List<Card> cards = state.getHand().getCards();
        for (int i = 0; i < Math.min(3, cards.size()); i++) {
            state.selectCard(cards.get(i));
        }
        return new PlayPanel(state, () -> {}, () -> {});
    }

    private static JPanel buildShop() {
        GameState state = new GameState();
        state.startNewRun(HeroFactory.createChoices().get(1));
        state.collectReward();
        ShopPanel panel = new ShopPanel(state, () -> {});
        panel.refresh();
        return panel;
    }

    private static JPanel buildEnd(boolean victory) {
        EndPanel panel = new EndPanel(() -> {});
        panel.show(victory, victory ? 8 : 3, 42);
        return panel;
    }

    // Clicks the real Play Hand button (via reflection, since it's a private
    // field on the production panel) and grabs frames mid-animation, so the
    // scoring sequence (card ticking in, joker firing, final combine) can be
    // reviewed without actually launching and playing the game.
    private static void captureScoringSequence(Rectangle bounds) throws Exception {
        JFrame[] holder = new JFrame[1];
        JPanel[] panelHolder = new JPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.setBounds(bounds);
            JPanel panel = buildPlayWithPairAndJoker();
            frame.add(panel);
            frame.setVisible(true);
            holder[0] = frame;
            panelHolder[0] = panel;
        });
        Thread.sleep(1300); // let the initial deal-in settle

        AbstractButton playButton = (AbstractButton) getPrivateField(panelHolder[0], "playButton");
        SwingUtilities.invokeAndWait(playButton::doClick);

        int[] offsetsMs = {150, 500, 850, 1150, 1500};
        int elapsed = 0;
        for (int i = 0; i < offsetsMs.length; i++) {
            Thread.sleep(offsetsMs[i] - elapsed);
            elapsed = offsetsMs[i];
            snap(panelHolder[0], bounds, "play_scoring_" + (i + 1));
        }

        SwingUtilities.invokeAndWait(() -> holder[0].dispose());
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    // Pair (two scoring cards, so two separate CARD tick events) plus one
    // owned Joker, so the captured frames show a card tick, then a joker
    // activation, then the final combine.
    private static JPanel buildPlayWithPairAndJoker() {
        GameState state = new GameState();
        state.startNewRun(HeroFactory.createChoices().get(0));

        List<Joker> pool = JokerFactory.createPool();
        state.buyJoker(pool.get(0));

        List<Card> cards = state.getHand().getCards();
        Card[] pair = findAnyPair(cards);
        if (pair != null) {
            state.selectCard(pair[0]);
            state.selectCard(pair[1]);
        } else {
            state.selectCard(cards.get(0));
            state.selectCard(cards.get(1));
        }
        return new PlayPanel(state, () -> {}, () -> {});
    }

    private static Card[] findAnyPair(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            for (int j = i + 1; j < cards.size(); j++) {
                if (cards.get(i).getRankValue() == cards.get(j).getRankValue()) {
                    return new Card[]{cards.get(i), cards.get(j)};
                }
            }
        }
        return null;
    }

    // Renders the panel's own pixels directly (JComponent.printAll into an
    // offscreen BufferedImage) instead of a real desktop screen capture. A
    // real screen grab would show whatever window actually has focus at
    // that instant - e.g. another app you're using - which is both wrong
    // and a privacy risk. Printing the component tree itself only ever
    // produces this panel's own content, regardless of what's on screen.
    private static void capture(String name, Supplier<JPanel> panelSupplier, Rectangle bounds, int settleMs)
            throws Exception {
        JFrame[] holder = new JFrame[1];
        JPanel[] panelHolder = new JPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.setBounds(bounds);
            JPanel panel = panelSupplier.get();
            frame.add(panel);
            frame.setVisible(true);
            holder[0] = frame;
            panelHolder[0] = panel;
        });
        Thread.sleep(settleMs); // let deal-in / layout animations settle before capturing
        snap(panelHolder[0], bounds, name);
        SwingUtilities.invokeAndWait(() -> holder[0].dispose());
    }

    private static void snap(JPanel panel, Rectangle bounds, String name) throws Exception {
        BufferedImage img = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        SwingUtilities.invokeAndWait(() -> {
            Graphics2D g2 = img.createGraphics();
            panel.printAll(g2);
            g2.dispose();
        });
        ImageIO.write(img, "png", new File(OUT_DIR + "/" + name + ".png"));
        System.out.println("Captured " + name + ".png");
    }
}

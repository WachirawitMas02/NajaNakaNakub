package UI;

import Engine.GameState;
import java.awt.CardLayout;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

// Top-level window. Owns the single GameState for the current run and
// switches between menu / play / shop / end screens via CardLayout.
public class GameFrame extends JFrame {

    private static final String MENU = "menu";
    private static final String PLAY = "play";
    private static final String SHOP = "shop";
    private static final String END = "end";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    private GameState state;
    private PlayPanel playPanel;
    private ShopPanel shopPanel;
    private EndPanel endPanel;

    public GameFrame() {
        super("Naja Naka Nakub");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupFullscreen();
        setupExitHotkey();

        root.add(buildMenu(), MENU);
        add(root);
    }

    private void setupFullscreen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode mode = device.getDisplayMode();
        setUndecorated(true);
        setResizable(false);
        setBounds(0, 0, mode.getWidth(), mode.getHeight());
    }

    private void setupExitHotkey() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitGame");
        getRootPane().getActionMap().put("exitGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private JPanel buildMenu() {
        return new MenuPanel(hero -> {
            state = new GameState();
            state.startNewRun(hero);

            playPanel = new PlayPanel(state, this::goToShopOrVictory, this::goToEnd);
            shopPanel = new ShopPanel(state, this::goToNextBlind);
            endPanel = new EndPanel(this::restart);

            root.add(playPanel, PLAY);
            root.add(shopPanel, SHOP);
            root.add(endPanel, END);

            cardLayout.show(root, PLAY);
        });
    }

    private void goToShop() {
        shopPanel.refresh();
        cardLayout.show(root, SHOP);
    }

    private void goToNextBlind() {
        state.startBlind();
        playPanel.refresh();
        cardLayout.show(root, PLAY);
    }

    private void goToEnd() {
        endPanel.show(false, state.getAnte(), state.getMoney());
        cardLayout.show(root, END);
    }

    private void restart() {
        // Drop the old run's screens so a fresh hero pick builds a clean state.
        root.removeAll();
        root.add(buildMenu(), MENU);
        cardLayout.show(root, MENU);
        root.revalidate();
        root.repaint();
    }

    // Called when a blind is cleared: pays out reward + interest and checks
    // for the final-boss win condition before deciding shop vs. victory screen.
    private void goToShopOrVictory() {
        boolean victoryReached = state.collectReward();
        if (victoryReached) {
            endPanel.show(true, state.getAnte(), state.getMoney());
            cardLayout.show(root, END);
        } else {
            goToShop();
        }
    }
}

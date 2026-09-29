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
// switches between title / menu / play / shop / end screens via CardLayout.
public class GameFrame extends JFrame {

    private static final String TITLE = "title";
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
    private TitlePanel titlePanel;
    private MenuPanel menuPanel;

    public GameFrame() {
        super("Naja Naka Nakub");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupFullscreen();
        setupExitHotkey();

        // 1. Build persistent screens
        titlePanel = new TitlePanel(this);
        menuPanel = (MenuPanel) buildMenu();

        root.add(titlePanel, TITLE);
        root.add(menuPanel, MENU);

        add(root);

        // Start on Title Screen
        showTitleScreen();
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

    // --- Navigation Methods ---

    public void showTitleScreen() {
        cardLayout.show(root, TITLE);
    }

    public void showHeroSelect() {
        cardLayout.show(root, MENU);
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
        // Drop the old run's screens and return to title
        root.removeAll();
        titlePanel = new TitlePanel(this);
        menuPanel = (MenuPanel) buildMenu();

        root.add(titlePanel, TITLE);
        root.add(menuPanel, MENU);

        cardLayout.show(root, TITLE);
        root.revalidate();
        root.repaint();
    }

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
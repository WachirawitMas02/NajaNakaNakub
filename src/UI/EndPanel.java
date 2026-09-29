package UI;

import Audio.SoundManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

// Shown when a run ends, either by losing a blind or clearing ante 8.
public class EndPanel extends BackdropPanel {

    private final JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel statsLabel = new JLabel("", SwingConstants.CENTER);

    public EndPanel(Runnable onPlayAgain) {
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(70, 60, 70, 60));

        titleLabel.setFont(Theme.FONT_HEADER);
        add(titleLabel, BorderLayout.NORTH);

        statsLabel.setFont(Theme.HEADER_FONT);
        statsLabel.setForeground(Theme.TEXT);
        add(statsLabel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        StyledButton again = new StyledButton("Play Again");
        again.addActionListener(e -> onPlayAgain.run());
        bottom.add(again);
        add(bottom, BorderLayout.SOUTH);
    }

    public void show(boolean victory, int anteReached, int money) {
        if (victory) {
            titleLabel.setText("VICTORY!");
            titleLabel.setForeground(Theme.GOLD);
            statsLabel.setText("You conquered all 8 antes with $" + money + " to spare.");
            SoundManager.playSuccess();
        } else {
            titleLabel.setText("GAME OVER");
            titleLabel.setForeground(Theme.MULT_RED);
            statsLabel.setText("You fell at Ante " + anteReached + ".");
            SoundManager.playFail();
        }
    }
}

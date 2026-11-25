import javax.swing.SwingUtilities;

import model.GameConfig;
import model.MultiplayerGame;
import model.Plateau;
import model.placementstrategy.ArbitraryStrategy;
import views.gui.MainWindow;

/**
 * Entry point of the graphical game.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Plateau plateau = new Plateau(GameConfig.BOARD_SIZE, GameConfig.BOARD_SIZE,
                    GameConfig.COUNT_PIECES, new ArbitraryStrategy());
            MultiplayerGame game = new MultiplayerGame(plateau);
            new MainWindow(game).setVisible(true);
        });
    }
}

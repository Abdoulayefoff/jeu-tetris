package views.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import model.Game;
import model.MultiplayerGame;
import observer.ListeningModel;

/**
 * Main window of the game. It hosts the {@link Rendu} panel and keeps its title in sync with the game.
 */
public class MainWindow extends JFrame implements ListeningModel {

    private static final long serialVersionUID = 7376825297884956163L;

    private final Game game;
    private final BoardView grid;
    private final Rendu zoneRendu;

    public MainWindow(Game game) {
        super(Constants.WINDOW_TITLE);
        try {
            // native file chooser and message boxes; the game itself is custom-painted
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            // keep the default look and feel
        }
        this.game = game;
        this.grid = new BoardView(game, game.getBoard());
        this.zoneRendu = new Rendu(game, grid);
        game.addListening(this);

        getContentPane().setBackground(Theme.PANEL);
        getContentPane().add(zoneRendu, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(960, 680));
        setLocationRelativeTo(null);
    }

    public Rendu getZoneRendu() {
        return zoneRendu;
    }

    public BoardView getGrid() {
        return grid;
    }

    private void updateTitle() {
        String title = Constants.WINDOW_TITLE;
        if (game instanceof MultiplayerGame && ((MultiplayerGame) game).isTwoPlayers() && game.isOnGoing()) {
            title += ", " + Constants.CURRENT_PLAYER_LABEL + ((MultiplayerGame) game).getCurrentPlayer();
        }
        setTitle(title);
    }

    @Override
    public void modelUpdated(Object source) {
        SwingUtilities.invokeLater(this::updateTitle);
    }
}

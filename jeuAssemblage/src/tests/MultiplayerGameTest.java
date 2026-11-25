import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.MultiplayerGame;
import model.Plateau;
import model.ai.RandomAI;
import model.placementstrategy.ArbitraryStrategy;
import piece.Coordinates;
import piece.Piece;

class MultiplayerGameTest {

    private MultiplayerGame game;

    @BeforeEach
    void setUp() {
        game = new MultiplayerGame(new Plateau(30, 30, 9, new ArbitraryStrategy()));
    }

    @Test
    void mode_can_only_change_between_games() {
        assertTrue(game.setTwoPlayers(true));
        game.start();
        assertFalse(game.setTwoPlayers(false));
        assertTrue(game.isTwoPlayers());
    }

    @Test
    void players_alternate_and_score_their_area_gain() {
        game.setTwoPlayers(true);
        game.start();
        assertEquals(1, game.getCurrentPlayer());

        Piece far = game.getBoard().getPieces().get(8); // bottom-right T
        int before = game.getBoard().evaluate();
        Coordinates c = far.getOutterCenter();
        assertTrue(game.movePiece(far, new Coordinates(c.getX() - 1, c.getY() - 1)));
        int gain = before - game.getBoard().evaluate();
        assertTrue(gain > 0);
        assertEquals(gain, game.getPlayerScore(1));
        assertEquals(0, game.getPlayerScore(2));
        assertEquals(2, game.getCurrentPlayer());
        assertEquals(1, game.getWinner());
    }

    @Test
    void failed_action_keeps_the_turn() {
        game.setTwoPlayers(true);
        game.start();
        assertFalse(game.movePiece(game.getBoard().getPieces().get(0), new Coordinates(-5, -5)));
        assertEquals(1, game.getCurrentPlayer());
    }

    @Test
    void equal_scores_are_a_draw() {
        game.setTwoPlayers(true);
        game.start();
        assertEquals(0, game.getWinner());
    }

    @Test
    void single_player_mode_does_not_track_players() {
        game.start();
        Piece p = game.getBoard().getPieces().get(8);
        Coordinates c = p.getOutterCenter();
        game.movePiece(p, new Coordinates(c.getX() - 1, c.getY() - 1));
        assertEquals(1, game.getCurrentPlayer());
        assertEquals(0, game.getPlayerScore(1));
    }

    @Test
    void ai_is_refused_in_two_player_mode() {
        game.setTwoPlayers(true);
        game.start();
        assertFalse(game.playAI(new RandomAI()));
        assertFalse(game.isAIPlaying());
    }

    @Test
    void restart_resets_scores_and_turn() {
        game.setTwoPlayers(true);
        game.start();
        Piece p = game.getBoard().getPieces().get(8);
        Coordinates c = p.getOutterCenter();
        game.movePiece(p, new Coordinates(c.getX() - 1, c.getY() - 1));
        game.gameOver();
        game.start();
        assertEquals(1, game.getCurrentPlayer());
        assertEquals(0, game.getPlayerScore(1));
    }
}

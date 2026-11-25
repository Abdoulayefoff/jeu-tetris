import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Game;
import model.GameConfig;
import model.Plateau;
import model.placementstrategy.ArbitraryStrategy;
import model.placementstrategy.RandomStrategy;
import piece.Coordinates;
import piece.Piece;

class GameTest {

    private Plateau board;
    private Game game;

    @BeforeEach
    void setUp() {
        board = new Plateau(30, 30, 9, new ArbitraryStrategy());
        game = new Game(board);
    }

    @Test
    void game_is_not_running_before_start() {
        assertFalse(game.isOnGoing());
        assertFalse(game.canMove());
        assertFalse(game.movePiece(new piece.PieceR(3, 3), new Coordinates(1, 1)));
    }

    @Test
    void start_places_pieces_and_resets_counters() {
        game.start();
        assertTrue(game.isOnGoing());
        assertEquals(GameConfig.MAX_ACTIONS, game.getRemainingActions());
        assertEquals(9, board.getPieces().size());
        assertEquals(0, game.getScore());
    }

    @Test
    void successful_move_consumes_one_action() {
        game.start();
        Piece p = board.getPieces().get(0);
        Coordinates c = p.getOutterCenter();
        assertTrue(game.movePiece(p, new Coordinates(c.getX() + 1, c.getY() + 1)));
        assertEquals(GameConfig.MAX_ACTIONS - 1, game.getRemainingActions());
    }

    @Test
    void failed_move_consumes_nothing() {
        game.start();
        Piece p = board.getPieces().get(0);
        assertFalse(game.movePiece(p, new Coordinates(-10, -10)));
        assertEquals(GameConfig.MAX_ACTIONS, game.getRemainingActions());
    }

    @Test
    void rotation_consumes_one_action() {
        game.start();
        Piece p = board.getPieces().get(1); // 3x3 T in the middle of free space
        assertTrue(game.rotatePiece(p, true));
        assertEquals(GameConfig.MAX_ACTIONS - 1, game.getRemainingActions());
    }

    @Test
    void current_score_is_board_area_minus_bounding_area() {
        game.start();
        assertEquals(30 * 30 - board.evaluate(), game.getCurrentScore());
    }

    @Test
    void game_ends_when_actions_run_out() {
        game.start();
        Piece p = board.getPieces().get(1);
        for (int i = 0; i < GameConfig.MAX_ACTIONS; i++) {
            assertTrue(game.rotatePiece(p, i % 2 == 0));
        }
        assertFalse(game.isOnGoing());
        assertFalse(game.canMove());
        assertEquals(30 * 30 - board.evaluate(), game.getScore());
    }

    @Test
    void game_over_is_idempotent() {
        game.start();
        game.gameOver();
        int score = game.getScore();
        game.gameOver();
        assertEquals(score, game.getScore());
    }

    @Test
    void placement_strategy_can_only_change_between_games() {
        assertTrue(game.useRandomPlacementStrategy(true));
        assertInstanceOf(RandomStrategy.class, board.getPlacementStrategy());
        game.start();
        assertFalse(game.useRandomPlacementStrategy(false));
        assertInstanceOf(RandomStrategy.class, board.getPlacementStrategy());
    }

    @Test
    void ai_delay_is_never_negative() {
        game.setAIDelay(-5);
        assertEquals(0, game.getAIDelay());
        game.setAIDelay(300);
        assertEquals(300, game.getAIDelay());
    }

    @Test
    void ai_refuses_to_start_when_no_game_is_running() {
        assertFalse(game.playAI(g -> { }));
    }

    @Test
    void ai_thread_plays_until_the_game_is_over() throws InterruptedException {
        game.start();
        game.setAIDelay(0);
        assertTrue(game.playAI(new model.ai.RandomAI()));
        long deadline = System.currentTimeMillis() + 10_000;
        while ((game.isAIPlaying() || game.isOnGoing()) && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertFalse(game.isAIPlaying());
        assertFalse(game.isOnGoing());
    }
}

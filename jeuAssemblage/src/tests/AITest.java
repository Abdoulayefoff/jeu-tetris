import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import model.Game;
import model.GameConfig;
import model.Plateau;
import model.ai.AdvancedAI;
import model.ai.GreedyAI;
import model.ai.RandomAI;
import model.placementstrategy.ArbitraryStrategy;

class AITest {

    private static Game startedGame() {
        Game game = new Game(new Plateau(30, 30, 9, new ArbitraryStrategy()));
        game.start();
        return game;
    }

    @RepeatedTest(20)
    void random_ai_plays_at_most_one_action_per_turn() {
        Game game = startedGame();
        new RandomAI(new Random()).nextMove(game);
        int used = GameConfig.MAX_ACTIONS - game.getRemainingActions();
        assertTrue(used == 0 || used == 1, "used " + used);
    }

    @Test
    void random_ai_moves_stay_legal() {
        Game game = startedGame();
        RandomAI ai = new RandomAI(new Random(42));
        for (int i = 0; i < 50 && game.canMove(); i++) {
            ai.nextMove(game);
        }
        int cells = game.getBoard().getPieces().stream().mapToInt(p -> p.getOccupiedExternesCoordinates().size()).sum();
        long distinct = game.getBoard().getPieces().stream()
                .flatMap(p -> p.getOccupiedExternesCoordinates().stream()).distinct().count();
        assertEquals(cells, distinct, "pieces must never overlap");
    }

    @Test
    void advanced_ai_updates_min_area() {
        Game game = startedGame();
        AdvancedAI ai = new AdvancedAI(new Random(1));
        assertEquals(Integer.MAX_VALUE, ai.getMinArea());
        ai.nextMove(game);
        assertTrue(ai.getMinArea() < Integer.MAX_VALUE);
        assertTrue(ai.getMinArea() <= 30 * 30);
    }

    @Test
    void advanced_ai_state_is_per_instance() {
        Game game = startedGame();
        AdvancedAI first = new AdvancedAI(new Random(1));
        first.nextMove(game);
        assertEquals(Integer.MAX_VALUE, new AdvancedAI().getMinArea());
    }

    @Test
    void advanced_ai_never_makes_the_area_worse_than_the_start() {
        Game game = startedGame();
        int start = game.getBoard().evaluate();
        AdvancedAI ai = new AdvancedAI(new Random(7));
        for (int i = 0; i < GameConfig.MAX_ACTIONS && game.canMove(); i++) {
            ai.nextMove(game);
        }
        assertTrue(ai.getMinArea() <= start);
    }

    @Test
    void greedy_ai_strictly_improves_the_area() {
        Game game = startedGame();
        int before = game.getBoard().evaluate();
        new GreedyAI().nextMove(game);
        assertTrue(game.getBoard().evaluate() < before);
        assertEquals(GameConfig.MAX_ACTIONS - 1, game.getRemainingActions());
    }

    @Test
    void greedy_ai_beats_the_initial_layout_by_a_lot() {
        Game game = startedGame();
        int before = game.getBoard().evaluate();
        GreedyAI ai = new GreedyAI();
        while (game.canMove()) {
            ai.nextMove(game);
        }
        assertTrue(game.getBoard().evaluate() <= before * 2 / 3,
                "area " + game.getBoard().evaluate() + " vs " + before);
    }

    @Test
    void greedy_ai_does_nothing_when_the_game_is_over() {
        Game game = startedGame();
        game.gameOver();
        new GreedyAI().nextMove(game);
        assertEquals(GameConfig.MAX_ACTIONS, game.getRemainingActions());
    }
}

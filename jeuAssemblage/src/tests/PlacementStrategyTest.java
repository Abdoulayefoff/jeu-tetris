import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import model.Plateau;
import model.placementstrategy.ArbitraryStrategy;
import model.placementstrategy.RandomStrategy;
import piece.PieceFactory;

class PlacementStrategyTest {

    @Test
    void arbitrary_strategy_places_the_whole_default_layout() {
        Plateau plateau = new Plateau(30, 30, 9, new ArbitraryStrategy());
        plateau.initialConfig();
        assertEquals(ArbitraryStrategy.DEFAULT_LAYOUT.size(), plateau.getPieces().size());
        assertEquals(9, plateau.getPieces().size());
        assertTrue(plateau.getPieces().stream().anyMatch(p -> "I".equals(p.getShape())));
    }

    @Test
    void arbitrary_strategy_rejects_an_invalid_layout() {
        List<ArbitraryStrategy.Placement> bad = List.of(
                new ArbitraryStrategy.Placement(PieceFactory.R, 3, 3, 0, 1, 1),
                new ArbitraryStrategy.Placement(PieceFactory.R, 3, 3, 0, 2, 2));
        Plateau plateau = new Plateau(10, 10, 2, new ArbitraryStrategy(bad));
        assertThrows(IllegalArgumentException.class, plateau::initialConfig);
    }

    @RepeatedTest(10)
    void random_strategy_places_the_requested_number_of_pieces() {
        Plateau plateau = new Plateau(30, 30, 15, new RandomStrategy(new Random()));
        plateau.initialConfig();
        assertEquals(15, plateau.getPieces().size());
    }

    @Test
    void random_strategy_gives_up_when_the_board_is_too_small() {
        Plateau plateau = new Plateau(3, 3, 10, new RandomStrategy(new Random(3)));
        plateau.initialConfig();
        assertTrue(plateau.getPieces().size() < 10);
    }

    @Test
    void initial_config_replaces_previous_pieces() {
        Plateau plateau = new Plateau(30, 30, 9, new ArbitraryStrategy());
        plateau.initialConfig();
        plateau.initialConfig();
        assertEquals(9, plateau.getPieces().size());
    }
}

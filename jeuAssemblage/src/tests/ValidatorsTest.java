import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import model.Plateau;
import model.placementstrategy.RandomStrategy;
import model.validators.AbstractValidator;
import model.validators.OverflowValidator;
import model.validators.OverlappingValidator;
import piece.Coordinates;
import piece.Piece;
import piece.PieceR;

class ValidatorsTest {

    private final Plateau plateau = new Plateau(10, 10, 0, new RandomStrategy());

    @Test
    void overflow_validator_accepts_inside_and_rejects_outside() {
        AbstractValidator v = new OverflowValidator();
        Piece p = new PieceR(3, 3);
        assertTrue(v.validate(plateau, p, new Coordinates(1, 1)));
        assertTrue(v.validate(plateau, p, new Coordinates(8, 8)));
        assertFalse(v.validate(plateau, p, new Coordinates(0, 5)));
        assertFalse(v.validate(plateau, p, new Coordinates(9, 5)));
    }

    @Test
    void overlapping_validator_rejects_occupied_cells() {
        plateau.addPiece(new PieceR(3, 3), new Coordinates(5, 5));
        AbstractValidator v = new OverlappingValidator();
        assertFalse(v.validate(plateau, new PieceR(3, 3), new Coordinates(6, 6)));
        assertTrue(v.validate(plateau, new PieceR(3, 3), new Coordinates(1, 1)));
    }

    @Test
    void overlapping_validator_ignores_the_moved_piece_itself() {
        Piece p = new PieceR(3, 3);
        plateau.addPiece(p, new Coordinates(5, 5));
        assertTrue(new OverlappingValidator().validate(plateau, p, new Coordinates(6, 6)));
    }

    @Test
    void chain_stops_on_the_first_failure() {
        int[] calls = {0};
        AbstractValidator counting = new AbstractValidator() {
            @Override
            protected boolean check(Plateau pl, Piece piece, Coordinates to) {
                calls[0]++;
                return true;
            }
        };
        AbstractValidator chain = AbstractValidator.buildChain(new OverflowValidator(), counting);
        assertFalse(chain.validate(plateau, new PieceR(3, 3), new Coordinates(-3, -3)));
        assertEquals(0, calls[0], "the second validator must not run after a failure");
        assertTrue(chain.validate(plateau, new PieceR(3, 3), new Coordinates(4, 4)));
        assertEquals(1, calls[0]);
    }
}

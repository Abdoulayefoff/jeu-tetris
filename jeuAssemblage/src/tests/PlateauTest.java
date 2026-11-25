import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Plateau;
import model.placementstrategy.RandomStrategy;
import piece.Coordinates;
import piece.Piece;
import piece.PieceFactory;
import piece.PieceI;
import piece.PieceL;
import piece.PieceR;

class PlateauTest {

    private Plateau plateau;
    private PieceFactory factory;

    @BeforeEach
    void setUp() {
        plateau = new Plateau(10, 10, 3, new RandomStrategy());
        factory = new PieceFactory();
    }

    @Test
    void add_piece_at_valid_position_returns_true() {
        assertTrue(plateau.addPiece(factory.buildRandom(), new Coordinates(5, 5)));
        assertEquals(1, plateau.getPieces().size());
    }

    @Test
    void add_piece_out_of_bounds_returns_false() {
        assertFalse(plateau.addPiece(factory.buildRandom(), new Coordinates(50, 50)));
        assertEquals(0, plateau.getPieces().size());
    }

    @Test
    void add_overlapping_piece_returns_false() {
        assertTrue(plateau.addPiece(new PieceR(3, 3), new Coordinates(5, 5)));
        assertFalse(plateau.addPiece(new PieceR(3, 3), new Coordinates(6, 6)));
        assertEquals(1, plateau.getPieces().size());
    }

    @Test
    void move_piece_to_free_cell_updates_its_center() {
        Piece piece = new PieceR(3, 3);
        plateau.addPiece(piece, new Coordinates(2, 2));
        assertTrue(plateau.movePiece(piece, new Coordinates(7, 7)));
        assertEquals(new Coordinates(7, 7), piece.getOutterCenter());
    }

    @Test
    void move_piece_outside_keeps_its_center() {
        Piece piece = new PieceR(3, 3);
        plateau.addPiece(piece, new Coordinates(2, 2));
        assertFalse(plateau.movePiece(piece, new Coordinates(0, 9)));
        assertEquals(new Coordinates(2, 2), piece.getOutterCenter());
    }

    @Test
    void rotate_clockwise_then_anticlockwise_restores_orientation() {
        Piece piece = new PieceL(3, 3);
        plateau.addPiece(piece, new Coordinates(5, 5));
        int initial = piece.getOrientation();
        assertTrue(plateau.rotateClockWisePiece(piece));
        assertTrue(plateau.rotateAntiClockWisePiece(piece));
        assertEquals(initial, piece.getOrientation());
        assertEquals(new Coordinates(5, 5), piece.getOutterCenter());
    }

    @Test
    void rotation_that_would_overflow_is_refused() {
        Piece bar = new PieceI(1, 5);
        plateau.addPiece(bar, new Coordinates(5, 0)); // vertical bar on the left edge
        assertFalse(plateau.canRotate(bar, true));
        assertFalse(plateau.rotateClockWisePiece(bar));
        assertEquals(0, bar.getOrientation());
    }

    @Test
    void evaluate_returns_bounding_rectangle_area() {
        assertEquals(0, plateau.evaluate());
        plateau.addPiece(new PieceR(3, 3), new Coordinates(1, 1));
        assertEquals(9, plateau.evaluate());
        plateau.addPiece(new PieceR(3, 3), new Coordinates(8, 8));
        assertEquals(100, plateau.evaluate());
    }

    @Test
    void reset_clears_all_pieces() {
        plateau.addPiece(new PieceR(3, 3), new Coordinates(2, 2));
        plateau.addPiece(new PieceR(3, 3), new Coordinates(7, 7));
        assertEquals(2, plateau.getPieces().size());
        plateau.reset();
        assertEquals(0, plateau.getPieces().size());
    }

    @Test
    void overflow_validator_rejects_position_outside_board() {
        Piece piece = factory.buildRandom();
        assertFalse(plateau.validate(piece, new Coordinates(-1, 5)));
        assertFalse(plateau.validate(piece, new Coordinates(5, -1)));
        assertFalse(plateau.validate(piece, new Coordinates(10, 5)));
        assertFalse(plateau.validate(piece, new Coordinates(5, 10)));
    }

    @Test
    void rectangular_board_uses_width_for_columns_and_height_for_rows() {
        Plateau wide = new Plateau(20, 6, 0, new RandomStrategy());
        assertTrue(wide.addPiece(new PieceR(3, 3), new Coordinates(4, 17)));
        assertFalse(wide.addPiece(new PieceR(3, 3), new Coordinates(17, 4)));
    }

    @Test
    void text_rendering_contains_the_pieces() {
        plateau.addPiece(new PieceR(3, 3), new Coordinates(5, 5));
        assertTrue(plateau.affichagePlateau().contains("RRR"));
    }
}

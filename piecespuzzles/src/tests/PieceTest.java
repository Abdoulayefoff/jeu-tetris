import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import piece.Coordinates;
import piece.Piece;
import piece.PieceI;
import piece.PieceL;
import piece.PieceR;
import piece.PieceT;

class PieceTest {

    @Test
    void constructor_stores_base_dimensions_and_orientation() {
        Piece piece = new PieceR(3, 5, 0);
        assertEquals(3, piece.getWidth());
        assertEquals(5, piece.getHeight());
        assertEquals(0, piece.getOrientation());
    }

    @Test
    void odd_orientation_swaps_current_dimensions() {
        Piece piece = new PieceR(3, 5, 1);
        assertEquals(5, piece.getWidth());
        assertEquals(3, piece.getHeight());
        assertEquals(3, piece.getBaseWidth());
        assertEquals(5, piece.getBaseHeight());
    }

    @Test
    void invalid_orientation_is_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new PieceR(3, 4, 5));
        assertThrows(IllegalArgumentException.class, () -> new PieceR(3, 4, -1));
    }

    @Test
    void non_positive_dimensions_are_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new PieceL(0, 3));
        assertThrows(IllegalArgumentException.class, () -> new PieceT(3, -1));
    }

    @Test
    void clone_keeps_shape_dimensions_and_orientation() {
        Piece original = new PieceL(3, 5, 1);
        Piece copy = Piece.clone(original);
        assertNotSame(original, copy);
        assertEquals(original.getShape(), copy.getShape());
        assertEquals(original.getWidth(), copy.getWidth());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getOrientation(), copy.getOrientation());
    }

    @Test
    void four_clockwise_rotations_restore_orientation() {
        Piece piece = new PieceT(3, 5, 0);
        for (int i = 0; i < 4; i++) {
            piece.rotateClockWise();
        }
        assertEquals(0, piece.getOrientation());
        assertEquals(3, piece.getWidth());
    }

    @Test
    void clockwise_then_anticlockwise_is_identity() {
        Piece piece = new PieceL(3, 5, 2);
        piece.rotateClockWise();
        assertEquals(3, piece.getOrientation());
        piece.rotateAntiClockWise();
        assertEquals(2, piece.getOrientation());
    }

    @Test
    void rectangle_occupies_every_cell() {
        Piece piece = new PieceR(3, 3);
        piece.setOutterCenter(5, 5);
        assertEquals(9, piece.getOccupiedExternesCoordinates().size());
        assertTrue(piece.getOccupiedExternesCoordinates().contains(new Coordinates(4, 4)));
        assertTrue(piece.getOccupiedExternesCoordinates().contains(new Coordinates(6, 6)));
    }

    @Test
    void bar_is_one_cell_wide() {
        Piece bar = new PieceI(1, 5);
        bar.setOutterCenter(10, 10);
        assertEquals("I", bar.getShape());
        assertEquals(5, bar.getOccupiedExternesCoordinates().size());
        bar.rotateClockWise();
        assertEquals(5, bar.getWidth());
        assertEquals(1, bar.getHeight());
    }

    @Test
    void spanned_coordinates_and_corners_follow_the_center() {
        Piece piece = new PieceR(3, 5);
        piece.setOutterCenter(10, 20);
        assertArrayEquals(new int[] {8, 9, 10, 11, 12}, piece.getSpannedCoordinates(true));
        assertArrayEquals(new int[] {19, 20, 21}, piece.getSpannedCoordinates(false));
        assertEquals(8, piece.getTopRow());
        assertEquals(19, piece.getLeftCol());
    }

    @Test
    void keys_do_not_collide() {
        assertNotEquals(new PieceT(3, 15).getKey(), new PieceT(31, 5).getKey());
    }
}

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import piece.Piece;
import piece.PieceFactory;
import piece.PieceI;
import piece.PieceL;
import piece.PieceR;
import piece.PieceT;

class PieceFactoryTest {

    @Test
    void builds_each_piece_type() {
        PieceFactory f = new PieceFactory().withWidth(3).withHeight(5).withOrientation(0);
        assertInstanceOf(PieceL.class, f.withPiece(PieceFactory.L).build());
        assertInstanceOf(PieceT.class, f.withPiece(PieceFactory.T).build());
        assertInstanceOf(PieceR.class, f.withPiece(PieceFactory.R).build());
        assertInstanceOf(PieceI.class, f.withPiece(PieceFactory.I).build());
    }

    @Test
    void builder_values_are_used() {
        Piece p = new PieceFactory().withPiece(PieceFactory.R).withWidth(3).withHeight(5).withOrientation(1).build();
        assertEquals(1, p.getOrientation());
        assertEquals(5, p.getWidth()); // rotated: base 3x5 becomes 5x3
        assertEquals(3, p.getHeight());
    }

    @Test
    void unknown_type_is_rejected() {
        assertThrows(IllegalStateException.class, () -> new PieceFactory().withPiece(42).build());
    }

    @RepeatedTest(50)
    void random_pieces_are_valid() {
        Piece p = new PieceFactory().buildRandom();
        assertNotNull(p);
        assertTrue(p.getOrientation() >= 0 && p.getOrientation() <= 3);
        int w = p.getBaseWidth();
        int h = p.getBaseHeight();
        assertTrue(h == 3 || h == 5, "height " + h);
        if ("I".equals(p.getShape())) {
            assertEquals(1, w);
        } else {
            assertEquals(3, w);
        }
    }

    @Test
    void random_factory_eventually_builds_every_type() {
        PieceFactory f = new PieceFactory();
        java.util.Set<String> shapes = new java.util.HashSet<>();
        for (int i = 0; i < 500 && shapes.size() < 4; i++) {
            shapes.add(f.buildRandom().getShape());
        }
        assertEquals(java.util.Set.of("L", "T", "R", "I"), shapes);
    }
}

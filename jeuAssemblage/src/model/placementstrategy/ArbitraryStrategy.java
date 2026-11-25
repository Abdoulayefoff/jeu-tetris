package model.placementstrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Plateau;
import piece.Coordinates;
import piece.Piece;
import piece.PieceFactory;

/**
 * Deterministic placement: always the same pieces at the same positions.
 * Useful for tests, demos and for comparing AIs on an identical starting board.
 */
public class ArbitraryStrategy implements PlacementStrategy {

    /** Description of one piece of the layout. */
    public static final class Placement {
        final int type;
        final int width;
        final int height;
        final int orientation;
        final int row;
        final int col;

        /**
         * @param type        {@link PieceFactory#L}, {@link PieceFactory#T}, {@link PieceFactory#R} or {@link PieceFactory#I}
         * @param width       base width
         * @param height      base height
         * @param orientation 0 to 3
         * @param row         center row (x)
         * @param col         center column (y)
         */
        public Placement(int type, int width, int height, int orientation, int row, int col) {
            this.type = type;
            this.width = width;
            this.height = height;
            this.orientation = orientation;
            this.row = row;
            this.col = col;
        }
    }

    /** Nine pieces spread on a 3×3 grid of a 30×30 board. */
    public static final List<Placement> DEFAULT_LAYOUT = Collections.unmodifiableList(List.of(
            new Placement(PieceFactory.L, 3, 5, 0, 3, 3),
            new Placement(PieceFactory.T, 3, 3, 0, 3, 15),
            new Placement(PieceFactory.R, 3, 5, 0, 4, 26),
            new Placement(PieceFactory.I, 1, 5, 0, 14, 4),
            new Placement(PieceFactory.T, 5, 3, 0, 14, 15),
            new Placement(PieceFactory.L, 3, 3, 2, 15, 26),
            new Placement(PieceFactory.R, 3, 3, 0, 25, 4),
            new Placement(PieceFactory.I, 1, 5, 1, 25, 15),
            new Placement(PieceFactory.T, 3, 5, 2, 25, 26)));

    private final List<Placement> layout;

    public ArbitraryStrategy() {
        this(DEFAULT_LAYOUT);
    }

    public ArbitraryStrategy(List<Placement> layout) {
        this.layout = new ArrayList<>(layout);
    }

    /**
     * @throws IllegalArgumentException if a placement of the layout is not valid on this board
     */
    @Override
    public void initialConfig(Plateau plateau) {
        plateau.reset();
        for (Placement p : layout) {
            Piece piece = new PieceFactory()
                    .withPiece(p.type)
                    .withWidth(p.width)
                    .withHeight(p.height)
                    .withOrientation(p.orientation)
                    .build();
            if (!plateau.addPiece(piece, new Coordinates(p.row, p.col))) {
                throw new IllegalArgumentException("Invalid placement for " + piece.getKey()
                        + " at (" + p.row + "," + p.col + ")");
            }
        }
    }
}

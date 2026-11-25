package piece;

/**
 * Straight "I" piece: a bar that is {@code width} cells thick and {@code height} cells long.
 * With the default thickness of 1 it is a classic single-cell-wide bar.
 */
public class PieceI extends Piece {

    public PieceI(int width, int height, int orientation) {
        super(width, height, orientation);
    }

    public PieceI(int width, int height) {
        this(width, height, 0);
    }

    @Override
    protected boolean matrixCellVal(int i, int j, int height, int width) {
        // every cell of the (rotated) bounding matrix is part of the bar
        return true;
    }

    @Override
    public String getShape() {
        return "I";
    }
}

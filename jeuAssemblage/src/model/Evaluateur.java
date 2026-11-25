package model;

import java.util.List;

import piece.Piece;

/**
 * Computes the quantity the player tries to minimise: the area of the smallest
 * axis-aligned rectangle that encloses every piece on the board.
 */
public final class Evaluateur {

    private Evaluateur() {
    }

    /**
     * @param pieces the pieces placed on the board
     * @return the bounding-rectangle area, or 0 when there is no piece
     */
    public static int calculerAireRectangle(List<Piece> pieces) {
        int[] box = boundingBox(pieces);
        if (box == null) {
            return 0;
        }
        return (box[2] - box[0] + 1) * (box[3] - box[1] + 1);
    }

    /**
     * Bounding box of the pieces, using the same geometry as the validators:
     * a piece covers rows {@code top .. top + height - 1} and columns
     * {@code left .. left + width - 1}.
     *
     * @return {@code {minRow, minCol, maxRow, maxCol}} (inclusive), or null when there is no piece
     */
    public static int[] boundingBox(List<Piece> pieces) {
        if (pieces == null || pieces.isEmpty()) {
            return null;
        }
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int maxCol = Integer.MIN_VALUE;

        for (Piece piece : pieces) {
            int top = piece.getTopRow();
            int left = piece.getLeftCol();
            minRow = Math.min(minRow, top);
            minCol = Math.min(minCol, left);
            maxRow = Math.max(maxRow, top + piece.getHeight() - 1);
            maxCol = Math.max(maxCol, left + piece.getWidth() - 1);
        }
        return new int[] {minRow, minCol, maxRow, maxCol};
    }
}

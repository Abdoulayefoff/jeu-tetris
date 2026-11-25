package model.ai;

import java.util.List;

import piece.Piece;

/**
 * Helpers shared by the AIs to evaluate a candidate action without mutating the board.
 */
final class Moves {

    private Moves() {
    }

    /**
     * Bounding-rectangle area of the board if {@code target} were centered on
     * ({@code row}, {@code col}) with the given (possibly rotated) dimensions.
     */
    static int areaIf(List<Piece> pieces, Piece target, int row, int col, int height, int width) {
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (Piece p : pieces) {
            int top;
            int left;
            int h;
            int w;
            if (p == target) {
                h = height;
                w = width;
                top = row - h / 2;
                left = col - w / 2;
            } else {
                h = p.getHeight();
                w = p.getWidth();
                top = p.getTopRow();
                left = p.getLeftCol();
            }
            minRow = Math.min(minRow, top);
            minCol = Math.min(minCol, left);
            maxRow = Math.max(maxRow, top + h - 1);
            maxCol = Math.max(maxCol, left + w - 1);
        }
        return (maxRow - minRow + 1) * (maxCol - minCol + 1);
    }

    /** Area if {@code target} were moved to ({@code row}, {@code col}). */
    static int areaIfMoved(List<Piece> pieces, Piece target, int row, int col) {
        return areaIf(pieces, target, row, col, target.getHeight(), target.getWidth());
    }

    /** Area if {@code target} were rotated by a quarter turn in place (dimensions swap). */
    static int areaIfRotated(List<Piece> pieces, Piece target) {
        return areaIf(pieces, target, target.getOutterCenter().getX(), target.getOutterCenter().getY(),
                target.getWidth(), target.getHeight());
    }
}

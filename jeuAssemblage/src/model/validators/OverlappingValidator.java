package model.validators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Plateau;
import piece.Coordinates;
import piece.Piece;

/**
 * Rejects positions where the piece would share a cell with another piece.
 * Only occupied cells count: two bounding matrices may overlap as long as
 * their filled cells do not.
 */
public class OverlappingValidator extends AbstractValidator {

    @Override
    protected boolean check(Plateau plateau, Piece piece, Coordinates toPosition) {
        Piece moved = Piece.clone(piece);
        moved.setOutterCenter(toPosition.getX(), toPosition.getY());
        Set<Coordinates> newCells = null;

        List<Piece> pieces = plateau.getPieces();
        for (Piece other : pieces) {
            if (other == piece || other.getOutterCenter() == null) {
                continue;
            }
            if (!boxesIntersect(moved, other)) {
                continue; // cheap rejection before comparing cells
            }
            if (newCells == null) {
                newCells = new HashSet<>(moved.getOccupiedExternesCoordinates());
            }
            for (Coordinates c : other.getOccupiedExternesCoordinates()) {
                if (newCells.contains(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean boxesIntersect(Piece a, Piece b) {
        int aTop = a.getTopRow();
        int aLeft = a.getLeftCol();
        int bTop = b.getTopRow();
        int bLeft = b.getLeftCol();
        return aTop <= bTop + b.getHeight() - 1 && bTop <= aTop + a.getHeight() - 1
                && aLeft <= bLeft + b.getWidth() - 1 && bLeft <= aLeft + a.getWidth() - 1;
    }
}

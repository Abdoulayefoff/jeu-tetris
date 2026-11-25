package model.validators;

import model.Plateau;
import piece.Coordinates;
import piece.Piece;

/**
 * Rejects positions where part of the piece would leave the board.
 */
public class OverflowValidator extends AbstractValidator {

    @Override
    protected boolean check(Plateau plateau, Piece piece, Coordinates toPosition) {
        int height = piece.getHeight();
        int width = piece.getWidth();

        int startRow = toPosition.getX() - height / 2;
        int endRow = startRow + height - 1;
        int startCol = toPosition.getY() - width / 2;
        int endCol = startCol + width - 1;

        return startRow >= 0 && startCol >= 0
                && endRow < plateau.getHeight()
                && endCol < plateau.getWidth();
    }
}

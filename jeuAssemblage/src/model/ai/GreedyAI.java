package model.ai;

import java.util.List;

import model.Game;
import model.Plateau;
import piece.Coordinates;
import piece.Piece;

/**
 * Hill-climbing AI: each turn it tries every position (and both rotations) of every
 * piece and plays the single action that shrinks the bounding rectangle the most.
 * When no action improves the area, it has reached a local optimum and ends the game.
 */
public class GreedyAI implements AI {

    @Override
    public void nextMove(Game game) {
        Plateau board = game.getBoard();
        List<Piece> pieces = board.getPieces();
        if (pieces.isEmpty() || !game.canMove()) {
            return;
        }

        int bestArea = board.evaluate();
        Piece bestPiece = null;
        Coordinates bestPos = null;
        Boolean bestRotation = null; // null = move, TRUE = clockwise, FALSE = anti-clockwise

        for (Piece piece : pieces) {
            for (int x = 0; x < board.getHeight(); x++) {
                for (int y = 0; y < board.getWidth(); y++) {
                    // cheap arithmetic first; validate only promising candidates
                    int area = Moves.areaIfMoved(pieces, piece, x, y);
                    if (area < bestArea) {
                        Coordinates pos = new Coordinates(x, y);
                        if (board.validate(piece, pos)) {
                            bestArea = area;
                            bestPiece = piece;
                            bestPos = pos;
                            bestRotation = null;
                        }
                    }
                }
            }
            int rotatedArea = Moves.areaIfRotated(pieces, piece);
            if (rotatedArea < bestArea) {
                for (boolean clockwise : new boolean[] {true, false}) {
                    if (board.canRotate(piece, clockwise)) {
                        bestArea = rotatedArea;
                        bestPiece = piece;
                        bestPos = null;
                        bestRotation = clockwise;
                        break;
                    }
                }
            }
        }

        if (bestPiece == null) {
            game.gameOver(); // local optimum: no single action improves the area
        } else if (bestRotation != null) {
            game.rotatePiece(bestPiece, bestRotation);
        } else {
            game.movePiece(bestPiece, bestPos);
        }
    }
}

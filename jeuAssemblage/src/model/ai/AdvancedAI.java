package model.ai;

import java.util.List;
import java.util.Random;

import model.Game;
import model.GameConfig;
import model.Plateau;
import piece.Coordinates;
import piece.Piece;

/**
 * Picks the most profitable action among random samples.
 *
 * <p>Each turn it samples {@link GameConfig#AI_MAX_ROTATE_ATTEMPTS} random candidate
 * actions of the current type, plays the one giving the smallest bounding rectangle,
 * and remembers the best area reached so far ({@code minArea}). While an action type
 * keeps improving the area it is kept; otherwise another type is picked at random.</p>
 */
public class AdvancedAI extends RandomAI {

    private int minArea = Integer.MAX_VALUE;
    private int next = MOVE;

    public AdvancedAI() {
        super();
    }

    public AdvancedAI(Random random) {
        super(random);
    }

    @Override
    public void nextMove(Game game) {
        Plateau board = game.getBoard();
        if (board.getPieces().isEmpty() || !game.canMove()) {
            return;
        }
        minArea = Math.min(minArea, board.evaluate());

        boolean played = (next == MOVE) ? bestSampledMove(game) : bestSampledRotation(game, next == ROTATE_CLOCKWISE);
        if (!played) {
            played = randomMove(game); // nothing better found: keep exploring
        }

        int newArea = board.evaluate();
        if (newArea < minArea) {
            minArea = newArea;             // this kind of action pays off: keep it
        } else {
            next = random.nextInt(3);      // not efficient: try another kind of action
        }
    }

    private boolean bestSampledMove(Game game) {
        Plateau board = game.getBoard();
        List<Piece> pieces = board.getPieces();
        Piece bestPiece = null;
        Coordinates bestPos = null;
        int bestArea = board.evaluate();

        for (int attempt = 0; attempt < GameConfig.AI_MAX_ROTATE_ATTEMPTS; attempt++) {
            Piece piece = pieces.get(random.nextInt(pieces.size()));
            int row = random.nextInt(board.getHeight());
            int col = random.nextInt(board.getWidth());
            int area = Moves.areaIfMoved(pieces, piece, row, col);
            if (area < bestArea) {
                Coordinates pos = new Coordinates(row, col);
                if (board.validate(piece, pos)) {
                    bestArea = area;
                    bestPiece = piece;
                    bestPos = pos;
                }
            }
        }
        return bestPiece != null && game.movePiece(bestPiece, bestPos);
    }

    private boolean bestSampledRotation(Game game, boolean clockwise) {
        Plateau board = game.getBoard();
        List<Piece> pieces = board.getPieces();
        Piece best = null;
        int bestArea = board.evaluate();
        for (Piece piece : pieces) {
            int area = Moves.areaIfRotated(pieces, piece);
            if (area < bestArea && board.canRotate(piece, clockwise)) {
                bestArea = area;
                best = piece;
            }
        }
        return best != null && game.rotatePiece(best, clockwise);
    }

    /** @return the smallest bounding area this AI has reached (for tests and display) */
    public int getMinArea() {
        return minArea;
    }

    /** @return the type of the next action the AI will try */
    public int getNextAction() {
        return next;
    }
}

package model.ai;

import java.util.List;
import java.util.Random;

import model.Game;
import model.GameConfig;
import model.Plateau;
import piece.Coordinates;
import piece.Piece;

/**
 * Plays one random legal action per turn: a move or a rotation of a random piece.
 *
 * <p>Every {@link #nextMove(Game)} consumes at most one action, and the number of
 * tries is bounded by {@link GameConfig#AI_MAX_ROTATE_ATTEMPTS}, so the AI can
 * never loop forever.</p>
 */
public class RandomAI implements AI {
    public static final int MOVE = 0;
    public static final int ROTATE_ANTI_CLOCKWISE = 1;
    public static final int ROTATE_CLOCKWISE = 2;

    protected final Random random;

    public RandomAI() {
        this(new Random());
    }

    public RandomAI(Random random) {
        this.random = random;
    }

    @Override
    public void nextMove(Game game) {
        playAction(game, random.nextInt(3));
    }

    /**
     * Play exactly one action of the given type (or nothing if none is possible).
     * @return whether an action was played
     */
    protected boolean playAction(Game game, int action) {
        switch (action) {
            case MOVE:
                return randomMove(game);
            case ROTATE_ANTI_CLOCKWISE:
                return randomRotate(game, false);
            case ROTATE_CLOCKWISE:
                return randomRotate(game, true);
            default:
                throw new IllegalArgumentException("Unknown action " + action);
        }
    }

    /**
     * Move a random piece to a random valid position.
     * @return whether a piece was moved
     */
    public boolean randomMove(Game game) {
        Plateau plateau = game.getBoard();
        List<Piece> pieces = plateau.getPieces();
        if (pieces.isEmpty() || !game.canMove()) {
            return false;
        }
        for (int attempt = 0; attempt < GameConfig.AI_MAX_ROTATE_ATTEMPTS; attempt++) {
            Piece piece = pieces.get(random.nextInt(pieces.size()));
            Coordinates target = new Coordinates(
                    random.nextInt(plateau.getHeight()),   // x = row
                    random.nextInt(plateau.getWidth()));   // y = column
            if (!target.equals(piece.getOutterCenter()) && plateau.validate(piece, target)) {
                return game.movePiece(piece, target);
            }
        }
        return false;
    }

    /**
     * Rotate a random piece that can be rotated.
     * @return whether a piece was rotated
     */
    public boolean randomRotate(Game game, boolean clockwise) {
        Plateau plateau = game.getBoard();
        List<Piece> pieces = plateau.getPieces();
        if (pieces.isEmpty() || !game.canMove()) {
            return false;
        }
        for (int attempt = 0; attempt < GameConfig.AI_MAX_ROTATE_ATTEMPTS; attempt++) {
            Piece piece = pieces.get(random.nextInt(pieces.size()));
            if (plateau.canRotate(piece, clockwise)) {
                return game.rotatePiece(piece, clockwise);
            }
        }
        return false;
    }
}

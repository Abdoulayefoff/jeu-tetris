package model.validators;

import model.Plateau;
import piece.Coordinates;
import piece.Piece;

/**
 * Chain of Responsibility for move validation.
 *
 * <p>Each link performs one check; the chain stops at the first failing link,
 * and a position is valid only when every link accepts it.</p>
 */
public abstract class AbstractValidator {
    protected AbstractValidator next;

    /**
     * Build a chain of validators, in order.
     * @param first the head of the chain
     * @param chain the following links
     * @return the head of the chain
     */
    public static AbstractValidator buildChain(AbstractValidator first, AbstractValidator... chain) {
        AbstractValidator head = first;
        for (AbstractValidator nextInChain : chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    /**
     * Run this link and, when it succeeds, the rest of the chain.
     * @param plateau the board
     * @param piece the piece to move or add
     * @param toPosition the destination center of the piece
     * @return whether the move or addition is valid
     */
    public final boolean validate(Plateau plateau, Piece piece, Coordinates toPosition) {
        if (!check(plateau, piece, toPosition)) {
            return false;
        }
        return next == null || next.validate(plateau, piece, toPosition);
    }

    /**
     * The concrete check implemented by each link.
     */
    protected abstract boolean check(Plateau plateau, Piece piece, Coordinates toPosition);

    public AbstractValidator getNext() {
        return next;
    }
}

package model.placementstrategy;

import java.util.Random;

import model.GameConfig;
import model.Plateau;
import piece.Coordinates;
import piece.Piece;
import piece.PieceFactory;

/**
 * Places {@code plateau.getNbPieces()} random pieces at random valid positions.
 *
 * <p>The number of tries is bounded by {@link GameConfig#PLACEMENT_MAX_ATTEMPTS}: on a
 * crowded board fewer pieces may be placed, but the method always terminates.</p>
 */
public class RandomStrategy implements PlacementStrategy {

    private final Random random;

    public RandomStrategy() {
        this(new Random());
    }

    public RandomStrategy(Random random) {
        this.random = random;
    }

    @Override
    public void initialConfig(Plateau plateau) {
        plateau.reset();
        PieceFactory factory = new PieceFactory();
        int attempts = 0;
        while (plateau.getPieces().size() < plateau.getNbPieces()
                && attempts < GameConfig.PLACEMENT_MAX_ATTEMPTS) {
            attempts++;
            Piece piece = factory.buildRandom();
            Coordinates center = new Coordinates(
                    random.nextInt(plateau.getHeight()),   // x = row
                    random.nextInt(plateau.getWidth()));   // y = column
            plateau.addPiece(piece, center);
        }
    }
}

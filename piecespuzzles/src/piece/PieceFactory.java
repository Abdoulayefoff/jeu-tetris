package piece;

import java.util.Random;

/**
 * Factory for class piece
 */
public class PieceFactory {

    /** Piece type identifiers accepted by {@link #withPiece(int)}. */
    public static final int L = 0;
    public static final int T = 1;
    public static final int R = 2;
    public static final int I = 3;
    /** Number of different piece types. */
    public static final int PIECE_TYPES = 4;

    private static final Random RANDOM = new Random();

    protected int width;
    protected int height;
    protected int orientation;
    protected int piece;

    

    /**
     * Constructor for PieceFactory
     */
    public PieceFactory() {
        randomValues(); // default
    }

    /**
     * Use custom width
     * @param width
     * @return PieceFactory
     */
    public PieceFactory withWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * Use custom height
     * @param height
     * @return PieceFactory
     */
    public PieceFactory withHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * Use custom orientation
     * @param orientation
     * @return PieceFactory
     */
    public PieceFactory withOrientation(int orientation) {
        this.orientation = orientation;
        return this;
    }

    /**
     * Use a custom piece type ({@link #L}, {@link #T}, {@link #R} or {@link #I})
     * @param piece
     * @return PieceFactory
     */
    public PieceFactory withPiece(int piece) {
        this.piece = piece;
        return this;
    }

    /**
     * Build a new instance of piece
     * @return a new instance of piece
     */
    public Piece build() {
        switch (piece) {
            case L:  return new PieceL(width, height, orientation);
            case T:  return new PieceT(width, height, orientation);
            case R:  return new PieceR(width, height, orientation);
            case I:  return new PieceI(width, height, orientation);
            default: throw new IllegalStateException("Unknown piece type: " + piece);
        }
    }
    
    /**
     * Randomly build a new insance of piece
     * @return a new random instance of piece
     */
    public Piece buildRandom(){
        randomValues();
        return build();
    }

    /**
     * Generate random odd
     * @param min
     * @param max
     * @return
     */
    private static int generateRandomOdd(int min, int max) {
        int range = (max - min) / 2 + 1;
        return RANDOM.nextInt(range) * 2 + min;
    }

    /**
     * Set random values for properties
     */
    private void randomValues() {
        this.piece = RANDOM.nextInt(PIECE_TYPES);
        this.height = generateRandomOdd(3, 6);                       // 3 or 5
        this.width = (piece == I) ? 1 : generateRandomOdd(3, 4);     // I is a 1-wide bar
        this.orientation = RANDOM.nextInt(4);
    }
}

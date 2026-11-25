package piece;

/**
 * Small console demo: prints every piece type in its four orientations.
 */
public class Test {

    public static void main(String[] args) {
        boolean clockwise = true; // change this to test clockwise or anti-clockwise
        Piece[] tab = {new PieceL(5, 9), new PieceR(5, 9), new PieceT(5, 9), new PieceI(1, 5)};
        for (Piece piece : tab) {
            System.out.println("============== " + piece.getShape() + " ==============");
            for (int j = 0; j < 4; j++) {
                System.out.println(piece);
                if (clockwise)
                    piece.rotateClockWise();
                else
                    piece.rotateAntiClockWise();
            }
        }
    }
}

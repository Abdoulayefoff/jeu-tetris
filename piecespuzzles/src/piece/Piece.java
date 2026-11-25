package piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import piece.observer.AbstractListenableModel;

public abstract class Piece extends AbstractListenableModel implements PieceModel {
    protected final int width;
    protected final int height;
    protected int orientation; // 0 for North, 1 for East, 2 for south, 3 for west
    protected Coordinates outterCenter;
    protected static Map<String, boolean[][]> piecesMatrices = new HashMap<String, boolean[][]>(); 

    /**
     * Constructor for piece
     * @param width
     * @param height
     * @param orientation
     */
    public Piece(int width, int height, int orientation) {
        if(orientation < 0 || orientation > 3)
            throw new IllegalArgumentException("Orientation must be between 0 and 3");
        if(width <= 0 || height <= 0)
            throw new IllegalArgumentException("Width and height must be positive");
        
        this.width = width;
        this.height = height;
        this.orientation = orientation;

        // Build matrix
        boolean[][] matrix = buildMatrix();
        piecesMatrices.put(getKey(), matrix); 
    }

    /**
     * Constructor for piece
     * @param width
     * @param height
     */
    public Piece(int width, int height) {
        this(width, height, 0);
    }

    /**
     * Create a new piece with the same shape, base dimensions and orientation.
     * The clone has no position and no listeners.
     *
     * <p>Uses the base {@code width}/{@code height} fields (not the rotated
     * {@link #getWidth()}/{@link #getHeight()}), otherwise a rotated piece would be
     * cloned transposed.</p>
     *
     * @param piece the piece to copy
     * @return a fresh copy of the piece
     */
    public static Piece clone(Piece piece) {
        switch (piece.getShape()) {
            case "T": return new PieceT(piece.width, piece.height, piece.orientation);
            case "R": return new PieceR(piece.width, piece.height, piece.orientation);
            case "L": return new PieceL(piece.width, piece.height, piece.orientation);
            case "I": return new PieceI(piece.width, piece.height, piece.orientation);
            default:
                throw new IllegalArgumentException("Unknown shape: " + piece.getShape());
        }
    }

    /**
     * Base (orientation 0) width of the piece.
     */
    public int getBaseWidth() {
        return width;
    }

    /**
     * Base (orientation 0) height of the piece.
     */
    public int getBaseHeight() {
        return height;
    }

    /**
     * Build a matrix for an orientation
     * @return a matrix for an orientation
     */
    protected boolean[][] buildMatrix() {
        int width = 0;
        int height = 0;

        // Vertical orientation
        if(orientation == 0 || orientation == 2 ) {
            width = this.width;
            height = this.height;
        } else { // horizontal orientation: switch dimensions
            width = this.height;
            height = this.width;
        }

        // buil matrix
        boolean[][] matrix =new boolean[height][width];
        for(int i =0; i< height; i++){
            for(int j = 0; j< width; j++){
                matrix[i][j] = matrixCellVal(i, j, height, width);
            }

        }

        return matrix;
    }

    /**
     * Return a matrix cell value based on piece shape and orientation
     * @param i cell row
     * @param j cell col
     * @param height matrix height
     * @param width matrix width
     * @return a matrix cell value based on piece shape and orientation
     */
    protected abstract boolean matrixCellVal(int i, int j, int height, int width);


    /**
     * Return the shape of the piece
     * @return
     */
    public abstract String getShape();


    /**
     * Return matrix of a piece base on its current orientation
     * @return
     */
    public boolean[][] getCurrentMatrix() {
        return piecesMatrices.get(getKey());
    }

    /**
     * Getter for a piece current width
     * @return
     */
    public int getWidth() {
        return getCurrentMatrix()[0].length;
    }


    /**
     * Getter for a piece current height
     * @return
     */
    public int getHeight() {
        return getCurrentMatrix().length;
    }


    /**
     * Return the orientation of a piece
     * @return the orientation of a piece
     */
    public int getOrientation() {
        return orientation;
    }


    /**
     * Retunr inner center of a piece
     * @return inner center of a piec
     */
    public Coordinates getInnerCenter() {
        return new Coordinates(getHeight() / 2, getWidth() / 2); // x = row, y = column
    }

    /**
     * Return outter center of a piece
     * @return outter center of a piece
     */
    public Coordinates getOutterCenter() {
        return outterCenter;
    }

    
    /**
     * Return all pieces matirces
     * @return all pieces matirces
     */
    public static Map<String, boolean[][]> getPiecesMatrices() {
        return piecesMatrices;
    }

    /**
     * Return the key of a piece in the pieces matrices map
     * @return
     */
    public String getKey() {
        // separators avoid collisions such as "T" + 3 + 15 vs "T" + 31 + 5
        return getShape() + "_" + width + "x" + height + "_" + getOrientation();
    }

    public void setOutterCenter(int x, int y){
        this.outterCenter = new Coordinates(x, y);

        this.fireChangement();
    }

    /**
     * Return wether a cell of a piece is occupied or not
     * @param i column
     * @param j row
     * @return
     */
    public boolean isOccupied(int i, int j){
        return getCurrentMatrix()[i][j];
    }

    /**
     * Rotatce clockwise
     */
    public void rotateClockWise() {
        orientation = (orientation + 1) % 4;
        addMatrixToHashmap(); // add matrix to hashmap if not exists
        this.fireChangement();
    }

    /**
     * Rotatce anti clockwise
     */
    public void rotateAntiClockWise() {
        orientation = (orientation - 1 + 4) % 4;
        addMatrixToHashmap(); // add matrix to hashmap if not exists
        this.fireChangement();
    }

    /**
     * Add matrix to hashmap if not exists
     */
    protected void addMatrixToHashmap() {
        if(!piecesMatrices.containsKey(getKey())) {
            piecesMatrices.put(getKey(), buildMatrix());
        }
    }
    public List<Coordinates> getOccupiedExternesCoordinates() {
        return getOccupiedExternesCoordinatesAux(getCurrentMatrix(), getOutterCenter().getX(), getOutterCenter().getY());
    }

    // Méthode pour obtenir les coordonnées externes dans la petite matrice
    private List<Coordinates> getOccupiedExternesCoordinatesAux(boolean[][] smallMatrix, int centerX, int centerY) {
        int rows = smallMatrix.length;
        int cols = smallMatrix[0].length;

        // Stocker les coordonnées externes
        List<Coordinates> externalCoordinates = new ArrayList<>();

        // Parcourir la petite matrice
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Vérifier si la valeur dans la petite matrice est 1
                if (smallMatrix[i][j] == true) {
                    // Calculer les coordonnées externes dans la grande matrice
                    int externalX = centerX - rows / 2 + i;
                    int externalY = centerY - cols / 2 + j;

                    // Stocker les coordonnées externes
                    externalCoordinates.add(new Coordinates(externalX, externalY));
                }
            }
        }

        // Redimensionner le tableau résultant pour éliminer les cases non utilisées
        return externalCoordinates;
    }

    /**
     * Return the board rows (useRows = true) or columns (useRows = false)
     * spanned by the piece's bounding matrix at its current position.
     * Replaces the two former near-identical X/Y helpers.
     *
     * @param useRows true for rows (x), false for columns (y)
     * @return the spanned rows or columns, in increasing order
     */
    public int[] getSpannedCoordinates(boolean useRows) {
        int size = useRows ? getHeight() : getWidth();
        int center = useRows ? getOutterCenter().getX() : getOutterCenter().getY();
        int[] result = new int[size];
        for (int k = 0; k < size; k++) {
            result[k] = center - size / 2 + k;
        }
        return result;
    }

    /** First board row covered by the piece's bounding matrix. */
    public int getTopRow() {
        return getOutterCenter().getX() - getHeight() / 2;
    }

    /** First board column covered by the piece's bounding matrix. */
    public int getLeftCol() {
        return getOutterCenter().getY() - getWidth() / 2;
    }

    /**
     * Return string version of piece
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        boolean[][] matrix = getCurrentMatrix();
        for(int i =0; i< getHeight(); i++){
            for(int j = 0; j< getWidth(); j++){
                string.append(matrix[i][j] ? " 1 " : " . ");
            }
            string.append("\n");
        }
        string.append("\n");
        return string.toString();
    }
}

package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.placementstrategy.PlacementStrategy;
import model.placementstrategy.RandomStrategy;
import model.validators.AbstractValidator;
import model.validators.OverflowValidator;
import model.validators.OverlappingValidator;
import observer.AbstractListenableModel;
import piece.Coordinates;
import piece.Piece;
import piece.PieceFactory;

/**
 * The game board: a {@code height x width} grid holding the pieces.
 *
 * <p>Coordinates follow the project convention: {@code x} is the row
 * (0 .. height-1) and {@code y} is the column (0 .. width-1).</p>
 */
public class Plateau extends AbstractListenableModel {
    protected int width;
    protected int height;
    protected List<Piece> pieces;
    protected AbstractValidator validator;
    private PlacementStrategy placementStrategy;
    private int nbPieces;
    private boolean isEnabled;

    public Plateau(int width, int height, int nbPieces, PlacementStrategy strategy) {
        this.width = width;
        this.height = height;
        this.pieces = new ArrayList<>();
        this.nbPieces = nbPieces;
        this.placementStrategy = strategy;
        setValidator();
    }

    /**
     * Build the validation chain (Chain of Responsibility):
     * the position must stay inside the board, then must not overlap another piece.
     */
    public void setValidator() {
        this.validator = AbstractValidator.buildChain(new OverflowValidator(), new OverlappingValidator());
    }

    public boolean addPiece(Piece piece, Coordinates position) {
        if (this.validate(piece, position)) {
            piece.setOutterCenter(position.getX(), position.getY());
            this.pieces.add(piece);
            return true;
        }
        return false;
    }

    public boolean movePiece(Piece piece, Coordinates position) {
        if (validate(piece, position)) {
            piece.setOutterCenter(position.getX(), position.getY());
            return true;
        }
        return false;
    }

    public boolean rotateClockWisePiece(Piece piece) {
        piece.rotateClockWise();
        if (validate(piece, new Coordinates(piece.getOutterCenter().getX(), piece.getOutterCenter().getY()))) {
            return true;
        }
        piece.rotateAntiClockWise();
        return false;
    }

    public boolean rotateAntiClockWisePiece(Piece piece) {
        piece.rotateAntiClockWise();
        if (validate(piece, new Coordinates(piece.getOutterCenter().getX(), piece.getOutterCenter().getY()))) {
            return true;
        }
        piece.rotateClockWise();
        return false;
    }

    /**
     * Check, without changing anything, whether a piece could be rotated in place.
     * @param piece a piece of this board
     * @param clockwise rotation direction
     * @return whether the rotation would be accepted by the validators
     */
    public boolean canRotate(Piece piece, boolean clockwise) {
        int index = pieces.indexOf(piece);
        if (index < 0 || piece.getOutterCenter() == null) {
            return false;
        }
        Piece rotated = Piece.clone(piece);
        if (clockwise) {
            rotated.rotateClockWise();
        } else {
            rotated.rotateAntiClockWise();
        }
        rotated.setOutterCenter(piece.getOutterCenter().getX(), piece.getOutterCenter().getY());
        // swap the rotated copy in place of the piece (set() is not a structural change, so callers
        // iterating over getPieces() are not disturbed and the piece order is kept)
        pieces.set(index, rotated);
        try {
            return validate(rotated, rotated.getOutterCenter());
        } finally {
            pieces.set(index, piece);
        }
    }

    /**
     * @return the area of the smallest rectangle enclosing every piece (0 when the board is empty)
     */
    public int evaluate() {
        return Evaluateur.calculerAireRectangle(pieces);
    }

    public void initialConfig() {
        this.placementStrategy.initialConfig(this);
        this.fireChangement();
    }

    /**
     * Legacy quick configuration: six random pieces at fixed positions.
     */
    public void initialConfigTmp() {
        PieceFactory pieceFactory = new PieceFactory();
        this.addPiece(pieceFactory.buildRandom(), new Coordinates(3, 3));
        this.addPiece(pieceFactory.buildRandom(), new Coordinates(10, 10));
        this.addPiece(pieceFactory.buildRandom(), new Coordinates(20, 18));
        this.addPiece(pieceFactory.buildRandom(), new Coordinates(17, 6));
        this.addPiece(pieceFactory.buildRandom(), new Coordinates(10, 23));
        this.addPiece(pieceFactory.buildRandom(), new Coordinates(23, 13));
        this.fireChangement();
    }

    public void setPlacementStrategy(PlacementStrategy placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    public PlacementStrategy getPlacementStrategy() {
        return placementStrategy;
    }

    public boolean validate(Piece piece, Coordinates position) {
        return this.validator.validate(this, piece, position);
    }

    public void reset() {
        this.pieces.clear();
        this.fireChangement();
    }

    /**
     * Notify the views that the board changed (used after a bulk update such as loading a save).
     */
    public void notifyChanged() {
        this.fireChangement();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public AbstractValidator getValidator() {
        return validator;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public int getNbPieces() {
        return nbPieces;
    }

    public void setNbPieces(int nbPieces) {
        this.nbPieces = nbPieces;
    }

    public boolean randomPlacementStrategy() {
        return placementStrategy instanceof RandomStrategy;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        this.fireChangement();
    }

    /**
     * Text rendering of the board for the console mode.
     * Each cell is {@code .} when empty, or the shape letter of the piece occupying it.
     * Column indexes (mod 10) and row indexes are printed so the player can type coordinates.
     */
    public String affichagePlateau() {
        char[][] grid = new char[height][width];
        for (char[] row : grid) {
            Arrays.fill(row, '.');
        }
        for (Piece p : this.pieces) {
            char letter = p.getShape().charAt(0);
            for (Coordinates c : p.getOccupiedExternesCoordinates()) {
                if (c.getX() >= 0 && c.getX() < height && c.getY() >= 0 && c.getY() < width) {
                    grid[c.getX()][c.getY()] = letter;
                }
            }
        }

        StringBuilder res = new StringBuilder("    ");
        for (int j = 0; j < width; j++) {
            res.append(j % 10);
        }
        res.append('\n');
        for (int i = 0; i < height; i++) {
            res.append(String.format("%3d|", i)).append(grid[i]).append("|\n");
        }
        return res.toString();
    }
}

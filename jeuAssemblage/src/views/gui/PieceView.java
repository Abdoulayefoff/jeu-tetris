package views.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import javax.swing.SwingUtilities;

import model.Game;
import piece.Coordinates;
import piece.Piece;
import piece.observer.ListeningModel;

/**
 * Graphical view of one piece, drawn as a card-stock cut-out lying on the mat.
 * It can be clicked (selection) and dragged (move).
 */
public class PieceView extends DraggableComponent implements ListeningModel {

    private static final long serialVersionUID = 1L;
    /** gap between the card edge and the cell edge, in pixels (also the shadow offset) */
    private static final float CARD_INSET = 2f;
    /** distance of the dashed selection line from the card edge */
    private static final float SCORE_INSET = 4f;

    private final Piece piece;
    private final Game game;
    private final BoardView board;
    private final Color color;
    private final Color edge;

    public PieceView(Game game, Piece piece, BoardView board) {
        this.piece = piece;
        this.game = game;
        this.board = board;
        this.color = PieceColors.of(piece.getShape());
        this.edge = PieceColors.isLight(color) ? new Color(0xB9B5A8) : color.darker();
        this.piece.addListening(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        drawPiece();
    }

    /** Stop listening to the model (called when the view is discarded). */
    public void dispose() {
        piece.removeListening(this);
    }

    /** Place and size the component according to the piece position and the current cell size. */
    public void drawPiece() {
        Coordinates center = piece.getOutterCenter();
        if (center == null) {
            return;
        }
        int cw = board.getCellWidth();
        int ch = board.getCellHeight();
        setBounds(piece.getLeftCol() * cw, piece.getTopRow() * ch, piece.getWidth() * cw, piece.getHeight() * ch);
        repaint();
    }

    public Piece getPiece() {
        return piece;
    }

    public boolean isSelected() {
        return board.getSelectedPiece() == piece;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int cw = board.getCellWidth();
        int ch = board.getCellHeight();
        boolean selected = isSelected();

        // the outline of the cut-out card: the union of its cells, trimmed so the mat shows around it
        Area cells = new Area();
        for (int i = 0; i < piece.getHeight(); i++) {
            for (int j = 0; j < piece.getWidth(); j++) {
                if (piece.isOccupied(i, j)) {
                    cells.add(new Area(new Rectangle(j * cw, i * ch, cw, ch)));
                }
            }
        }
        Area card = new Area(cells);
        card.subtract(new Area(new BasicStroke(2 * CARD_INSET).createStrokedShape(cells)));

        // a hard, short shadow: the card lies on the mat, and lifts a little when picked up
        Area shadow = card.createTransformedArea(AffineTransform.getTranslateInstance(CARD_INSET, CARD_INSET));
        g2d.setColor(selected ? Theme.MAT_SHADOW.darker() : Theme.MAT_SHADOW);
        g2d.fill(shadow);
        if (selected) {
            card.transform(AffineTransform.getTranslateInstance(-1, -1));
        }
        g2d.setColor(color);
        g2d.fill(card);
        g2d.setColor(edge);
        g2d.setStroke(new BasicStroke(1f));
        g2d.draw(card);

        if (selected) {
            // a dashed scored line just inside the edge marks the piece in hand
            Area score = new Area(card);
            score.subtract(new Area(new BasicStroke(2 * SCORE_INSET).createStrokedShape(card)));
            g2d.setColor(PieceColors.isLight(color) ? Theme.INK : Theme.PANEL);
            g2d.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                    new float[] {4f, 3f}, 0f));
            g2d.draw(score);
        }
        g2d.dispose();
    }

    @Override
    public int ajustX(int x) {
        int cw = board.getCellWidth();
        int col = Math.floorDiv(x + cw / 2, cw);
        return col * cw;
    }

    @Override
    public int ajustY(int y) {
        int ch = board.getCellHeight();
        int row = Math.floorDiv(y + ch / 2, ch);
        return row * ch;
    }

    @Override
    protected boolean canDrag() {
        return game.canMove() && !game.isAIPlaying();
    }

    @Override
    protected void onMousePressed() {
        if (canDrag()) {
            board.setSelectedPiece(piece);
        }
    }

    @Override
    public void afterMouseDragged(int x, int y) {
        // nothing: the target cell is computed on release
    }

    @Override
    public void onMouseReleased() {
        int row = getY() / board.getCellHeight() + piece.getHeight() / 2;
        int col = getX() / board.getCellWidth() + piece.getWidth() / 2;
        Coordinates target = new Coordinates(row, col);
        if (target.equals(piece.getOutterCenter()) || !game.movePiece(piece, target)) {
            drawPiece(); // invalid or unchanged: snap back
        }
    }

    @Override
    public void onMouseClicked() {
        if (canDrag()) {
            board.setSelectedPiece(piece); // deselect with Escape or a click on an empty cell
        }
    }

    @Override
    public void modelUpdated(Object source) {
        if (SwingUtilities.isEventDispatchThread()) {
            drawPiece();
        } else {
            SwingUtilities.invokeLater(this::drawPiece);
        }
    }
}

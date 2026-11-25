package views.gui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.Evaluateur;
import model.Game;
import model.Plateau;
import observer.ListeningModel;
import piece.Piece;

/**
 * The game grid, drawn as a cutting mat: it paints the cells, measures the bounding
 * rectangle of the pieces and hosts one
 * {@link PieceView} per piece. It also owns the current piece selection.
 */
public class BoardView extends JPanel implements ListeningModel {

    private static final long serialVersionUID = 1L;

    private static final int PREFERRED_CELL = 22;
    /** a stronger grid line every RULING cells, as on a cutting mat */
    private static final int RULING = 5;
    private static final Font MEASURE_FONT = Theme.demi(12f);

    private final Plateau plateau;
    private final Game game;
    private final int rows;
    private final int cols;

    private int cellWidth = PREFERRED_CELL;
    private int cellHeight = PREFERRED_CELL;
    private final List<PieceView> pieceViews = new ArrayList<>();
    private Piece selectedPiece;
    private final List<Runnable> selectionListeners = new ArrayList<>();

    public BoardView(Game game, Plateau plateau) {
        this.plateau = plateau;
        this.game = game;
        this.rows = plateau.getHeight();
        this.cols = plateau.getWidth();
        setLayout(null);
        setBackground(Theme.PANEL);
        setOpaque(true);
        setPreferredSize(new Dimension(cols * PREFERRED_CELL + 1, rows * PREFERRED_CELL + 1));

        plateau.addListening(this);   // pieces added / removed
        game.addListening(source -> repaintLater()); // any action: refresh the bounding box

        // a click on an empty cell clears the selection
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setSelectedPiece(null);
            }
        });
        rebuildPieceViews();
    }

    // ------------------------------------------------------------------ selection

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public void setSelectedPiece(Piece piece) {
        if (piece == selectedPiece) {
            return;
        }
        selectedPiece = piece;
        for (PieceView v : pieceViews) {
            v.repaint();
        }
        // keep the selected piece on top of the others
        for (PieceView v : pieceViews) {
            if (v.getPiece() == piece) {
                setComponentZOrder(v, 0);
            }
        }
        selectionListeners.forEach(Runnable::run);
    }

    public void addSelectionListener(Runnable listener) {
        selectionListeners.add(listener);
    }

    // ------------------------------------------------------------------ layout

    public int getCellWidth() {
        return cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public Plateau getPlateau() {
        return plateau;
    }

    @Override
    public void doLayout() {
        // square cells that follow the panel size
        int cell = Math.max(1, Math.min((getWidth() - 1) / cols, (getHeight() - 1) / rows));
        cellWidth = cell;
        cellHeight = cell;
        for (PieceView v : pieceViews) {
            v.drawPiece();
        }
    }

    private void rebuildPieceViews() {
        for (PieceView v : pieceViews) {
            v.dispose();
        }
        pieceViews.clear();
        removeAll();
        if (selectedPiece != null && !plateau.getPieces().contains(selectedPiece)) {
            selectedPiece = null;
            selectionListeners.forEach(Runnable::run);
        }
        for (Piece piece : plateau.getPieces()) {
            PieceView view = new PieceView(game, piece, this);
            pieceViews.add(view);
            add(view);
        }
        revalidate();
        doLayout();
        repaint();
    }

    // ------------------------------------------------------------------ painting

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        int gw = cols * cellWidth;
        int gh = rows * cellHeight;

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Theme.MAT);
        g2d.fillRect(0, 0, gw, gh);

        // the printed grid of a cutting mat: thin lines, a stronger ruling every five cells
        for (int pass = 0; pass < 2; pass++) {
            g2d.setColor(pass == 0 ? Theme.MAT_LINE : Theme.MAT_RULE);
            for (int i = 0; i <= rows; i++) {
                if ((i % RULING == 0) == (pass == 1)) {
                    g2d.drawLine(0, i * cellHeight, gw, i * cellHeight);
                }
            }
            for (int j = 0; j <= cols; j++) {
                if ((j % RULING == 0) == (pass == 1)) {
                    g2d.drawLine(j * cellWidth, 0, j * cellWidth, gh);
                }
            }
        }

        int[] box = Evaluateur.boundingBox(plateau.getPieces());
        if (box != null) {
            paintMeasure(g2d, box);
        }
        g2d.dispose();
    }

    /**
     * Draw the bounding rectangle the player has to shrink as a technical dimension:
     * a yellow frame plus a width line above it and a height line on its right,
     * each with slash ticks and the number of cells.
     */
    private void paintMeasure(Graphics2D g2d, int[] box) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int x = box[1] * cellWidth;
        int y = box[0] * cellHeight;
        int widthCells = box[3] - box[1] + 1;
        int heightCells = box[2] - box[0] + 1;
        int w = widthCells * cellWidth;
        int h = heightCells * cellHeight;

        g2d.setColor(Theme.MEASURE_WASH);
        g2d.fillRect(x, y, w, h);
        g2d.setColor(Theme.MEASURE);
        g2d.setStroke(new BasicStroke(1.6f));
        g2d.drawRect(x, y, w, h);

        int gap = Math.max(8, Math.round(cellHeight * 0.7f));
        // width: above the frame, or just inside it when the frame touches the top edge
        int dy = (y - gap >= 10) ? y - gap : y + gap;
        // height: right of the frame, or just inside it when it touches the right edge
        int dx = (x + w + gap <= cols * cellWidth - 10) ? x + w + gap : x + w - gap;

        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawLine(x, dy, x + w, dy);
        g2d.drawLine(dx, y, dx, y + h);
        int t = 4;
        g2d.setStroke(new BasicStroke(1.8f));
        for (int px : new int[] {x, x + w}) {
            g2d.drawLine(px - t, dy + t, px + t, dy - t);
        }
        for (int py : new int[] {y, y + h}) {
            g2d.drawLine(dx - t, py + t, dx + t, py - t);
        }
        label(g2d, String.valueOf(widthCells), x + w / 2, dy);
        label(g2d, String.valueOf(heightCells), dx, y + h / 2);
    }

    /** A number centred on a point, on a patch of mat so the dimension line breaks around it. */
    private void label(Graphics2D g2d, String text, int cx, int cy) {
        g2d.setFont(MEASURE_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getAscent() - fm.getDescent() / 2;
        g2d.setColor(Theme.MAT);
        g2d.fillRect(cx - tw / 2 - 4, cy - th / 2 - 3, tw + 8, th + 6);
        g2d.setColor(Theme.MEASURE);
        g2d.drawString(text, cx - tw / 2, cy + th / 2);
    }

    private void repaintLater() {
        if (SwingUtilities.isEventDispatchThread()) {
            repaint();
        } else {
            SwingUtilities.invokeLater(this::repaint);
        }
    }

    @Override
    public void modelUpdated(Object source) {
        if (SwingUtilities.isEventDispatchThread()) {
            rebuildPieceViews();
        } else {
            SwingUtilities.invokeLater(this::rebuildPieceViews);
        }
    }
}

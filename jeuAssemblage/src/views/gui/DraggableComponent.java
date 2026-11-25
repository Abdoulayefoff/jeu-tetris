package views.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/**
 * A component that can be dragged with the mouse inside its parent, snapping to a grid.
 * Subclasses decide whether dragging is allowed and what to do when it ends.
 */
public abstract class DraggableComponent extends JComponent {

    private static final long serialVersionUID = 1L;

    protected int screenX = 0;
    protected int screenY = 0;
    protected int myX = 0;
    protected int myY = 0;
    /** whether the component was actually moved since the last mouse press */
    protected boolean dragged = false;

    protected DraggableComponent() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // start a new drag from the current position
                screenX = e.getXOnScreen();
                screenY = e.getYOnScreen();
                myX = getX();
                myY = getY();
                dragged = false;
                onMousePressed();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragged) {
                    onMouseReleased();
                }
                dragged = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                onMouseEntered();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!canDrag()) {
                    return;
                }
                int x = ajustX(myX + e.getXOnScreen() - screenX);
                int y = ajustY(myY + e.getYOnScreen() - screenY);
                if (x != getX() || y != getY()) {
                    dragged = true;
                    afterMouseDragged(x, y);
                    setLocation(x, y);
                    if (getParent() != null) {
                        getParent().repaint();
                    }
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    /** Snap a horizontal position to the grid. */
    public abstract int ajustX(int x);

    /** Snap a vertical position to the grid. */
    public abstract int ajustY(int y);

    /** @return whether the component may currently be dragged */
    protected abstract boolean canDrag();

    public abstract void afterMouseDragged(int x, int y);

    public abstract void onMouseReleased();

    public abstract void onMouseClicked();

    protected void onMousePressed() {
    }

    public void onMouseEntered() {
    }
}

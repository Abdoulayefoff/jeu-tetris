package views.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;

import javax.swing.JComponent;

/**
 * A row of mutually exclusive options drawn as one outlined strip, the chosen one filled in ink.
 */
public class SegmentedControl extends JComponent {

    private static final long serialVersionUID = 1L;
    private static final int ARC = 8;
    private static final int HEIGHT = 32;

    private final String[] options;
    private int selected;
    private IntConsumer listener = i -> { };

    public SegmentedControl(String... options) {
        this.options = options.clone();
        setFont(Theme.medium(13f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) {
                    return;
                }
                int i = Math.min(SegmentedControl.this.options.length - 1,
                        e.getX() * SegmentedControl.this.options.length / Math.max(1, getWidth()));
                if (i != selected) {
                    selected = i;
                    repaint();
                    listener.accept(i);
                }
            }
        });
    }

    /** Called with the index of the option the user picked. */
    public void onChange(IntConsumer listener) {
        this.listener = listener;
    }

    public int getSelectedIndex() {
        return selected;
    }

    /** Select an option without notifying the listener. */
    public void setSelectedIndex(int index) {
        if (index != selected) {
            selected = index;
            repaint();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int widest = 0;
        for (String o : options) {
            widest = Math.max(widest, fm.stringWidth(o));
        }
        return new Dimension(options.length * (widest + 24), HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        boolean enabled = isEnabled();
        int w = getWidth() - 1;
        int h = getHeight() - 1;
        int n = options.length;
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();

        // filled segment: clip a rounded rectangle to the segment so only the outer corners are round
        int x0 = selected * w / n;
        int x1 = (selected + 1) * w / n;
        g2.setColor(enabled ? Theme.INK : Theme.INK_FAINT);
        g2.setClip(x0, 0, x1 - x0 + 1, h + 1);
        g2.fillRoundRect(0, 0, w, h, ARC, ARC);
        g2.setClip(null);

        g2.setColor(enabled ? Theme.INK_SOFT : Theme.HAIRLINE);
        g2.drawRoundRect(0, 0, w, h, ARC, ARC);
        for (int i = 1; i < n; i++) {
            int x = i * w / n;
            if (i != selected && i != selected + 1) {
                g2.drawLine(x, 1, x, h - 1);
            }
        }

        for (int i = 0; i < n; i++) {
            int sx = i * w / n;
            int sw = (i + 1) * w / n - sx;
            g2.setColor(i == selected ? Theme.PANEL : enabled ? Theme.INK : Theme.INK_FAINT);
            g2.drawString(options[i], sx + (sw - fm.stringWidth(options[i])) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent() + 1);
        }
        g2.dispose();
    }
}

package views.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * A flat, hand-drawn button: either a filled ink "primary" button or an outlined one.
 */
public class FlatButton extends JButton {

    private static final long serialVersionUID = 1L;
    private static final int ARC = 8;
    private static final int HEIGHT = 34;

    private final boolean primary;

    public FlatButton(String label, boolean primary) {
        super(label);
        this.primary = primary;
        setUI(new BasicButtonUI());
        setFont(Theme.medium(13f));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false); // keep the keyboard shortcuts working
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 28, HEIGHT);
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
        ButtonModel m = getModel();
        boolean enabled = isEnabled();
        int w = getWidth();
        int h = getHeight();

        Color fill;
        Color text;
        Color border = null;
        if (primary) {
            fill = !enabled ? Theme.HAIRLINE : m.isPressed() ? Color.BLACK : m.isRollover() ? new Color(0x2C3A35) : Theme.INK;
            text = enabled ? Theme.PANEL : Theme.INK_FAINT;
        } else {
            fill = enabled && m.isPressed() ? Theme.HAIRLINE : enabled && m.isRollover() ? Theme.PANEL_DEEP : null;
            text = enabled ? Theme.INK : Theme.INK_FAINT;
            border = enabled ? Theme.INK_SOFT : Theme.HAIRLINE;
        }
        if (fill != null) {
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);
        }
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);
        }
        g2.setFont(getFont());
        g2.setColor(text);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}

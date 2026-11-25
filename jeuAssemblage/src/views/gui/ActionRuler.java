package views.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * The remaining actions drawn as the notches of a ruler: one notch per action,
 * a longer one every five. Spent notches fade out, the last five turn red.
 */
public class ActionRuler extends JComponent {

    private static final long serialVersionUID = 1L;
    private static final int LOW_THRESHOLD = 5;

    private final int total;
    private int remaining;
    private boolean live;

    public ActionRuler(int total) {
        this.total = total;
        this.remaining = total;
        setFont(Theme.regular(10f));
    }

    /**
     * @param remaining actions left
     * @param live whether a game is running (spent notches are only meaningful then)
     */
    public void setRemaining(int remaining, boolean live) {
        this.remaining = remaining;
        this.live = live;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(220, 38);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 38);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();

        int base = 22;            // y of the ruler edge
        float pad = 3f;
        float step = (getWidth() - 2 * pad - 10) / (float) (total - 1);
        boolean low = live && remaining <= LOW_THRESHOLD;
        Color on = low ? Theme.ALERT : Theme.INK;

        g2.setColor(Theme.HAIRLINE);
        g2.fillRect(0, base, getWidth(), 1);
        for (int i = 0; i < total; i++) {
            boolean major = (i + 1) % 5 == 0;
            int len = major ? 18 : 11;
            int x = Math.round(pad + i * step);
            g2.setColor(i < remaining ? on : Theme.HAIRLINE);
            g2.fillRoundRect(x, base - len, 3, len, 2, 2);
            if (major) {
                String n = String.valueOf(i + 1);
                g2.setColor(Theme.INK_FAINT);
                g2.drawString(n, x + 1 - fm.stringWidth(n) / 2f, base + 4 + fm.getAscent());
            }
        }
        g2.dispose();
    }
}

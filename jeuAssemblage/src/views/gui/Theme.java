package views.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Visual identity of the interface: a green cutting mat on a pale workbench,
 * card-stock pieces and a yellow measuring frame.
 */
public final class Theme {

    private Theme() {
    }

    // workbench and ink
    public static final Color PANEL = new Color(0xE3E8E4);
    public static final Color PANEL_DEEP = new Color(0xD6DDD8);
    public static final Color HAIRLINE = new Color(0xC3CCC6);
    public static final Color INK = new Color(0x1F2A26);
    public static final Color INK_SOFT = new Color(0x5B6B65);
    public static final Color INK_FAINT = new Color(0x9AA8A1);

    // cutting mat
    public static final Color MAT = new Color(0x2E5A4C);
    public static final Color MAT_LINE = new Color(0x3B6B5C);
    public static final Color MAT_RULE = new Color(0x4F8070);
    public static final Color MAT_SHADOW = new Color(0, 0, 0, 70);

    // measuring frame
    public static final Color MEASURE = new Color(0xF2DB6B);
    public static final Color MEASURE_WASH = new Color(0xF2, 0xDB, 0x6B, 22);

    // players and warnings (reuse the card colours)
    public static final Color PLAYER_1 = new Color(0x2F5FA6);
    public static final Color PLAYER_2 = new Color(0xD2493A);
    public static final Color ALERT = new Color(0xD2493A);

    private static final String FAMILY = "Avenir Next";

    public static Font regular(float size) {
        return load("AvenirNext-Regular", Font.PLAIN, size);
    }

    public static Font medium(float size) {
        return load("AvenirNext-Medium", Font.PLAIN, size);
    }

    public static Font demi(float size) {
        return load("AvenirNext-DemiBold", Font.BOLD, size);
    }

    /**
     * Load an Avenir Next face by its PostScript name, falling back to the
     * logical SansSerif font (with the given style) when it is not installed.
     */
    private static Font load(String face, int fallbackStyle, float size) {
        Font f = new Font(face, Font.PLAIN, 1);
        if (!FAMILY.equals(f.getFamily())) {
            f = new Font(Font.SANS_SERIF, fallbackStyle, 1);
        }
        return f.deriveFont(size);
    }
}

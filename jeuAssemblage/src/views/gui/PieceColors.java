package views.gui;

import java.awt.Color;
import java.util.Map;

import model.GameConfig;

/**
 * Maps a piece shape ("T", "L", "R", "I") to its card colour, as configured in {@link GameConfig}.
 */
public class PieceColors {

    private static final Map<String, Color> COLORS = Map.of(
            "T", GameConfig.PIECE_T_COLOR,
            "L", GameConfig.PIECE_L_COLOR,
            "R", GameConfig.PIECE_R_COLOR,
            "I", GameConfig.PIECE_I_COLOR);

    /** @return the immutable shape → colour mapping */
    public Map<String, Color> getColors() {
        return COLORS;
    }

    /** @return the colour of a shape, grey for an unknown one */
    public static Color of(String shape) {
        return COLORS.getOrDefault(shape, Color.GRAY);
    }

    /** @return whether text or marks drawn on this card should be dark (light card) */
    public static boolean isLight(Color c) {
        return (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) > 160;
    }
}

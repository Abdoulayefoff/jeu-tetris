package model;

import java.awt.Color;

/**
 * Central place for the game constants.
 */
public final class GameConfig {
    public static final int MAX_ACTIONS = 20;
    public static final int COUNT_PIECES = 15;
    public static final int BOARD_SIZE = 30;

    /** Default delay between two AI moves, in milliseconds. */
    public static final int AI_MOVE_DELAY_MS = 1000;
    /** Bounds of the AI speed slider, in milliseconds. */
    public static final int AI_MIN_DELAY_MS = 100;
    public static final int AI_MAX_DELAY_MS = 2000;
    /** Maximum number of random tries an AI makes before giving up on a move. */
    public static final int AI_MAX_ROTATE_ATTEMPTS = 100;
    /** Consecutive unproductive AI turns after which the AI stops playing. */
    public static final int AI_MAX_IDLE_TURNS = 10;
    /** Maximum number of random tries when placing one piece with the random strategy. */
    public static final int PLACEMENT_MAX_ATTEMPTS = 500;

    public static final Color PIECE_I_COLOR = new Color(244, 242, 234);  // white card
    public static final Color PIECE_T_COLOR = new Color(47, 95, 166);   // cobalt
    public static final Color PIECE_L_COLOR = new Color(210, 73, 58);   // vermilion
    public static final Color PIECE_R_COLOR = new Color(226, 179, 59);  // mustard

    private GameConfig() {
    }
}

package model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.placementstrategy.ArbitraryStrategy;
import piece.Coordinates;
import piece.Piece;
import piece.PieceFactory;

/**
 * Saves and loads a game as a small, human-readable JSON document.
 *
 * <pre>{@code
 * {
 *   "version": 1,
 *   "boardWidth": 30, "boardHeight": 30,
 *   "remainingActions": 12, "score": 0, "onGoing": true,
 *   "twoPlayers": false, "currentPlayer": 1, "scores": [0, 0],
 *   "pieces": [ {"shape":"T","width":3,"height":5,"orientation":1,"x":4,"y":5}, ... ]
 * }
 * }</pre>
 *
 * <p>No external library is needed: the format is flat and parsed with regular expressions.</p>
 */
public final class GameSerializer {

    public static final int VERSION = 1;

    private static final Pattern PIECE = Pattern.compile("\\{([^{}]*)\\}");

    private GameSerializer() {
    }

    /** @return the JSON representation of the game */
    public static String toJson(Game game) {
        Plateau board = game.getBoard();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": ").append(VERSION).append(",\n");
        sb.append("  \"boardWidth\": ").append(board.getWidth()).append(",\n");
        sb.append("  \"boardHeight\": ").append(board.getHeight()).append(",\n");
        sb.append("  \"remainingActions\": ").append(game.getRemainingActions()).append(",\n");
        sb.append("  \"score\": ").append(game.getScore()).append(",\n");
        sb.append("  \"onGoing\": ").append(game.isOnGoing()).append(",\n");
        if (game instanceof MultiplayerGame) {
            MultiplayerGame mg = (MultiplayerGame) game;
            sb.append("  \"twoPlayers\": ").append(mg.isTwoPlayers()).append(",\n");
            sb.append("  \"currentPlayer\": ").append(mg.getCurrentPlayer()).append(",\n");
            sb.append("  \"scores\": [").append(mg.getPlayerScore(1)).append(", ")
                    .append(mg.getPlayerScore(2)).append("],\n");
        }
        sb.append("  \"pieces\": [");
        List<Piece> pieces = board.getPieces();
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    {\"shape\":\"").append(p.getShape()).append('"')
                    .append(",\"width\":").append(p.getBaseWidth())
                    .append(",\"height\":").append(p.getBaseHeight())
                    .append(",\"orientation\":").append(p.getOrientation())
                    .append(",\"x\":").append(p.getOutterCenter().getX())
                    .append(",\"y\":").append(p.getOutterCenter().getY())
                    .append('}');
        }
        sb.append(pieces.isEmpty() ? "]\n" : "\n  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Replace the state of {@code game} with the saved one.
     * @throws IllegalArgumentException if the document is malformed or describes an invalid board
     */
    public static void applyJson(String json, Game game) {
        Plateau board = game.getBoard();
        int width = intField(json, "boardWidth");
        int height = intField(json, "boardHeight");
        if (width != board.getWidth() || height != board.getHeight()) {
            throw new IllegalArgumentException("Board size " + width + "x" + height
                    + " does not match " + board.getWidth() + "x" + board.getHeight());
        }
        List<Piece> pieces = parsePieces(json);
        int remainingActions = intField(json, "remainingActions");
        int score = intField(json, "score");
        boolean onGoing = booleanField(json, "onGoing");
        boolean hasMultiplayer = game instanceof MultiplayerGame && json.contains("\"twoPlayers\"");
        boolean twoPlayers = hasMultiplayer && booleanField(json, "twoPlayers");
        int currentPlayer = hasMultiplayer ? intField(json, "currentPlayer") : 1;
        int[] scores = hasMultiplayer ? intArray(json, "scores") : new int[2];

        // check every placement on a scratch board first, so that an invalid file
        // leaves the current game untouched
        Plateau scratch = new Plateau(width, height, 0, new ArbitraryStrategy());
        for (Piece piece : pieces) {
            Coordinates center = piece.getOutterCenter();
            if (!scratch.addPiece(Piece.clone(piece), center)) {
                throw new IllegalArgumentException("Invalid position for piece " + piece.getKey()
                        + " at " + center);
            }
        }

        game.stopAI();
        board.reset();
        for (Piece piece : pieces) {
            board.addPiece(piece, piece.getOutterCenter());
        }
        game.restoreState(remainingActions, score, onGoing);
        if (hasMultiplayer) {
            ((MultiplayerGame) game).restoreMultiplayer(twoPlayers, currentPlayer, scores[0], scores[1]);
        }
        board.notifyChanged();
    }

    /** @return a new game built from the saved document */
    public static MultiplayerGame fromJson(String json) {
        Plateau board = new Plateau(intField(json, "boardWidth"), intField(json, "boardHeight"),
                GameConfig.COUNT_PIECES, new ArbitraryStrategy());
        MultiplayerGame game = new MultiplayerGame(board);
        applyJson(json, game);
        return game;
    }

    public static void save(Game game, Path file) throws IOException {
        Files.writeString(file, toJson(game), StandardCharsets.UTF_8);
    }

    public static void load(Path file, Game game) throws IOException {
        applyJson(Files.readString(file, StandardCharsets.UTF_8), game);
    }

    // ---------------------------------------------------------------- parsing helpers

    private static List<Piece> parsePieces(String json) {
        int start = json.indexOf("\"pieces\"");
        if (start < 0) {
            throw new IllegalArgumentException("Missing field \"pieces\"");
        }
        int open = json.indexOf('[', start);
        int close = json.indexOf(']', open);
        if (open < 0 || close < 0) {
            throw new IllegalArgumentException("Malformed field \"pieces\"");
        }
        List<Piece> pieces = new ArrayList<>();
        Matcher m = PIECE.matcher(json.substring(open + 1, close));
        while (m.find()) {
            String obj = m.group(1);
            Piece piece = new PieceFactory()
                    .withPiece(shapeToType(stringField(obj, "shape")))
                    .withWidth(intField(obj, "width"))
                    .withHeight(intField(obj, "height"))
                    .withOrientation(intField(obj, "orientation"))
                    .build();
            piece.setOutterCenter(intField(obj, "x"), intField(obj, "y"));
            pieces.add(piece);
        }
        return pieces;
    }

    private static int shapeToType(String shape) {
        switch (shape) {
            case "L": return PieceFactory.L;
            case "T": return PieceFactory.T;
            case "R": return PieceFactory.R;
            case "I": return PieceFactory.I;
            default: throw new IllegalArgumentException("Unknown shape: " + shape);
        }
    }

    private static String raw(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*(\"[^\"]*\"|-?\\d+|true|false)").matcher(json);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing field \"" + name + "\"");
        }
        return m.group(1);
    }

    private static int intField(String json, String name) {
        try {
            return Integer.parseInt(raw(json, name));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Field \"" + name + "\" is not an integer", e);
        }
    }

    private static boolean booleanField(String json, String name) {
        String v = raw(json, name);
        if (!"true".equals(v) && !"false".equals(v)) {
            throw new IllegalArgumentException("Field \"" + name + "\" is not a boolean");
        }
        return Boolean.parseBoolean(v);
    }

    private static String stringField(String json, String name) {
        String v = raw(json, name);
        if (!v.startsWith("\"")) {
            throw new IllegalArgumentException("Field \"" + name + "\" is not a string");
        }
        return v.substring(1, v.length() - 1);
    }

    private static int[] intArray(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\]").matcher(json);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing field \"" + name + "\"");
        }
        return new int[] {Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
    }
}

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.Game;
import model.GameSerializer;
import model.MultiplayerGame;
import model.Plateau;
import model.placementstrategy.ArbitraryStrategy;
import model.placementstrategy.RandomStrategy;
import piece.Coordinates;
import piece.Piece;

class GameSerializerTest {

    private static MultiplayerGame playedGame() {
        MultiplayerGame game = new MultiplayerGame(new Plateau(30, 30, 9, new ArbitraryStrategy()));
        game.setTwoPlayers(true);
        game.start();
        Piece p = game.getBoard().getPieces().get(8);
        Coordinates c = p.getOutterCenter();
        game.movePiece(p, new Coordinates(c.getX() - 1, c.getY() - 1));
        game.rotatePiece(game.getBoard().getPieces().get(1), true);
        return game;
    }

    private static String describe(List<Piece> pieces) {
        StringBuilder sb = new StringBuilder();
        for (Piece p : pieces) {
            sb.append(p.getKey()).append('@').append(p.getOutterCenter()).append(';');
        }
        return sb.toString();
    }

    @Test
    void json_round_trip_restores_everything() {
        MultiplayerGame original = playedGame();
        MultiplayerGame copy = GameSerializer.fromJson(GameSerializer.toJson(original));

        assertEquals(describe(original.getBoard().getPieces()), describe(copy.getBoard().getPieces()));
        assertEquals(original.getRemainingActions(), copy.getRemainingActions());
        assertEquals(original.isOnGoing(), copy.isOnGoing());
        assertEquals(original.getBoard().evaluate(), copy.getBoard().evaluate());
        assertTrue(copy.isTwoPlayers());
        assertEquals(original.getCurrentPlayer(), copy.getCurrentPlayer());
        assertEquals(original.getPlayerScore(1), copy.getPlayerScore(1));
        assertEquals(original.getPlayerScore(2), copy.getPlayerScore(2));
    }

    @Test
    void file_round_trip(@TempDir Path dir) throws IOException {
        MultiplayerGame original = playedGame();
        Path file = dir.resolve("partie.json");
        GameSerializer.save(original, file);
        assertTrue(Files.size(file) > 0);

        Game target = new Game(new Plateau(30, 30, 9, new RandomStrategy()));
        GameSerializer.load(file, target);
        assertEquals(describe(original.getBoard().getPieces()), describe(target.getBoard().getPieces()));
        assertEquals(original.getRemainingActions(), target.getRemainingActions());
        assertTrue(target.canMove());
    }

    @Test
    void loaded_game_can_be_played() {
        MultiplayerGame copy = GameSerializer.fromJson(GameSerializer.toJson(playedGame()));
        Piece p = copy.getBoard().getPieces().get(0);
        Coordinates c = p.getOutterCenter();
        assertTrue(copy.movePiece(p, new Coordinates(c.getX() + 1, c.getY())));
    }

    @Test
    void board_size_mismatch_is_rejected() {
        String json = GameSerializer.toJson(playedGame());
        Game small = new Game(new Plateau(10, 10, 3, new RandomStrategy()));
        assertThrows(IllegalArgumentException.class, () -> GameSerializer.applyJson(json, small));
    }

    @Test
    void garbage_is_rejected() {
        Game game = new Game(new Plateau(30, 30, 9, new ArbitraryStrategy()));
        assertThrows(IllegalArgumentException.class, () -> GameSerializer.applyJson("not json", game));
    }

    @Test
    void invalid_placement_leaves_current_game_untouched() {
        String json = GameSerializer.toJson(playedGame());
        int start = json.indexOf("{\"shape\"");
        int end = json.indexOf('}', start) + 1;
        // the same piece twice at the same place: the second one overlaps the first
        String broken = json.substring(0, end) + ",\n    " + json.substring(start, end) + json.substring(end);

        MultiplayerGame target = playedGame();
        String piecesBefore = describe(target.getBoard().getPieces());
        int actionsBefore = target.getRemainingActions();

        assertThrows(IllegalArgumentException.class, () -> GameSerializer.applyJson(broken, target));
        assertEquals(piecesBefore, describe(target.getBoard().getPieces()));
        assertEquals(actionsBefore, target.getRemainingActions());
        assertTrue(target.isOnGoing());
    }
}

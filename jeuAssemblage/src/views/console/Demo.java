package views.console;

import java.util.List;
import java.util.Scanner;

import model.Game;
import model.GameConfig;
import model.Plateau;
import model.placementstrategy.ArbitraryStrategy;
import piece.Coordinates;
import piece.Piece;

/**
 * Console version of the game: the board is printed as text and the player types commands.
 */
public class Demo {

    private final Game jeu;
    private final Scanner scanner;

    public Demo(Game jeu, Scanner scanner) {
        this.jeu = jeu;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        Plateau plateau = new Plateau(GameConfig.BOARD_SIZE, GameConfig.BOARD_SIZE,
                GameConfig.COUNT_PIECES, new ArbitraryStrategy());
        new Demo(new Game(plateau), new Scanner(System.in)).run();
    }

    public void run() {
        jeu.start();
        while (jeu.canMove()) {
            System.out.println(jeu.getBoard().affichagePlateau());
            System.out.println("Score actuel : " + jeu.getCurrentScore()
                    + " | Actions restantes : " + jeu.getRemainingActions());
            listPieces();
            System.out.println("Commandes : d (déplacer), h (rotation horaire), a (rotation anti-horaire), q (quitter)");
            String cmd = scanner.hasNextLine() ? scanner.nextLine().trim().toLowerCase() : "q";
            switch (cmd) {
                case "d": move(); break;
                case "h": rotate(true); break;
                case "a": rotate(false); break;
                case "q": jeu.gameOver(); break;
                default: System.out.println("Commande inconnue.");
            }
        }
        System.out.println(jeu.getBoard().affichagePlateau());
        System.out.println("Partie terminée ! Score final : " + jeu.getScore());
    }

    private void listPieces() {
        List<Piece> pieces = jeu.getBoard().getPieces();
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            System.out.println("  [" + i + "] " + p.getShape() + " " + p.getWidth() + "x" + p.getHeight()
                    + " centre " + p.getOutterCenter().getX() + "," + p.getOutterCenter().getY());
        }
    }

    private Piece askPiece() {
        int index = askInt("Numéro de la pièce : ");
        List<Piece> pieces = jeu.getBoard().getPieces();
        if (index < 0 || index >= pieces.size()) {
            System.out.println("Pièce inexistante.");
            return null;
        }
        return pieces.get(index);
    }

    private void move() {
        Piece piece = askPiece();
        if (piece == null) {
            return;
        }
        int row = askInt("Ligne du centre : ");
        int col = askInt("Colonne du centre : ");
        if (!jeu.movePiece(piece, new Coordinates(row, col))) {
            System.out.println("Déplacement impossible.");
        }
    }

    private void rotate(boolean clockwise) {
        Piece piece = askPiece();
        if (piece != null && !jeu.rotatePiece(piece, clockwise)) {
            System.out.println("Rotation impossible.");
        }
    }

    private int askInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return -1;
            }
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre entier.");
            }
        }
    }
}

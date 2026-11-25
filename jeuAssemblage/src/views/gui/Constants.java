package views.gui;

/**
 * User-facing labels of the graphical interface (in French).
 */
public final class Constants {

    private Constants() {
    }

    public static final String WINDOW_TITLE = "Jeu d'assemblage";

    // game
    public static final String GAME_GROUP = "Partie";
    public static final String START_GAME_LABEL = "Commencer";
    public static final String RESTART_GAME_LABEL = "Recommencer";
    public static final String END_GAME_LABEL = "Terminer";
    public static final String LAYOUT_LABEL = "Disposition";
    public static final String DEFAULT_CONFIG_LABEL = "Fixe";
    public static final String RANDOM_CONFIG_LABEL = "Aléatoire";
    public static final String MODE_LABEL = "Joueurs";
    public static final String ONE_PLAYER_LABEL = "Seul";
    public static final String TWO_PLAYERS_LABEL = "À deux";

    // pieces
    public static final String PIECE_GROUP = "Tourner la pièce";
    public static final String ROTATE_ACW_LABEL = "−90°";
    public static final String ROTATE_CW_LABEL = "+90°";

    // AI
    public static final String AI_GROUP = "Faire jouer l'ordinateur";
    public static final String RANDOM_AI_LABEL = "Au hasard";
    public static final String ADVANCED_AI_LABEL = "Avancée";
    public static final String GREEDY_AI_LABEL = "Gloutonne";
    public static final String STOP_AI_LABEL = "Arrêter l'ordinateur";
    public static final String AI_SPEED_LABEL = "Vitesse";
    public static final String SLOW_LABEL = "Lent";
    public static final String FAST_LABEL = "Rapide";

    // file
    public static final String FILE_GROUP = "Fichier";
    public static final String SAVE_LABEL = "Enregistrer";
    public static final String LOAD_LABEL = "Ouvrir…";

    // readouts
    public static final String SCORE_LABEL = "Score";
    public static final String SCORE_IDLE_NOTE = "Commencez une partie pour poser les pièces.";
    public static final String SCORE_LIVE_NOTE = "en cours, sur %d";
    public static final String SCORE_FINAL_NOTE = "score final, sur %d";
    public static final String ACTIONS_LABEL = "Actions restantes";
    public static final String RECTANGLE_LABEL = "Rectangle";
    public static final String AREA_NOTE = "%d cases, %d %% du tapis";
    public static final String PIECES_NOTE = "%d pièces sur le tapis";
    public static final String PLAYERS_LABEL = "Joueurs";
    public static final String PLAYER_LABEL = "Joueur ";
    public static final String YOUR_TURN_SUFFIX = ", à vous";
    public static final String CURRENT_PLAYER_LABEL = "au tour du joueur ";

    // status bar hints
    public static final String HINT_IDLE = "Choisissez une disposition, puis cliquez sur Commencer.";
    public static final String HINT_PLAY = "Cliquez sur une pièce pour la prendre, ou faites-la glisser sur le tapis.";
    public static final String HINT_SELECTED = "Flèches : déplacer d'une case.   R / E : tourner.   Échap : reposer la pièce.";
    public static final String HINT_AI = "L'ordinateur joue. « Arrêter l'ordinateur » vous rend la main.";
    public static final String HINT_OVER = "Partie terminée. Recommencer pour rejouer.";
    public static final String HINT_TURN = "Joueur %d : ";
    public static final String HINT_FILE = "%s+S enregistrer   %s+O ouvrir";

    // dialogs
    public static final String WARNING_LABEL = "Attention";
    public static final String ERROR_LABEL = "Erreur";
    public static final String WARNING_CHANGE_CONFIG_LABEL = "Impossible de changer la configuration d'une partie en cours. "
            + "Terminez la partie pour choisir une nouvelle disposition.";
    public static final String WARNING_CHANGE_MODE_LABEL = "Impossible de changer de mode pendant une partie. "
            + "Terminez la partie pour changer de mode.";
    public static final String GAME_OVER_TITLE = "Fin de la partie";
    public static final String GAME_OVER_HEADING = "Partie terminée";
    public static final String GAME_OVER_LABEL = "Rectangle final : %d × %d, soit %d cases.";
    public static final String WINNER_LABEL = "Le joueur %d gagne, %d contre %d.";
    public static final String DRAW_LABEL = "Égalité : %d partout.";
    public static final String PLAY_AGAIN_LABEL = "Rejouer";
    public static final String CLOSE_LABEL = "Fermer";
    public static final String SELECT_PIECE_LABEL = "Cliquez sur une pièce avant de la faire tourner.";
    public static final String WARNING_START_GAME_BEFORE_LABEL = "Commencez une partie avant de continuer.";
    public static final String WARNING_AI_TWO_PLAYERS_LABEL = "L'ordinateur ne joue qu'en partie seule.";
    public static final String SAVE_ERROR_LABEL = "Impossible d'enregistrer la partie : ";
    public static final String LOAD_ERROR_LABEL = "Impossible d'ouvrir la partie : ";
    public static final String SAVE_FILE_FILTER = "Parties enregistrées (*.json)";
}

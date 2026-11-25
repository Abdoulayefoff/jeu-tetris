package model;

/**
 * A game that can be played by two players on the same board ("hot seat").
 *
 * <p>When the two-player mode is on, players alternate after every successful
 * action. Each action earns the current player the reduction of the bounding
 * rectangle it caused (a move that enlarges the rectangle costs points).
 * When the mode is off, this class behaves exactly like {@link Game}.</p>
 */
public class MultiplayerGame extends Game {
    private boolean twoPlayers;
    private int currentPlayer = 1;
    private final int[] scores = {0, 0};

    public MultiplayerGame(Plateau board) {
        super(board);
    }

    /**
     * Switch the two-player mode on or off. Only possible between two games.
     * @return whether the mode was changed
     */
    public boolean setTwoPlayers(boolean twoPlayers) {
        if (isOnGoing()) {
            return false;
        }
        this.twoPlayers = twoPlayers;
        fireChangement();
        return true;
    }

    public boolean isTwoPlayers() {
        return twoPlayers;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * @param player 1 or 2
     * @return the score of that player
     */
    public int getPlayerScore(int player) {
        return scores[player - 1];
    }

    /**
     * @return 1 or 2 for the winner, 0 for a draw
     */
    public int getWinner() {
        if (scores[0] == scores[1]) {
            return 0;
        }
        return scores[0] > scores[1] ? 1 : 2;
    }

    @Override
    protected void onStart() {
        currentPlayer = 1;
        scores[0] = 0;
        scores[1] = 0;
    }

    @Override
    protected void onSuccessfulAction(int areaBefore, int areaAfter) {
        if (!twoPlayers) {
            return;
        }
        scores[currentPlayer - 1] += areaBefore - areaAfter;
        currentPlayer = 3 - currentPlayer; // alternate between 1 and 2
    }

    @Override
    public synchronized boolean playAI(model.ai.AI ai) {
        if (twoPlayers) {
            return false; // the AI is a single-player feature
        }
        return super.playAI(ai);
    }

    /**
     * Restore the multiplayer part of a saved state (used by {@link GameSerializer}).
     */
    public void restoreMultiplayer(boolean twoPlayers, int currentPlayer, int score1, int score2) {
        this.twoPlayers = twoPlayers;
        this.currentPlayer = (currentPlayer == 2) ? 2 : 1;
        this.scores[0] = score1;
        this.scores[1] = score2;
        fireChangement();
    }
}

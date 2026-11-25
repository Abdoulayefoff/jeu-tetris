package model;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import model.ai.AI;
import model.ai.AdvancedAI;
import model.ai.GreedyAI;
import model.ai.RandomAI;
import model.placementstrategy.ArbitraryStrategy;
import model.placementstrategy.RandomStrategy;
import observer.AbstractListenableModel;
import piece.Coordinates;
import piece.Piece;

/**
 * A single-player game session.
 *
 * <p>The player has {@link GameConfig#MAX_ACTIONS} actions (a move or a rotation)
 * to pack the pieces as tightly as possible. The final score is
 * {@code boardArea - boundingRectangleArea}: the higher, the better.</p>
 */
public class Game extends AbstractListenableModel {
    protected Plateau board;
    protected int score;
    protected int remainingActions;
    protected boolean onGoing;
    protected volatile boolean isAIplaying;

    private volatile int aiDelay = GameConfig.AI_MOVE_DELAY_MS;
    private volatile boolean aiStopRequested;
    private Thread aiThread;

    public Game(Plateau board) {
        this.board = board;
        this.remainingActions = GameConfig.MAX_ACTIONS;
        this.onGoing = false;
        this.isAIplaying = false;
    }

    /**
     * Start (or restart) a game: clear the board, place the pieces with the
     * selected placement strategy and reset the score and the action counter.
     */
    public void start() {
        stopAI();
        board.reset();
        board.initialConfig();

        this.score = 0;
        this.remainingActions = GameConfig.MAX_ACTIONS;
        this.onGoing = true;
        this.board.setEnabled(true);
        onStart();
        this.fireChangement();
    }

    /** Hook for subclasses, called by {@link #start()} before listeners are notified. */
    protected void onStart() {
    }

    /**
     * @return whether the player still has actions left in a running game
     */
    public boolean canMove() {
        return onGoing && this.remainingActions > 0;
    }

    public boolean movePiece(Piece piece, Coordinates position) {
        if (!canMove()) {
            return false;
        }
        int areaBefore = board.evaluate();
        boolean success = this.board.movePiece(piece, position);
        afterAction(success, areaBefore);
        return success;
    }

    public boolean rotatePiece(Piece piece, boolean clockwise) {
        if (!canMove()) {
            return false;
        }
        int areaBefore = board.evaluate();
        boolean success = clockwise
                ? this.board.rotateClockWisePiece(piece)
                : this.board.rotateAntiClockWisePiece(piece);
        afterAction(success, areaBefore);
        return success;
    }

    private void afterAction(boolean success, int areaBefore) {
        if (success) {
            this.remainingActions--;
            onSuccessfulAction(areaBefore, board.evaluate());
        }
        if (remainingActions <= 0) {
            gameOver();
        } else {
            this.fireChangement();
        }
    }

    /**
     * Hook for subclasses, called after each action that consumed an action point.
     * @param areaBefore bounding area before the action
     * @param areaAfter bounding area after the action
     */
    protected void onSuccessfulAction(int areaBefore, int areaAfter) {
    }

    /**
     * @return the final score (only meaningful once the game is over)
     */
    public int getScore() {
        return this.score;
    }

    /**
     * @return the score the player would get if the game ended now
     */
    public int getCurrentScore() {
        return (board.getHeight() * board.getWidth()) - board.evaluate();
    }

    public void calculateScore() {
        this.score = getCurrentScore();
    }

    /**
     * End the game and compute the final score. Calling it twice has no extra effect.
     */
    public void gameOver() {
        if (!onGoing) {
            return;
        }
        calculateScore();
        this.onGoing = false;
        this.fireChangement();
    }

    public void playRandomAI() {
        playAI(new RandomAI());
    }

    public void playAdvancedAI() {
        playAI(new AdvancedAI());
    }

    public void playGreedyAI() {
        playAI(new GreedyAI());
    }

    /**
     * Let an AI play the remaining actions on a background thread.
     * Every AI move is executed on the Swing event thread (so the views never see
     * a half-updated board), with {@link #getAIDelay()} ms between two moves.
     * The AI stops when the game ends, when {@link #stopAI()} is called, or after
     * {@link GameConfig#AI_MAX_IDLE_TURNS} turns in a row without progress.
     *
     * @return whether the AI was started
     */
    public synchronized boolean playAI(AI ai) {
        if (!isOnGoing() || isAIplaying) {
            return false;
        }
        this.isAIplaying = true;
        this.aiStopRequested = false;
        this.board.setEnabled(false);
        this.fireChangement();

        aiThread = new Thread(() -> runAI(ai), "ai-player");
        aiThread.setDaemon(true);
        aiThread.start();
        return true;
    }

    private void runAI(AI ai) {
        int idleTurns = 0;
        try {
            while (canMove() && !aiStopRequested) {
                int before = remainingActions;
                runOnEdt(() -> ai.nextMove(this));
                idleTurns = (remainingActions == before) ? idleTurns + 1 : 0;
                if (idleTurns >= GameConfig.AI_MAX_IDLE_TURNS) {
                    break; // the AI cannot find a legal or useful move any more
                }
                Thread.sleep(aiDelay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            boolean stoppedByUser = aiStopRequested;
            runOnEdt(() -> {
                isAIplaying = false;
                board.setEnabled(true);
                if (!stoppedByUser) {
                    gameOver();
                }
                fireChangement();
            });
        }
    }

    private static void runOnEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("AI move failed", e.getCause());
        }
    }

    /**
     * Ask the running AI to stop after its current move. The game keeps going
     * and the human player can continue with the remaining actions.
     */
    public void stopAI() {
        aiStopRequested = true;
        Thread t = aiThread;
        if (t != null) {
            t.interrupt();
        }
    }

    public boolean isAIPlaying() {
        return isAIplaying;
    }

    /**
     * @param ms delay between two AI moves, clamped to the slider bounds
     */
    public void setAIDelay(int ms) {
        this.aiDelay = Math.max(0, ms);
    }

    public int getAIDelay() {
        return aiDelay;
    }

    public Plateau getBoard() {
        return board;
    }

    public int getRemainingActions() {
        return remainingActions;
    }

    public boolean isOnGoing() {
        return onGoing;
    }

    /**
     * Restore a saved state (used by {@link GameSerializer}).
     */
    public void restoreState(int remainingActions, int score, boolean onGoing) {
        stopAI();
        this.remainingActions = remainingActions;
        this.score = score;
        this.onGoing = onGoing;
        this.board.setEnabled(true);
        this.fireChangement();
    }

    public boolean useRandomPlacementStrategy(boolean yes) {
        if (onGoing) {
            return false;
        }
        if (yes) {
            this.board.setPlacementStrategy(new RandomStrategy());
        } else {
            this.board.setPlacementStrategy(new ArbitraryStrategy());
        }
        return true;
    }
}

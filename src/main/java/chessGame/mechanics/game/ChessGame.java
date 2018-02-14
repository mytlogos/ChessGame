package chessGame.mechanics.game;

import chessGame.mechanics.Player;
import chessGame.mechanics.move.PlayerMove;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

import java.util.Queue;

/**
 * Interface for usage of a Game which connects to the Gui.
 */
public interface ChessGame extends Game {
    BooleanProperty madeMoveProperty();

    IntegerProperty roundProperty();

    ReadOnlyBooleanProperty finishedProperty();

    Queue<PlayerMove> getRedoQueue();

    void setLoser(Player player);

    boolean isFinished();

    boolean isRunning();

    void setRunning(boolean running);

    ReadOnlyBooleanProperty runningProperty();

    boolean isWon();

    Player getWinner();

    boolean isDraw();

    boolean isPaused();

    void setPaused(boolean paused);

    void decideEnd();

    void redo();

    void start();

    boolean isRedo();

    void nextRound();

    int getRound();

    void setRound(int round);

    SimulationGame getSimulation();

}

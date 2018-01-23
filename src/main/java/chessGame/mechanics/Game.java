package chessGame.mechanics;

import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.Pawn;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface Game {
    void start();

    List<PlayerMove> getAllowedMoves();

    MoveHistory getHistory();

    /**
     * Gets the Bench of the Player.
     * A Bench holds the beaten Figures of the enemy.
     *
     * @return a Map of player and their figures they beat
     */
    Map<Player, List<Figure>> getBench();

    Map<Player, List<Pawn>> getPromoted();

    Player getWhite();

    Player getBlack();

    IntegerProperty roundProperty();

    boolean makeMove(PlayerMove move) throws IllegalMoveException;

    boolean redo();

    boolean simulateRedo();

    int getRound();

    void setRound(int round);

    void addPromoted(Pawn figure);

    void addBench(Figure figure);

    PlayerMove getLastMove();

    void removeFromBench(Figure figure);

    void removeFromPromoted(Pawn figure);

    ObjectProperty<PlayerMove> lastMoveProperty();

    Player getAtMove();

    ReadOnlyBooleanProperty finishedProperty();

    Board getBoard();

    void atMoveFinished();

    void simulateAtMoveFinished();

    void setLoser(Player player);

    Player getEnemy(Player player);

    boolean isFinished();

    boolean isRunning();

    void setRunning(boolean running);

    ReadOnlyBooleanProperty runningProperty();

    ReadOnlyStringProperty timeProperty();

    boolean isWon();

    Player getWinner();

    boolean isDraw();

    boolean isPaused();

    void setPaused(boolean paused);

    void decideEnd();

    void nextRound();

    Game copy();
}

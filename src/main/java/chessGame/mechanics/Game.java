package chessGame.mechanics;

import chessGame.gui.BoardGrid;
import chessGame.gui.RoundManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Game {
    private final BoardGrid boardGrid;
    private Timer timer;
    private List<PlayerMove> moves = new ArrayList<>();

    private BooleanProperty running = new SimpleBooleanProperty();
    private BooleanProperty paused = new SimpleBooleanProperty();
    private BooleanProperty finished = new SimpleBooleanProperty();

    public Game(BoardGrid boardGrid) {
        this.boardGrid = boardGrid;

        timer = new Timer();
        boardGrid.buildBoard();
        initListener(boardGrid);
    }

    private void initListener(BoardGrid boardGrid) {
        boardGrid.getBoard().lastMoveProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                moves.add(newValue);
            }
        });

        pausedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
            } else {
                timer.start();
            }
        });

        runningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.start();
                boardGrid.getBoard().atMoveProperty().set(Player.WHITE);
            } else {
                restart();
            }
        });

        finishedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
            }
        });
    }

    public List<PlayerMove> getMoves() {
        return moves;
    }

    public ReadOnlyStringProperty timeProperty() {
        return timer.timeProperty();
    }

    public boolean isRunning() {
        return running.get();
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public BooleanProperty pausedProperty() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused.set(paused);
    }

    public boolean isFinished() {
        return finished.get();
    }

    public BooleanProperty finishedProperty() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished.set(finished);
    }

    public void restart() {
        saveGame();
        timer = new Timer();
        boardGrid.buildBoard();
        finishedProperty().set(false);
        pausedProperty().set(false);
        runningProperty().set(false);
    }

    private void saveGame() {
        //todo save game for playing again or further review
    }
}

package chessGame.mechanics;

import chessGame.engine.EngineWorker;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Game {
    public Board board;
    private Timer timer;

    private List<PlayerMove> movesHistory = new ArrayList<>();
    private Player white;
    private Player black;

    private Player loser;
    private Player atMovePlayer;

    private BooleanProperty running = new SimpleBooleanProperty();
    private BooleanProperty paused = new SimpleBooleanProperty();
    private BooleanProperty finished = new SimpleBooleanProperty();

    public Game(Board board) {
        init(board);
    }

    public Game(List<Player> players) {
        final Player player1 = players.get(0);
        final Player player2 = players.get(1);

        if (player1.isWhite() && !player2.isWhite()) {
            white = player1;
            black = player2;
        } else if (player2.isWhite() && !player1.isWhite()) {
            white = player2;
            black = player1;
        } else {
            throw new IllegalArgumentException();
        }
        init(new Board(white, black, this));
        EngineWorker.getEngine().addGame(this);
    }

    private void init(Board board) {
        this.board = board;
        initListener();

        timer = new Timer();
        board.buildBoard();
    }

    public Player getWhite() {
        return white;
    }

    public Player getBlack() {
        return black;
    }

    private void initListener() {
        board.lastMoveProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                movesHistory.add(newValue);
            }
        });

        board.getAllowedMoves().addListener((InvalidationListener) observable -> {
            if (board.getAllowedMoves().isEmpty()) {
                setLoser(board.getAtMovePlayer());
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
                setFinished(false);
                timer = new Timer();
                timer.start();
                board.atMovePlayerProperty().set(white);
            } else {
                setFinished(true);
            }
        });

        finishedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                System.out.println("winner: " + getWinner());
            }
        });
    }

    public List<PlayerMove> getMovesHistory() {
        return movesHistory;
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
        board.buildBoard();
        finishedProperty().set(false);
        pausedProperty().set(false);
        runningProperty().set(false);
    }

    public Board getBoard() {
        return board;
    }

    private void saveGame() {
        //todo save game for playing again or further review
    }

    public void redoLastMove() {
        if (!movesHistory.isEmpty()) {
            final PlayerMove move = movesHistory.get(movesHistory.size() - 1);
            try {
                board.makeMove(move);
            } catch (IllegalMoveException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLoser(Player player) {
        this.loser = player;
        setRunning(false);
    }

    public boolean isWon() {
        return isFinished() && loser != null;
    }

    public boolean isDraw() {
        return isFinished() && loser == null;
    }

    public Player getLoser() {
        return loser;
    }

    public Player getWinner() {
        return white.equals(loser) ? black : black.equals(loser) ? white : null;
    }
}

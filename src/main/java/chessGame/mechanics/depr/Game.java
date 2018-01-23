package chessGame.mechanics.depr;

import chessGame.engine.EngineWorker;
import chessGame.mechanics.Player;
import chessGame.mechanics.Timer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.List;

/**
 *
 */
public final class Game {
  /*  private Board board;
    private Timer timer;

    private Player white;
    private Player black;

    private Player loser;

    private BooleanProperty running = new SimpleBooleanProperty();
    private BooleanProperty paused = new SimpleBooleanProperty();
    private BooleanProperty finished = new SimpleBooleanProperty();

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
        EngineWorker.getEngineWorker().getEngines(this);
    }

    private void init(Board board) {
        this.board = board;
        initListener();

        timer = new Timer();
    }

    private void initListener() {
        pausedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
            } else {
                timer.start();
            }
        });

        runningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !isFinished()) {
                System.out.println("starting");
                timer = new Timer();
                timer.start();
                board.atMovePlayerProperty().set(white);
            } else {
                setFinished(true);
                System.out.println(timer.timeProperty().get());
            }
        });

        finishedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                System.out.println("winner: " + getWinner());
            }
        });
    }

    public BooleanProperty pausedProperty() {
        return paused;
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public boolean isFinished() {
        return finished.get();
    }

    public BooleanProperty finishedProperty() {
        return finished;
    }

    public Player getWinner() {
        return white.equals(loser) ? black : black.equals(loser) ? white : null;
    }

    public void setFinished(boolean finished) {
        this.finished.set(finished);
    }

    public Player getWhite() {
        return white;
    }

    public Player getBlack() {
        return black;
    }

    public ReadOnlyStringProperty timeProperty() {
        return timer.timeProperty();
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void setPaused(boolean paused) {
        this.paused.set(paused);
    }

    public Board getBoard() {
        return board;
    }

    public void redoLastMove() {
        final Player player = board.getAtMovePlayer();
        if (!white.isHuman()) {
            board.redo();

            if (!player.equals(white)) {
                board.redo();
            }
        } else if (!black.isHuman()) {
            board.redo();

            if (!player.equals(black)) {
                board.redo();
            }
        }
        if (white.isHuman() && black.isHuman()) {
            //todo request redo of enemy player
        }
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

    public void setLoser(Player player) {
        this.loser = player;
        setRunning(false);
    }

    private void saveGame() {
        //todo save game for playing again or further review
    }*/
}

package chessGame.engine;

import chessGame.mechanics.*;
import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.move.PlayerMove;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.*;

/**
 * The Base Class for the Chess Engine.
 * Needs to be initialised from the JavaFX-ApplicationThread.
 * It needs to be be started by a call of {@link #processRound(Number)}.
 */
public abstract class Engine extends Service<PlayerMove> {
    final ChessGame game;
    final Player player;
    int maxDepth;

    final int drawMali = -500;
    final int winBonus = 2000;

    private final Random random = new Random();

    Engine(ChessGame game, Player player, int maxDepth) {
        this(game, player);
        this.maxDepth = maxDepth;
    }

    Engine(ChessGame game, Player player) {
        Objects.requireNonNull(game);
        Objects.requireNonNull(player);

        if (!Platform.isFxApplicationThread()) {
            throw new IllegalThreadStateException("Wurde nicht vom FX-Thread initialisiert");
        }

        this.game = game;
        this.player = player;
    }

    public void processRound(Number newValue) {
        System.out.println("new round" + newValue + "|" + player);
        if (newValue != null && Objects.equals(game.getAtMove(), player)) {
            System.out.println("starting up");
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(this::startEngine);
            } else {
                startEngine();
            }
        }
    }

    private void startEngine() {
        if (getState() == State.FAILED || getState() == State.SUCCEEDED) {
            restart();
        } else if (getState() == State.READY) {
            start();
        } else {
            throw new IllegalStateException("Engine wurde nicht in einem Anfangs oder EndZustand gestartet: War in Zustand: " + getState());
        }
    }

    @Override
    protected void succeeded() {
        if (!Platform.isFxApplicationThread()) {
            System.out.print("");
        }

        final PlayerMove playerMove = getValue();
        if (playerMove == null) {
            //null means player has no valid moves anymore, but does not result automatically in ones loss, can still claim draw
            game.decideEnd();
        } else {
            //make a move of this gameBoard on this gameBoard
            game.makeMove(playerMove);
            game.nextRound();
        }
    }

    @Override
    protected void failed() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("Engine für Spieler " + player.getColor() + " crashed");
        alert.show();
        getException().printStackTrace();
    }

    @Override
    protected Task<PlayerMove> createTask() {
        return new Task<PlayerMove>() {
            @Override
            protected PlayerMove call() {
                return getChoice();
            }
        };
    }

    abstract PlayerMove getChoice();

    /**
     * Chooses a {@link PlayerMove} from a List per Random.
     *
     * @param moves moves to choose from
     * @return a PlayerMove or null if List is empty
     */
    PlayerMove chooseMove(List<PlayerMove> moves) {
        return moves.isEmpty() ? null : moves.get(random.nextInt(moves.size()));
    }
}


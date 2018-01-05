package chessGame.engine;

import chessGame.mechanics.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 */
public class EngineWorker implements ChessEngine {
    private ObservableList<Game> games = FXCollections.observableArrayList();

    private static EngineWorker engine = new EngineWorker();

    private EngineWorker() {
        if (engine != null) {
            throw new IllegalStateException();
        }
        initListener();
    }

    private void initListener() {
        games.addListener((ListChangeListener<? super Game>) observable -> {
            if (observable.next()) {
                observable.getAddedSubList().forEach(this::addEngine);
            }
        });
    }

    private void addEngine(Game game) {
        game.finishedProperty().addListener((observable, oldValue, newValue) -> games.remove(game));
        if (game.getBlack().isHuman() && game.getWhite().isHuman()) {
            games.remove(game);
        } else {
            game.getBoard().atMoveProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.getPlayer().isHuman()) {
                    makeMove(game, newValue);
                }
            });
        }
    }

    public static EngineWorker getEngine() {
        return engine;
    }

    @Override
    public void addGame(Game game) {
        games.add(game);
    }

    private void makeMove(Game game, AtMove atMove) {
        final EngineTask engineTask = new EngineTask(game, atMove);
        engineTask.setOnSucceeded(event -> {
            final PlayerMove move = engineTask.getValue();
            try {
                if (move == null) {
                    System.out.println(atMove + " lost");
                    game.setLoser(atMove.getPlayer());
                } else {
                    System.out.println("making move");
                    game.getBoard().makeMove(move);
                    System.out.println("made move");
                }
            } catch (IllegalMoveException e) {
                Platform.runLater(e::printStackTrace);
            }
        });
        engineTask.start();
    }
}

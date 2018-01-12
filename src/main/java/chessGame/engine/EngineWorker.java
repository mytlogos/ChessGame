package chessGame.engine;

import chessGame.mechanics.*;
import javafx.application.Platform;
import javafx.collections.*;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EngineWorker{
    private Map<Game, Map<Player, Engine>> games = new HashMap<>();

    private static EngineWorker engine = new EngineWorker();

    private EngineWorker() {
        if (engine != null) {
            throw new IllegalStateException();
        }
    }

    public static EngineWorker getEngineWorker() {
        return engine;
    }

    public void addGame(Game game) {
        game.finishedProperty().addListener((observable, oldValue, newValue) -> games.remove(game));
        final Player black = game.getBlack();

        if (!black.isHuman()) {
            final Engine engine = black.getDifficulty().getEngine(game, black);
            games.computeIfAbsent(game, game1 -> new HashMap<>()).put(black, engine);
        }

        final Player white = game.getWhite();

        if (!white.isHuman()) {
            final Engine engine = black.getDifficulty().getEngine(game, black);
            games.computeIfAbsent(game, game1 -> new HashMap<>()).put(black, engine);
        }
    }
}

package chessGame.engine;

import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.Player;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EngineWorker {
    private static final EngineWorker engine = new EngineWorker();
    private final Map<ChessGame, Map<Player, Engine>> games = new HashMap<>();

    private EngineWorker() {
        if (engine != null) {
            throw new IllegalStateException();
        }
    }

    public static EngineWorker getEngineWorker() {
        return engine;
    }

    public Map<Player, Engine> getEngines(ChessGame game) {
        Map<Player, Engine> engineMap = new HashMap<>();
        game.finishedProperty().addListener((observable, oldValue, newValue) -> processFinish(game, newValue));
        final Player black = game.getBlack();

        if (!black.isHuman()) {
            Engine engine = black.getDifficulty().getEngine(game, black);
            games.computeIfAbsent(game, game1 -> new HashMap<>()).put(black, engine);
            engineMap.put(black, engine);
        }

        final Player white = game.getWhite();

        if (!white.isHuman()) {
            Engine engine = white.getDifficulty().getEngine(game, white);
            games.computeIfAbsent(game, game1 -> new HashMap<>()).put(white, engine);
            engineMap.put(white, engine);
        }
        return engineMap;
    }

    private void processFinish(ChessGame game, boolean finished) {
        if (finished) {
            Map<Player, Engine> engineMap = games.remove(game);

            if (engineMap != null) {
                engineMap.values().forEach(Engine::cancel);
            }
        }
    }
}

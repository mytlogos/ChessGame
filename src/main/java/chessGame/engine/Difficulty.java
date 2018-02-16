package chessGame.engine;

import chessGame.mechanics.Player;
import chessGame.mechanics.game.ChessGame;

/**
 *
 */
public enum Difficulty {
    ROOKIE(((game, player, depth1) -> new RookieEngine(game, player)), 0),

    EASY(AlphaBetaExtendedEngine::new, 2),

    INTERMEDIATE(AlphaBetaExtendedEngine::new, 4),

    HARD(AlphaBetaExtendedEngine::new, 6),

    PROFESSIONAL(AlphaBetaEngine::new, 8),

    KOREAN(AlphaBetaEngine::new, 10),;

    private final EngineSupplier supplier;
    private final int depth;

    Difficulty(EngineSupplier supplier, int depth) {
        this.supplier = supplier;
        this.depth = depth;
    }

    Engine getEngine(ChessGame game, Player player) {
        return supplier.getEngine(game, player, depth);
    }

    private interface EngineSupplier {
        Engine getEngine(ChessGame game, Player player, int depth);
    }

}

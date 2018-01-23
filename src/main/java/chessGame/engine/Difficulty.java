package chessGame.engine;

import chessGame.mechanics.Game;
import chessGame.mechanics.Player;

/**
 *
 */
public enum Difficulty {
    ROOKIE {
        @Override
        Engine getEngine(Game game, Player player) {
            return new RookieEngine(game, player);
        }
    },
    EASY {
        @Override
        Engine getEngine(Game game, Player player) {
            return new AlphaBetaEngine(game, player, 2);
        }
    },
    INTERMEDIATE {
        @Override
        Engine getEngine(Game game, Player player) {
            return new AlphaBetaEngine(game, player, 4);
        }
    },
    HARD {
        @Override
        Engine getEngine(Game game, Player player) {
            return new AlphaBetaEngine(game, player, 6);
        }
    },
    PROFESSIONAL {
        @Override
        Engine getEngine(Game game, Player player) {
            return new AlphaBetaEngine(game, player, 8);
        }
    },
    KOREAN {
        @Override
        Engine getEngine(Game game, Player player) {
            return new AlphaBetaEngine(game, player, 10);
        }
    },;

    abstract Engine getEngine(Game game, Player player);
}

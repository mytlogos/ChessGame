package chessGame.engine;

import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.Player;

/**
 *
 */
public enum Difficulty {
    ROOKIE {
        @Override
        Engine getEngine(ChessGame game, Player player) {
            return new RookieEngine(game, player);
        }
    },
    EASY {
        @Override
        Engine getEngine(ChessGame game, Player player) {
            return new AlphaBetaExtendedEngine(game, player, 2);
        }
    },
    INTERMEDIATE {
        @Override
        Engine getEngine(ChessGame game, Player player) {
            return new AlphaBetaExtendedEngine(game, player, 4);
        }
    },
    HARD {
        @Override
        Engine getEngine(ChessGame game, Player player) {
            return new AlphaBetaExtendedEngine(game, player, 6);
        }
    },
    PROFESSIONAL {
        @Override
        Engine getEngine(ChessGame game, Player player) {
            return new AlphaBetaEngine(game, player, 8);
        }
    },
    KOREAN {
        @Override
        Engine getEngine(ChessGame game, Player player) {
            return new AlphaBetaEngine(game, player, 10);
        }
    },;

    abstract Engine getEngine(ChessGame game, Player player);
}

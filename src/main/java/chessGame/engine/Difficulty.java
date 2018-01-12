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
            return new Engine(game, player,1);
        }
    },
    INTERMEDIATE {
        @Override
        Engine getEngine(Game game, Player player) {
            return new Engine(game, player, 2);
        }
    },
    HARD {
        @Override
        Engine getEngine(Game game, Player player) {
            return new HardEngine(game, player);
        }
    },
    PROFESSIONAL {
        @Override
        Engine getEngine(Game game, Player player) {
            return new ProfessionalEngine(game, player);
        }
    },
    KOREAN {
        @Override
        Engine getEngine(Game game, Player player) {
            return new KoreanEngine(game, player);
        }
    },;

    abstract Engine getEngine(Game game, Player player);
}

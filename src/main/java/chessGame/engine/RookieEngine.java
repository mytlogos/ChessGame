package chessGame.engine;

import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.game.Game;
import chessGame.mechanics.Player;
import chessGame.mechanics.move.PlayerMove;

import java.util.List;

/**
 *
 */
class RookieEngine extends Engine {
    RookieEngine(ChessGame game, Player player) {
        super(game, player);
    }

    @Override
    PlayerMove getChoice() {
        final List<PlayerMove> moves = game.getAllowedMoves();
        return chooseMove(moves);
    }
}

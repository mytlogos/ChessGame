package chessGame.engine;

import chessGame.mechanics.Game;
import chessGame.mechanics.MoveGenerator;
import chessGame.mechanics.Player;
import chessGame.mechanics.PlayerMove;

import java.util.List;

/**
 *
 */
public class RookieEngine extends Engine {
    RookieEngine(Game game, Player player) {
        super(game, player);
    }

    @Override
    PlayerMove getChoice() {
        final List<PlayerMove> moves = MoveGenerator.getAllowedMoves(player, game);
        return chooseMove(moves);
    }
}

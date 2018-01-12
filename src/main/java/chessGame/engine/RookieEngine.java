package chessGame.engine;

import chessGame.mechanics.*;

import java.util.List;

/**
 *
 */
public class RookieEngine extends Engine {
    RookieEngine(Game game, Player player) {
        super(game,player);
    }

    @Override
    PlayerMove getChoice() {
        final List<PlayerMove> moves = game.getBoard().getGenerator().getAllowedMoves(player);
        return chooseMove(moves);
    }
}

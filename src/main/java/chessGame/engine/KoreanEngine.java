package chessGame.engine;

import chessGame.mechanics.Game;
import chessGame.mechanics.Player;
import chessGame.mechanics.PlayerMove;

import java.util.List;

/**
 *
 */
public class KoreanEngine extends Engine {

    KoreanEngine(Game game, Player player) {
        super(game,player);
    }

    @Override
    PlayerMove getChoice() {
        final List<PlayerMove> moves = game.getBoard().getGenerator().getAllowedMoves(player);
        return chooseMove(moves);
    }


}

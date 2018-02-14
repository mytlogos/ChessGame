package chessGame.multiplayer;

import chessGame.mechanics.Player;
import chessGame.mechanics.board.Board;
import chessGame.mechanics.game.ChessGameImpl;
import chessGame.mechanics.move.PlayerMove;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.BitSet;

/**
 *
 */
public class MultiPlayerGame extends ChessGameImpl {

    private final PlayerClient client;

    public MultiPlayerGame(PlayerClient client) {
        this.client = client;
    }

    public MultiPlayerGame(Player black, Player white, PlayerClient client) {
        super(black, white);
        this.client = client;
    }

    public MultiPlayerGame(Player black, Player white, Duration duration, PlayerClient client) {
        super(black, white, duration);
        this.client = client;
    }

    public MultiPlayerGame(Board board, BitSet set, PlayerClient client) {
        super(board, set);
        this.client = client;
    }

    public MultiPlayerGame(ChessGameImpl game, PlayerClient client) {
        super(game);
        this.client = client;
    }

    @Override
    public void makeMove(PlayerMove move) {
        super.makeMove(move);

        if (client.getPlayer().getColor() == move.getColor()) {
            client.getWrapper().writeMove(move);
        }
    }
}

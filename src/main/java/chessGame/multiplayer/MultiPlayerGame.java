package chessGame.multiplayer;

import chessGame.mechanics.Figure;
import chessGame.mechanics.Player;
import chessGame.mechanics.board.FigureBoard;
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

    public MultiPlayerGame(FigureBoard board, BitSet set, PlayerClient client) {
        super(board, set);
        this.client = client;
    }

    public MultiPlayerGame(ChessGameImpl game, PlayerClient client) {
        super(game);
        this.client = client;
    }

    @Override
    public void nextRound() {
        PlayerMove lastMove = getLastMove();
        super.nextRound();

        String endState = "";

        if (isFinished()) {
            endState = "END";
            client.endGame();
        }

        if (lastMove != null && client.getPlayer().getColor() == lastMove.getColor()) {
            client.getWrapper().writeMove(lastMove, endState);
        }
    }
}

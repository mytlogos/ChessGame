package chessGame.multiplayer;

import chessGame.mechanics.game.ChessGameImpl;
import chessGame.mechanics.move.PlayerMove;
import javafx.util.Duration;

/**
 *
 */
public class MultiPlayerGame extends ChessGameImpl {

    private final PlayerClient client;

    public MultiPlayerGame(PlayerClient client) {
        this.client = client;
    }

    public MultiPlayerGame(MultiPlayer black, MultiPlayer white, PlayerClient client) {
        super(black, white);
        this.client = client;
    }

    public MultiPlayerGame(MultiPlayer black, MultiPlayer white, Duration duration, PlayerClient client) {
        super(black, white, duration);
        this.client = client;
    }

    @Override
    public MultiPlayer getWinner() {
        return (MultiPlayer) super.getWinner();
    }

    @Override
    public MultiPlayer getBlack() {
        return (MultiPlayer) super.getBlack();
    }

    @Override
    public MultiPlayer getWhite() {
        return (MultiPlayer) super.getWhite();
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

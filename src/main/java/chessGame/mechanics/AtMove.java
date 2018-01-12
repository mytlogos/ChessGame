package chessGame.mechanics;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public final class AtMove {
    private Player player;
    private List<PlayerMove> allowedMoves;


    AtMove(Player player, List<PlayerMove> allowedMoves) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(allowedMoves);

        this.player = player;
        this.allowedMoves = allowedMoves;
    }

    public Player getPlayer() {
        return player;
    }

    public List<PlayerMove> getAllowedMoves() {
        return allowedMoves;
    }
}

package chessGame.engine;

import chessGame.mechanics.AbstractBoard;
import chessGame.mechanics.PlayerMove;

/**
 *
 */
public class EngineMove extends PlayerMove {
    private final PlayerMove playerMove;
    private final AbstractBoard board;

    public EngineMove(PlayerMove playerMove, AbstractBoard board) {
        super(playerMove.getMainMove(), playerMove.getSecondaryMove());
        this.playerMove = playerMove;
        this.board = board;
        setPromotionMove(playerMove.getPromotionMove());
        setType(playerMove);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !PlayerMove.class.isAssignableFrom(o.getClass())) return false;
        if (!super.equals(o)) return false;

        if (!(o instanceof EngineMove)) {
            return true;
        }
        EngineMove that = (EngineMove) o;
        return board != null ? board.equals(that.board) : that.board == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (board != null ? board.hashCode() : 0);
        return result;
    }

    public AbstractBoard getBoard() {
        return board;
    }
}

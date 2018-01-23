package chessGame.engine;

import chessGame.mechanics.Board;
import chessGame.mechanics.PlayerMove;

/**
 *
 */
public class EngineMove extends PlayerMove {
    private final Board board;

    public EngineMove(PlayerMove playerMove, Board board) {
        super(playerMove.getMainMove(), playerMove.getSecondaryMove().orElse(null));
        this.board = board;
        setPromotionMove(playerMove.getPromotionMove().orElse(null));
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

    public Board getBoard() {
        return board;
    }
}

package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

/**
 *
 */
public final class Rook extends Figure {

    public Rook(Position position, Player player, Board board) {
        super(position, player, FigureType.ROOK, board);
    }

    public boolean eligibleForCastling() {
        return (getPosition().getColumn() == 8 || getPosition().getColumn() == 1);
    }
}

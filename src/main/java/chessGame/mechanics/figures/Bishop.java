package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

import java.util.List;

/**
 *
 */
public final class Bishop extends Figure {
    public Bishop(Position position, Player player, Board board) {
        super(position, player, FigureType.BISHOP, board);
    }

    @Override
    public List<Position> getAllowedPositions() {
        return checkPositions(getDiagonal(8));
    }
}

package chessGame.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Queen extends Figure {
    public Queen(Position position, Player player, Board board) {
        super(position, player, FigureType.QUEEN, board);
    }

    @Override
    public List<Position> getAllowedPositions() {
        final ArrayList<Position> positions = new ArrayList<>();
        positions.addAll(getDiagonal(8));
        positions.addAll(getHorizontal(8));
        positions.addAll(getVertical(8));

        return checkPositions(positions);
    }

}

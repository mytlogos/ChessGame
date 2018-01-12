package chessGame.mechanics.figures;

import chessGame.mechanics.AbstractBoard;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class Knight extends Figure {
    public Knight(Position position, Player player, AbstractBoard board) {
        super(position, player, FigureType.KNIGHT, board);
    }

    @Override
    public List<Position> getAllowedPositions() {
        List<Position> positions = new ArrayList<>();

        final int row = getPosition().getRow();
        final int column = getPosition().getColumn();

        addPosition(row + 2, column + 1, positions);
        addPosition(row + 2, column - 1, positions);

        addPosition(row - 2, column + 1, positions);
        addPosition(row - 2, column - 1, positions);

        addPosition(row + 1,column + 2,positions);
        addPosition(row + 1,column - 2,positions);

        addPosition(row - 1,column + 2,positions);
        addPosition(row - 1,column - 2,positions);

        return checkPositions(positions);
    }
}

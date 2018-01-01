package chessGame.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Knight extends Figure {
    public Knight(Position position, Player player, Board board) {
        super(position, player, FigureType.KNIGHT, board);
    }

    public Knight() {
        this(null, null, null);
    }

    @Override
    public List<Position> getAllowedPositions() {
        List<Position> positions = new ArrayList<>();

        final int row = getPosition().getRow();
        final int column = getPosition().getColumn();

        positions.add(new Position(row + 2, column + 1));
        positions.add(new Position(row + 2, column - 1));

        positions.add(new Position(row - 2, column + 1));
        positions.add(new Position(row - 2, column - 1));

        positions.add(new Position(row + 1, column + 2));
        positions.add(new Position(row - 1, column + 2));

        positions.add(new Position(row + 1, column - 2));
        positions.add(new Position(row - 1, column - 2));
        return checkPositions(positions);
    }
}

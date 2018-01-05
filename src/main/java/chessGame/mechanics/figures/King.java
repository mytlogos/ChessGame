package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public final class King extends Figure {
    private boolean moved;

    public King(Position position, Player player, Board board) {
        super(position, player, FigureType.KING, board);
        positionProperty().addListener(observable -> moved = true);
    }

    @Override
    public List<Position> getAllowedPositions() {
        List<Position> positions = new ArrayList<>();

        positions.addAll(getHorizontal(1));
        positions.addAll(getDiagonal(1));
        positions.addAll(getVertical(1));

        return checkPositions(positions);
    }

    public boolean hasMoved() {
        return moved;
    }
}

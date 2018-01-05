package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class Rook extends Figure {
    private boolean moved;

    public Rook(Position position, Player player, Board board) {
        super(position, player, FigureType.ROOK, board);
        positionProperty().addListener(observable -> moved = true);
    }

    @Override
    public List<Position> getAllowedPositions() {
        List<Position> positions = new ArrayList<>();

        positions.addAll(getHorizontal(8));
        positions.addAll(getVertical(8));

        return checkPositions(positions);
    }

    public boolean eligibleForCastling() {
        return moved && (getPosition().getColumn() == 8 || getPosition().getColumn() ==  1);
    }
}

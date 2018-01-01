package chessGame.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Pawn extends Figure {

    public Pawn(Position position, Player player, Board board) {
        super(position, player, FigureType.PAWN, board);
    }

    @Override
    public List<Position> getAllowedPositions() {
        final int row;
        final List<Position> positions = new ArrayList<>();

        final int currentRow = getPosition().getRow();
        if (Player.BLACK == getPlayer()) {
            row = currentRow - 1;

            if (currentRow == 7 && board.getFigure(new Position(6, getPosition().getColumn())) == null) {
                final Position position = new Position(5, getPosition().getColumn());
                if (board.getFigure(position) == null) {
                    positions.add(position);
                }
            }

            addDiagonalStrike(positions, new Position(currentRow - 1, getPosition().getColumn() + 1));
            addDiagonalStrike(positions, new Position(currentRow - 1, getPosition().getColumn() - 1));

        } else {
            if (currentRow == 2 && board.getFigure(new Position(3,getPosition().getColumn())) == null) {
                final Position position = new Position(4, getPosition().getColumn());
                if (board.getFigure(position) == null) {
                    positions.add(position);
                }
            }

            addDiagonalStrike(positions, new Position(currentRow + 1, getPosition().getColumn() + 1));
            addDiagonalStrike(positions, new Position(currentRow + 1, getPosition().getColumn() - 1));
            row = currentRow + 1;
        }
        positions.add(new Position(row, getPosition().getColumn()));
        return checkPositions(positions);
    }

    private void addDiagonalStrike(List<Position> positions, Position position) {
        if (checkPositionIndex(position)) {
            final Figure leftFigure = board.getFigure(position);
            if (leftFigure != null && leftFigure.getPlayer() != getPlayer()) {
                positions.add(position);
            }
        }
    }
}

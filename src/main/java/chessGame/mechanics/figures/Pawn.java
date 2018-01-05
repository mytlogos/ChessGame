package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class Pawn extends Figure {

    public Pawn(Position position, Player player, Board board) {
        super(position, player, FigureType.PAWN, board);
    }

    @Override
    public List<Position> getAllowedPositions() {
        final int row;
        final List<Position> positions = new ArrayList<>();

        final int currentRow = getPosition().getRow();
        if (getPlayer().isWhite()) {

            //moving two rows in one move, only if is at start position
            if (currentRow == 2 && board.getFigure(Position.get(3, getPosition().getColumn())) == null) {
                final Position position = Position.get(4, getPosition().getColumn());
                if (board.getFigure(position) == null) {
                    positions.add(position);
                }
            }

            addDiagonalStrike(positions, currentRow + 1, getPosition().getColumn() + 1);
            addDiagonalStrike(positions, currentRow + 1, getPosition().getColumn() - 1);
            row = currentRow + 1;

        } else {
            row = currentRow - 1;

            //moving two rows in one move, only if is at start position
            if (currentRow == 7 && board.getFigure(Position.get(6, getPosition().getColumn())) == null) {
                final Position position = Position.get(5, getPosition().getColumn());

                if (board.getFigure(position) == null) {
                    positions.add(position);
                }
            }
            addDiagonalStrike(positions, currentRow - 1, getPosition().getColumn() + 1);
            addDiagonalStrike(positions, currentRow - 1, getPosition().getColumn() - 1);
        }

        if (Position.isInBoard(row, getPosition().getColumn())) {
            Position position = Position.get(row, getPosition().getColumn());
            final Figure figure = board.getFigure(position);

            if (figure == null) {
                positions.add(position);
            }
        }
        return checkPositions(positions);
    }

    private void addDiagonalStrike(List<Position> positions, int row, int column) {
        if (Position.isInBoard(row, column)) {
            final Position position = Position.get(row, column);
            final Figure leftFigure = board.getFigure(position);
            if (leftFigure != null && leftFigure.getPlayer() != getPlayer()) {
                positions.add(position);
            }
        }
    }

}

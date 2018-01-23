package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Position generator. Has only the static Method {@link #getAllowedPositions(Figure, Board)}.
 * Is abstract.
 */
public abstract class PositionGenerator {

    /**
     * Generates a List of allowed Positions for a given Figure on the given Board.
     * Does not pay attention to Checks or special moves.
     *
     * @param figure a figure, not null
     * @param board a board, not null
     * @return a List of allowed Positions, or empty if none are possible
     */
    public static List<Position> getAllowedPositions(Figure figure, Board board) {
        Objects.requireNonNull(figure);
        Objects.requireNonNull(board);

        final List<Position> result;

        //bishop can only move diagonal in four directions
        if (figure.getType() == FigureType.BISHOP) {
            result = getDiagonal(8, figure, board);

        //rook can only move vertical or horizontal in two directions
        } else if (figure.getType() == FigureType.ROOK) {

            List<Position> positions = new ArrayList<>();

            positions.addAll(getHorizontal(8, figure, board));
            positions.addAll(getVertical(8, figure, board));

            result = positions;

        //rook can jump, always two fields vertical/horizontal and one in the other (if two in vertical, then one in horizontal)
        } else if (figure.getType() == FigureType.KNIGHT) {
            result = getKnightPositions(figure, board);

        //can move to all adjacent fields
        } else if (figure.getType() == FigureType.KING) {
            List<Position> positions = new ArrayList<>();

            positions.addAll(getHorizontal(1, figure, board));
            positions.addAll(getDiagonal(1, figure, board));
            positions.addAll(getVertical(1, figure, board));

            result = positions;

        //queen moves vertical/horizontal and diagonal in all directions, excluding jumping
        } else if (figure.getType() == FigureType.QUEEN) {
            final ArrayList<Position> positions = new ArrayList<>();
            positions.addAll(getDiagonal(8, figure, board));
            positions.addAll(getHorizontal(8, figure, board));
            positions.addAll(getVertical(8, figure, board));

            result = positions;

        //can only move one field forward, except in start position, where he can move two fields forward, or strike diagonal
        } else if (figure.getType() == FigureType.PAWN) {
            result = getPawnPosition(figure, board);

        } else {
            //should never reach here
            result = new ArrayList<>();
        }
        return checkPositions(result, figure, board);
    }

    private static List<Position> getDiagonal(int max, Figure figure, Board board) {
        List<Position> positions = new ArrayList<>();

        final int row = figure.getPosition().getRow();
        final int column = figure.getPosition().getColumn();

        boolean rightForward = true;
        boolean leftForward = true;
        boolean rightBackward = true;
        boolean leftBackward = true;

        for (int i = 1; i < max + 1; i++) {
            if (leftBackward) {
                leftBackward = addPosition(row - i, column - i, positions, board, figure);
            }
            if (rightBackward) {
                rightBackward = addPosition(row - i, column + i, positions, board, figure);
            }
            if (rightForward) {
                rightForward = addPosition(row + i, column + i, positions, board, figure);
            }
            if (leftForward) {
                leftForward = addPosition(row + i, column - i, positions, board, figure);
            }
        }
        return positions;
    }

    private static List<Position> getHorizontal(int max, Figure figure, Board board) {
        List<Position> positions = new ArrayList<>();

        final int row = figure.getPosition().getRow();
        final int column = figure.getPosition().getColumn();

        boolean left = true;
        boolean right = true;

        for (int i = 1; i < max + 1; i++) {

            if (left) {
                left = addPosition(row, column - i, positions, board, figure);
            }
            if (right) {
                right = addPosition(row, column + i, positions, board, figure);
            }
        }
        return positions;
    }

    private static List<Position> getVertical(int max, Figure figure, Board board) {
        List<Position> positions = new ArrayList<>();

        final int row = figure.getPosition().getRow();
        final int column = figure.getPosition().getColumn();

        boolean backward = true;
        boolean forward = true;

        for (int i = 1; i < max + 1; i++) {
            if (backward) {
                backward = addPosition(row - i, column, positions, board, figure);
            }
            if (forward) {
                forward = addPosition(row + i, column, positions, board, figure);
            }
        }
        return positions;
    }

    private static List<Position> getKnightPositions(Figure figure, Board board) {
        List<Position> result;
        List<Position> positions = new ArrayList<>();

        final int row = figure.getPosition().getRow();
        final int column = figure.getPosition().getColumn();

        addPosition(row + 2, column + 1, positions, board, figure);
        addPosition(row + 2, column - 1, positions, board, figure);

        addPosition(row - 2, column + 1, positions, board, figure);
        addPosition(row - 2, column - 1, positions, board, figure);

        addPosition(row + 1, column + 2, positions, board, figure);
        addPosition(row + 1, column - 2, positions, board, figure);

        addPosition(row - 1, column + 2, positions, board, figure);
        addPosition(row - 1, column - 2, positions, board, figure);

        result = positions;
        return result;
    }

    private static List<Position> getPawnPosition(Figure figure, Board board) {
        final int row;
        final List<Position> positions = new ArrayList<>();

        final int currentRow = figure.getPosition().getRow();
        if (figure.getPlayer().isWhite()) {

            //moving two rows in one move, only if is at start position
            if (currentRow == 2 && board.isEmptyAt(Position.get(3, figure.getPosition().getColumn()))) {
                final Position position = Position.get(4, figure.getPosition().getColumn());
                if (board.isEmptyAt(position)) {
                    positions.add(position);
                }
            }

            addDiagonalStrike(positions, currentRow + 1, figure.getPosition().getColumn() + 1, board, figure);
            addDiagonalStrike(positions, currentRow + 1, figure.getPosition().getColumn() - 1, board, figure);
            row = currentRow + 1;

        } else {
            row = currentRow - 1;

            //moving two rows in one move, only if is at start position
            if (currentRow == 7 && board.isEmptyAt(Position.get(6, figure.getPosition().getColumn()))) {
                final Position position = Position.get(5, figure.getPosition().getColumn());

                if (board.isEmptyAt(position)) {
                    positions.add(position);
                }
            }
            addDiagonalStrike(positions, currentRow - 1, figure.getPosition().getColumn() + 1, board, figure);
            addDiagonalStrike(positions, currentRow - 1, figure.getPosition().getColumn() - 1, board, figure);
        }

        if (Position.isInBoard(row, figure.getPosition().getColumn())) {
            Position position = Position.get(row, figure.getPosition().getColumn());
            final Figure boardFigure = board.figureAt(position);

            if (boardFigure == null) {
                positions.add(position);
            }
        }
        return positions;
    }

    private static List<Position> checkPositions(Collection<Position> positions, Figure figure, Board board) {
        final List<Position> positionList = positions.
                stream().
                filter(Position::isInBoard).
                filter(((Predicate<Position>) Position::isAlly).negate()).
                collect(Collectors.toList());
        positionList.forEach(position -> checkPositionState(position, board));

        return figure.getPosition().equals(Position.Bench) ? new ArrayList<>() : positionList;
    }

    private static boolean addPosition(int newRow, int newColumn, Collection<Position> positions, Board board, Figure figure1) {
        if (!Position.isInBoard(newRow, newColumn)) {
            return false;
        }
        Position position = Position.get(newRow, newColumn);
        final Figure figure = board.figureAt(position);

        if (figure == null) {
            positions.add(position);
            return true;
        } else {
            if (!figure.getPlayer().equals(figure1.getPlayer())) {
                positions.add(position);
            }
            return false;
        }
    }

    private static void addDiagonalStrike(List<Position> positions, int row, int column, Board board, Figure figure) {
        if (Position.isInBoard(row, column)) {
            final Position position = Position.get(row, column);
            final Figure leftFigure = board.figureAt(position);

            if (leftFigure != null && leftFigure.getPlayer() != figure.getPlayer()) {
                positions.add(position);
            }
        }
    }

    private static void checkPositionState(Position position, Board board) {
        final Figure figure = board.figureAt(position);

        if (figure != null) {
            position.setEnemy(figure.getPlayer() != figure.getPlayer());
            position.setEmpty(false);
        }
    }
}

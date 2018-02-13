package chessGame.mechanics.move;

import chessGame.mechanics.Figure;
import chessGame.mechanics.Position;
import chessGame.mechanics.board.Board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static chessGame.mechanics.FigureType.*;

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
     * @param board  a board, not null
     * @return a List of allowed Positions, or empty if none are possible
     */
    public static List<Position> getAllowedPositions(Figure figure, Board board) {
        Objects.requireNonNull(figure);
        Objects.requireNonNull(board);

        return getPositions(figure, board, false);
    }

    private static List<Position> getPositions(Figure figure, Board board, boolean inclusive) {
        final List<Position> result;

        Position position = board.positionOf(figure);

        //abort if figure is not on this board
        if (!position.isInBoard()) {
            return new ArrayList<>();
        }

        //can only move one field forward, except in start position, where he can move two fields forward, or strike diagonal
        if (figure.is(PAWN)) {
            result = getPawnPosition(figure, board, position, inclusive);

            //rook can only move vertical or horizontal in two directions
        } else if (figure.is(ROOK)) {
            List<Position> positions = new ArrayList<>();

            positions.addAll(getHorizontal(8, figure, board, position, inclusive));
            positions.addAll(getVertical(8, figure, board, position, inclusive));

            result = positions;

            //knight can jump, always two fields vertical/horizontal and one in the other (if two in vertical, then one in horizontal)
        } else if (figure.is(KNIGHT)) {
            result = getKnightPositions(figure, board, position, inclusive);

            //bishop can only move diagonal in four directions
        } else if (figure.is(BISHOP)) {
            result = getDiagonal(8, figure, board, position, inclusive);


            //queen moves vertical/horizontal and diagonal in all directions, excluding jumping
        } else if (figure.is(QUEEN)) {
            final ArrayList<Position> positions = new ArrayList<>();

            positions.addAll(getDiagonal(8, figure, board, position, inclusive));
            positions.addAll(getHorizontal(8, figure, board, position, inclusive));
            positions.addAll(getVertical(8, figure, board, position, inclusive));

            result = positions;
            //can move to all adjacent fields
        } else if (figure.is(KING)) {
            List<Position> positions = new ArrayList<>();

            positions.addAll(getHorizontal(1, figure, board, position, inclusive));
            positions.addAll(getDiagonal(1, figure, board, position, inclusive));
            positions.addAll(getVertical(1, figure, board, position, inclusive));

            result = positions;

        } else {
            //should never reach here
            result = new ArrayList<>();
        }
        return result;
    }

    private static List<Position> getDiagonal(int max, Figure figure, Board board, Position position, boolean inclusive) {
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int column = position.getColumn();

        boolean rightForward = true;
        boolean leftForward = true;
        boolean rightBackward = true;
        boolean leftBackward = true;

        for (int i = 1; i < max + 1; i++) {
            int rightUpLeftDownAddend = 9 * i;
            int leftUpRightDownAddend = 7 * i;

            if (leftBackward) {
                leftBackward = column != 1 && isValid(figure, board, positions, panel - rightUpLeftDownAddend, 1, inclusive);
            }
            if (rightBackward) {
                rightBackward = column != 8 && isValid(figure, board, positions, panel - leftUpRightDownAddend, 8, inclusive);
            }
            if (rightForward) {
                rightForward = column != 8 && isValid(figure, board, positions, panel + rightUpLeftDownAddend, 8, inclusive);
            }
            if (leftForward) {
                leftForward = column != 1 && isValid(figure, board, positions, panel + leftUpRightDownAddend, 1, inclusive);
            }
        }
        return positions;
    }

    private static boolean isValid(Figure figure, Board board, List<Position> positions, int newPanel, int limit, boolean inclusive) {
        if (Position.isInBoard(newPanel)) {
            Position newPosition = Position.get(newPanel);

            if (newPosition.getColumn() == limit) {
                addPosition(positions, board, figure, newPosition, inclusive);
                return false;
            } else {
                return addPosition(positions, board, figure, newPosition, inclusive);
            }
        }
        return true;
    }

    private static List<Position> getHorizontal(int max, Figure figure, Board board, Position position, boolean inclusive) {
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int column = position.getColumn();

        boolean left = true;
        boolean right = true;

        for (int addend = 1; addend < max + 1; addend++) {
            if (left) {
                left = column != 1 && isValid(figure, board, positions, panel - addend, 1, inclusive);
            }
            if (right) {
                right = column != 8 && isValid(figure, board, positions, panel + addend, 8, inclusive);
            }
        }
        return positions;
    }

    private static List<Position> getVertical(int max, Figure figure, Board board, Position position, boolean inclusive) {
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int row = position.getRow();

        boolean backward = true;
        boolean forward = true;

        for (int i = 1; i < max + 1; i++) {
            int verticalAddend = 8 * i;
            if (backward) {
                backward = row != 1 && isValid(figure, board, positions, panel - verticalAddend, 0, inclusive);
            }
            if (forward) {
                forward = row != 8 && isValid(figure, board, positions, panel + verticalAddend, 0, inclusive);
            }
        }
        return positions;
    }

    private static List<Position> getKnightPositions(Figure figure, Board board, Position position, boolean inclusive) {
        List<Position> result;
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int column = position.getColumn();

        if (column < 7 && column > 2) {
            //left up down
            knightJump(figure, board, positions, panel, 6, 10, inclusive);
            //right up down
            knightJump(figure, board, positions, panel, 10, 6, inclusive);

            //up down left
            knightJump(figure, board, positions, panel, 15, 17, inclusive);
            //up down right
            knightJump(figure, board, positions, panel, 17, 15, inclusive);

        } else if (column == 1) {
            //right up down
            knightJump(figure, board, positions, panel, 10, 6, inclusive);
            //up down right
            knightJump(figure, board, positions, panel, 17, 15, inclusive);

        } else if (column == 2) {
            //right up down
            knightJump(figure, board, positions, panel, 10, 6, inclusive);
            //up down left
            knightJump(figure, board, positions, panel, 15, 17, inclusive);
            //up down right
            knightJump(figure, board, positions, panel, 17, 15, inclusive);

        } else if (column == 8) {
            //left up down
            knightJump(figure, board, positions, panel, 6, 10, inclusive);
            //up down left
            knightJump(figure, board, positions, panel, 15, 17, inclusive);

        } else if (column == 7) {
            //left up down
            knightJump(figure, board, positions, panel, 6, 10, inclusive);
            //up down left
            knightJump(figure, board, positions, panel, 15, 17, inclusive);
            //up down right
            knightJump(figure, board, positions, panel, 17, 15, inclusive);
        }

        result = positions;
        return result;
    }

    private static void knightJump(Figure figure, Board board, List<Position> positions, int panel, int up, int down, boolean inclusive) {
        isValid(figure, board, positions, panel - down, 0, inclusive);
        isValid(figure, board, positions, panel + up, 0, inclusive);
    }

    private static List<Position> getPawnPosition(Figure figure, Board board, Position positionOf, boolean inclusive) {
        final List<Position> positions = new ArrayList<>();

        int panel = positionOf.getPanel();

        final int newPanel;
        final int currentRow = positionOf.getRow();
        final int currentColumn = positionOf.getColumn();

        if (figure.isWhite()) {

            //moving two rows in one move, only if is at start position
            if (currentRow == 2 && board.isEmptyAt(Position.get(panel + 8))) {
                final Position position = Position.get(panel + 16);

                if (board.isEmptyAt(position)) {
                    positions.add(position);
                }
            }

            addDiagonalStrike(currentColumn, positions, panel + 9, board, figure, inclusive);
            addDiagonalStrike(currentColumn, positions, panel + 7, board, figure, inclusive);
            newPanel = panel + 8;

        } else {
            newPanel = panel - 8;

            //moving two rows in one move, only if is at start position
            if (currentRow == 7 && board.isEmptyAt(Position.get(panel - 8))) {
                final Position position = Position.get(panel - 16);

                if (board.isEmptyAt(position)) {
                    positions.add(position);
                }
            }
            addDiagonalStrike(currentColumn, positions, panel - 9, board, figure, inclusive);
            addDiagonalStrike(currentColumn, positions, panel - 7, board, figure, inclusive);
        }

        //the pawn step one row forward in one direction
        if (Position.isInBoard(newPanel)) {
            Position position = Position.get(newPanel);
            final Figure boardFigure = board.figureAt(position);

            if (boardFigure == null) {
                positions.add(position);
            }
        }
        return positions;
    }

    private static boolean addPosition(Collection<Position> positions, Board board, Figure figure, Position newPosition, boolean inclusive) {
        final Figure boardFigure = board.figureAt(newPosition);

        if (boardFigure == null) {
            positions.add(newPosition);
            return true;
        } else {
            if (!boardFigure.getColor().equals(figure.getColor()) || inclusive) {
                positions.add(newPosition);
            }
            return false;
        }
    }

    private static boolean checkEdgeTrespassing(int column, Position newPosition) {
        int newColumn = newPosition.getColumn();
        return column == 1 && (newColumn == 7 || newColumn == 8) || column == 8 && (newColumn == 1 || newColumn == 2);
    }

    private static void addDiagonalStrike(int currentColumn, List<Position> positions, int panel, Board board, Figure figure, boolean inclusive) {
        if (Position.isInBoard(panel)) {
            final Position position = Position.get(panel);

            //check if strike goes over the edge
            if (checkEdgeTrespassing(currentColumn, position)) return;

            final Figure leftFigure = board.figureAt(position);

            if (inclusive || (leftFigure != null && leftFigure.getColor() != figure.getColor())) {
                positions.add(position);
            }
        }
    }

    public static List<Position> getPositions(Figure figure, Board board) {
        Objects.requireNonNull(figure);
        Objects.requireNonNull(board);

        return getPositions(figure, board, true);
    }
}

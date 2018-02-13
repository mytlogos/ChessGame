package chessGame.mechanics.move;

import chessGame.mechanics.Color;
import chessGame.mechanics.Figure;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;
import chessGame.mechanics.board.Board;

import java.util.*;

import static chessGame.mechanics.FigureType.*;

/**
 *
 */
public class CheckValidator {

    private final Figure figure;
    private final Position figurePosition;
    private final Board board;
    private List<Position> possibleFigurePositions;
    private final Map<FigureType, List<CheckItem>> map = new HashMap<>();

    public CheckValidator(Figure figure, Board board) {
        this.figure = figure;
        this.board = board;
        figurePosition = this.board.positionOf(figure);
        init();
    }

    private void init() {
        possibleFigurePositions = getAllowedPositions(figure, board);
        List<Figure> enemyFigures = board.getFigures(Color.getEnemy(figure.getColor()));


        for (Figure enemyFigure : enemyFigures) {
            CheckItem item = getCheckItem(enemyFigure);

            if (item != null) {
                map.computeIfAbsent(item.blocker, k -> new ArrayList<>()).add(item);
            }
        }

    }

    private List<Position> getAllowedPositions(Figure figure, Board board) {
        return new ArrayList<>();
    }

    boolean isInvalidMove(PlayerMove move) {
        if (move == null) {
            return true;
        }

        if (move.getMainMove().isMoving(KING)) {
            Position to = move.getMainMove().getTo();
            return !possibleFigurePositions.contains(to);
        } else {

        }
        return false;
    }

    private static class CheckItem {
        private FigureType threat;
        private Position threatPosition;
        private List<Position> unblocked = new ArrayList<>();
        private FigureType blocker = null;
        private Position blockerPosition = null;
        private List<Position> blocked = new ArrayList<>();
    }

    private CheckItem getCheckItem(Figure figure) {
        final CheckItem result;

        //should never be not on Board
        Position position = board.positionOf(figure);

        //can only move one field forward, except in start position, where he can move two fields forward, or strike diagonal
        if (figure.is(PAWN)) {
            result = getPawnPosition(figure, board, position);

            //rook can only move vertical or horizontal in two directions
        } else if (figure.is(ROOK)) {
            CheckItem item = null;

            item = getHorizontal(8, figure, board, position);
            item = getVertical(8, figure, board, position);

            result = item;

            //knight can jump, always two fields vertical/horizontal and one in the other (if two in vertical, then one in horizontal)
        } else if (figure.is(KNIGHT)) {
            result = getKnightPositions(figure, board, position);

            //bishop can only move diagonal in four directions
        } else if (figure.is(BISHOP)) {
            result = getDiagonal(8, figure, board, position);


            //queen moves vertical/horizontal and diagonal in all directions, excluding jumping
        } else if (figure.is(QUEEN)) {

            CheckItem item = null;

            item = getHorizontal(8, figure, board, position);
            item = getVertical(8, figure, board, position);
            item = getDiagonal(8, figure, board, position);

            result = item;
            //can move to all adjacent fields
        } else if (figure.is(KING)) {

            CheckItem item = null;

            item = getHorizontal(1, figure, board, position);
            item = getVertical(1, figure, board, position);
            item = getDiagonal(1, figure, board, position);

            result = item;
        } else {
            //should never reach here
            result = null;
        }
        return null;
    }

    private CheckItem getDiagonal(int max, Figure figure, Board board, Position position) {
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
                leftBackward = column != 1 && isValid(figure, board, positions, panel - rightUpLeftDownAddend, 1);
            }
            if (rightBackward) {
                rightBackward = column != 8 && isValid(figure, board, positions, panel - leftUpRightDownAddend, 8);
            }
            if (rightForward) {
                rightForward = column != 8 && isValid(figure, board, positions, panel + rightUpLeftDownAddend, 8);
            }
            if (leftForward) {
                leftForward = column != 1 && isValid(figure, board, positions, panel + leftUpRightDownAddend, 1);
            }
        }
        return null;
    }

    private boolean isValid(Figure figure, Board board, List<Position> positions, int newPanel, int limit) {
        if (Position.isInBoard(newPanel)) {
            Position newPosition = Position.get(newPanel);

            if (newPosition.getColumn() == limit) {
                addPosition(positions, board, figure, newPosition);
                return false;
            } else {
                return addPosition(positions, board, figure, newPosition);
            }
        }
        return true;
    }

    private CheckItem getHorizontal(int max, Figure figure, Board board, Position position) {
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int column = position.getColumn();

        boolean left = true;
        boolean right = true;

        for (int addend = 1; addend < max + 1; addend++) {
            if (left) {
                left = column != 1 && isValid(figure, board, positions, panel - addend, 1);
            }
            if (right) {
                right = column != 8 && isValid(figure, board, positions, panel + addend, 8);
            }
        }
        return null;
    }

    private CheckItem getVertical(int max, Figure figure, Board board, Position position) {
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int row = position.getRow();

        boolean backward = true;
        boolean forward = true;

        for (int i = 1; i < max + 1; i++) {
            int verticalAddend = 8 * i;
            if (backward) {
                backward = row != 1 && isValid(figure, board, positions, panel - verticalAddend, 0);
            }
            if (forward) {
                forward = row != 8 && isValid(figure, board, positions, panel + verticalAddend, 0);
            }
        }
        return null;
    }

    private CheckItem getKnightPositions(Figure figure, Board board, Position position) {
        List<Position> result;
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int column = position.getColumn();

        if (column < 7 && column > 2) {
            //left up down
            knightJump(figure, board, positions, panel, 6, 10);
            //right up down
            knightJump(figure, board, positions, panel, 10, 6);

            //up down left
            knightJump(figure, board, positions, panel, 15, 17);
            //up down right
            knightJump(figure, board, positions, panel, 17, 15);

        } else if (column == 1) {
            //right up down
            knightJump(figure, board, positions, panel, 10, 6);
            //up down right
            knightJump(figure, board, positions, panel, 17, 15);

        } else if (column == 2) {
            //right up down
            knightJump(figure, board, positions, panel, 10, 6);
            //up down left
            knightJump(figure, board, positions, panel, 15, 17);
            //up down right
            knightJump(figure, board, positions, panel, 17, 15);

        } else if (column == 8) {
            //left up down
            knightJump(figure, board, positions, panel, 6, 10);
            //up down left
            knightJump(figure, board, positions, panel, 15, 17);

        } else if (column == 7) {
            //left up down
            knightJump(figure, board, positions, panel, 6, 10);
            //up down left
            knightJump(figure, board, positions, panel, 15, 17);
            //up down right
            knightJump(figure, board, positions, panel, 17, 15);
        }

        result = positions;
        return null;
    }

    private void knightJump(Figure figure, Board board, List<Position> positions, int panel, int up, int down) {
        isValid(figure, board, positions, panel - down, 0);
        isValid(figure, board, positions, panel + up, 0);
    }

    private CheckItem getPawnPosition(Figure figure, Board board, Position positionOf) {
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

            addDiagonalStrike(currentColumn, positions, panel + 9, board, figure);
            addDiagonalStrike(currentColumn, positions, panel + 7, board, figure);
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
            addDiagonalStrike(currentColumn, positions, panel - 9, board, figure);
            addDiagonalStrike(currentColumn, positions, panel - 7, board, figure);
        }

        //the pawn step one row forward in one direction
        if (Position.isInBoard(newPanel)) {
            Position position = Position.get(newPanel);
            final Figure boardFigure = board.figureAt(position);

            if (boardFigure == null) {
                positions.add(position);
            }
        }
        return null;
    }

    private boolean addPosition(Collection<Position> positions, Board board, Figure figure, Position newPosition) {
        final Figure boardFigure = board.figureAt(newPosition);

        if (boardFigure == null) {
            positions.add(newPosition);
            return true;
        } else {
            if (!boardFigure.getColor().equals(figure.getColor())) {
                positions.add(newPosition);
            }
            return false;
        }
    }

    private boolean checkEdgeTrespassing(int column, Position newPosition) {
        int newColumn = newPosition.getColumn();
        return column == 1 && (newColumn == 7 || newColumn == 8) || column == 8 && (newColumn == 1 || newColumn == 2);
    }

    private void addDiagonalStrike(int currentColumn, List<Position> positions, int panel, Board board, Figure figure) {
        if (Position.isInBoard(panel)) {
            final Position position = Position.get(panel);

            //check if strike goes over the edge
            if (checkEdgeTrespassing(currentColumn, position)) return;

            final Figure leftFigure = board.figureAt(position);

            if ((leftFigure != null && leftFigure.getColor() != figure.getColor())) {
                positions.add(position);
            }
        }
    }
}

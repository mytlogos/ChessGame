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
    private final Map<FigureType, List<CheckItem>> map = new HashMap<>();
    private List<Position> possibleFigurePositions;

    public CheckValidator(Figure figure, Board board) {
        this.figure = figure;
        this.board = board;
        figurePosition = this.board.positionOf(figure);
        init();
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

    private CheckItem getCheckItem(Figure figure) {

        //should never be not on Board
        Position position = board.positionOf(figure);

        CheckItem result = null;

        //can only move one field forward, except in start position, where he can move two fields forward, or strike diagonal
        if (figure.is(PAWN)) {
            result = removePawnPosition(figure, position);

            //rook can only move vertical or horizontal in two directions
        } else if (figure.is(ROOK)) {
            CheckItem item = getHorizontal(8, figure, position);
            CheckItem vertical = getVertical(8, figure, position);

            result = item == null ? vertical : item;

            //knight can jump, always two fields vertical/horizontal and one in the other (if two in vertical, then one in horizontal)
        } else if (figure.is(KNIGHT)) {
            removeKnightPositions(figure, position);

            //bishop can only move diagonal in four directions
        } else if (figure.is(BISHOP)) {
            result = getDiagonal(8, figure, position);

            //queen moves vertical/horizontal and diagonal in all directions, excluding jumping
        } else if (figure.is(QUEEN)) {

            CheckItem horizontal = getHorizontal(8, figure, position);
            CheckItem vertical = getVertical(8, figure, position);
            CheckItem diagonal = getDiagonal(8, figure, position);

            result = horizontal == null ? vertical == null ? diagonal : vertical : horizontal;
            //can move to all adjacent fields
        } else if (figure.is(KING)) {

            getHorizontal(1, figure, position);
            getVertical(1, figure, position);
            getDiagonal(1, figure, position);
        } else {
            //should never reach here
            result = null;
        }
        return result;
    }

    private CheckItem getDiagonal(int max, Figure figure, Position position) {
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
                leftBackward = column != 1 && isValid(figure, positions, panel - rightUpLeftDownAddend, 1);
            }
            if (rightBackward) {
                rightBackward = column != 8 && isValid(figure, positions, panel - leftUpRightDownAddend, 8);
            }
            if (rightForward) {
                rightForward = column != 8 && isValid(figure, positions, panel + rightUpLeftDownAddend, 8);
            }
            if (leftForward) {
                leftForward = column != 1 && isValid(figure, positions, panel + leftUpRightDownAddend, 1);
            }
        }
        return null;
    }

    private boolean isValid(Figure figure, List<Position> positions, int newPanel, int limit) {
        if (Position.isInBoard(newPanel)) {
            Position newPosition = Position.get(newPanel);

            if (newPosition.getColumn() == limit) {
                addPosition(positions, figure, newPosition);
                return false;
            } else {
                return addPosition(positions, figure, newPosition);
            }
        }
        return true;
    }

    private CheckItem getHorizontal(int max, Figure figure, Position position) {
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int column = position.getColumn();

        boolean left = true;
        boolean right = true;

        for (int addend = 1; addend < max + 1; addend++) {
            if (left) {
                left = column != 1 && isValid(figure, positions, panel - addend, 1);
            }
            if (right) {
                right = column != 8 && isValid(figure, positions, panel + addend, 8);
            }
        }
        return null;
    }

    private CheckItem getVertical(int max, Figure figure, Position position) {
        List<Position> positions = new ArrayList<>();

        int panel = position.getPanel();
        int row = position.getRow();

        boolean backward = true;
        boolean forward = true;

        for (int i = 1; i < max + 1; i++) {
            int verticalAddend = 8 * i;
            if (backward) {
                backward = row != 1 && isValid(figure, positions, panel - verticalAddend, 0);
            }
            if (forward) {
                forward = row != 8 && isValid(figure, positions, panel + verticalAddend, 0);
            }
        }
        return null;
    }

    private void removeKnightPositions(Figure figure, Position position) {
        int panel = position.getPanel();
        int column = position.getColumn();

        if (column < 7 && column > 2) {
            //left up down
            knightJump(figure, panel, 6, 10);
            //right up down
            knightJump(figure, panel, 10, 6);

            //up down left
            knightJump(figure, panel, 15, 17);
            //up down right
            knightJump(figure, panel, 17, 15);

        } else if (column == 1) {
            //right up down
            knightJump(figure, panel, 10, 6);
            //up down right
            knightJump(figure, panel, 17, 15);

        } else if (column == 2) {
            //right up down
            knightJump(figure, panel, 10, 6);
            //up down left
            knightJump(figure, panel, 15, 17);
            //up down right
            knightJump(figure, panel, 17, 15);

        } else if (column == 8) {
            //left up down
            knightJump(figure, panel, 6, 10);
            //up down left
            knightJump(figure, panel, 15, 17);

        } else if (column == 7) {
            //left up down
            knightJump(figure, panel, 6, 10);
            //up down left
            knightJump(figure, panel, 15, 17);
            //up down right
            knightJump(figure, panel, 17, 15);
        }
    }

    private void knightJump(Figure figure, int panel, int up, int down) {
        isValid(figure, new ArrayList<>(), panel - down, 0);
        isValid(figure, new ArrayList<>(), panel + up, 0);
    }

    private CheckItem removePawnPosition(Figure figure, Position positionOf) {
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

            addDiagonalStrike(currentColumn, positions, panel + 9, figure);
            addDiagonalStrike(currentColumn, positions, panel + 7, figure);
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
            addDiagonalStrike(currentColumn, positions, panel - 9, figure);
            addDiagonalStrike(currentColumn, positions, panel - 7, figure);
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

    private boolean addPosition(Collection<Position> positions, Figure figure, Position newPosition) {
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

    private void addDiagonalStrike(int currentColumn, List<Position> positions, int panel, Figure figure) {
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

    private static class CheckItem {
        private FigureType threat;
        private Position threatPosition;
        private List<Position> unblocked = new ArrayList<>();
        private FigureType blocker = null;
        private Position blockerPosition = null;
        private List<Position> blocked = new ArrayList<>();
    }
}

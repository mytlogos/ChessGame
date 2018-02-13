package chessGame.mechanics.board;

import chessGame.mechanics.Color;
import chessGame.mechanics.Figure;
import chessGame.mechanics.Position;

import java.util.*;
import java.util.stream.Stream;

/**
 *
 */
public class MultiArrayBoard extends AbstractBoard {
    private Figure[][] board = new Figure[8][8];

    @Override
    public void setFigure(Figure figure, Position position) {
        Objects.requireNonNull(figure);

//        if (kingsNotSet()) setKing(figure);

        final int row = position.getRow() - 1;
        final int column = position.getColumn() - 1;
        Figure previous = board[column][row];

        if (previous == null) {
            board[column][row] = figure;
        } else {
            throw new IllegalArgumentException("Is not Empty at " + position);
        }
    }

    @Override
    public void setEmpty(Position position) {
        final int row = position.getRow() - 1;
        final int column = position.getColumn() - 1;
        board[column][row] = null;
    }

    @Override
    public Position positionOf(Figure figure) {
        for (int column = 0; column < 8; column++) {
            for (int row = 0; row < 8; row++) {
                if (Objects.equals(board[column][row], figure)) {
                    return Position.get(row +1,column +1);
                }
            }
        }
        return Position.Unknown;
    }

    @Override
    public Figure figureAt(Position position) {
        final int row = position.getRow() - 1;
        final int column = position.getColumn() - 1;
        return board[column][row];
    }

    @Override
    public List<Figure> getFigures(Color player) {
        return getPlayerFigures().get(player);
    }

    @Override
    public Map<Color, List<Figure>> getPlayerFigures() {
        List<Figure> white = new ArrayList<>();
        List<Figure> black = new ArrayList<>();

        for (Figure figure : this) {
            if (figure != null) {
                if (figure.isWhite()) {
                    white.add(figure);
                } else {
                    black.add(figure);
                }
            }

        }
        Map<Color, List<Figure>> map = new HashMap<>();
        map.put(Color.WHITE, white);
        map.put(Color.BLACK, black);
        return map;
//        return getPlayerFiguresByStream();
    }

    @Override
    public List<Figure> getFigures() {
        List<Figure> figures = new ArrayList<>();

        for (Figure figure : this) {
            if (figure != null) {
                figures.add(figure);
            }
        }
        return figures;
//        return getFiguresByStream();
    }

    @Override
    Stream<Figure> stream() {
        return Arrays.stream(board).flatMap(Arrays::stream);
    }

    @Override
    public BoardSnapShot getSnapShot() {
        String[] snapshot = new String[64];

        for (int column = 0; column < 8; column++) {
            for (int row = 0; row < 8; row++) {
                Figure figure = board[column][row];

                if (figure != null) {
                    snapshot[row * 8 + column] = getNotation(figure);
                }
            }
        }
        return new BoardSnapShot(snapshot);
//        return getSnapShotByStream();
    }
}

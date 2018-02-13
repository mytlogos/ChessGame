package chessGame.mechanics.board;

import chessGame.mechanics.Figure;
import chessGame.mechanics.Color;
import chessGame.mechanics.Position;

import java.util.*;
import java.util.stream.Stream;

/**
 *
 */
public class ArrayBoard extends AbstractBoard{
    private final Figure[] board = new Figure[64];

    @Override
    public void setFigure(Figure figure, Position position) {
        Objects.requireNonNull(figure);

        final int index = position.getPanel();
        Figure previous = board[index];

        if (previous == null) {
            board[index] = figure;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setEmpty(Position position) {
        int index = position.getPanel();
        board[index] = null;
    }

    @Override
    public Position positionOf(Figure figure) {
        for (int i = 0; i < board.length; i++) {
            if (Objects.equals(board[i], figure)) {
                return Position.get(i);
            }
        }
        return Position.Unknown;
    }

    @Override
    public Figure figureAt(Position position) {
        return board[position.getPanel()];
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
    }

    @Override
    Stream<Figure> stream() {
        return Arrays.stream(board);
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
    }

    @Override
    public BoardSnapShot getSnapShot() {
        String[] snapshot = new String[64];

        for (int i = 0; i < board.length; i++) {
            Figure figure = board[i];
            if (figure != null) {
                snapshot[i] = getNotation(figure);
            }
        }
        return new BoardSnapShot(snapshot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayBoard figures = (ArrayBoard) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(board, figures.board);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(board);
    }
}


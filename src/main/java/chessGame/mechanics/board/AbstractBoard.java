package chessGame.mechanics.board;

import chessGame.mechanics.Figure;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public abstract class AbstractBoard implements FigureBoard {
    private Figure whiteKing;
    private Figure blackKing;
    private long hash;

    @Override
    public Iterator<Figure> iterator() {
        return new BoardIterator(this);
    }

    @Override
    public Figure getKing(boolean white) {
        return white ? whiteKing : blackKing;
    }

    @Override
    public boolean isEmptyAt(Position position) {
        return figureAt(position) == null;
    }

    void setKing(Figure figure) {
        if (figure.is(FigureType.KING)) {
            if (figure.isWhite()) {
                whiteKing = figure;
            } else {
                blackKing = figure;
            }
        }
    }

    @Override
    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    abstract Stream<Figure> stream();

    Map<Boolean, List<Figure>> getPlayerFiguresByStream() {
        return stream().filter(Objects::nonNull).collect(Collectors.groupingBy(Figure::isWhite));
    }

    List<Figure> getFiguresByStream() {
        return stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static class BoardIterator implements Iterator<Figure> {
        private final Board<Figure> board;
        private int index;

        private BoardIterator(Board<Figure> board) {
            this.board = board;
        }

        @Override
        public boolean hasNext() {
            return index < 64;
        }

        @Override
        public Figure next() {
            Figure figure = board.figureAt(Position.get(index));
            index++;
            return figure;
        }
    }
}

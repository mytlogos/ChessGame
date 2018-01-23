package chessGame.mechanics;

import chessGame.mechanics.Board;
import chessGame.mechanics.figures.Figure;

import java.util.Objects;

/**
 *
 */
final public class Move implements Cloneable {
    private final PositionChange change;
    private Figure figure;

    public Move(Figure figure, PositionChange change) {
        Objects.requireNonNull(figure);
        Objects.requireNonNull(change);

        this.change = change;
        this.figure = figure;
        checkState();
    }

    private void checkState() {
        if (!Objects.equals(figure.getPosition(), getFrom())) {
            throw new IllegalStateException("Figur sitzt nicht am richtigen Platz: " + figure + " Sollte sein: " + getFrom());
        }
    }

    public Position getFrom() {
        return change.getFrom();
    }

    public Position getTo() {
        return change.getTo();
    }

    final public Move clone(Board board, Game game, boolean promoting) {
        final Move clonedMove = clone();

        //should never happen, because it implements the Cloneable interface
        if (clonedMove == null) {
            return null;
        }
        if (promoting) {
            if (getFigure().getPosition() == null) {
                clonedMove.figure = getFigure().clone(board);
            } else {
                throw new IllegalStateException("Figure should not have a position prior to promoting");
            }
        } else {
            final Position position = getFigure().getPosition();
            if (Position.Bench.equals(position)) {
                clonedMove.figure = game.getBench().get(getFigure().getPlayer()).
                        stream().
                        filter(figure1 -> figure1.equals(getFigure())).
                        findFirst().
                        orElse(null);
            } else if (Position.Promoted.equals(position)) {
                clonedMove.figure = game.getPromoted().get(getFigure().getPlayer()).
                        stream().
                        filter(figure1 -> figure1.equals(getFigure())).
                        findFirst().
                        orElse(null);
            } else if (position != null) {
                clonedMove.figure = board.figureAt(position);
            } else {
                clonedMove.figure = figure.clone(board);
            }
        }
        if (clonedMove.figure == null) {
            throw new NullPointerException("figure is not allowed to be null");
        }
        return clonedMove;
    }

    public Figure getFigure() {
        return figure;
    }

    @Override
    public int hashCode() {
        int result = getChange() != null ? getChange().hashCode() : 0;
        result = 31 * result + (getFigure() != null ? getFigure().hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (getChange() != null ? !getChange().equals(move.getChange()) : move.getChange() != null) return false;
        return getFigure() != null ? getFigure().equals(move.getFigure()) : move.getFigure() == null;
    }

    @Override
    protected Move clone() {
        try {
            return (Move) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        final Figure figure = getFigure();
        final Position from = getChange().getFrom();
        final Position to = getChange().getTo();

        String fromNotation = from == null ? null : from.getColumnName() + from.getRow();
        final String notation = figure.getType() + "(" + figure.getPlayer().getType() + ") " + fromNotation + "->";

        if (to == Position.Bench) {
            return notation + "BENCH";
        } else if (to == Position.Promoted) {
            return notation + "PROMOTED";
        } else {
            return notation + to.getColumnName() + to.getRow();

        }
    }

    private PositionChange getChange() {
        return change;
    }
}

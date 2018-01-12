package chessGame.mechanics;

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
    }

    public Position getFrom() {
        return change.getFrom();
    }

    public Position getTo() {
        return change.getTo();
    }

    public Figure getFigure() {
        return figure;
    }

    final public Move clone(AbstractBoard board) {
        final Move clonedMove = clone();
        if (clonedMove == null) {
            return null;
        }
        clonedMove.figure = board.getFigure(getFigure().getPosition());
        if (clonedMove.figure == null) {
            throw new NullPointerException("figure is not allowed to be null");
        }
        return clonedMove;
    }

    @Override
    protected Move clone()  {
        try {
            return (Move) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
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
    public int hashCode() {
        int result = getChange() != null ? getChange().hashCode() : 0;
        result = 31 * result + (getFigure() != null ? getFigure().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final Figure figure = getFigure();
        final Position from = getChange().getFrom();
        final Position to = getChange().getTo();

        final String notation = figure.getType() + "(" + figure.getPlayer().getType() + ") " + from.getColumnName() + from.getRow() + "->";

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

package chessGame.mechanics.move;

import chessGame.mechanics.Color;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;

import java.util.Objects;

/**
 *
 */
final public class Move implements Cloneable {
    private final Position from;
    private final Position to;

    private final FigureType figure;
    private final Color player;

    public Move(Position from, Position to, FigureType figure, Color color) {
        Objects.requireNonNull(color);
        Objects.requireNonNull(figure);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        this.player = color;
        this.from = from;
        this.to = to;
        this.figure = figure;
    }

    public boolean isMoving(FigureType type) {
        return type == this.figure;
    }

    @Override
    public int hashCode() {
        int result = getFrom().hashCode();
        result = 31 * result + getTo().hashCode();
        result = 31 * result + getFigure().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        return getFrom().equals(move.getFrom())
                && getTo().equals(move.getTo())
                && getFigure().equals(move.getFigure());
    }

    @Override
    public String toString() {
        final FigureType figure = getFigure();

        String fromNotation = from == null ? null :
                Position.Bench == from ? "BENCH" :
                        Position.Promoted == from ? "PROMOTED" :
                                Position.Unknown == from ? "UNKOWNN" :
                                        from.getColumnName() + from.getRow();

        final String notation = figure + "(" + getColor() + ") " + fromNotation + "->";

        if (to == Position.Bench) {
            return notation + "BENCH";
        } else if (to == Position.Promoted) {
            return notation + "PROMOTED";
        } else {
            return notation + to.getColumnName() + to.getRow();
        }
    }

    public Color getColor() {
        return player;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public FigureType getFigure() {
        return figure;
    }

    public boolean isWhite() {
        return getColor() == Color.WHITE;
    }
}

package chessGame.mechanics;

import java.util.Objects;

/**
 *
 */
final class PositionChange {
    private final Position from;
    private final Position to;

    PositionChange(Position from, Position to) {
        if (Objects.equals(from, to)) {
            System.out.println("illegal positionChange");
        }

        this.from = from;
        this.to = to;
    }

    Position getFrom() {
        return from;
    }

    Position getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PositionChange that = (PositionChange) o;

        if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) return false;
        return getTo() != null ? getTo().equals(that.getTo()) : that.getTo() == null;
    }

    @Override
    public int hashCode() {
        int result = getFrom() != null ? getFrom().hashCode() : 0;
        result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PositionChange{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}

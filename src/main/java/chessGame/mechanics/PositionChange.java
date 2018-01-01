package chessGame.mechanics;

/**
 *
 */
public class PositionChange {
    private Position from;
    private Position to;

    public PositionChange(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }
}

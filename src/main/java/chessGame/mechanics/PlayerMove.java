package chessGame.mechanics;

/**
 *
 */
public class PlayerMove {
    private final Move move;
    private final Move second;

    public PlayerMove(Move move, Move second) {
        this.move = move;
        this.second = second;
    }

    public Move getMove() {
        return move;
    }

    public Move getSecondMove() {
        return second;
    }
}

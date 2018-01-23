package chessGame.mechanics;

import java.util.Arrays;

/**
 *
 */
public class BoardSnapShot {
    private final String[] board;
    private String key;

    public BoardSnapShot(String[] board) {
        this.board = board;
        key = Arrays.toString(board);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoardSnapShot that = (BoardSnapShot) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}

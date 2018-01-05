package chessGame.mechanics;

import java.io.Serializable;

/**
 *
 */
public class Position implements Comparable<Position>, Serializable {
    private final int row;
    private final int column;

    private boolean isEmpty = true;
    private boolean isDangerous;
    private boolean isEnemy;

    public static Position Bench = new Position(-1, -1);
    public static Position Promoted = new Position(-2, -2);

    private Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public static Position get(int rowIndex, int columnIndex) {
        if (!isInBoard(rowIndex, columnIndex)) {
            throw new IllegalArgumentException();
        }
        return new Position(rowIndex, columnIndex);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getColumnName() {
        if (column > 8 || column < 1) {
            throw new IllegalStateException();
        }
        return String.valueOf((char) (column -1 + 'A'));
    }

    public boolean isAlly() {
        return !isDangerous() && !isEmpty() && !isEnemy();
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public boolean isDangerous() {
        return isDangerous;
    }

    public void setDangerous(boolean dangerous) {
        isDangerous = dangerous;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public void setEnemy(boolean enemy) {
        isEnemy = enemy;
    }

    public boolean isInBoard() {
        return getRow() >= 1 && getRow() <= 8 && getColumn() >= 1 && getColumn() <= 8;
    }

    public static boolean isInBoard(int row, int column) {
        return row >= 1 && row <= 8 && column >= 1 && column <= 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (getRow() != position.getRow()) return false;
        return getColumn() == position.getColumn();
    }

    @Override
    public int hashCode() {
        int result = getRow();
        result = 31 * result + getColumn();
        return result;
    }

    @Override
    public String toString() {
        return this == Position.Bench ? "Position{Bench}" :
                this == Position.Promoted ? "Position{Promoted}" :
                "Position{" + getColumnName() + getRow() + "}";
    }

    @Override
    public int compareTo(Position o) {
        if (o == this) return 0;
        if (o == null || !getClass().equals(o.getClass())) return -1;

        final int compare = getRow() - o.getRow();

        if (compare == 0) {
            return getColumn() - o.getColumn();
        }
        return compare;
    }
}

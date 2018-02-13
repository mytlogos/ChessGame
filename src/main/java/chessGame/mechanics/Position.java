package chessGame.mechanics;

import java.io.Serializable;

/**
 *
 */
public final class Position implements Comparable<Position>, Serializable {
    public static final Position Bench = new Position(-1);
    public static final Position Promoted = new Position(-2);
    public static final Position Unknown = new Position(-3);

    private final int panel;
    private int column;
    private int row;

    private Position(int panel) {
        this.panel = panel;
    }

    /**
     * Creates a new Position with the given Row and Column.
     *
     * @param row    row of the boardMap, needs to be in range of 1 <= row <= 8
     * @param column column of the boardMap, needs to be in range of 1 <= column <= 8
     * @return a Position Object.
     * @throws IllegalArgumentException if row or column is not in range
     */
    public static Position get(int row, int column) {
        int panel = convert(row, column);
        if (!isInBoard(panel)) {
            throw new IllegalArgumentException("out of index: " + row + "|" + column);
        }
        return get(panel);
    }

    private static int convert(int row, int column) {
        return (row - 1) * 8 + column - 1;
    }

    public static boolean isInBoard(int panel) {
        return panel >= 0 && panel <= 63;
    }

    public static Position get(int panel) {
        if (!isInBoard(panel)) {
            throw new IllegalArgumentException("Not on Board! " + panel);
        }
        return new Position(panel);
    }

    public boolean isInBoard() {
        return isInBoard(panel);
    }

    @Override
    public int hashCode() {
        return getPanel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;
        return getPanel() == position.getPanel();
    }

    public int getPanel() {
        return panel;
    }

    @Override
    public String toString() {
        return this == Position.Bench ? "Position{Bench}" :
                this == Position.Promoted ? "Position{Promoted}" :
                        this == Position.Unknown ? "Position{Unknown}" :
                                "Position{" + getColumnName() + getRow() + "}";
    }

    public String getColumnName() {
        if (getColumn() > 8 || getColumn() < 1) {
            throw new IllegalStateException();
        }
        return String.valueOf((char) (getColumn() - 1 + 'A'));
    }

    public String notation() {
        return getColumnName() + getRow();
    }

    public int getRow() {
        if (row == 0) {
            row = (panel / 8) + 1;
        }
        return row;
    }

    public int getColumn() {
        if (column == 0) {
            column = (panel % 8) + 1;
        }
        return column;
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

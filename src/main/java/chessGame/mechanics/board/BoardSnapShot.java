package chessGame.mechanics.board;

import chessGame.mechanics.Color;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;

import java.util.*;

import static chessGame.mechanics.FigureType.PAWN;
import static chessGame.mechanics.Color.BLACK;
import static chessGame.mechanics.Color.WHITE;

/**
 *
 */
public class BoardSnapShot implements Iterable<String> {
    private final String[] board;
    private final String key;
    private int blackCounter = -1;
    private Map<Color, Map<Integer, List<Integer>>> advanceMap;
    private int whiteCounter = -1;

    BoardSnapShot(String[] board) {
        this.board = board;
        key = Arrays.toString(board);
    }

    public String getKey() {
        return key;
    }

    /**
     * Returns true if this is more advanced than the Parameter.
     * Advance is specified by the count and the advance of the pawns of each player.
     * Pawns can only move forward not backward.
     * If this Snapshot has less Pawns of any player than the parameter, it means it is more advanced,
     * because you can only lose Pawns.
     * The state of white will be examined before the state of black.
     *
     * @param snapShot snapShot to examine
     * @return true if this snapShot is more advanced than the Parameter.
     */
    public boolean isAdvanced(BoardSnapShot snapShot) {
        return getCount(PAWN, WHITE) < snapShot.getCount(PAWN, WHITE)
                || getCount(PAWN, BLACK) < snapShot.getCount(PAWN, BLACK)
                /*|| compareAdvance(snapShot, WHITE)
                || compareAdvance(snapShot, BLACK)*/;

    }

    private int getCount(FigureType type, Color player) {
        if (player == BLACK && blackCounter >= 0) {
            return blackCounter;
        } else if (player == WHITE && whiteCounter >= 0) {
            return whiteCounter;
        }

        char notation = type.getNotation();
        String figureNotation = String.valueOf(player == WHITE ? notation : Character.toLowerCase(notation));

        int counter = 0;
        for (String figure : this) {
            if (Objects.equals(figure, figureNotation)) {
                counter++;
            }
        }

        if (player == BLACK) {
            blackCounter = counter;
        } else {
            whiteCounter = counter;
        }
        return counter;
    }

    private boolean compareAdvance(BoardSnapShot snapShot, Color type) {
        Map<Integer, List<Integer>> otherListMap = snapShot.getAdvance().get(type);
        Map<Integer, List<Integer>> ownListMap = getAdvance().get(type);

        for (int column = 1; column <= 8; column++) {
            List<Integer> ownRows = ownListMap.get(column);
            List<Integer> otherRows = otherListMap.get(column);

            int ownSize = ownRows.size();
            int otherSize = otherRows.size();

            if (ownSize == otherSize) {
                int ownSum = getSum(ownRows);
                int otherSum = getSum(otherRows);

                //if black: more advanced means the sum is smaller then the other, else the opposite
                return type == BLACK ? ownSum < otherSum : ownSum > otherSum;
            } else if (ownSize < otherSize) {
                if (ownSize != 0) {
                    int ownMin = ownRows.stream().mapToInt(Integer::intValue).min().orElse(0);
                    int otherMin = otherRows.stream().mapToInt(Integer::intValue).min().orElse(0);
                }
            }
        }
        return false;
    }

    private Map<Color, Map<Integer, List<Integer>>> getAdvance() {
        if (advanceMap != null) {
            return advanceMap;
        }
        advanceMap = new HashMap<>();
        char notationWhite = PAWN.getNotation();
        String pawnNotationWhite = String.valueOf(notationWhite);
        String pawnNotationBlack = pawnNotationWhite.toLowerCase();

        advanceMap.put(WHITE, new HashMap<>());
        advanceMap.put(BLACK, new HashMap<>());

        for (int panel = 0; panel < board.length; panel++) {
            String s = board[panel];

            Position position = Position.get(panel);
            if (pawnNotationWhite.equals(s)) {
                advanceMap.get(WHITE).computeIfAbsent(position.getColumn(), k -> new ArrayList<>()).add(position.getRow());
            } else if (pawnNotationBlack.equals(s)) {
                advanceMap.get(BLACK).computeIfAbsent(position.getColumn(), k -> new ArrayList<>()).add(position.getRow());
            }
        }
        return advanceMap;
    }

    private int getSum(List<Integer> otherRows) {
        return otherRows.stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoardSnapShot that = (BoardSnapShot) o;
        return key.equals(that.key);
    }

    @Override
    public Iterator<String> iterator() {
        return new SnapShotIterator(this);
    }

    private static class SnapShotIterator implements Iterator<String> {
        private final BoardSnapShot snapShot;
        private int index;

        SnapShotIterator(BoardSnapShot snapShot) {
            this.snapShot = snapShot;
        }

        @Override
        public boolean hasNext() {
            return index < 64;
        }

        @Override
        public String next() {
            String figure = snapShot.board[index];
            index++;
            return figure;
        }
    }

}

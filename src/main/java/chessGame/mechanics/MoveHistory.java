package chessGame.mechanics;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class MoveHistory implements Iterable<PlayerMove> {
    private LinkedList<PlayerMove> moves = new LinkedList<>();
    private Map<BoardSnapShot, Integer> boardFrequency = new HashMap<>();

    MoveHistory() {

    }

    private MoveHistory(Collection<PlayerMove> moves) {
        this.moves.addAll(moves);
    }

    public boolean contains(PlayerMove move) {
        return moves.contains(move);
    }

    public PlayerMove getLast() {
        return moves.isEmpty() ? null : moves.getLast();
    }

    public int size() {
        return moves.size();
    }

    public Collection<PlayerMove> getMovesHistory() {
        return new ArrayList<>(moves);
    }

    public void removeLast() {
        if (!isEmpty()) {
            PlayerMove move = moves.removeLast();
            BoardSnapShot boardKey = move.getBoardSnap();
            Integer frequency = boardFrequency.get(boardKey);
            --frequency;

            if (frequency <= 0) {
                boardFrequency.remove(boardKey);
            } else {
                boardFrequency.put(boardKey, frequency);
            }
        }
    }

    public boolean isEmpty() {
        return moves.isEmpty();
    }

    public boolean add(PlayerMove move, Board board) {
        BoardSnapShot snapShot = board.getSnapShot();

        move.setBoardSnap(snapShot);
        boardFrequency.compute(snapShot, (k, integer) -> integer == null ? 1 : ++integer);
        return moves.add(move);
    }

    public int checkOccurrences(Board board) {
        BoardSnapShot snapShot = board.getSnapShot();
        return boardFrequency.getOrDefault(snapShot, 0);
    }

    public MoveHistory clone(Board board, Game game) {
        final List<PlayerMove> moves = this.moves.stream().map(move -> move.clone(board, game)).collect(Collectors.toList());
        return new MoveHistory(moves);
    }

    @Override
    public Iterator<PlayerMove> iterator() {
        return moves.iterator();
    }

    public Stream<PlayerMove> stream() {
        return moves.stream();
    }

    public List<PlayerMove> lastHundred() {
        int size = moves.size();
        if (size < 100) {
            return moves.subList(0, size);
        }
        return moves.subList(size - 100, size);
    }
}

package chessGame.mechanics.move;

import chessGame.mechanics.BoardEncoder;
import chessGame.mechanics.game.Game;
import chessGame.mechanics.Position;

import java.util.*;
import java.util.stream.Stream;

import static chessGame.mechanics.FigureType.KING;
import static chessGame.mechanics.FigureType.PAWN;
import static chessGame.mechanics.FigureType.ROOK;

/**
 *
 */
public class MoveHistory implements Iterable<PlayerMove> {
    private final LinkedList<PlayerMove> moves = new LinkedList<>();
    private final Map<BitSet, Integer> boardFrequency = new HashMap<>();
    private Game game;

    private CastlingRights whiteCastlingRights = new CastlingRights();
    private CastlingRights blackCastlingRights = new CastlingRights();

    private MoveHistory(Game game, Collection<PlayerMove> moves) {
        this(game);
        this.moves.addAll(moves);
    }

    public PlayerMove moveAtPly(int ply) {
        return moves.get(ply);
    }

    public MoveHistory(Game game) {
        this.game = game;
    }

    public MoveHistory(MoveHistory history) {
        this.game = history.game;
        this.moves.addAll(history.moves);
    }

    public int size() {
        return moves.size();
    }

    public int getEnPassantColumn(boolean white) {
        PlayerMove move = getLast();

        if (move != null && move.isWhite() == white) {
            Move mainMove = move.getMainMove();

            if (mainMove.isMoving(PAWN)) {
                Position from = mainMove.getFrom();
                int diff = Math.abs(from.getPanel() - mainMove.getTo().getPanel());

                //a difference of 16 panels symbolizes an advance of 2 rows of the color direction
                if (diff == 16) {
                    return from.getColumn();
                }
            }
        }
        return 0;
    }

    public Collection<PlayerMove> getMovesHistory() {
        return new ArrayList<>(moves);
    }

    public boolean longCastling(boolean white) {
        CastlingRights rights = getRights(white);
        CastleState state = rights.state;

        return state == CastleState.BOTH || state == CastleState.LONG;
    }

    public PlayerMove get(int i) {
        return moves.get(i);
    }

    private CastlingRights getRights(boolean white) {
        return white ? whiteCastlingRights : blackCastlingRights;
    }

    public boolean shortCastling(boolean white) {
        CastlingRights rights = getRights(white);
        CastleState state = rights.state;
        return state == CastleState.BOTH || state == CastleState.SHORT;
    }

    public PlayerMove getLast() {
        return moves.isEmpty() ? null : moves.getLast();
    }

    public void removeLast() {
        if (!isEmpty()) {
            PlayerMove move = moves.removeLast();

            removeFrequency();
            reduceCastlingStack(move);
        }
    }

    private void reduceCastlingStack(PlayerMove playerMove) {
        if (isStrikingRook(playerMove) || playerMove.isCastlingMove()) {
            //noinspection ConstantConditions
            reduceCastlingStack(playerMove, playerMove.getSecondaryMove().get());
        }

        if (playerMove.getMainMove().isMoving(ROOK)) {
            reduceCastlingStack(playerMove, playerMove.getMainMove());

        } else if (playerMove.getMainMove().isMoving(KING)) {
            CastlingRights rights = getRights(playerMove.isWhite());

            if (playerMove.equals(rights.kingMoves.peekLast())) {
                rights.kingMoves.removeLast();
                rights.changeCastleState();
            }
        }
    }

    private void reduceCastlingStack(PlayerMove playerMove, Move strikeMove) {
        CastlingRights rights = getRights(strikeMove.isWhite());

        Deque<PlayerMove> lastRookMoves = rights.lastRookMoves;
        Deque<PlayerMove> firstRookMoves = rights.firstRookMoves;

        if (playerMove.equals(lastRookMoves.peekLast())) {
            lastRookMoves.removeLast();
            rights.changeCastleState();
        }

        if (playerMove.equals(firstRookMoves.peekLast())) {
            firstRookMoves.removeLast();
            rights.changeCastleState();
        }
    }

    public boolean isEmpty() {
        return moves.isEmpty();
    }

    private void removeFrequency() {
        BitSet snapShot = game.getSnapShot();
        BitSet boardKey = BoardEncoder.getBoardSet(snapShot);
        Integer frequency = boardFrequency.get(boardKey);
        --frequency;

        if (frequency <= 0) {
            boardFrequency.remove(boardKey);
        } else {
            boardFrequency.put(boardKey, frequency);
        }
    }

    private void checkCastlingRights(PlayerMove playerMove) {
        if (isStrikingRook(playerMove) || playerMove.isCastlingMove()) {
            //noinspection ConstantConditions
            checkRookCastle(playerMove, playerMove.getSecondaryMove().get());
        }

        if (playerMove.getMainMove().isMoving(ROOK)) {
            checkRookCastle(playerMove, playerMove.getMainMove());

        } else if (playerMove.getMainMove().isMoving(KING)) {
            CastlingRights rights = getRights(playerMove.isWhite());
            rights.kingMoves.add(playerMove);
            rights.changeCastleState();
        }
    }

    private void checkRookCastle(PlayerMove playerMove, Move move) {
        Position from = move.getFrom();

        int column = from.getColumn();
        CastlingRights castlingRights = getRights(move.isWhite());

        Deque<PlayerMove> firstRookMoves = castlingRights.firstRookMoves;
        Deque<PlayerMove> lastRookMoves = castlingRights.lastRookMoves;

        Deque<PlayerMove> chosenDeque;

        if (column == 1) {
            chosenDeque = checkCastle(from, firstRookMoves, lastRookMoves);
        } else if (column == 8) {
            chosenDeque = checkCastle(from, lastRookMoves, firstRookMoves);
        } else {
            PlayerMove firstPeek = firstRookMoves.peekLast();
            PlayerMove secondPeek = lastRookMoves.peekLast();

            if (firstPeek == null) {
                chosenDeque = lastRookMoves;

            } else if (secondPeek == null) {
                chosenDeque = firstRookMoves;

            } else if (firstPeek.getMainMove().getTo().equals(from) || (castledFrom(from, firstPeek))) {
                chosenDeque = firstRookMoves;

            } else if (secondPeek.getMainMove().getTo().equals(from) || castledFrom(from, secondPeek)) {
                chosenDeque = lastRookMoves;

            } else {
                return;
            }
        }

        chosenDeque.add(playerMove);
        castlingRights.changeCastleState();
    }

    private boolean castledFrom(Position from, PlayerMove secondPeek) {
        //noinspection ConstantConditions
        return secondPeek.isCastlingMove() && secondPeek.getSecondaryMove().get().getTo().equals(from);
    }

    private Deque<PlayerMove> checkCastle(Position from, Deque<PlayerMove> rookStack, Deque<PlayerMove> otherRookStack) {
        PlayerMove peekLast = rookStack.peekLast();

        if (peekLast == null) {
            return rookStack;
        } else if (peekLast.getMainMove().getTo().equals(from) || isStrikingRook(peekLast)) {
            return rookStack;
        } else {
            PlayerMove last = otherRookStack.peekLast();
            if (last == null) {
                return otherRookStack;
            } else if (last.getMainMove().getTo().equals(from) || isStrikingRook(last)) {
                return otherRookStack;
            }
        }
        return new ArrayDeque<>();
    }

    private boolean isStrikingRook(PlayerMove peekLast) {
        return peekLast.isStrike() && peekLast.getSecondaryMove().map(move -> move.isMoving(ROOK)).orElse(Boolean.FALSE);
    }

    public boolean add(PlayerMove move) {
        addFrequency();
        checkCastlingRights(move);
        return moves.add(move);
    }

    private void addFrequency() {
        BitSet snapShot = game.getSnapShot();
        BitSet boardKey = BoardEncoder.getBoardSet(snapShot);
        boardFrequency.compute(boardKey, (k, integer) -> integer == null ? 1 : ++integer);
    }

    public int checkOccurrences() {
        BitSet snapShot = game.getSnapShot();
        return boardFrequency.getOrDefault(snapShot, 0);
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

    private static class CastlingRights {
        private Deque<CastleState> states = new ArrayDeque<>();
        private CastleState state = CastleState.BOTH;


        private Deque<PlayerMove> kingMoves = new ArrayDeque<>(20);
        private Deque<PlayerMove> firstRookMoves = new ArrayDeque<>(20);
        private Deque<PlayerMove> lastRookMoves = new ArrayDeque<>(20);


        public CastlingRights() {
        }

        private void changeCastleState() {
            if (!kingMoves.isEmpty()) {
                changeState(CastleState.NONE);
            } else {
                if (!firstRookMoves.isEmpty() && !lastRookMoves.isEmpty()) {
                    changeState(CastleState.NONE);
                } else if (firstRookMoves.isEmpty() && !lastRookMoves.isEmpty()) {
                    changeState(CastleState.LONG);
                } else if (!firstRookMoves.isEmpty()) {
                    changeState(CastleState.SHORT);
                } else {
                    changeState(CastleState.BOTH);
                }
            }
        }

        private void changeState(CastleState castleState) {
            if (state != castleState) {
                state = castleState;
            }
        }
    }

    public enum CastleState {
        NONE,
        LONG,
        SHORT,
        BOTH
    }

}

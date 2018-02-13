package chessGame.mechanics.board;

import chessGame.mechanics.*;
import chessGame.mechanics.game.Game;
import chessGame.mechanics.move.Move;
import chessGame.mechanics.move.MoveHistory;
import chessGame.mechanics.move.PlayerMove;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static chessGame.mechanics.FigureType.*;

/**
 *
 */
public class ZobristHasher implements BoardHasher {
    private static final long[][][] positionArray = new long[2][6][64];
    private static final long[][] castling_rights = new long[2][4];
    private static final long[] en_passant = new long[8];
    private static final long[] atMove = new long[2];

    private static final int White = 0;
    private static final int Black = 1;

    private static final int Castle_Both = 0;
    private static final int Castle_Short = 1;
    private static final int Castle_Long = 2;
    private static final int Castle_None = 3;

    private static final int Pawn = 0;
    private static final int Rook = 1;
    private static final int Knight = 2;
    private static final int Bishop = 3;
    private static final int Queen = 4;
    private static final int King = 5;

    static {
        initArrays();
    }

    private final Game game;
    private int[] previousCastles = new int[2];
    private int[] previousEnPassant = new int[2];

    public ZobristHasher(Game game) {
        this.game = game;
        previousCastles[0] = -1;
        previousCastles[1] = -1;

        previousEnPassant[0] = -1;
        previousEnPassant[1] = -1;
    }

    private static void initArrays() {
        Random random = new Random();
        Collection<Long> longs = new ArrayList<>();

        initPlayerDependantArrays(random, longs, White);
        initPlayerDependantArrays(random, longs, Black);

        for (int panel = 0; panel < 8; panel++) {
            en_passant[panel] = getNextLong(random, longs);
        }
    }

    private static void initPlayerDependantArrays(Random random, Collection<Long> longs, int color) {
        for (int piece = 0; piece < 6; piece++) {
            for (int panel = 0; panel < 64; panel++) {
                positionArray[color][piece][panel] = getNextLong(random, longs);
            }
        }

        castling_rights[color][Castle_None] = getNextLong(random, longs);
        castling_rights[color][Castle_Short] = getNextLong(random, longs);
        castling_rights[color][Castle_Long] = getNextLong(random, longs);
        castling_rights[color][Castle_Both] = getNextLong(random, longs);

        atMove[color] = getNextLong(random, longs);
    }

    private static long getNextLong(Random random, Collection<Long> longs) {
        long nextLong = random.nextLong();

        while (longs.contains(nextLong)) {
            nextLong = random.nextLong();
        }
        longs.add(nextLong);
        return nextLong;
    }

    @Override
    public void hashBoard() {
        long hash = getHash();
        ((AbstractBoard) game.getBoard()).setHash(hash);
    }

    public long getHash() {
        long hash = 0;
        Board board = game.getBoard();
        MoveHistory history = game.getHistory();

        for (int panel = 0; panel < 64; panel++) {
            Figure figure = board.figureAt(Position.get(panel));

            if (figure != null) {
                int color = getColor(figure.isWhite());
                int type = getType(figure.getType());
                hash ^= positionArray[color][type][panel];
            }
        }

        hash = hashCastling(hash, game.getBlack().getColor());
        hash = hashCastling(hash, game.getWhite().getColor());

        hash = hashEnpassant(hash, history.getLast());

        int atMoveIndex = getColor(game.getAtMove().isWhite());
        hash ^= atMove[atMoveIndex];
        return hash;
    }

    private int getColor(boolean white) {
        return white ? White : Black;
    }

    private int getType(FigureType figure) {
        int type;
        if (figure == PAWN) {
            type = Pawn;
        } else if (figure == ROOK) {
            type = Rook;
        } else if (figure == KNIGHT) {
            type = Knight;
        } else if (figure == BISHOP) {
            type = Bishop;
        } else if (figure == QUEEN) {
            type = Queen;
        } else if (figure == KING) {
            type = King;
        } else {
            throw new IllegalStateException();
        }
        return type;
    }

    private long hashEnpassant(long hash, PlayerMove move) {
        if (move == null) return hash;

        boolean moveWhite = move.isWhite();
        int colorIndex = getColor(moveWhite);
        int previousEP = previousEnPassant[colorIndex];

        if (previousEP >= 0) {
            hash ^= en_passant[previousEP];
        }

        int enPassantColumn = game.getHistory().getEnPassantColumn(moveWhite);
        int column = enPassantColumn - 1;

        if (column >= 0) {
            hash ^= en_passant[column];
        }

        previousEnPassant[colorIndex] = column;
        return hash;
    }

    private int getCastlingType(boolean longCastling, boolean shortCastling) {
        int castlingType;

        if (longCastling && shortCastling) {
            castlingType = Castle_Both;
        } else if (!longCastling && shortCastling) {
            castlingType = Castle_Short;
        } else if (longCastling) {
            castlingType = Castle_Long;
        } else {
            castlingType = Castle_None;
        }
        return castlingType;
    }

    @Override
    public void forwardHash(PlayerMove playerMove) {
        long hash = game.getBoard().getHash();

        if (playerMove.isPromotion()) {
            hash = hashForwardPromotion(playerMove, hash);

        } else {
            hash = hashMove(playerMove.getMainMove(), hash);

            long finalHash = hash;
            hash = playerMove.getSecondaryMove().map(move -> hashStrike(move, finalHash)).orElse(hash);
            hash = hashEnpassant(hash, playerMove);
        }
        hash = hashCastling(hash, game.getBlack().getColor());
        hash = hashCastling(hash, game.getWhite().getColor());
        hash = getMoveHash(hash);

        ((AbstractBoard) game.getBoard()).setHash(hash);
    }

    @Override
    public void backWardHash(PlayerMove playerMove) {
        long hash = game.getBoard().getHash();

        if (playerMove.isPromotion()) {
            hash = hashBackwardPromotion(playerMove, hash);
        } else {
            hash = hashMove(playerMove.getMainMove(), hash);

            long finalHash = hash;
            hash = playerMove.getSecondaryMove().map(move -> hashStrike(move, finalHash)).orElse(hash);
            hash = hashEnpassant(hash, playerMove);
        }
        hash = hashCastling(hash, game.getBlack().getColor());
        hash = hashCastling(hash, game.getWhite().getColor());
        hash = getMoveHash(hash);

        ((AbstractBoard) game.getBoard()).setHash(hash);
    }

    private long hashCastling(long hash, Color color) {
        MoveHistory history = game.getHistory();

        boolean white = color.isWhite();

        boolean shortCastling = history.shortCastling(white);
        boolean longCastling = history.longCastling(white);

        int currentCastling = getCastlingType(longCastling, shortCastling);
        int colorIndex = getColor(color.isWhite());

        int previousCastle = previousCastles[colorIndex];

        //save castle type
        previousCastles[colorIndex] = currentCastling;

        if (previousCastle < 0) {
            return hash ^ castling_rights[colorIndex][currentCastling];
        } else {
            return hash ^ castling_rights[colorIndex][previousCastle] ^ castling_rights[colorIndex][currentCastling];
        }
    }

    private long getMoveHash(long hash) {
        Color atMove = game.getAtMoveColor();
        boolean atMoveWhite = atMove.isWhite();

        hash ^= ZobristHasher.atMove[getColor(!atMoveWhite)];
        hash ^= ZobristHasher.atMove[getColor(atMoveWhite)];
        return hash;
    }

    private long hashForwardPromotion(PlayerMove playerMove, long hash) {
        Move mainMove = playerMove.getMainMove();
        int fromPanel = mainMove.getFrom().getPanel();
        int color = getColor(mainMove.getColor().isWhite());
        int type = getType(mainMove.getFigure());

        Move promotion = playerMove.getPromotionMove().orElseThrow(IllegalArgumentException::new);
        FigureType promotionFigure = promotion.getFigure();
        int promotionType = getType(promotionFigure);
        int toPanel = promotion.getTo().getPanel();

        hash ^= positionArray[color][type][fromPanel] ^ positionArray[color][promotionType][toPanel];
        long finalHash = hash;
        hash = playerMove.getSecondaryMove().map(move -> hashStrike(move, finalHash)).orElse(hash);
        return hash;
    }

    private long hashBackwardPromotion(PlayerMove playerMove, long hash) {
        Move mainMove = playerMove.getMainMove();
        int fromPanel = mainMove.getFrom().getPanel();
        int color = getColor(mainMove.getColor().isWhite());
        int type = getType(mainMove.getFigure());

        Move promotion = playerMove.getPromotionMove().orElseThrow(IllegalArgumentException::new);
        FigureType promotionFigure = promotion.getFigure();
        int promotionType = getType(promotionFigure);
        int toPanel = promotion.getTo().getPanel();

        hash ^= positionArray[color][promotionType][toPanel] ^ positionArray[color][type][fromPanel];

        long finalHash = hash;
        hash = playerMove.getSecondaryMove().map(move -> hashStrike(move, finalHash)).orElse(hash);
        return hash;
    }

    private long hashMove(Move move, long hash) {
        int fromPanel = move.getFrom().getPanel();
        int toPanel = move.getTo().getPanel();
        int color = getColor(move.getColor().isWhite());
        int type = getType(move.getFigure());

        return hash ^ positionArray[color][type][fromPanel] ^ positionArray[color][type][toPanel];
    }

    private long hashStrike(Move move, long hash) {
        int fromPanel = move.getFrom().getPanel();
        int color = getColor(move.getColor().isWhite());
        int type = getType(move.getFigure());

        hash ^= positionArray[color][type][fromPanel];
        return hash;
    }

}

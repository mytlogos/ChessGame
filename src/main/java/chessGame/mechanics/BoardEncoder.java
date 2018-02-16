package chessGame.mechanics;

import chessGame.mechanics.board.ArrayBoard;
import chessGame.mechanics.board.Board;
import chessGame.mechanics.board.FigureBoard;
import chessGame.mechanics.game.ChessGameImpl;
import chessGame.mechanics.game.Game;
import chessGame.mechanics.move.Move;
import chessGame.mechanics.move.MoveHistory;
import chessGame.mechanics.move.PlayerMove;

import java.util.BitSet;

/**
 *
 */
public abstract class BoardEncoder {
    public static String Start = "1000 0100 0110 1010 1100 0110 0100 1000 0010 0010 0010 0010 0010 0010 0010 0010";
    private static String empty = "000";
    private static String pawn = "001";
    private static String knight = "010";
    private static String bishop = "011";
    private static String rook = "100";
    private static String queen = "101";
    private static String king = "110";
    private static String castle_none = "00";
    private static String castle_short = "01";
    private static String castle_long = "10";
    private static String castle_both = "11";
    private static int positionSize = 4;

    private static int whiteEPIndex = 256;
    private static int blackEPIndex = 260;
    private static int whiteCastleIndex = 264;
    private static int blackCastleIndex = 266;
    private static int atMoveIndex = 268;
    private static int maxSize = 269;

    public static BitSet encode(Game game) {
        BitSet set = new BitSet(maxSize);

        FigureBoard board = game.getBoard();

        for (int panel = 0; panel < 64; panel++) {
            Figure figure = board.figureAt(Position.get(panel));
            int position = panel == 0 ? 0 : panel * positionSize;

            String bitPattern;
            int color = 0;

            if (figure != null) {
                color = getColor(figure.isWhite());
                bitPattern = getFigurePattern(figure.getType());
            } else {
                bitPattern = empty;
            }
            setFigure(set, position, color, bitPattern);
        }

        setGameParameter(set, game);

        return set;
    }

    private static int getColor(boolean white) {
        return white ? 0 : 1;
    }

    private static String getFigurePattern(FigureType figure) {
        String bitPattern;
        if (figure == FigureType.PAWN) {
            bitPattern = pawn;

        } else if (figure == FigureType.KNIGHT) {
            bitPattern = knight;

        } else if (figure == FigureType.BISHOP) {
            bitPattern = bishop;

        } else if (figure == FigureType.ROOK) {
            bitPattern = rook;

        } else if (figure == FigureType.QUEEN) {
            bitPattern = queen;

        } else if (figure == FigureType.KING) {
            bitPattern = king;
        } else {
            throw new IllegalArgumentException("Illegal Argument: " + figure);
        }
        return bitPattern;
    }

    private static void setFigure(BitSet set, int position, int color, String bitPattern) {
        setToBitSet(set, position, bitPattern + color);
    }

    private static void setGameParameter(BitSet set, Game game) {
        setCastle(set, game, game.getWhite(), whiteCastleIndex);
        setCastle(set, game, game.getBlack(), blackCastleIndex);
        setEnpassant(set, game, whiteEPIndex, game.getWhite().isWhite());
        setEnpassant(set, game, blackEPIndex, game.getBlack().isWhite());

        Player atMove = game.getAtMove();

        if (atMove.isWhite()) {
            set.clear(atMoveIndex);
        } else {
            set.set(atMoveIndex);
        }
    }

    private static void setToBitSet(BitSet set, int setIndex, String binaryString) {
        char zero = '0';
        char one = '1';

        int length = binaryString.length();
        for (int index = setIndex; index < setIndex + length; index++) {
            char charAt = binaryString.charAt(index - setIndex);

            if (charAt == zero) {
                set.clear(index);
            } else if (charAt == one) {
                set.set(index);
            } else {
                throw new IllegalArgumentException("Is not binary!");
            }
        }
    }

    private static void setCastle(BitSet set, Game game, Player player, int castleIndex) {
        MoveHistory history = game.getHistory();
        boolean longCastling = history.longCastling(player.isWhite());
        boolean shortCastling = history.shortCastling(player.isWhite());

        String pattern;

        if (longCastling && shortCastling) {
            pattern = castle_both;
        } else if (longCastling) {
            pattern = castle_long;
        } else if (shortCastling) {
            pattern = castle_short;
        } else {
            pattern = castle_none;
        }

        setToBitSet(set, castleIndex, pattern);
    }

    private static void setEnpassant(BitSet set, Game game, int epIndex, boolean white) {
        int enPassantColumn = game.getHistory().getEnPassantColumn(white);
        setToBitSet(set, epIndex, 4, enPassantColumn);
    }

    private static void setToBitSet(BitSet set, int setIndex, int portion, int i) {
        String format = "%" + portion + "s";
        String string = Integer.toBinaryString(i);
        String binaryString = String.format(format, string).replace(" ", "0");

        setToBitSet(set, setIndex, binaryString);
    }

    public static void updateForward(BitSet set, Game game, PlayerMove lastMove) {
        lastMove.getSecondaryMove().ifPresent(move -> makeMoveForward(move, set));
        lastMove.getPromotionMove().ifPresent(move -> makeMoveForward(move, set));
        makeMoveForward(lastMove.getMainMove(), set);

        setGameParameter(set, game);
    }

    public static FigureBoard getBoard(BitSet set) {
        FigureBoard board = new ArrayBoard();

        for (int panel = 0; panel < 64; panel++) {
            Position position = Position.get(panel);
            Figure figure = getFigure(panel * positionSize, set);

            if (figure != null) {
                board.setFigure(figure, position);
            }
        }

        return board;
    }

    private static void makeMoveForward(Move move, BitSet set) {
        Position from = move.getFrom();
        String figurePattern = getFigurePattern(move.getFigure());

        if (from.isInBoard()) {
            int index = from.getPanel() * positionSize;
            setToBitSet(set, index, empty);
        }

        Position to = move.getTo();

        if (to.isInBoard()) {
            int index = to.getPanel() * positionSize;
            setToBitSet(set, index, figurePattern);
        }
    }

    public static void setAtMove(BitSet set, Color color) {
        set.set(atMoveIndex, !color.isWhite());
    }

    public static BitSet getBoardSet(BitSet set) {
        //255 is the last index of the board bits (of boardPosition 63) 
        return set.get(0, 256);
    }

    public static void updateBackward(BitSet set, Game game, PlayerMove lastMove) {
        makeMoveBackward(lastMove.getMainMove(), set);
        lastMove.getPromotionMove().ifPresent(move -> makeMoveBackward(move, set));
        lastMove.getSecondaryMove().ifPresent(move -> makeMoveBackward(move, set));

        setGameParameter(set, game);
    }

    private static void makeMoveBackward(Move move, BitSet set) {
        Position from = move.getFrom();
        String figurePattern = getFigurePattern(move.getFigure());

        if (from.isInBoard()) {
            int index = from.getPanel() * positionSize;
            setToBitSet(set, index, figurePattern);
        }

        Position to = move.getTo();

        if (to.isInBoard()) {
            int index = to.getPanel() * positionSize;
            setToBitSet(set, index, empty);
        }
    }

    public static Game decode(BitSet set) {
        FigureBoard board = getBoard(set);

        ChessGameImpl chessGame = new ChessGameImpl(board, set);

        if (set.get(atMoveIndex)) {
            chessGame.setAtMove(chessGame.getBlack());
        } else {
            chessGame.setAtMove(chessGame.getWhite());
        }

        return chessGame;
    }


    public static boolean isSameColorState(BitSet set, Color color) {
        boolean atMoveFlag = set.get(atMoveIndex);
        return (atMoveFlag && Color.BLACK == color) || !atMoveFlag && Color.WHITE == color;
    }

    public static BitSet getAtMoveState(BitSet set, Color color) {
        BitSet clone = (BitSet) set.clone();
        if (color.isWhite()) {
            clone.clear(atMoveIndex);
        } else {
            clone.set(atMoveIndex);
        }
        return clone;
    }

    private static Figure getFigure(int index, BitSet set) {
        String pattern = set.get(index++) ? "1" : "0";
        pattern = set.get(index++) ? pattern + "1" : pattern + "0";
        pattern = set.get(index++) ? pattern + "1" : pattern + "0";

        Color color = set.get(index) ? Color.BLACK : Color.WHITE;

        Figure figure;

        if (pattern.equals(empty)) {
            figure = null;

        } else if (pattern.equals(pawn)) {
            figure = FigureType.PAWN.create(color);

        } else if (pattern.equals(knight)) {
            figure = FigureType.KNIGHT.create(color);

        } else if (pattern.equals(bishop)) {
            figure = FigureType.BISHOP.create(color);

        } else if (pattern.equals(rook)) {
            figure = FigureType.ROOK.create(color);

        } else if (pattern.equals(queen)) {
            figure = FigureType.QUEEN.create(color);

        } else if (pattern.equals(king)) {
            figure = FigureType.KING.create(color);

        } else {
            throw new IllegalArgumentException("no matching pattern");
        }

        return figure;
    }
}

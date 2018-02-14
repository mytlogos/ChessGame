package chessGame.mechanics.move;

import chessGame.mechanics.Color;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *
 */
public class MoveCoder {
    private static String pawn = "001";
    private static String knight = "010";
    private static String bishop = "011";
    private static String rook = "100";
    private static String queen = "101";
    private static String king = "110";

    private static int figureIndex = 0;
    private static int colorIndex = 3;
    private static int fromInBoardIndex = 4;
    private static int fromOutBoardIndex = 10;
    private static int toInBoardIndex = 11;
    private static int toOutBoardIndex = 17;
    private static int nullFlagIndex = 18;

    private static int figureSize = 3;
    private static int colorSize = 1;
    private static int positionSize = 7;
    private static int nullFlagSize = 1;
    private static int moveSize = figureSize + colorSize + 2 * positionSize + nullFlagSize;
    private static int playerMoveSize = moveSize * 3 + 1;

    private static String[] positionArray = new String[64];

    static {
        for (int panel = 0; panel < positionArray.length; panel++) {
            positionArray[panel] = getString(6, panel) + "0";
        }
    }



    public static BitSet encode(List<PlayerMove> moves) {
        BitSet set = new BitSet();

        StringBuilder builder = new StringBuilder();

        for (PlayerMove move : moves) {
            encodeMove(move, builder);
        }

        setToBitSet(set, builder.toString());
        return set;
    }

    public static String encode(PlayerMove move) {
        StringBuilder builder = new StringBuilder();
        encodeMove(move, builder);
        return builder.toString();
    }

    private static void encodeMove(PlayerMove playerMove, StringBuilder builder) {
        encodeMove(playerMove.getMainMove(), builder);
        encodeMove(playerMove.getSecondaryMove().orElse(null), builder);
        encodeMove(playerMove.getPromotionMove().orElse(null), builder);

        //append 1 as a flag so that the highest set bit is the flag of the last encoded move
        builder.append(1);
    }

    private static void setToBitSet(BitSet set, String binaryString) {
        char zero = '0';
        char one = '1';

        int length = binaryString.length();
        for (int index = 0; index < length; index++) {
            char charAt = binaryString.charAt(index);

            if (charAt == zero) {
                set.clear(index);
            } else if (charAt == one) {
                set.set(index);
            } else {
                throw new IllegalArgumentException("Is not binary!");
            }
        }
    }

    private static void encodeMove(Move move, StringBuilder builder) {
        if (move == null) {
            //flag for null
            builder.append("0000000000000000001");
        } else {
            String figurePattern = getFigurePattern(move.getFigure());
            int color = getColor(move.isWhite());
            String from = getPositionPattern(move.getFrom());
            String to = getPositionPattern(move.getTo());

            builder.append(figurePattern).append(color).append(from).append(to).append("0");
        }
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

    private static int getColor(boolean white) {
        return white ? 0 : 1;
    }

    private static String getPositionPattern(Position position) {
        if (position.isInBoard()) {
            return positionArray[position.getPanel()];
        } else {
            return "0000001";
        }
    }

    private static String getString(int portion, int i) {
        String format = "%" + portion + "s";
        String string = Integer.toBinaryString(i);
        return String.format(format, string).replace(" ", "0");
    }

    public static List<PlayerMove> decode(BitSet set) {
        List<PlayerMove> moves = new ArrayList<>();

        /*StringBuilder builder = new StringBuilder();

        for (int i = 0; i < set.length(); i++) {
            builder.append(set.get(i) ? 1 : 0);

            if (i % playerMoveSize == 0 && i != 0) {
                String encodedMove = builder.substring(i - playerMoveSize, i);
                PlayerMove move = decode(encodedMove);
                moves.add(move);
            }
        }*/
            int movesSize = set.length() / playerMoveSize;

            for (int index = 0; index < movesSize; index++) {
                int setIndex = index * playerMoveSize;
                PlayerMove move = decode(set, setIndex);

                if (move != null) {
                    moves.add(move);
                }
            }
        return moves;
    }

    public static PlayerMove decode(String encodedMove) {
        Move move = decodeMove(encodedMove, 0, false);
        Move secondary = decodeMove(encodedMove, moveSize, false);
        Move promotion = decodeMove(encodedMove, moveSize * 2, true);

        if (secondary != null && secondary.isMoving(FigureType.ROOK) && secondary.isWhite() == move.isWhite()) {
            if (!move.isMoving(FigureType.KING)) {
                throw new IllegalArgumentException("Move is not moving king, when it is moving Rook of the same Color " + move);
            }
            return PlayerMove.CastlingMove(move, secondary);
        }

        if (promotion != null) {
            return PlayerMove.PromotionMove(move, secondary, promotion);
        }

        return new PlayerMove(move, secondary);
    }

    private static Move decodeMove(String encodedMove, int offSet, boolean promotion) {
        //check nullMove Flag
        if (encodedMove.charAt(offSet + nullFlagIndex) == '1') {
            return null;
        }

        int figureIndex = MoveCoder.figureIndex + offSet;
        int colorIndex = MoveCoder.colorIndex + offSet;

        String figureString = encodedMove.substring(figureIndex, colorIndex);
        FigureType figure = getFigureType(figureString);

        Position from;
        //check if position is not in board
        int fromOutBoardIndex = offSet + MoveCoder.fromOutBoardIndex;
        if (encodedMove.charAt(fromOutBoardIndex) == '1') {
            if (figure != FigureType.PAWN && figure != FigureType.KING) {
                from = Position.Unknown;
            } else {
                throw new IllegalStateException("Pawn or King can only be moved from on the Board");
            }
        } else {
            int fromInBoardIndex = MoveCoder.fromInBoardIndex + offSet;
            String fromString = encodedMove.substring(fromInBoardIndex, fromOutBoardIndex);
            int fromPanel = Integer.parseInt(fromString, 2);
            from = Position.get(fromPanel);
        }

        Position to;
        //check if position is not in board
        int toOutBoardIndex = MoveCoder.toOutBoardIndex + offSet;
        if (encodedMove.charAt(toOutBoardIndex) == '1') {
            if (promotion) {
                to = Position.Promoted;
            } else {
                to = Position.Bench;
            }
        } else {
            int toInBoardIndex = MoveCoder.toInBoardIndex + offSet;
            String toString = encodedMove.substring(toInBoardIndex, toOutBoardIndex);
            int fromPanel = Integer.parseInt(toString, 2);
            to = Position.get(fromPanel);
        }
        Color color = encodedMove.charAt(colorIndex) == '0' ? Color.WHITE : Color.BLACK;
        return new Move(from, to, figure, color);
    }

    private static FigureType getFigureType(String pattern) {
        FigureType result;

        if (pattern.equals(pawn)) {
            result = FigureType.PAWN;

        } else if (pattern.equals(knight)) {
            result = FigureType.KNIGHT;

        } else if (pattern.equals(bishop)) {
            result = FigureType.BISHOP;

        } else if (pattern.equals(rook)) {
            result = FigureType.ROOK;

        } else if (pattern.equals(queen)) {
            result = FigureType.QUEEN;

        } else if (pattern.equals(king)) {
            result = FigureType.KING;

        } else {
            throw new IllegalArgumentException("no matching pattern: " + pattern);
        }
        return result;
    }

    public static String getString(BitSet encode) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < encode.size(); i++) {
            if (encode.get(i)) {
                stringBuilder.append(1);
            } else {
                stringBuilder.append(0);
            }

            if (stringBuilder.length() % 19 == 0 && i != 0) {
                stringBuilder.append(" ");
            }
        }

        System.out.println(encode);
        return stringBuilder.toString();
    }

    private static PlayerMove decode(BitSet set, int index) {
        Move move = decodeMove(set, index, false);
        index += moveSize;

        Move secondary = decodeMove(set, index, false);
        index += moveSize;

        Move promotion = decodeMove(set, index, true);

        if (secondary != null && secondary.isMoving(FigureType.ROOK) && secondary.isWhite() == move.isWhite()) {
            if (!move.isMoving(FigureType.KING)) {
                throw new IllegalArgumentException("Move is not moving king, when it is moving Rook of the same Color " + move);
            }
            return PlayerMove.CastlingMove(move, secondary);
        }

        if (promotion != null) {
            return PlayerMove.PromotionMove(move, secondary, promotion);
        }

        return new PlayerMove(move, secondary);
    }

    private static Move decodeMove(BitSet set, int index, boolean promotion) {
        //check nullMove Flag
        if (set.get(index + nullFlagIndex)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = index; i < index + moveSize; i++) {
            builder.append(set.get(i) ? 1 : 0);
        }

        String binaryString = builder.toString();

        String figureString = binaryString.substring(figureIndex, colorIndex);
        FigureType figure = getFigureType(figureString);

        Position from;
        //check if position is not in board
        if (binaryString.charAt(fromOutBoardIndex) == '1') {
            if (figure != FigureType.PAWN && figure != FigureType.KING) {
                from = Position.Unknown;
            } else {
                throw new IllegalStateException("Pawn or King can only be moved from on the Board");
            }
        } else {
            String fromString = binaryString.substring(fromInBoardIndex, fromOutBoardIndex);
            int fromPanel = Integer.parseInt(fromString, 2);
            from = Position.get(fromPanel);
        }

        Position to;
        //check if position is not in board
        if (binaryString.charAt(toOutBoardIndex) == '1') {
            if (promotion) {
                to = Position.Promoted;
            } else {
                to = Position.Bench;
            }
        } else {
            String toString = binaryString.substring(toInBoardIndex, toOutBoardIndex);
            int fromPanel = Integer.parseInt(toString, 2);
            to = Position.get(fromPanel);
        }
        Color color = binaryString.charAt(colorIndex) == '1' ? Color.BLACK : Color.WHITE;
        return new Move(from, to, figure, color);
    }
}

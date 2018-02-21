package chessGame.data;

import chessGame.mechanics.Color;
import chessGame.mechanics.Figure;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;
import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.game.ChessGameImpl;
import chessGame.mechanics.move.Move;
import chessGame.mechanics.move.MoveForGenerator;
import chessGame.mechanics.move.MoveHistory;
import chessGame.mechanics.move.PlayerMove;

import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
public class GameConverter {
    private final static Map<Character, FigureType> figureNotations;
    private final static Collection<String> endNotations;
    private static String whiteWon = "1-0";
    private static String blackWon = "0-1";
    private static String draw = "1/2-1/2";
    private static String queenSideCastle = "O-O-O";
    private static String kingSideCastle = "O-O";
    private static char checkMate = '#';
    private static char checked = '+';

    static {
        figureNotations = Arrays.stream(FigureType.values()).collect(Collectors.toMap(FigureType::getNotation, Function.identity()));
        endNotations = new ArrayList<>();
        endNotations.add(whiteWon);
        endNotations.add(blackWon);
        endNotations.add(draw);
    }

    /**
     * May lead to incorrect conversion if the game is manipulated
     * during the execution of this method.
     *
     * @param game game to convert to SAN
     * @return a SAN representation of the parameter
     * @see <a href="https://en.wikipedia.org/wiki/Algebraic_notation_(chess)">Algebraic Chess Notation</a>
     */
    public static String convert(ChessGame game) {
        ChessGame copy = new ChessGameImpl();
        MoveHistory history = game.getHistory();

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < history.size(); i++) {
            if (copy.isFinished()) {
                throw new IllegalArgumentException("Illegal GameHistory - led to premature game end");
            }

            PlayerMove move = history.get(i);

            if (move.isWhite()) {
                int round = (i + 2) / 2;
                builder.append(round).append(". ");
            }

            setMoveNotation(move, copy, builder);
            copy.makeMove(move);

            if (MoveForGenerator.isInCheck(copy.getBoard().getKing(!move.isWhite()), copy.getBoard())) {
                builder.append(checked);
            }

            builder.append(" ");

            copy.nextRound();
        }

        if (game.isFinished()) {

            if (game.isWon()) {
                if (builder.charAt(builder.length() - 2) == checked) {
                    builder.replace(builder.length() - 2, builder.length() - 1, checkMate + "");
                }

                if (game.getWinner().isWhite()) {
                    builder.append(whiteWon);
                } else {
                    builder.append(blackWon);
                }
            } else if (game.isDraw()) {
                builder.append(draw);
            } else {
                throw new IllegalStateException("game at the end neither won or draw!");
            }
        }
        return builder.toString();
    }

    private static void setMoveNotation(PlayerMove move, ChessGame game, StringBuilder builder) {

        if (game.getAtMoveColor() != move.getColor()) {
            throw new IllegalStateException("illegal Move: " + move);
        }

        if (move.isCastlingMove()) {
            Move castle = move.getSecondaryMove().orElseThrow(IllegalStateException::new);

            if (castle.getFrom().getColumn() == 1) {
                //queen side castling
                builder.append(queenSideCastle);
            } else {
                //king side castling
                builder.append(kingSideCastle);
            }
        } else {
            Move mainMove = move.getMainMove();
            FigureType figure = mainMove.getFigure();
            Position to = mainMove.getTo();

            List<PlayerMove> sameDestinationMoves = new ArrayList<>();

            for (PlayerMove playerMove : game.getAllowedMoves()) {
                Move allowedMain = playerMove.getMainMove();

                if (allowedMain.getFigure().equals(figure) && to.equals(allowedMain.getTo())) {
                    sameDestinationMoves.add(playerMove);
                }
            }

            if (sameDestinationMoves.isEmpty()) {
                throw new IllegalStateException("History is not legal, contains illegal move " + move);
            }

            if (!mainMove.isMoving(FigureType.PAWN)) {
                builder.append(figure.getNotation());

                if (sameDestinationMoves.size() != 1) {
                    addInEqualPosition(move, builder, mainMove, sameDestinationMoves);
                }
            } else if (sameDestinationMoves.size() != 1 && !move.isEnpassant()) {
                addInEqualPosition(move, builder, mainMove, sameDestinationMoves);
            }

            if (move.isStrike()) {
                if (move.isEnpassant()) {
                    builder.append(mainMove.getFrom().getColumnName().toLowerCase());
                }
                builder.append("x");
            }


            if (move.isPromotion()) {
                Move promotion = move.getPromotionMove().orElseThrow(IllegalStateException::new);

                builder.append(promotion.getTo().getColumnName().toLowerCase()).append(promotion.getTo().getRow());
                builder.append("=").append(promotion.getFigure().getNotation());
            } else {
                builder.append(to.getColumnName().toLowerCase()).append(to.getRow());
            }
        }
    }

    private static void addInEqualPosition(PlayerMove move, StringBuilder builder, Move mainMove, List<PlayerMove> sameDestinationMoves) {
        sameDestinationMoves.removeIf(destinationMove -> checkInEqualMoveState(move, destinationMove));

        boolean sameColumn = true;
        boolean sameRow = true;

        Position mainMoveFrom = mainMove.getFrom();

        for (PlayerMove playerMove : sameDestinationMoves) {
            Position from = playerMove.getMainMove().getFrom();

            if (sameColumn) {
                sameColumn = from.getColumnName().equals(mainMoveFrom.getColumnName());
            }

            if (sameRow) {
                sameRow = from.getRow() == mainMoveFrom.getRow();
            }
        }

        //if same column and same Row means the sameDestinationMoves hold only the parameter move
        if (sameColumn && sameRow && !move.isPromotion()) {
            System.err.println("Illegal State - There cannot be multiple Moves with which start from the same panel and end in the same panel");
        } else if (!sameColumn && sameRow) {
            //if it is on the same row/rank but not on the same column/file then specify the starting column
            builder.append(mainMoveFrom.getColumnName().toLowerCase());
        } else if (sameColumn) {
            //if it is on the same column/file but not on the same row/rank then specify the starting row
            builder.append(mainMoveFrom.getColumn());
        } else {
            //if it is neither on the same row/rank or column/file then specify the whole starting position
            builder.append(mainMoveFrom.notation().toLowerCase());
        }
    }

    private static boolean checkInEqualMoveState(PlayerMove move, PlayerMove destinationMove) {
        return destinationMove.isPromotion() != move.isPromotion()
                || destinationMove.isCastlingMove() != move.isCastlingMove()
                || destinationMove.isStrike() != move.isStrike();
    }

    public static ChessGame convert(String string) throws ParseException {
        //remove any check or checkMate notations, because their are irrelevant to this conversion and to prevent errors
        string = string.replace(checked + "", "");
        string = string.replace(checkMate + "", "");

        String[] moves = string.split("\\d\\.\\s|\\s");
        ScenarioGame game = new ScenarioGame();

        for (int i = 0, movesLength = moves.length; i < movesLength; i++) {
            String move = moves[i];

            if (move.isEmpty()) {
                continue;
            }
            if (game.isFinished() && !endNotations.contains(move)) {
                throw new ParseException("Illegal Parameter, consists of Moves that would lead beyond the end of a game.", i);
            }

            if (endNotations.contains(move)) {
                if (move.equals(whiteWon)) {
                    game.setLoser(game.getWhite());

                } else if (move.equals(blackWon)) {
                    game.setLoser(game.getBlack());

                } else {
                    game.setLoser(null);
                }
            } else {
                PlayerMove playerMove = decode(move, game, i);
                try {
                    game.makeMove(playerMove);
                } catch (Exception e) {
                    throw new ParseException("parsed an illegal move: " + move, i);
                }
            }
        }
        return game;
    }

    private static PlayerMove decode(String moveString, ScenarioGame game, int i) throws ParseException {
        if (queenSideCastle.equals(moveString)) {
            return getCastlingMove(game, 1);

        } else if (kingSideCastle.equals(moveString)) {
            return getCastlingMove(game, 8);
        }

        FigureType mainFigure = null, promotedFigure = null;
        int mainFromRow = 0, mainFromColumn = 0;

        Position mainTo = null;
        boolean isStrike = false;

        for (MoveIterator iterator = new MoveIterator(moveString); iterator.hasNext(); ) {
            char nextChar = iterator.next();

            if (iterator.isFirst()) {
                mainFigure = figureNotations.getOrDefault(nextChar, FigureType.PAWN);
            }

            if (isColumn(nextChar)) {
                if (iterator.isOnlyColumn()) {
                    char toRowChar = iterator.next();
                    int column = Position.getColumn(nextChar);
                    int row = Character.digit(toRowChar, 10);
                    mainTo = Position.get(row, column);

                } else {
                    mainFromColumn = Position.getColumn(nextChar);
                }
            } else if (isRow(nextChar)) {
                mainFromRow = Character.digit(nextChar, 10);

            } else if (isStrike(nextChar)) {
                isStrike = true;

            } else if (isPromotion(nextChar)) {

                char promotionFigure = iterator.next();
                promotedFigure = figureNotations.get(promotionFigure);
            }
        }

        if (mainTo == null) {
            throw new ParseException("no goal panel could be parsed", i);
        }

        Position mainFrom = getFromPosition(game, i, mainFromRow, mainFromColumn, mainTo, promotedFigure, mainFigure);


        Move strike = null;
        Move mainMove = new Move(mainFrom, mainTo, mainFigure, game.getAtMoveColor());

        if (isStrike) {
            Figure figure = game.getBoard().figureAt(mainTo);

            Position strikeFrom = mainTo;

            //check if this move is enpassant
            if (figure == null
                    && mainFigure == FigureType.PAWN
                    && game.getHistory().getEnPassantColumn(!mainMove.isWhite()) == mainTo.getColumn()) {

                strikeFrom = Position.get(mainFrom.getRow(), mainTo.getColumn());
                figure = game.getBoard().figureAt(strikeFrom);

                if (figure == null || !figure.is(FigureType.PAWN)) {
                    throw new ParseException("expected a pawn: " + figure, i);
                }
            }

            //todo possible nullPointer Exception
            //noinspection ConstantConditions
            Color color = figure.getColor();

            if (color == mainMove.getColor()) {
                throw new ParseException("cannot strike figures of the same color", i);
            }
            strike = new Move(strikeFrom, Position.Bench, figure.getType(), color);
        }

        PlayerMove playerMove;

        if (promotedFigure != null) {
            Move pawnMove = new Move(mainFrom, Position.Promoted, mainFigure, mainMove.getColor());
            Move promotion = new Move(Position.Unknown, mainTo, promotedFigure, Color.getEnemy(mainMove.getColor()));

            playerMove = PlayerMove.PromotionMove(pawnMove, strike, promotion);
        } else {
            playerMove = new PlayerMove(mainMove, strike);
        }

        return playerMove;
    }

    private static PlayerMove getCastlingMove(ScenarioGame game, int rookColumn) {
        Color atMoveColor = game.getAtMoveColor();
        int row = atMoveColor.isWhite() ? 1 : 8;

        int kingColumn = 5;
        int newKingColumn = rookColumn < kingColumn ? kingColumn - 2 : kingColumn + 2;
        int newRookColumn = rookColumn < kingColumn ? newKingColumn + 1 : newKingColumn - 1;

        Move kingMove = new Move(Position.get(row, kingColumn), Position.get(row, newKingColumn), FigureType.KING, atMoveColor);
        Move rookMove = new Move(Position.get(row, rookColumn), Position.get(row, newRookColumn), FigureType.ROOK, atMoveColor);

        return PlayerMove.CastlingMove(kingMove, rookMove);
    }

    private static boolean isColumn(char c) {
        if (Character.isLowerCase(c)) {
            int column = Position.getColumn(c);
            return column <= 8 && column >= 1;
        } else {
            return false;
        }
    }

    private static boolean isRow(char c) {
        return Character.isDigit(c) && (int) c >= 1 && (int) c <= 8;
    }

    private static boolean isStrike(char c) {
        return c == 'x';
    }

    private static boolean isPromotion(char c) {
        return c == '=';
    }

    private static Position getFromPosition(ScenarioGame game, int i, int mainFromRow, int mainFromColumn, Position mainTo, FigureType promotion, FigureType mainFigure) throws ParseException {
        List<PlayerMove> moves = game.getAllowedMoves().
                stream().
                filter(move -> isSameMoveType(mainTo, mainFigure, promotion, move)).
                collect(Collectors.toList());

        PlayerMove playerMove = null;

        if (mainFromColumn == 0 && mainFromRow == 0) {

            if (moves.size() == 1) {
                playerMove = moves.get(0);
            } else {
                throw new ParseException("if no start row and/or column is specified, one unique move should be available", i);
            }
        } else if (mainFromColumn != 0 && mainFromRow == 0) {
            playerMove = moves.
                    stream().
                    filter(move -> move.getMainMove().getFrom().getColumn() == mainFromColumn).
                    findFirst().
                    orElseThrow(() -> new ParseException("no move with specified start column available", i));

        } else if (mainFromColumn != 0) {
            playerMove = moves.
                    stream().
                    filter(move -> move.getMainMove().getFrom().getColumn() == mainFromColumn
                            && move.getMainMove().getFrom().getRow() == mainFromRow).
                    findFirst().
                    orElseThrow(() -> new ParseException("no move with specified start column and row available", i));

        }
        if (playerMove == null) {
            throw new IllegalStateException("could not find acceptable move for row: " + mainFromRow + ", column: " + mainFromColumn);
        }
        return playerMove.getMainMove().getFrom();
    }

    private static boolean isSameMoveType(Position mainTo, FigureType mainFigure, FigureType promotion, PlayerMove move) {
        if (promotion != null && move.isPromotion()) {
            Move promotionMove = move.getPromotionMove().orElseThrow(() -> new IllegalStateException("expected promotionMove"));

            if (promotionMove.isMoving(promotion) && promotionMove.getTo().equals(mainTo)) {
                return true;
            }
        }
        return promotion == null && move.getMainMove().getTo().equals(mainTo) && move.getMainMove().isMoving(mainFigure);
    }

    private static class MoveIterator implements Iterator<Character> {
        private StringBuilder builder;
        private int currentIndex;
        private int nextIndex;
        private Matcher columnMatcher;

        private MoveIterator(String s) {
            builder = new StringBuilder(s);
            columnMatcher = Pattern.compile("[abcdefgh]").matcher(builder);
        }

        @Override
        public boolean hasNext() {
            return builder.length() > nextIndex;
        }

        @Override
        public Character next() {
            char charAt = builder.charAt(nextIndex);
            currentIndex = nextIndex++;
            return charAt;
        }

        @Override
        public void remove() {
            builder.deleteCharAt(currentIndex);
        }

        private boolean isFirst() {
            return nextIndex == 1;
        }

        private boolean isOnlyColumn() {
            columnMatcher.reset();

            boolean once = columnMatcher.find(currentIndex);
            if (!once) {
                throw new IllegalStateException("needs to be present at least once");
            }

            once = !columnMatcher.find();
            return once;
        }

    }
}

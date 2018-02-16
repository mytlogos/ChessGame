package chessGame.mechanics.move;

import chessGame.mechanics.*;
import chessGame.mechanics.board.Board;
import chessGame.mechanics.board.FigureBoard;
import chessGame.mechanics.game.Game;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates moves, which are allowed for the current state of the oard.
 * Has only the Method {@link #getAllowedMoves(Color, Game)}, which is accessible from outside the class.
 */
public final class MoveStreamGenerator {

    private MoveStreamGenerator() {
        throw new IllegalStateException("No Instances Allowed");
    }

    /**
     * Generates a List of {@link PlayerMove}s  for the player.
     *
     * @param player color the check for
     * @param game game to generate the Moves from
     * @return a list of allowed Moves, empty if none available
     */
    public static List<PlayerMove> getAllowedMoves(Color player, Game game) {
        if (player == null) return new ArrayList<>();

        FigureBoard board = game.getBoard();

        final Map<Color, List<Figure>> playerListMap = board.getPlayerFigures();
        final Figure king = board.getKing(player.isWhite());

        //king shall never be null
        if (king == null) {
            throw new IllegalStateException();
        }

        final List<Figure> figures = playerListMap.get(player);

        return figures.stream().
                //flat maps all possible Moves of each figure into one stream
                        flatMap(figure -> mapToPlayerMove(figure, board, game)).
                //filter null moves
                        filter(Objects::nonNull).
                //checks playerMove on validity
                        filter(move -> checkPlayerMove(king, move, board, game)).
                //collects stream into list
                        collect(Collectors.toList());
    }

    private static Stream<? extends PlayerMove> mapToPlayerMove(Figure figure, FigureBoard board, Game game) {
        final List<Position> allowedPositions = PositionGenerator.getAllowedPositions(figure, board);

        //no playerMoves if no position is allowed
        if (allowedPositions.isEmpty()) {
            return new ArrayList<PlayerMove>().stream();
        }

        if (figure.is(FigureType.KING)) {
            final List<PlayerMove> moves = allowedPositions.stream().map(position -> transform(figure, position, board)).collect(Collectors.toList());
            Collection<PlayerMove> castling = getCastling(figure, board, game);
            moves.addAll(castling);
            return moves.stream();
        }

        if (figure.is(FigureType.PAWN)) {
            final List<PlayerMove> pawnMoves = allowedPositions.stream().map(position -> transform(figure, position, board)).filter(Objects::nonNull).collect(Collectors.toList());
            List<PlayerMove> promotions = getPromotions(pawnMoves, figure);
            pawnMoves.addAll(promotions);
            PlayerMove enPassant = getEnPassant(figure, game);
            if (enPassant != null) {
                pawnMoves.add(enPassant);
            }
            return pawnMoves.stream();
        }

        return allowedPositions.stream().map(position -> transform(figure, position, board));
    }

    private static boolean checkPlayerMove(Figure king, PlayerMove playerMove, FigureBoard board, Game game) {
        if (playerMove == null) {
            return false;
        }

        //throw error if king is not of same playerType as the one of the move
        if (!playerMove.getMainMove().getColor().equals(king.getColor())) {
            throw new IllegalStateException("generierter zug stimmt nicht mit Spieler des Königs überein!");
        }

        MoveMaker.makeMove(playerMove, board, game);
        boolean inCheck = isInCheck(king, board);
        MoveMaker.redo(board, game, playerMove);

        return inCheck;
    }

    private static PlayerMove transform(Figure figure, Position position, FigureBoard board) {

        if (figure == null) {
            return null;
        }

        final Figure boardFigure = board.figureAt(position);
        Position from = board.positionOf(figure);


        final Move move = new Move(from, position, figure.getType(), figure.getColor());
        Move second = null;

        //add strike move if position is not empty and is an enemy piece
        if (boardFigure != null && !boardFigure.getColor().equals(figure.getColor())) {

            //a king shall never be defeated
            if (boardFigure.is(FigureType.KING)) {
                return null;
            }

            second = new Move(position, Position.Bench, boardFigure.getType(), boardFigure.getColor());
        }
        return new PlayerMove(move, second);
    }

    private static Collection<PlayerMove> getCastling(Figure king, FigureBoard board, Game game) {
        final List<PlayerMove> moves = new ArrayList<>();

        Color kingPlayer = king.getColor();
        boolean longCastling = game.getHistory().longCastling(kingPlayer.isWhite());
        boolean shortCastling = game.getHistory().shortCastling(kingPlayer.isWhite());

        if (longCastling) {
            PlayerMove castling = addCastling(king, board, 1);
            if (castling != null) moves.add(castling);
        }
        if (shortCastling) {
            PlayerMove castling = addCastling(king, board, 8);
            if (castling != null) moves.add(castling);
        }

        return moves;
    }

    private static List<PlayerMove> getPromotions(List<PlayerMove> pawnMoves, Figure pawn) {
        final List<PlayerMove> promotionAble = pawnMoves.
                stream().
                filter(MoveStreamGenerator::checkPromotable).
                collect(Collectors.toList());

        //get promotions within the rules of https://en.wikipedia.org/wiki/Promotion_(chess)
        return promotionAble
                .stream()
                .flatMap(move -> getPromotable(pawn)
                        .stream()
                        .map(promoter -> transformPromotion(move, promoter)))
                .collect(Collectors.toList());
    }

    private static PlayerMove getEnPassant(Figure pawn, Game game) {
        //check for en passant move (https://en.wikipedia.org/wiki/En_passant)
        final Color enemy = Color.getEnemy(pawn.getColor());
        final PlayerMove lastMove = game.getLastMove();

        //fast fail if there is no last move, last move is no normal move or a figure is defeated
        if (lastMove == null || !lastMove.isNormal() || lastMove.isStrike()) {
            return null;
        }

        Position enemyFrom = lastMove.getMainMove().getFrom();
        Position enemyPawnPosition = lastMove.getMainMove().getTo();

        int diff = enemyFrom.getPanel() - enemyPawnPosition.getPanel();

        //a difference of 16 panels equals a move of 2 rows on the same column in either direction
        if (Math.abs(diff) != 16) {
            return null;
        }

        final int rowFrom = enemyFrom.getRow();
        final int rowTo = enemyPawnPosition.getRow();

        final Figure enemyPawn = game.getBoard().figureAt(enemyPawnPosition);
        final int enemyColumn = enemyPawnPosition.getColumn();

        Position from = game.getBoard().positionOf(pawn);

        //check if pawn, if pawn is of enemy, lastMove was no castling or promotionMove
        //last move pawn moved two fields, enemy pawn and current pawn are side by side
        //in same row and adjacent columns
        if (enemyPawn.is(FigureType.PAWN)
                && lastMove.getColor().equals(enemy)
                && lastMove.isNormal()
                && lastMove.getSecondaryMove().orElse(null) == null
                && Math.abs(rowFrom - rowTo) == 2
                && Math.abs(from.getColumn() - enemyColumn) == 1
                && enemyPawnPosition.getRow() == from.getRow()) {


            final int row;
            if (pawn.isWhite()) {
                row = 6;
            } else {
                row = 3;
            }

            Position pawnTo = Position.get(row, enemyColumn);
            final Move mainMove = new Move(from, pawnTo, pawn.getType(), pawn.getColor());
            final Move strike = new Move(enemyPawnPosition, Position.Bench, enemyPawn.getType(), enemyPawn.getColor());

            return new PlayerMove(mainMove, strike);
        }
        return null;
    }

    public static boolean isInCheck(Figure king, FigureBoard board) {
        return !getCheckFigure(king, board, board.positionOf(king)).isEmpty();
    }

    private static PlayerMove addCastling(Figure king, FigureBoard board, int column) {
        int row = king.isWhite() ? 1 : 8;
        Position position = Position.get(row, column);
        Figure figure = board.figureAt(position);

        if (figure == null || !figure.is(FigureType.ROOK)) {
            return null;
//            throw new IllegalStateException("Expected a Rook at " + position);
        }

        final Position rookPosition = board.positionOf(figure);
        final Position kingPosition = board.positionOf(king);

        final int rookColumn = rookPosition.getColumn();
        final int kingColumn = kingPosition.getColumn();
        final Color enemy = Color.getEnemy(king.getColor());

        final List<Position> enemyPositions = allowedPositions(enemy, board)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return addCastling(king, figure, rookPosition, kingPosition, rookColumn, kingColumn, enemyPositions, board);
    }

    private static List<Figure> getPromotable(Figure pawn) {
        List<Figure> figures = new ArrayList<>();
        figures.add(FigureType.ROOK.create(pawn.getColor()));
        figures.add(FigureType.KNIGHT.create(pawn.getColor()));
        figures.add(FigureType.BISHOP.create(pawn.getColor()));
        figures.add(FigureType.QUEEN.create(pawn.getColor()));
        return figures;
    }

    private static PlayerMove transformPromotion(PlayerMove move, Figure promoter) {
        Move mainMove = move.getMainMove();
        Move pawnMove = new Move(mainMove.getFrom(), Position.Promoted, mainMove.getFigure(), mainMove.getColor());

        final Move promotionMove = new Move(Position.Unknown, mainMove.getTo(), promoter.getType(), promoter.getColor());
        return PlayerMove.PromotionMove(pawnMove, move.getSecondaryMove().orElse(null), promotionMove);
    }

    private static List<Figure> getCheckFigure(Figure figure, FigureBoard board, Position position) {
        Color player = figure.getColor();
        Color enemy = Color.getEnemy(player);
        //gets possible positions per figure of enemy
        return allowedPositions(enemy, board).
                entrySet().
                stream().
                //check if position of figure is in possible position of enemy
                        filter(entry -> entry.getValue().contains(position)).
                        map(Map.Entry::getKey).
                        collect(Collectors.toList());
    }

    private static Map<Figure, List<Position>> allowedPositions(Color player, FigureBoard board) {
        return board.getPlayerFigures().get(player).stream().collect(Collectors.toMap(Function.identity(), figure -> PositionGenerator.getAllowedPositions(figure, board)));
    }

    private static PlayerMove addCastling(Figure king, Figure rook, Position rookPosition, Position kingPosition, int rookColumn, int kingColumn, List<Position> enemyPositions, FigureBoard board) {
        boolean legal;
        int newKingColumn;
        int newRookColumn;
        final int row = kingPosition.getRow();

        if (rookColumn > kingColumn) {
            legal = checkEmpty(rookColumn, kingColumn, row, board);
            newKingColumn = kingColumn + 2;
            newRookColumn = newKingColumn - 1;

            for (int i = kingColumn; i < newKingColumn; i++) {
                final Position position = Position.get(row, i);

                if (enemyPositions.contains(position)) {
                    legal = false;
                    break;
                }
            }
        } else {
            legal = checkEmpty(kingColumn, rookColumn, row, board);
            newKingColumn = kingColumn - 2;
            newRookColumn = newKingColumn + 1;

            for (int i = kingColumn; i < newKingColumn; i--) {
                final Position position = Position.get(row, i);

                if (enemyPositions.contains(position)) {
                    legal = false;
                    break;
                }
            }
        }
        if (legal) {
            final Position newKingPosition = Position.get(row, newKingColumn);
            final Position newRookPosition = Position.get(rookPosition.getRow(), newRookColumn);

            final Move kingMove = new Move(kingPosition, newKingPosition, king.getType(), king.getColor());
            final Move rookMove = new Move(rookPosition, newRookPosition, rook.getType(), rook.getColor());
            return PlayerMove.CastlingMove(kingMove, rookMove);
        }
        return null;
    }

    private static boolean checkEmpty(int rightColumn, int leftColumn, int row, FigureBoard board) {
        boolean empty = true;

        for (int column = leftColumn + 1; column < rightColumn; column++) {
            final Position position = Position.get(row, column);

            if (!board.isEmptyAt(position)) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    private static boolean checkPromotable(PlayerMove move) {
        //is white and backRow of black
        return (move.getMainMove().getTo().getRow() == 8 && move.getMainMove().isWhite()) ||
                //is black und backRow of white
                (move.getMainMove().getTo().getRow() == 1 && !move.getMainMove().isWhite());
    }

    public static List<PlayerMove> getStrikes(Game game, Color player) {
        if (player == null) return new ArrayList<>();

        FigureBoard board = game.getBoard();

        final Map<Color, List<Figure>> playerListMap = board.getPlayerFigures();
        final Figure king = board.getKing(player.isWhite());

        //king shall never be null
        if (king == null) {
            throw new IllegalStateException();
        }

        final List<Figure> figures = playerListMap.get(player);

        return figures.stream().
                //flat maps all possible Moves of each figure into one stream
                        flatMap(figure -> mapToStrikeMove(figure, board)).
                //filter null moves
                        filter(Objects::nonNull).
                //checks playerMove on validity
                        filter(move -> checkPlayerMove(king, move, board, game)).
                //collects stream into list
                        collect(Collectors.toList());
    }

    private static Stream<PlayerMove> mapToStrikeMove(Figure figure, FigureBoard board) {
        final List<Position> allowedPositions = PositionGenerator.getAllowedPositions(figure, board);

        //no playerMoves if no position is allowed
        if (allowedPositions.isEmpty()) {
            return new ArrayList<PlayerMove>().stream();
        }

        if (figure.is(FigureType.PAWN)) {
            List<PlayerMove> moves = allowedPositions.stream().map(position -> transformStrike(figure, position, board)).collect(Collectors.toList());
            List<PlayerMove> promotions = getPromotions(moves, figure);
            moves.addAll(promotions);
            return moves.stream();
        }

        return allowedPositions.stream().map(position -> transformStrike(figure, position, board));
    }

    private static PlayerMove transformStrike(Figure figure, Position position, FigureBoard board) {
        if (figure == null) {
            return null;
        }

        final Figure boardFigure = board.figureAt(position);
        Position from = board.positionOf(figure);

        final Move move = new Move(from, position, figure.getType(), figure.getColor());

        //add strike move if position is not empty and is an enemy piece
        if (boardFigure != null && !boardFigure.getColor().equals(figure.getColor())) {

            //a king shall never be defeated
            if (boardFigure.is(FigureType.KING)) {
                return null;
            }

            Move second = new Move(position, Position.Bench, boardFigure.getType(), boardFigure.getColor());
            return new PlayerMove(move, second);
        } else {
            return null;
        }
    }
}

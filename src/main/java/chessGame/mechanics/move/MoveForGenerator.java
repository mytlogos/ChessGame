package chessGame.mechanics.move;

import chessGame.mechanics.*;
import chessGame.mechanics.board.FigureBoard;
import chessGame.mechanics.game.Game;

import java.util.*;

/**
 *
 */
public class MoveForGenerator {
    private static Map<BitSet, BitSet> cache = new HashMap<>();

    /**
     * Generates a List of {@link PlayerMove}s  for the player.
     *
     * @param player player to generate the moves for
     * @param game   game to generate the moves from
     * @return list of valid PlayerMoves for the player, or empty if none are available
     */
    public static List<PlayerMove> getAllowedMoves(Color player, Game game) {
        if (player == null) return new ArrayList<>();

        FigureBoard board = game.getBoard();
       /* BitSet snapShot = game.getSnapShot();

        if (game.getAtMoveColor() == player) {
            BitSet set = cache.get(snapShot);

            if (set != null) {
                return MoveCoder.decode(set);
            }

        } else {
            BitSet notAtMoveState = BoardEncoder.getAtMoveState(snapShot, player);
            BitSet moves = cache.get(notAtMoveState);

            if (moves == null) {
                snapShot = notAtMoveState;
            } else {
                return MoveCoder.decode(moves);
            }
        }*/

        final Map<Color, List<Figure>> playerListMap = board.getPlayerFigures();
        final Figure king = board.getKing(player.isWhite());

        //king shall never be null
        if (king == null) {
            throw new IllegalStateException();
        }

        final List<Figure> figures = playerListMap.get(player);

        List<PlayerMove> playerMoves = new ArrayList<>();

        for (Figure figure : figures) {
            List<PlayerMove> moves = mapToPlayerMove(figure, board, game);
            playerMoves.addAll(moves);
        }

        playerMoves.removeIf(move -> isInvalidPlayerMove(king, move, board, game));

//        cache.put(snapShot, MoveCoder.encode(playerMoves));
        return playerMoves;
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

        List<PlayerMove> playerMoves = new ArrayList<>();

        for (Figure figure : figures) {
            List<PlayerMove> moves = mapToStrikeMove(figure, board);
            playerMoves.addAll(moves);
        }

        playerMoves.removeIf(move -> isInvalidPlayerMove(king, move, board, game));

        return playerMoves;
    }

    private static List<PlayerMove> mapToStrikeMove(Figure figure, FigureBoard board) {
        final List<Position> allowedPositions = PositionGenerator.getAllowedPositions(figure, board);

        //no playerMoves if no position is allowed
        if (allowedPositions.isEmpty()) {
            return new ArrayList<>();
        }

        List<PlayerMove> moves = new ArrayList<>();

        for (Position position : allowedPositions) {
            PlayerMove transform = transformStrike(figure, position, board);
            if (transform != null) {
                moves.add(transform);
            }
        }

        if (figure.is(FigureType.PAWN)) {
            List<PlayerMove> promotions = getPromotions(moves, figure);
            moves.addAll(promotions);
            return moves;
        }

        return moves;
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

    private static List<PlayerMove> mapToPlayerMove(Figure figure, FigureBoard board, Game game) {
        final List<Position> allowedPositions = PositionGenerator.getAllowedPositions(figure, board);

        //no playerMoves if no position is allowed
        if (allowedPositions.isEmpty()) {
            return new ArrayList<>();
        }

        List<PlayerMove> moves = new ArrayList<>();

        for (Position position : allowedPositions) {
            PlayerMove transform = transform(figure, position, board);
            if (transform != null) {
                moves.add(transform);
            }
        }

        if (figure.is(FigureType.KING)) {
            Collection<PlayerMove> castling = getCastling(figure, board, game);
            moves.addAll(castling);
            return moves;
        }

        if (figure.is(FigureType.PAWN)) {

            List<PlayerMove> promotions = getPromotions(moves, figure);
            moves.addAll(promotions);
            PlayerMove enPassant = getEnPassant(figure, game);
            if (enPassant != null) {
                moves.add(enPassant);
            }
            return moves;
        }

        return moves;
    }

    private static List<PlayerMove> getPromotions(List<PlayerMove> pawnMoves, Figure pawn) {
        //get promotions within the rules of https://en.wikipedia.org/wiki/Promotion_(chess)

        List<PlayerMove> promotions = new ArrayList<>();
        for (PlayerMove pawnMove : pawnMoves) {
            if (checkPromotable(pawnMove)) {
                List<Figure> promotable = getPromotable(pawn);

                for (Figure figure : promotable) {
                    PlayerMove promotion = transformPromotion(pawnMove, figure);
                    promotions.add(promotion);
                }
            }
        }
        return promotions;
    }

    private static PlayerMove transformPromotion(PlayerMove move, Figure promoter) {
        Move mainMove = move.getMainMove();
        Move pawnMove = new Move(mainMove.getFrom(), Position.Promoted, mainMove.getFigure(), mainMove.getColor());

        final Move promotionMove = new Move(Position.Unknown, mainMove.getTo(), promoter.getType(), promoter.getColor());
        return PlayerMove.PromotionMove(pawnMove, move.getSecondaryMove().orElse(null), promotionMove);
    }


    private static List<Figure> getPromotable(Figure pawn) {
        List<Figure> figures = new ArrayList<>();
        figures.add(FigureType.ROOK.create(pawn.getColor()));
        figures.add(FigureType.KNIGHT.create(pawn.getColor()));
        figures.add(FigureType.BISHOP.create(pawn.getColor()));
        figures.add(FigureType.QUEEN.create(pawn.getColor()));
        return figures;
    }

    private static boolean checkPromotable(PlayerMove move) {
        //is white and backRow of black
        return (move.getMainMove().getTo().getRow() == 8 && move.getMainMove().isWhite()) ||
                //is black und backRow of white
                (move.getMainMove().getTo().getRow() == 1 && !move.getMainMove().isWhite());
    }

    private static PlayerMove getEnPassant(Figure pawn, Game game) {
        //check for en passant move (https://en.wikipedia.org/wiki/En_passant)
        Color enemy = Color.getEnemy(pawn.getColor());
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

    private static boolean isInvalidPlayerMove(Figure king, PlayerMove playerMove, FigureBoard board, Game game) {
        if (playerMove == null) {
            return true;
        }

        //throw error if king is not of same playerType as the one of the move
        if (!playerMove.getMainMove().getColor().equals(king.getColor())) {
            throw new IllegalStateException("generierter zug stimmt nicht mit Spieler des Königs überein!");
        }
        MoveMaker.makeMove(playerMove, board, game);
        Position position = board.positionOf(king);

        boolean check = isInCheck(king, board, position);
        MoveMaker.redo(board, game, playerMove);

        return check;
    }

    public static boolean isInCheck(Figure figure, FigureBoard board) {
        return isInCheck(figure, board, board.positionOf(figure));
    }

    private static boolean isInCheck(Figure figure, FigureBoard board, Position position) {
        Color player = figure.getColor();
        Color enemy = Color.getEnemy(player);

        boolean check = false;

        for (Map.Entry<Figure, List<Position>> entry : allowedPositions(enemy, board).entrySet()) {
            if (entry.getValue().contains(position)) {
                check = true;
                break;
            }
        }
        return check;
    }


    private static PlayerMove transform(Figure figure, Position position, FigureBoard board) {

        if (figure == null) {
            return null;
        }

        final Figure boardFigure = board.figureAt(position);
        Position from = board.positionOf(figure);

        if (from == Position.Unknown) {
            return null;
        }

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

        boolean isWhite = king.isWhite();

        boolean longCastling = game.getHistory().longCastling(isWhite);
        boolean shortCastling = game.getHistory().shortCastling(isWhite);

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

        List<Position> positionList = new ArrayList<>();

        for (List<Position> positions : allowedPositions(enemy, board).values()) {
            positionList.addAll(positions);
        }

        return addCastling(king, figure, rookPosition, kingPosition, rookColumn, kingColumn, positionList, board);
    }

    private static Map<Figure, List<Position>> allowedPositions(Color player, FigureBoard board) {
        Map<Figure, List<Position>> map = new HashMap<>();

        for (Figure figure : board.getFigures(player)) {
            List<Position> allowedPositions = PositionGenerator.getAllowedPositions(figure, board);
            map.put(figure, allowedPositions);
        }
        return map;
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
}

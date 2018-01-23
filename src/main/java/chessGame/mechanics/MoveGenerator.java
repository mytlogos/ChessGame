package chessGame.mechanics;

import chessGame.mechanics.figures.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates moves, which are allowed for the current state of the Board.
 * Has only the Method {@link #getAllowedMoves(Player, Game)}, which is accessible from outside the class.
 */
public final class MoveGenerator {
    private Board board;

    MoveGenerator(Board board) {
        this.board = board;
    }

    /**
     * Generates a List of {@link PlayerMove}s  for the player.
     *
     * @param player
     * @param game
     * @return
     */
    public static List<PlayerMove> getAllowedMoves(Player player, Game game) {
        if (player == null) return new ArrayList<>();
        Objects.requireNonNull(game);
        Board board = game.getBoard();
        Objects.requireNonNull(board);

        final Map<Player, List<Figure>> playerListMap = board.getPlayerFigures();

        final King king = board.getKing(player);

//        board.setPlaying(true);


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

    private static Stream<? extends PlayerMove> mapToPlayerMove(Figure figure, Board board, Game game) {
        final List<Position> allowedPositions = figure.getAllowedPositions();

        if (figure instanceof King) {
            final List<PlayerMove> moves = allowedPositions.stream().map(position -> transform(figure, position, board)).collect(Collectors.toList());
            moves.addAll(getCastling((King) figure, board, game));
            return moves.stream();
        }

        if (figure instanceof Pawn) {
            final List<PlayerMove> pawnMoves = allowedPositions.stream().map(position -> transform(figure, position, board)).collect(Collectors.toList());
            addPromotions(pawnMoves, (Pawn) figure, board);
            addEnPassant(pawnMoves, (Pawn) figure, game);
            return pawnMoves.stream();
        }

        return allowedPositions.stream().map(position -> transform(figure, position, board));
    }

    private static boolean checkPlayerMove(King king, PlayerMove playerMove, Board board, Game game) {
        if (playerMove == null) {
            return false;
        }

        if (playerMove.getMainMove().getFigure().getPlayer() != king.getPlayer()) {
            throw new IllegalStateException("generierter zug stimmt nicht mit Spieler des Königs überein!");
        }
        try {
            final Figure clonedKing = board.figureAt(king.getPosition());

            MoveMaker.makeMove(playerMove, board, game);
            final List<Figure> checkFigure = getCheckFigure(clonedKing, board, game);
            MoveMaker.redo(board, game, playerMove);
            return checkFigure.isEmpty();
        } catch (IllegalMoveException ignored) {
        }
        return false;
    }

    private static PlayerMove transform(Figure figure, Position position, Board board) {
        if (figure == null) {
            return null;
        }

        final Figure boardFigure = board.figureAt(position);

        final Move move = new Move(figure, new PositionChange(figure.getPosition(), position));
        Move second = null;

        if (boardFigure != null && !boardFigure.getPlayer().equals(figure.getPlayer())) {
            second = new Move(boardFigure, new PositionChange(boardFigure.getPosition(), Position.Bench));
        }
        return new PlayerMove(move, second);
    }

    private static Collection<PlayerMove> getCastling(King king, Board board, Game game) {
        final ArrayList<PlayerMove> moves = new ArrayList<>();

        boolean kingMovePresent = game.getHistory().stream().anyMatch(move -> move.getMainMove().getFigure().equals(king));

        if (!kingMovePresent) {
            //check for rooks which fit the castling criteria
            final List<Rook> castlingRooks = board.getPlayerFigures().
                    get(king.getPlayer()).
                    stream().
                    filter(figure -> figure.getType() == FigureType.ROOK).
                    filter(figure -> figure.getPosition().isInBoard()).
                    map(figure -> figure instanceof Rook ? (Rook) figure : null).
                    filter(Objects::nonNull).
                    filter(Rook::eligibleForCastling).
                    filter(rook -> isMoved(game, rook)).
                    collect(Collectors.toList());


            if (!castlingRooks.isEmpty()) {

                for (Rook rook : castlingRooks) {
                    //check if castling move for this rook is valid
                    final Position rookPosition = rook.getPosition();
                    final Position kingPosition = king.getPosition();

                    final int rookColumn = rookPosition.getColumn();
                    final int kingColumn = kingPosition.getColumn();

                    final Player enemy = game.getEnemy(king.getPlayer());
                    final List<Position> enemyPositions = possiblePositions(enemy, board)
                            .values()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());

                    addCastling(king, moves, rook, rookPosition, kingPosition, rookColumn, kingColumn, enemyPositions, board);
                }
            }
        }

        return moves;
    }

    private static boolean isMoved(Game game, Rook rook) {
        return game.getHistory().stream().anyMatch(move -> move.getMainMove().getFigure().equals(rook) || move.getSecondaryMove().map(move1 -> move1.getFigure().equals(rook)).orElse(false));
    }

    private static void addPromotions(List<PlayerMove> pawnMoves, Pawn pawn, Board board) {
        final List<PlayerMove> promotionAble = pawnMoves.stream().filter(move -> {
            final Position to = move.getMainMove().getTo();
            final Figure moveFigure = move.getMainMove().getFigure();

            return (to.getRow() == 8 && moveFigure.getPlayer().isWhite()) ||
                    (to.getRow() == 1 && !moveFigure.getPlayer().isWhite());

        }).collect(Collectors.toList());

        //get promotions within the rules of https://en.wikipedia.org/wiki/Promotion_(chess)
        final List<PlayerMove> promotions = promotionAble.stream()
                .flatMap(move -> List.of(
                        new Rook(null, pawn.getPlayer(), board),
                        new Knight(null, pawn.getPlayer(), board),
                        new Bishop(null, pawn.getPlayer(), board),
                        new Queen(null, pawn.getPlayer(), board))
                        .stream()
                        .map(promoter -> {
                            final Figure figure = move.getMainMove().getFigure();
                            final Move mainMove = new Move(figure, new PositionChange(figure.getPosition(), Position.Promoted));
                            final Move promotionMove = new Move(promoter, new PositionChange(promoter.getPosition(), move.getMainMove().getTo()));

                            return PlayerMove.PromotionMove(mainMove, move.getSecondaryMove().orElse(null), promotionMove);
                        }))
                .collect(Collectors.toList());

        pawnMoves.addAll(promotions);
    }

    private static void addEnPassant(List<PlayerMove> pawnMoves, Pawn pawn, Game game) {
        //check for en passant move (https://en.wikipedia.org/wiki/En_passant)
        final Player enemy = game.getEnemy(pawn.getPlayer());
        final PlayerMove lastMove = game.getLastMove();

        if (lastMove == null) {
            return;
        }

        final int rowFrom = lastMove.getMainMove().getFrom().getRow();
        final Position to = lastMove.getMainMove().getTo();
        final int rowTo = to.getRow();


        final Figure enemyPawn = lastMove.getMainMove().getFigure();
        final int enemyColumn = enemyPawn.getPosition().getColumn();

        //check if pawn, if pawn is of enemy, lastMove was no castling or promotionMove
        //last move pawn moved two fields, enemy pawn and current pawn are side by side
        //in same row and adjacent columns
        if (enemyPawn.getType() == FigureType.PAWN
                && lastMove.getPlayer().equals(enemy)
                && lastMove.isNormal()
                && lastMove.getSecondaryMove().orElse(null) == null
                && Math.abs(rowFrom - rowTo) == 2
                && Math.abs(pawn.getPosition().getColumn() - enemyColumn) == 1
                && enemyPawn.getPosition().getRow() == pawn.getPosition().getRow()) {


            final int row;
            if (pawn.getPlayer().isWhite()) {
                row = 6;
            } else {
                row = 3;
            }

            final PositionChange mainChange = new PositionChange(pawn.getPosition(), Position.get(row, enemyColumn));
            final Move mainMove = new Move(pawn, mainChange);
            final Move strike = new Move(enemyPawn, new PositionChange(enemyPawn.getPosition(), Position.Bench));

            final PlayerMove en_Passant = new PlayerMove(mainMove, strike);
            pawnMoves.add(en_Passant);
        }
    }

    private static List<Figure> getCheckFigure(Figure figure, Board board, Game game) {
        if (figure == null) {
            System.out.println();
        }
        Player player = figure.getPlayer();
        Player enemy = game.getEnemy(player);
        return possiblePositions(enemy, board).
                entrySet().
                stream().
                filter(entry -> entry.getValue().contains(figure.getPosition())).
                map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static Map<Figure, List<Position>> possiblePositions(Player player, Board board) {
        return board.getPlayerFigures().get(player).stream().collect(Collectors.toMap(Function.identity(), Figure::getAllowedPositions));
    }

    private static void addCastling(King king, ArrayList<PlayerMove> moves, Rook rook, Position rookPosition, Position kingPosition, int rookColumn, int kingColumn, List<Position> enemyPositions, Board board) {
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

            final Move kingMove = new Move(king, new PositionChange(kingPosition, newKingPosition));
            final Move rookMove = new Move(rook, new PositionChange(rookPosition, newRookPosition));
            final PlayerMove playerMove = PlayerMove.CastlingMove(kingMove, rookMove);
            moves.add(playerMove);
        }
    }

    private static boolean checkEmpty(int rightColumn, int leftColumn, int row, Board board) {
        boolean empty = true;

        for (int column = leftColumn + 1; column < rightColumn; column++) {
            final Position position = Position.get(row, column);
            final Figure figure = board.figureAt(position);
            if (figure != null) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    public static boolean isInCheck(King king, Board board, Game game) {
        return !getCheckFigure(king, board, game).isEmpty();
    }
}

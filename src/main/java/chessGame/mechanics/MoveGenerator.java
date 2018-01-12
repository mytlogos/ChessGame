package chessGame.mechanics;

import chessGame.mechanics.figures.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates moves, which are allowed for the current state of the Board.
 */
public final class MoveGenerator {
    private AbstractBoard board;

    MoveGenerator(AbstractBoard board) {
        this.board = board;
    }

    /**
     * Generates a List of {@link PlayerMove}s  for the player.
     * @param player
     * @return
     */
    public List<PlayerMove> getAllowedMoves(Player player) {
        if (player == null) return new ArrayList<>();

        final Map<Player, List<Figure>> playerListMap = getPlayerFiguresMap(board);

        final List<Figure> figures = playerListMap.get(player);
        final King king = (King) figures.stream().collect(Collectors.groupingBy(Figure::getType)).get(FigureType.KING).get(0);

        //king shall never be null
        if (king == null) {
            throw new IllegalStateException();
        } else {
            final List<Figure> dangerFigures = getCheckFigure(king, board);

            //todo check if this fork is necessary
            //check if the king is checked
            if (!dangerFigures.isEmpty()) {
                //king is checked, proceed to get moves to get out of check
                final Map<Figure, List<Position>> possiblePositions = possiblePositions(player, board);
                final Stream<PlayerMove> moveStream = getOutOfCheckMoveStream(king, possiblePositions);
                return getCollect(king, moveStream);
            } else {
                //king not checked, proceed to other moves
                final Stream<PlayerMove> moveStream = getMoveStream(figures);
                return getCollect(king, moveStream);
            }
        }
    }

    private List<PlayerMove> getCollect(King king, Stream<PlayerMove> stream) {
        return stream.filter(Objects::nonNull).filter(move -> checkPlayerMove(king, move)).collect(Collectors.toList());
    }

    private Stream<PlayerMove> getMoveStream(List<Figure> figures) {
        return figures.stream().flatMap(this::map);
    }

    private Stream<PlayerMove> getOutOfCheckMoveStream(King king, Map<Figure, List<Position>> possiblePositions) {
        return possiblePositions.
                entrySet().
                parallelStream().
                flatMap(entry-> simulate(king,entry));
    }

    private Stream<PlayerMove> simulate(King king, Map.Entry<Figure, List<Position>> entry) {
        final List<PlayerMove> moves = entry.getValue()
                .stream()
                .map(position -> simulateOutOfCheckMove(king, entry, position))
                .collect(Collectors.toList());

        return moves.stream();
    }

    private Stream<? extends PlayerMove> map(Figure figure) {
        final List<Position> allowedPositions = figure.getAllowedPositions();

        if (figure instanceof King) {
            final List<PlayerMove> moves = allowedPositions.stream().map(position -> transform(figure, position, board)).collect(Collectors.toList());
            moves.addAll(getCastling((King) figure));
            return moves.stream();
        }

        if (figure instanceof Pawn) {
            final List<PlayerMove> pawnMoves = allowedPositions.stream().map(position -> transform(figure, position, board)).collect(Collectors.toList());
            addPromotions(pawnMoves, (Pawn) figure, board);
            addEnPassant(pawnMoves, (Pawn) figure, board);
            return pawnMoves.stream();
        }

        return allowedPositions.stream().map(position -> transform(figure, position, board));
    }

    private void addEnPassant(List<PlayerMove> pawnMoves, Pawn pawn, AbstractBoard board) {
        //check for en passant move (https://en.wikipedia.org/wiki/En_passant)
        final Player enemy = board.getEnemy(pawn.getPlayer());
        final PlayerMove lastMove = board.getLastMove();

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
                && lastMove.getSecondaryMove() == null
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

    private void addPromotions(List<PlayerMove> pawnMoves, Pawn pawn, AbstractBoard board) {
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

                            return PlayerMove.PromotionMove(mainMove, move.getSecondaryMove(), promotionMove);
                        }))
                .collect(Collectors.toList());

        pawnMoves.addAll(promotions);
    }

    private Collection<PlayerMove> getCastling(King king) {
        final ArrayList<PlayerMove> moves = new ArrayList<>();

        if (!king.hasMoved()) {
            final List<Rook> castlingRooks = getPlayerFiguresMap(board).
                    get(king.getPlayer()).
                    stream().
                    filter(figure -> figure.getType() == FigureType.ROOK).
                    filter(figure -> figure.getPosition().isInBoard()).
                    map(figure -> figure instanceof Rook ? (Rook) figure : null).
                    filter(Objects::nonNull).
                    filter(Rook::eligibleForCastling).
                    collect(Collectors.toList());

            if (!castlingRooks.isEmpty()) {
                for (Rook rook : castlingRooks) {
                    final Position rookPosition = rook.getPosition();
                    final Position kingPosition = king.getPosition();

                    final int rookColumn = rookPosition.getColumn();
                    final int kingColumn = kingPosition.getColumn();

                    final Player enemy = board.getEnemy(king.getPlayer());
                    final List<Position> enemyPositions = possiblePositions(enemy, board)
                            .values()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());

                    addCastling(king, moves, rook, rookPosition, kingPosition, rookColumn, kingColumn, enemyPositions);
                }
            }
        }

        return moves;
    }

    private void addCastling(King king, ArrayList<PlayerMove> moves, Rook rook, Position rookPosition, Position kingPosition, int rookColumn, int kingColumn, List<Position> enemyPositions) {
        boolean legal;
        int newKingColumn;
        int newRookColumn;
        final int row = kingPosition.getRow();

        if (rookColumn > kingColumn) {
            legal = checkEmpty(rookColumn, kingColumn, row);
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
            legal = checkEmpty(kingColumn, rookColumn, row);
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

    private boolean checkEmpty(int rightColumn, int leftColumn, int row) {
        boolean empty = true;

        for (int column = leftColumn + 1; column < rightColumn; column++) {
            final Position position = Position.get(row, column);
            final Figure figure = board.getFigure(position);
            if (figure != null) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    private PlayerMove transform(Figure figure, Position position, AbstractBoard board) {
        if (figure == null) {
            return null;
        }

        final Figure boardFigure = board.getFigure(position);

        final Move move = new Move(figure, new PositionChange(figure.getPosition(), position));
        Move second = null;

        if (boardFigure != null && !boardFigure.getPlayer().equals(figure.getPlayer())) {
            second = new Move(boardFigure, new PositionChange(boardFigure.getPosition(), Position.Bench));
        }
        return new PlayerMove(move, second);
    }

    private boolean checkPlayerMove(King king, PlayerMove playerMove) {
        if (playerMove == null || playerMove.getMainMove().getFigure().getPlayer() != king.getPlayer()) {
            return false;
        }
        final LockedBoard clone = board.cloneBoard();
        try {
            if (clone != null) {
                final Figure clonedKing = clone.getFigure(king.getPosition());

                if (clonedKing.getType() != FigureType.KING) {
                    throw new IllegalStateException("cloned boardMap is not in same state as original");
                }

                final PlayerMove clonedMove = playerMove.clone(clone);
                clone.move(clonedMove);
                final List<Figure> checkFigure = getCheckFigure(clonedKing, clone);
                return checkFigure.isEmpty();
            }
        } catch (IllegalMoveException ignored) {
        }
        return false;
    }

    private PlayerMove simulateOutOfCheckMove(King king, Map.Entry<Figure, List<Position>> entry, Position position) {
        final Figure key = entry.getKey();
        final PlayerMove move = transform(key, position, board);

        if (checkPlayerMove(king, move)) {
            return move;
        }
        return null;
    }

    private Map<Player, List<Figure>> getPlayerFiguresMap(AbstractBoard board) {
        return board.figures().
                stream().
                collect(Collectors.groupingBy(Figure::getPlayer));
    }

    private List<Figure> getCheckFigure(Figure figure, AbstractBoard board) {
        return possiblePositions(board.getEnemy(figure.getPlayer()), board).
                entrySet().
                stream().
                filter(entry -> entry.getValue().contains(figure.getPosition())).
                map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<Figure, List<Position>> possiblePositions(Player player, AbstractBoard board) {
        final List<Figure> figures = getPlayerFiguresMap(board).get(player);
        return figures.stream().collect(Collectors.toMap(Function.identity(), Figure::getAllowedPositions));
    }
}

package chessGame.mechanics;

import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;
import chessGame.mechanics.figures.King;
import chessGame.mechanics.figures.Pawn;
import javafx.scene.control.ToggleGroup;

import java.time.OffsetDateTime;

/**
 *
 */
public final class MoveMaker {

    private MoveMaker() {
        throw new IllegalStateException("No Instances allowed!");
    }

    public static boolean makeSafeMove(PlayerMove playerMove, Board board, Game game) throws IllegalMoveException {
        if (!game.getAllowedMoves().contains(playerMove)) {
            throw new IllegalMoveException("der zug " + playerMove + " ist nicht erlaubt für " + game.getAtMove());
        } else if (game.getLastMove() != null && game.getLastMove().getPlayer().equals(playerMove.getPlayer())) {
            throw new IllegalMoveException("ein spieler darf nicht zweimal hintereinander ziehen!");
        }
        return makeMove(playerMove, board, game);
    }

    public static boolean makeMove(PlayerMove playerMove, Board board, Game game) throws IllegalMoveException {
        Move secondMove = playerMove.getSecondaryMove().orElse(null);

        if (secondMove != null && secondMove.getFigure().is(FigureType.KING) && secondMove.getTo() == Position.Bench) {
            throw new IllegalMoveException("König darf nicht geschlagen werden!");
        }

        if (playerMove.isCastlingMove()) {
            castle(playerMove, board, game);
        } else if (playerMove.isPromotion()) {
            promote(playerMove, board, game);
        } else {

            if (secondMove != null) {
                makeMove(secondMove, board, game);
            }

            makeMove(playerMove.getMainMove(), board, game);
        }
        return true;
    }

    private static void castle(PlayerMove playerMove, Board board, Game game) throws IllegalMoveException {
        final Move mainMove = playerMove.getMainMove();
        final Move secondaryMove = playerMove.getSecondaryMove().orElseThrow(() -> new IllegalStateException("rook move for castling is null"));

        makeMove(mainMove, board, game);
        makeMove(secondaryMove, board, game);
    }

    private static void promote(PlayerMove playerMove, Board board, Game game) throws IllegalMoveException {
        final Move mainMove = playerMove.getMainMove();
        final Figure figure = mainMove.getFigure();

        if (figure.getType() != FigureType.PAWN) {
            throw new IllegalArgumentException("only pawns can be promoted!");
        } else {
            //remove figure from old position, now empty
            board.setEmpty(mainMove.getFrom());

            figure.setPosition(Position.Promoted);
            game.addPromoted((Pawn) figure);
        }

        final Move secondMove = playerMove.getSecondaryMove().orElse(null);

        if (secondMove != null) {
            makeMove(secondMove, board, game);
        }

        //set promoted figure to new position
        final Move promotionMove = playerMove.getPromotionMove().orElseThrow(() -> new IllegalStateException("promotion move is null"));
        board.setFigure(promotionMove.getFigure(), promotionMove.getTo());
    }

    private static void makeMove(Move move, Board board, Game game) throws IllegalMoveException {
        final Figure figure = move.getFigure();
        final Position from = move.getFrom();

        final Figure boardFigure = board.figureAt(from);

        if (!figure.equals(boardFigure)) {
            throw new IllegalMoveException("boardMap is not synched with its figure: different position, boardMap has: " + boardFigure + " for " + figure);
        } else {
            final Position to = move.getTo();

            //check king move, BEFORE changing state of board
            if (to == Position.Bench && figure.is(FigureType.KING)) {
                throw new IllegalMoveException("König darf nicht geschlagen werden");
            }

            board.setEmpty(from);

            if (to.equals(Position.Bench)) {
                figure.setPosition(Position.Bench);
                game.addBench(figure);
            } else {
                board.setFigure(figure, to);
            }
        }
    }


    /**
     * This Method reverses the Changes of a {@link PlayerMove} in reverse order.
     * First the mainMove, then the promotionMove and at last the secondaryMove
     * will be undone.
     */
    public static boolean redo(Board board, Game game, PlayerMove lastMove) {
        if (lastMove != null) {
            final Move mainMove = lastMove.getMainMove();

            //first redo mainMove
            redo(mainMove, board, game);

            //then remove promoted
            lastMove.getPromotionMove().ifPresent(move -> redo(move, board, game));
            //so that a defeated figure can take the place
            lastMove.getSecondaryMove().ifPresent(move -> redo(move, board, game));
        }
        return true;
    }

    private static void redo(Move move, Board board, Game game) {
        final Figure figure = move.getFigure();
        final Position from = move.getFrom();
        final Position to = move.getTo();

        if (to == Position.Bench) {
            game.removeFromBench(figure);
        } else if (to == Position.Promoted) {
            game.removeFromPromoted((Pawn) figure);
        } else {
            board.setEmpty(to);
        }

        if (from != null) {
            board.setFigure(figure, from);
        }
    }
}

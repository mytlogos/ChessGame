package chessGame.mechanics.move;

import chessGame.mechanics.Figure;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;
import chessGame.mechanics.board.FigureBoard;
import chessGame.mechanics.game.Game;

/**
 *
 */
public abstract class MoveMaker {

    private MoveMaker() {
        throw new IllegalStateException("No Instances allowed!");
    }

    public static void makeSafeMove(PlayerMove playerMove, FigureBoard board, Game game) {
        if (!game.getAllowedMoves().contains(playerMove)) {
            throw new IllegalStateException("der zug " + playerMove + " ist nicht erlaubt für " + game.getAtMove());
        } else if (game.getLastMove() != null && game.getLastMove().getColor().equals(playerMove.getColor())) {
            throw new IllegalStateException("ein spieler darf nicht zweimal hintereinander ziehen!");
        }

        makeMove(playerMove, board, game);
    }

    static void makeMove(PlayerMove playerMove, FigureBoard board, Game game) {
        Move secondMove = playerMove.getSecondaryMove().orElse(null);

        if (secondMove != null && secondMove.getFigure() == FigureType.KING && secondMove.getTo() == Position.Bench) {
            throw new IllegalStateException("König darf nicht geschlagen werden!");
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
    }

    private static void castle(PlayerMove playerMove, FigureBoard board, Game game) {
        final Move mainMove = playerMove.getMainMove();
        final Move secondaryMove = playerMove.getSecondaryMove().orElseThrow(() -> new IllegalStateException("rook move for castling is null"));

        makeMove(mainMove, board, game);
        makeMove(secondaryMove, board, game);
    }

    private static void promote(PlayerMove playerMove, FigureBoard board, Game game) {
        final Move mainMove = playerMove.getMainMove();
        final FigureType figureType = mainMove.getFigure();

        final Figure boardFigure = board.figureAt(mainMove.getFrom());

        if (!figureType.equals(boardFigure.getType())) {
            throw new IllegalStateException("boardMap is not synched with its figure: different position, boardMap has: " + boardFigure + " for " + figureType);
        }

        if (figureType != FigureType.PAWN) {
            throw new IllegalArgumentException("only pawns can be promoted!");
        } else {
            //remove figureType from old position, now empty
            board.setEmpty(mainMove.getFrom());
            game.addPromoted(boardFigure);
        }

        playerMove.getSecondaryMove().ifPresent(secondMove -> makeMove(secondMove, board, game));

        //set promoted figureType to new position
        final Move promotionMove = playerMove.getPromotionMove().orElseThrow(() -> new IllegalStateException("promotion move is null"));
        board.setFigure(promotionMove.getFigure().create(mainMove.getColor()), promotionMove.getTo());
    }

    private static void makeMove(Move move, FigureBoard board, Game game) {
        final FigureType figureType = move.getFigure();
        final Position from = move.getFrom();


        final Figure boardFigure = board.figureAt(from);

        if (boardFigure == null || !figureType.equals(boardFigure.getType())) {
            throw new IllegalStateException("boardMap is not synched with its figure: different position, boardMap has: " + boardFigure + " for " + figureType);
        } else {
            final Position to = move.getTo();
            board.setEmpty(from);

            if (to.equals(Position.Bench)) {
                game.addBench(boardFigure);
            } else {
                board.setFigure(boardFigure, to);
            }
        }
    }


    /**
     * This Method reverses the Changes of a {@link PlayerMove} in reverse order.
     * First the mainMove, then the promotionMove and at last the secondaryMove
     * will be undone.
     */
    public static void redo(FigureBoard board, Game game, PlayerMove lastMove) {
        if (lastMove != null) {
            final Move mainMove = lastMove.getMainMove();

            //first redo mainMove
            redo(mainMove, board, game);

            //then remove promoted
            lastMove.getPromotionMove().ifPresent(move -> redo(move, board, game));
            //so that a defeated figure can take the place
            lastMove.getSecondaryMove().ifPresent(move -> redo(move, board, game));
        }
    }

    private static void redo(Move move, FigureBoard board, Game game) {
        final Position from = move.getFrom();
        final Position to = move.getTo();


        Figure figure;

        if (to == Position.Bench) {
            figure = game.removeFromBench(move.getColor(), move.getFigure());
        } else if (to == Position.Promoted) {
            figure = game.removeFromPromoted(move.getColor());
        } else {
            figure = board.figureAt(to);

            if (figure == null) {
                throw new NullPointerException("Figure at " + to + " is null");
            }
            board.setEmpty(to);
        }

        if (figure == null) {
            throw new NullPointerException();
        }

        if (from != Position.Unknown) {
            board.setFigure(figure, from);
        }
    }
}

package chessGame.mechanics;

import chessGame.mechanics.figures.*;

/**
 *
 */
public class BoardInitiator {
    public static void initiate(Board board, Game game) {
        setPositions(Rook::new, 1, 1, board, game);
        setPositions(Knight::new, 1, 2, board, game);
        setPositions(Bishop::new, 1, 3, board, game);

        setFigure(Queen::new, 1, 4, game.getWhite(), board);
        setFigure(King::new, 1, 5, game.getWhite(), board);
        setFigure(Queen::new, 8, 4, game.getBlack(), board);
        setFigure(King::new, 8, 5, game.getBlack(), board);

        setPositions(Bishop::new, 1, 6, board, game);
        setPositions(Knight::new, 1, 7, board, game);
        setPositions(Rook::new, 1, 8, board, game);

        for (int i = 0; i < 8; i++) {
            setPositions(Pawn::new, 2, i + 1, board, game);
        }
    }

    private static void setPositions(TriFunction<Position, Player, Board, Figure> figureFunction, int row, int column, Board board, Game game) {
        setFigure(figureFunction, row, column, game.getWhite(), board);

        row = 9 - row;
        column = 9 - column;

        setFigure(figureFunction, row, column, game.getBlack(), board);
    }

    private static void setFigure(TriFunction<Position, Player, Board, Figure> figureFunction, int row, int column, Player player, Board board) {
        final Position position = Position.get(row, column);
        Figure figure = figureFunction.apply(position, player, board);
        board.setFigure(figure, position);
    }
}

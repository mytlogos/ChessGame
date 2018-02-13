package chessGame.mechanics.board;

import chessGame.mechanics.*;
import chessGame.mechanics.game.Game;

import java.util.function.Function;

/**
 *
 */
public class BoardInitiator {
    public static void initiate(Game game) {
        Board board = game.getBoard();
        Color white = Color.WHITE;
        Color black = Color.BLACK;

        setPositions(FigureType.ROOK::create, 1, 1, board);
        setPositions(FigureType.KNIGHT::create, 1, 2, board);
        setPositions(FigureType.BISHOP::create, 1, 3, board);

        setFigure(FigureType.QUEEN::create, 1, 4, white, board);
        setFigure(FigureType.KING::create, 1, 5, white, board);
        setFigure(FigureType.QUEEN::create, 8, 4, black, board);
        setFigure(FigureType.KING::create, 8, 5, black, board);

        setPositions(FigureType.BISHOP::create, 1, 6, board);
        setPositions(FigureType.KNIGHT::create, 1, 7, board);
        setPositions(FigureType.ROOK::create, 1, 8, board);

        for (int i = 0; i < 8; i++) {
            setPositions(FigureType.PAWN::create, 2, i + 1, board);
        }
    }

    public static void initiateHashBoard(Game game) {
        Board board = game.getBoard();
        Color white = Color.WHITE;
        Color black = Color.BLACK;

        setFigure(FigureType.ROOK::create, 1, 2, black, board);
        setFigure(FigureType.ROOK::create, 1, 8, white, board);
        setFigure(FigureType.ROOK::create, 8, 1, black, board);
        setFigure(FigureType.ROOK::create, 8, 8, black, board);

        setFigure(FigureType.QUEEN::create, 1, 4, white, board);
        setFigure(FigureType.KING::create, 1, 5, white, board);
        setFigure(FigureType.QUEEN::create, 8, 4, black, board);
        setFigure(FigureType.KING::create, 8, 5, black, board);

        setFigure(FigureType.BISHOP::create, 8, 3, black, board);
        setFigure(FigureType.BISHOP::create, 8, 6, black, board);
        setFigure(FigureType.BISHOP::create, 2, 2, white, board);
        setFigure(FigureType.BISHOP::create, 1, 6, white, board);

        setFigure(FigureType.KNIGHT::create, 1, 7, white, board);
        setFigure(FigureType.KNIGHT::create, 8, 2, black, board);
        setFigure(FigureType.KNIGHT::create, 8, 7, black, board);

        setFigure(FigureType.PAWN::create, 4, 1, black, board);


        for (int i = 0; i < 8; i++) {
            if (i == 1) {
                continue;
            }
            setFigure(FigureType.PAWN::create, 2, i + 1, white, board);

            if (i <= 1) {
                continue;
            }
            setFigure(FigureType.PAWN::create, 7, i + 1, black, board);
        }
    }

    public static void initiateBoard(Game game) {
        Board board = game.getBoard();
        Color white = Color.WHITE;
        Color black = Color.BLACK;

        board.setFigure(FigureType.PAWN.create(white), Position.get(9));
        board.setFigure(FigureType.PAWN.create(black), Position.get(10));

        Figure whiteKing = FigureType.KING.create(white);
        Figure blackKing = FigureType.KING.create(black);

        board.setFigure(whiteKing, Position.get(56));
        board.setFigure(blackKing, Position.get(24));

        if (board instanceof AbstractBoard) {
            ((AbstractBoard) board).setKing(blackKing);
            ((AbstractBoard) board).setKing(whiteKing);
        }
    }

    private static void setPositions(Function<Color, Figure> figureFunction, int row, int column, Board board) {
        setFigure(figureFunction, row, column, Color.WHITE, board);

        row = 9 - row;
        column = 9 - column;

        setFigure(figureFunction, row, column, Color.BLACK, board);
    }

    private static void setFigure(Function<Color, Figure> figureFunction, int row, int column, Color color, Board board) {
        final Position position = Position.get(row, column);
        Figure figure = figureFunction.apply(color);

        if (board instanceof AbstractBoard) {
            ((AbstractBoard) board).setKing(figure);
        }

        board.setFigure(figure, position);
    }
}

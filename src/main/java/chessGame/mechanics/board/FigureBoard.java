package chessGame.mechanics.board;

import chessGame.mechanics.Figure;

/**
 *
 */
public interface FigureBoard extends Board<Figure> {
    long getHash();

    Figure getKing(boolean white);
}

package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

/**
 *
 */
public final class Queen extends Figure {
    public Queen(Position position, Player player, Board board) {
        super(position, player, FigureType.QUEEN, board);
    }

}

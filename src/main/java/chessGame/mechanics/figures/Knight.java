package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

/**
 *
 */
public final class Knight extends Figure {
    public Knight(Position position, Player player, Board board) {
        super(position, player, FigureType.KNIGHT, board);
    }
}

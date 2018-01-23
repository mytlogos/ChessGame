package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

/**
 *
 */
public final class King extends Figure {

    public King(Position position, Player player, Board board) {
        super(position, player, FigureType.KING, board);
    }
}

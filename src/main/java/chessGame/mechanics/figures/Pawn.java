package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;

/**
 *
 */
public final class Pawn extends Figure {

    public Pawn(Position position, Player player, Board board) {
        super(position, player, FigureType.PAWN, board);
    }

}

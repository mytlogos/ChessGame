package chessGame.mechanics;

import chessGame.mechanics.figures.Figure;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PositionGenerator {
    private final AbstractBoard board;

    public PositionGenerator(AbstractBoard board) {
        this.board = board;
    }

    List<Position> getAllowedPosition(Figure figure) {
        return new ArrayList<>();
    }
}

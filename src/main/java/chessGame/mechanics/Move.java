package chessGame.mechanics;

import chessGame.figures.Figure;
import chessGame.figures.FigureType;

import java.util.Objects;

/**
 *
 */
public class Move {
    private final PositionChange change;
    private final Figure figure;

    private SecondaryMoveType type = SecondaryMoveType.None;

    public Move(Figure figure, PositionChange change) {
        Objects.requireNonNull(figure);
        Objects.requireNonNull(change);

        this.change = change;
        this.figure = figure;
    }

    public PositionChange getChange() {
        return change;
    }

    public Figure getFigure() {
        return figure;
    }

    enum SecondaryMoveType {
        Castling {
            @Override
            boolean checkValidity(Figure first, Figure second) {
                return ((first.getType() == FigureType.KING) && (second.getType() == FigureType.ROOK))
                        || ((first.getType() == FigureType.ROOK) && (second.getType() == FigureType.KING));
            }
        },
        Defeat{
            @Override
            boolean checkValidity(Figure first, Figure second) {
                return first.getPlayer() != second.getPlayer();
            }
        },
        None;

        boolean checkValidity(Figure first, Figure second) {
            return true;
        }
    }
}

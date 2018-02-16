package chessGame.mechanics.board;

import chessGame.mechanics.Color;
import chessGame.mechanics.Position;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface Board<E> extends Iterable<E>{

    void setFigure(E figure, Position position);

    void setEmpty(Position position);

    Position positionOf(E figure);

    E figureAt(Position position);

    List<E> getFigures(Color player);

    Map<Color, List<E>> getPlayerFigures();

    List<E> getFigures();

    boolean isEmptyAt(Position position);
}

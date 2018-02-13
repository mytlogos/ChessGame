package chessGame.mechanics.board;

import chessGame.mechanics.Color;
import chessGame.mechanics.Figure;
import chessGame.mechanics.Position;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface Board extends Iterable<Figure>{

    void setFigure(Figure figure, Position position);

    void setEmpty(Position position);

    Position positionOf(Figure figure);

    Figure figureAt(Position position);

    List<Figure> getFigures(Color player);

    Map<Color, List<Figure>> getPlayerFigures();

    List<Figure> getFigures();

    BoardSnapShot getSnapShot();

    Figure getKing(boolean white);

    boolean isEmptyAt(Position position);

    long getHash();
}

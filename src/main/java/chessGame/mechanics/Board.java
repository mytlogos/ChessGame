package chessGame.mechanics;

import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.King;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface Board {

    void setFigure(Figure figure, Position position);

    void setEmpty(Position position);

    Figure figureAt(Position position);

    List<Figure> getFigures(Player player);

    Map<Player, List<Figure>> getPlayerFigures();

    List<Figure> getFigures();

    Map<Position, Figure> getBoardMap();

    BoardSnapShot getSnapShot();

    King getKing(Player player);

    boolean isEmptyAt(Position position);

    Board copy();
}

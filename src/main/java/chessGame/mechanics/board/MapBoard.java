package chessGame.mechanics.board;

import chessGame.mechanics.Figure;
import chessGame.mechanics.Color;
import chessGame.mechanics.Position;

import java.util.*;
import java.util.stream.Stream;

/**
 *
 */
public class MapBoard extends AbstractBoard {
    private Map<Position, Figure> boardMap = new TreeMap<>();


    @Override
    public void setFigure(Figure figure, Position position) {
        Objects.requireNonNull(figure);

//        if (kingsNotSet()) setKing(figure);

        Figure previous = boardMap.put(position, figure);

        if (previous != null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setEmpty(Position position) {
        boardMap.remove(position);
    }

    @Override
    public Position positionOf(Figure figure) {
        for (Map.Entry<Position, Figure> entry : boardMap.entrySet()) {
            if (entry.getValue().equals(figure)) {
                return entry.getKey();
            }
        }
        return Position.Unknown;
//        return boardMap.entrySet().stream().filter(entry -> entry.getValue().equals(figure)).findFirst().map(Map.Entry::getKey).orElse(Position.Unknown);
    }

    @Override
    public Figure figureAt(Position position) {
        return boardMap.get(position);
    }

    @Override
    public List<Figure> getFigures(Color player) {
        return getPlayerFigures().get(player);
    }

    @Override
    public Map<Color, List<Figure>> getPlayerFigures() {
        List<Figure> black = new ArrayList<>();
        List<Figure> white = new ArrayList<>();

        for (Figure figure : this) {

            if (figure != null) {
                if (figure.isWhite()) {
                    white.add(figure);
                } else {
                    black.add(figure);
                }
            }
        }
        Map<Color, List<Figure>> map = new HashMap<>();
        map.put(Color.WHITE, white);
        map.put(Color.BLACK, black);
        return map;
//        return getPlayerFiguresByStream();
    }

    @Override
    Stream<Figure> stream() {
        return boardMap.values().stream();
    }

    @Override
    public List<Figure> getFigures() {
        List<Figure> figures = new ArrayList<>();

        for (Figure figure : this) {
            if (figure != null) {
                figures.add(figure);
            }
        }
        return figures;
//        return getFiguresByStream();
    }

}

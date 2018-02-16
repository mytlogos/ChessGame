package chessGame.gui;

import chessGame.mechanics.Color;
import chessGame.mechanics.Figure;
import chessGame.mechanics.Position;
import chessGame.mechanics.board.Board;

import java.util.*;

/**
 *
 */
public class VisualBoard implements Board<FigureView> {
    private final Board<Figure> figureBoard;
    private final BoardGridManager manager;
    private FigureView[] visualBoard = new FigureView[64];

    VisualBoard(Board<Figure> board, BoardGridManager manager) {
        this.figureBoard = board;
        this.manager = manager;
    }

    @Override
    public void setFigure(FigureView figure, Position position) {
        Objects.requireNonNull(figure);

        final int index = position.getPanel();
        visualBoard[index] = figure;
    }

    @Override
    public void setEmpty(Position position) {
        visualBoard[position.getPanel()] = null;
    }

    @Override
    public Position positionOf(FigureView figure) {
        return null;
    }

    @Override
    public FigureView figureAt(Position position) {
        return visualBoard[position.getPanel()];
    }

    @Override
    public List<FigureView> getFigures(Color player) {
        return getPlayerFigures().get(player);
    }

    @Override
    public Map<Color, List<FigureView>> getPlayerFigures() {
        List<FigureView> white = new ArrayList<>();
        List<FigureView> black = new ArrayList<>();

        for (FigureView figure : this) {
            if (figure != null) {
                if (figure.getFigure().isWhite()) {
                    white.add(figure);
                } else {
                    black.add(figure);
                }
            }
        }
        Map<Color, List<FigureView>> map = new HashMap<>();
        map.put(Color.WHITE, white);
        map.put(Color.BLACK, black);
        return map;
    }

    @Override
    public List<FigureView> getFigures() {
        List<FigureView> figures = new ArrayList<>();

        for (FigureView figure : this) {
            if (figure != null) {
                figures.add(figure);
            }
        }
        return figures;
    }

    @Override
    public boolean isEmptyAt(Position position) {
        return false;
    }

    @Override
    public Iterator<FigureView> iterator() {
        return new BoardIterator();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(visualBoard);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisualBoard that = (VisualBoard) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(visualBoard, that.visualBoard);
    }

    private class BoardIterator implements Iterator<FigureView> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < 64;
        }

        @Override
        public FigureView next() {
            FigureView view = visualBoard[index];
            index++;
            return view;
        }
    }

    void mirrorBoard() {
        for (int panel = 0; panel < 64; panel++) {
            Position position = Position.get(panel);
            Figure figure = figureBoard.figureAt(position);

            if (figure == null) {
                setEmpty(position);
            } else {
                FigureView figureView = manager.getFigureView(figure);
                setFigure(figureView, position);
            }
        }
    }
}

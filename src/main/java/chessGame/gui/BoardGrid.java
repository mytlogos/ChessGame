package chessGame.gui;

import chessGame.figures.*;
import chessGame.mechanics.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
public class BoardGrid implements Serializable {
    private GridPane gridPane;
    private Chess chess;
    private final Board board;
    private Map<Position, FigurePosition> positionMap = new TreeMap<>();

    public BoardGrid(GridPane gridPane, Board board) {
        this.gridPane = gridPane;
        this.board = board;

        buildBoard();
        initGridListener();
    }

    BoardGrid(Chess chess, Board board) {
        this.chess = chess;
        this.gridPane = chess.getBoardGrid();
        this.board = board;

        buildBoard();
        initGridListener();
    }

    public Board getBoard() {
        return board;
    }

    private void initGridListener() {
        gridPane.setOnMouseDragOver(event -> {
            final Object source = event.getGestureSource();
            if (source != null && source instanceof FigureView) {
                drag((FigureView) source, event);
            }
        });

        gridPane.setOnMouseDragExited(event -> {
            final Object source = event.getGestureSource();

            if (source instanceof FigureView) {
                final FigureView figure = (FigureView) source;

                final double eventX = event.getX();
                final double eventY = event.getY();

                if (eventX < 0 || eventY < 0 || gridPane.getWidth() < eventX || gridPane.getHeight() < eventY) {
                    setToOldPosition(figure);
                }
                figure.setManaged(true);
                figure.setMouseTransparent(false);
            }
        });

        board.atMoveProperty().addListener((observable, oldValue, newValue) -> chess.showPlayerAtMove(newValue));
    }

    private Map<Figure, FigureView> figureViewMap = new HashMap<>();

    public void buildBoard() {
        board.buildBoard();
        gridPane.getChildren().clear();
        figureViewMap.clear();
        positionMap.clear();

        setBorder(gridPane);

        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                final Position position = new Position(row, column);
                final ObjectProperty<Figure> property = board.figureObjectProperty(position);

                final FigurePosition figurePosition = getFigurePosition(position);
                positionMap.put(position, figurePosition);
                final Figure figure = board.getFigure(position);

                if (figure != null) {
                    final FigureView view = new FigureView(figure, this);
                    figureViewMap.put(figure, view);
                }

                figurePosition.figureViewProperty().bind(
                        Bindings.createObjectBinding(
                                () -> property.get() == null
                                        ? null
                                        : figureViewMap.get(property.get()), property)
                );
                gridPane.add(figurePosition, column, row);
            }
        }
    }

    public Collection<FigurePosition> getPositions() {
        return positionMap.values();
    }

    public FigureView getFigure(Figure figure) {
        return figureViewMap.get(figure);
    }

    public FigurePosition getPositionPane(Position position) {
        return positionMap.get(position);
    }

    public GridPane getGrid() {
        return gridPane;
    }

    public void drag(FigureView view, MouseEvent event) {
        final Cursor cursor = view.getCursor();
        view.saveCursor(cursor);
        view.setCursor(Cursor.CLOSED_HAND);

        final Parent parent = view.getParent();
        final double layoutX = parent.getLayoutX();
        final double layoutY = parent.getLayoutY();

        final double x = event.getSceneX();
        final double y = event.getSceneY();

        final double newX = x - layoutX - 20;
        final double newY = y - layoutY - 20;
        view.relocate(newX, newY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoardGrid boardGrid = (BoardGrid) o;

        return positionMap.equals(boardGrid.positionMap);
    }

    @Override
    public int hashCode() {
        return positionMap.hashCode();
    }

    private FigurePosition getFigurePosition(Position position) {
        final FigurePosition figurePosition = new FigurePosition(position, this);

        return figurePosition;
    }

    private void setBorder(GridPane gridPane) {
        for (int row = 1; row <= 8; row++) {
            gridPane.add(new Text("" + (row)), 0, row);
        }

        for (int column = 1; column <= 8; column++) {
            final String text = String.valueOf((char) (column - 1 + 'A'));
            gridPane.add(new Text(text), column, 0);
        }
    }

    private void setToOldPosition(FigureView figure) {
        final Position position = figure.getFigure().getPosition();
        final FigurePosition positionPane = getPositionPane(position);
        positionPane.addCurrent();
    }

    @Override
    public String toString() {
        return "BoardGrid{" +
                "board=" + board +
                ", positionMap=" + positionMap +
                ", figureViewMap=" + figureViewMap +
                '}';
    }
}

package chessGame.gui;

import chessGame.mechanics.*;
import chessGame.mechanics.figures.Figure;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
public class BoardGrid implements Serializable {
    private final MoveMaker moveMaker;
    private GridPane gridPane;
    private ChessGame chess;

    private final ObjectProperty<Board> board = new SimpleObjectProperty<>();
    private final ObjectProperty<FigurePosition> selectedPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<FigurePosition> chosenPosition = new SimpleObjectProperty<>();

    private Map<Position, FigurePosition> positionMap = new TreeMap<>();

    public BoardGrid(GridPane gridPane, Board board) {
        this.gridPane = gridPane;
        this.board.set(board);

        buildBoardGrid();
        initListener();
        moveMaker = new MoveMaker(this);
    }

    BoardGrid(ChessGame chess) {
        this.chess = chess;
        this.gridPane = chess.getBoardGrid();
        this.moveMaker = new MoveMaker(this);

        buildBoardGrid();
        initListener();
    }

    public Board getBoard() {
        return board.get();
    }

    private void initListener() {
        gridPane.setOnMouseDragOver(event -> {
            final Object source = event.getGestureSource();
            if (source != null && source instanceof FigureView) {
                drag((FigureView) source, event);
            }
        });

        gridPane.setOnMouseDragExited(event -> {
            final Object source = event.getGestureSource();

            if (source instanceof FigureView) {
                final FigureView figureView = (FigureView) source;

                final double eventX = event.getX();
                final double eventY = event.getY();

                if (eventX < 0 || eventY < 0 || gridPane.getWidth() < eventX || gridPane.getHeight() < eventY) {
                    setToOldPosition(figureView);
                }
                figureView.setDragging(false);
            }
        });

        gridPane.setOnMouseDragReleased(event -> {
            final Object source = event.getGestureSource();
            if (source instanceof FigureView) {
                setToOldPosition((FigureView) source);
            }
        });

        selectedPosition.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.setSelected(true);
            }

            if (oldValue != null) {
                oldValue.setSelected(false);
            }
        });

        chosenPosition.addListener((observable, oldValue, newValue) -> {
            if (oldValue == null && newValue != null) {
                if (newValue.getFigureView() != null && newValue.getFigureView().isActive()) {
                    newValue.setChosen(true);
                    RoundManager.showPositions(newValue.getFigureView().getFigure(), this);
                } else {
                    chosenPosition.set(null);
                }

            } else if (oldValue != null && newValue != null && oldValue != newValue) {
                if (newValue.getFigureView() != null && newValue.getFigureView().getFigure().getPlayer() == oldValue.getFigureView().getFigure().getPlayer()) {
                    oldValue.setChosen(false);
                    newValue.setChosen(true);
                } else {
                    newValue.moveTo(oldValue.getFigureView());

                    oldValue.setChosen(false);
                    newValue.setChosen(false);
                    chosenPosition.set(null);
                }

            } else if (oldValue != null) {
                oldValue.setChosen(false);
            }
        });

        board.addListener((observable, oldValue, newValue) -> {
            buildBoard(newValue);
            newValue.atMovePlayerProperty().addListener((observable1, oldValue1, playerAtMove) -> {
                if (chess != null && playerAtMove != null) {
                    chess.showPlayerAtMove(playerAtMove);
                }
            });
        });
    }

    ObjectProperty<Board> boardProperty() {
        return board;
    }

    ChessGame getChess() {
        return chess;
    }

    private void buildBoard(Board board) {
        figureViewMap.clear();
        positionMap.forEach(((position, figurePosition) -> {
            figurePosition.setFigure(null);

            final Figure figure = board.getFigure(position);

            if (figure != null) {
                final FigureView figureView = new FigureView(figure, this);
                figureViewMap.put(figure, figureView);
                figurePosition.setFigure(figureView);
            } else {
                figurePosition.setFigure(null);
            }
        }));

    }

    private Map<Figure, FigureView> figureViewMap = new HashMap<>();

    private void buildBoardGrid() {
        setBorder();

        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                final Position position = Position.get(row, column);

                final FigurePosition figurePosition = new FigurePosition(position, this);
                positionMap.put(position, figurePosition);
                gridPane.add(figurePosition, column, row);
            }
        }
    }

    public Collection<FigurePosition> getPositions() {
        return positionMap.values();
    }

    public FigureView getFigureView(Figure figure) {
        return figureViewMap.computeIfAbsent(figure, (k) -> {
            final FigureView figureView = new FigureView(k, this);
            final Position position = figure.getPosition();

            if (position.isInBoard()) {
                getPositionPane(position).setFigure(figureView);
                return figureView;
            } else {
                return null;
            }
        });
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

    @Override
    public String toString() {
        return "BoardGrid{" +
                "board=" + board +
                ", positionMap=" + positionMap +
                ", figureViewMap=" + figureViewMap +
                '}';
    }

    public void moveFocus(KeyEvent event) {
        final FigurePosition previousSelected = selectedPosition.get();
        if (previousSelected == null) {
            selectedPosition.set(getPositionPane(Position.get(1, 1)));


        } else {
            final Position previousSelectedPosition = previousSelected.getPosition();
            final int column = previousSelectedPosition.getColumn();
            final int row = previousSelectedPosition.getRow();

            if (event.getCode() == KeyCode.KP_LEFT || event.getCode() == KeyCode.LEFT) {
                if (column == 1) {
                    selectedPosition.set(getPositionPane(Position.get(row, 8)));
                } else {
                    selectedPosition.set(getPositionPane(Position.get(row, column - 1)));
                }
            } else if (event.getCode() == KeyCode.KP_RIGHT || event.getCode() == KeyCode.RIGHT) {
                if (column == 8) {
                    selectedPosition.set(getPositionPane(Position.get(row, 1)));

                } else {
                    selectedPosition.set(getPositionPane(Position.get(row, column + 1)));
                }
            } else if (event.getCode() == KeyCode.KP_UP || event.getCode() == KeyCode.UP) {
                if (row == 1) {
                    selectedPosition.set(getPositionPane(Position.get(8, column)));
                } else {
                    selectedPosition.set(getPositionPane(Position.get(row - 1, column)));
                }
            } else if (event.getCode() == KeyCode.KP_DOWN || event.getCode() == KeyCode.DOWN) {
                if (row == 8) {
                    selectedPosition.set(getPositionPane(Position.get(1, column)));

                } else {
                    selectedPosition.set(getPositionPane(Position.get(row + 1, column)));
                }
            }
        }
    }

    private void setBorder() {
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

    public void setChosenPosition(FigurePosition chosenPosition) {
        if (chosenPosition != null) {

            final FigurePosition previousChosen = this.chosenPosition.get();
            if (previousChosen != null) {
                this.chosenPosition.set(chosenPosition);

            } else if (chosenPosition.getFigureView() != null) {
                this.chosenPosition.set(chosenPosition);
            }
        } else {
            this.chosenPosition.set(null);
        }
    }

    void setSelectedPosition(FigurePosition selectedPosition) {
        this.selectedPosition.set(selectedPosition);
    }

    public void setChosen() {
        final FigurePosition figurePosition = selectedPosition.get();
        if (figurePosition == null) {
            selectedPosition.set(getPositionPane(Position.get(1, 1)));
            setChosenPosition(selectedPosition.get());
        } else if (figurePosition == chosenPosition.get()) {
            setChosenPosition(null);
        } else {
            setChosenPosition(figurePosition);
        }
    }

    public void setBoard(Board board) {
        this.board.set(board);
    }
}

package chessGame.gui;

import chessGame.mechanics.*;
import chessGame.mechanics.figures.Figure;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
 * Manages the Board at Gui Level.
 * Enables moving pieces per dragging, clicking and selecting per keyboard.
 */
class BoardGridManager implements Serializable {
    private final MoveMaker moveMaker;
    private final MoveShower moveShower;
    private GridPane gridPane;
    private ChessGame chess;

    private final ObjectProperty<Board> board = new SimpleObjectProperty<>();
    private final ObjectProperty<FigurePosition> selectedPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<FigurePosition> chosenPosition = new SimpleObjectProperty<>();

    private Map<Position, FigurePosition> positionMap = new TreeMap<>();
    private ChangeListener<Player> playerChangeListener;

    BoardGridManager(ChessGame chess) {
        this.chess = chess;
        this.gridPane = chess.getBoardGrid();

        this.moveMaker = new MoveMaker(this);
        this.moveShower = new MoveShower(this);

        buildBoardGrid();
        initListener();
    }

    Board getBoard() {
        return board.get();
    }

    Collection<FigurePosition> getFigurePositions() {
        return positionMap.values();
    }

    FigurePosition getChosenPosition() {
        return chosenPosition.get();
    }

    ObjectProperty<FigurePosition> chosenPositionProperty() {
        return chosenPosition;
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
                ((FigureView) source).setDragging(false);
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

        chosenPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null && newValue != null) {
                if (newValue.getFigureView() != null && newValue.getFigureView().isActive()) {
                    newValue.setChosen(true);
                } else {
                    chosenPositionProperty().set(null);
                }

            } else if (oldValue != null && newValue != null && oldValue != newValue) {
                if (newValue.getFigureView() != null && newValue.getFigureView().getFigure().getPlayer() == oldValue.getFigureView().getFigure().getPlayer()) {
                    oldValue.setChosen(false);
                    newValue.setChosen(true);
                } else {
                    newValue.moveTo(oldValue.getFigureView());

                    oldValue.setChosen(false);
                    newValue.setChosen(false);
                    chosenPositionProperty().set(null);
                }

            } else if (oldValue != null) {
                oldValue.setChosen(false);
            }
        });

        playerChangeListener = (observable1, playerNotAtMove, playerAtMove) -> {
            if (chess != null && playerAtMove != null) {
                chess.showPlayerAtMove(playerAtMove);
            }

            if (playerNotAtMove != null) {
                playerNotAtMove.getFigures().forEach(figure -> getFigureView(figure).setActive(false));
            }
            if (playerAtMove != null && playerAtMove.isHuman()) {
                playerAtMove.getFigures().forEach(figure -> getFigureView(figure).setActive(true));
            }
        };

        boardProperty().addListener((observable, oldValue, newValue) -> {
            buildBoard(newValue);
            newValue.atMovePlayerProperty().addListener(playerChangeListener);

            if (oldValue != null) {
                oldValue.atMovePlayerProperty().removeListener(playerChangeListener);
            }

        });
    }

    ObjectProperty<Board> boardProperty() {
        return board;
    }

    ChessGame getChess() {
        return chess;
    }

    private void buildBoard(Board board) {
        positionMap.values().forEach(FigurePosition::clear);
        figureViewMap.clear();

        positionMap.forEach(((position, figurePosition) -> {
            final Figure figure = board.getFigure(position);

            if (figure != null) {
                final FigureView figureView = new FigureView(figure, this);
                figureViewMap.put(figure, figureView);
                figurePosition.setFigure(figureView);
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

    FigureView getFigureView(Figure figure) {
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

    FigurePosition getPositionPane(Position position) {
        return positionMap.get(position);
    }

    GridPane getGrid() {
        return gridPane;
    }

    void drag(FigureView view, MouseEvent event) {
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

        BoardGridManager boardGrid = (BoardGridManager) o;

        return positionMap.equals(boardGrid.positionMap);
    }

    @Override
    public int hashCode() {
        return positionMap.hashCode();
    }

    @Override
    public String toString() {
        return "BoardGridManager{" +
                "board=" + board +
                ", positionMap=" + positionMap +
                ", figureViewMap=" + figureViewMap +
                '}';
    }

    void moveFocus(KeyEvent event) {
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

    void setChosenPosition(FigurePosition chosenPosition) {
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

    void setChosen() {
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

    void setBoard(Board board) {
        this.board.set(board);
    }
}

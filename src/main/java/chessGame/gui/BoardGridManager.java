package chessGame.gui;

import chessGame.mechanics.Board;
import chessGame.mechanics.Game;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;
import chessGame.mechanics.figures.Figure;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manages the Board at Gui Level.
 * Enables moving pieces per dragging, clicking and selecting per keyboard.
 */
class BoardGridManager implements Serializable {
    private final MoveAnimator moveAnimator;
    private final MoveShower moveShower;
    private final ObjectProperty<Game> game = new SimpleObjectProperty<>();
    private final ObjectProperty<FigurePosition> selectedPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<FigurePosition> chosenPosition = new SimpleObjectProperty<>();
    private GridPane gridPane;
    private ChessGameGui chess;
    private Map<Position, FigurePosition> positionMap = new TreeMap<>();
    private ChangeListener<Number> roundListener;
    private Map<Figure, FigureView> figureViewMap = new HashMap<>();

    BoardGridManager(ChessGameGui chess) {
        this.chess = chess;
        this.gridPane = chess.getBoardGrid();

        this.moveAnimator = new MoveAnimator(this);
        this.moveShower = new MoveShower(this);

        buildBoardGrid();
        initListener();
    }

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

    private void initListener() {
        gridPane.setOnMouseDragOver(this::dragFigureView);
        gridPane.setOnMouseDragExited(this::resetFigureViewDrag);
        gridPane.setOnMouseDragReleased(this::snapToOld);

        selectedPosition.addListener((observable, oldValue, newValue) -> processSelectionChange(oldValue, newValue));
        roundListener = (observable1, playerNotAtMove, playerAtMove) -> processRoundChange();

        chosenPositionProperty().addListener((observable, oldValue, newValue) -> processChosenChange(oldValue, newValue));
        gameProperty().addListener((observable, oldValue, newValue) -> processGameChange(oldValue, newValue));
    }

    private void processSelectionChange(FigurePosition oldValue, FigurePosition newValue) {
        if (newValue != null) {
            newValue.setSelected(true);
        }

        if (oldValue != null) {
            oldValue.setSelected(false);
        }
    }

    private void snapToOld(MouseDragEvent event) {
        final Object source = event.getGestureSource();
        if (source instanceof FigureView) {
            setToOldPosition((FigureView) source);
            ((FigureView) source).setDragging(false);
        }
    }

    private void resetFigureViewDrag(MouseDragEvent event) {
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
    }

    private void dragFigureView(MouseDragEvent event) {
        final Object source = event.getGestureSource();
        if (source != null && source instanceof FigureView) {
            drag((FigureView) source, event);
        }
    }

    private void processChosenChange(FigurePosition oldValue, FigurePosition newValue) {
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
    }

    private void processGameChange(Game oldValue, Game newValue) {
        buildBoard(newValue.getBoard());
        newValue.roundProperty().addListener(roundListener);

        if (oldValue != null) {
            oldValue.roundProperty().removeListener(roundListener);
        }
    }

    private void processRoundChange() {
        Player atMove = getGame().getAtMove();
        Player notAtMove = getGame().getEnemy(atMove);

        if (chess != null && atMove != null) {
            chess.showPlayerAtMove(atMove);
        }

        if (notAtMove != null) {
            gameProperty().get().getBoard().getFigures(notAtMove).forEach(figure -> getFigureView(figure).setActive(false));
        }
        if (atMove != null && atMove.isHuman()) {
            gameProperty().get().getBoard().getFigures(atMove).forEach(figure -> getFigureView(figure).setActive(true));
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

    private void setToOldPosition(FigureView figure) {
        final Position position = figure.getFigure().getPosition();
        final FigurePosition positionPane = getPositionPane(position);
        positionPane.addCurrent();
    }

    ObjectProperty<FigurePosition> chosenPositionProperty() {
        return chosenPosition;
    }

    ObjectProperty<Game> gameProperty() {
        return game;
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

    private void buildBoard(Board board) {
        positionMap.values().forEach(FigurePosition::clear);
        figureViewMap.clear();

        positionMap.forEach(((position, figurePosition) -> {
            final Figure figure = board.figureAt(position);

            if (figure != null) {
                final FigureView figureView = new FigureView(figure, this);
                figureViewMap.put(figure, figureView);
                figurePosition.setFigure(figureView);
            }
        }));
    }

    FigurePosition getPositionPane(Position position) {
        return positionMap.get(position);
    }

    @Override
    public int hashCode() {
        return positionMap.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoardGridManager boardGrid = (BoardGridManager) o;

        return positionMap.equals(boardGrid.positionMap);
    }

    @Override
    public String toString() {
        return "BoardGridManager{" +
                "game=" + game +
                ", positionMap=" + positionMap +
                ", figureViewMap=" + figureViewMap +
                '}';
    }

    Game getGame() {
        return game.get();
    }

    void setGame(Game game) {
        this.game.set(game);
    }

    Collection<FigurePosition> getFigurePositions() {
        return positionMap.values();
    }

    FigurePosition getChosenPosition() {
        return chosenPosition.get();
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

    ChessGameGui getChess() {
        return chess;
    }

    GridPane getGrid() {
        return gridPane;
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
}

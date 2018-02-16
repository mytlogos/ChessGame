package chessGame.gui;

import chessGame.mechanics.Color;
import chessGame.mechanics.Figure;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;
import chessGame.mechanics.board.Board;
import chessGame.mechanics.game.ChessGame;
import chessGame.multiplayer.MultiPlayerGame;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
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
 * Manages the Board<Figure> at Gui Level.
 * Enables moving pieces per dragging, clicking and selecting per keyboard.
 */
class BoardGridManager implements Serializable {
    private final MoveAnimator moveAnimator;
    private final MoveShower moveShower;

    private final ObjectProperty<ChessGame> game = new SimpleObjectProperty<>();
    private final ObjectProperty<BoardPanel> selectedPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<BoardPanel> chosenPosition = new SimpleObjectProperty<>();

    private final GridPane gridPane;
    private final ChessGameGui chess;
    private final Map<Position, BoardPanel> positionMap = new TreeMap<>();
    private final Map<Figure, FigureView> figureViewMap = new HashMap<>();
    private final ObjectProperty<SideOrientation> orientationProperty = new SimpleObjectProperty<>();

    private ChangeListener<Number> roundListener = (observable1, playerNotAtMove, playerAtMove) -> processRoundChange();
    private ChangeListener<Boolean> madeMoveListener = (observable1, oldMadeMove, newMadeMove) -> processBoardChange(newMadeMove);
    private Map<String, Text> descriptionMap = new HashMap<>();
    private VisualBoard visualBoard = null;

    BoardGridManager(ChessGameGui chess) {
        this.chess = chess;
        this.gridPane = chess.getBoardGrid();

        this.moveAnimator = new MoveAnimator(this);
        this.moveShower = new MoveShower(this);

        initListener();

        for (int i = 1; i <= 8; i++) {
            String rowText = "" + (i);
            Text rowNode = new Text(rowText);

            descriptionMap.put(rowText, rowNode);
            gridPane.getChildren().add(rowNode);

            final String columnText = String.valueOf((char) (i - 1 + 'A'));
            Text columnNode = new Text(columnText);

            descriptionMap.put(columnText, columnNode);
            gridPane.getChildren().add(columnNode);

            GridPane.setHalignment(rowNode, HPos.CENTER);
            GridPane.setHalignment(columnNode, HPos.CENTER);

            GridPane.setValignment(rowNode, VPos.CENTER);
            GridPane.setValignment(columnNode, VPos.CENTER);
        }

        orientationProperty.set(SideOrientation.UP);
    }

    private void initListener() {
        gridPane.setOnMouseDragOver(this::dragFigureView);
        gridPane.setOnMouseDragExited(this::resetFigureViewDrag);
        gridPane.setOnMouseDragReleased(this::snapToOld);

        selectedPosition.addListener((observable, oldValue, newValue) -> processSelectionChange(oldValue, newValue));
        orientationProperty.addListener((observable, oldValue, newValue) -> newValue.changeOrientation(this));

        chosenPositionProperty().addListener((observable, oldValue, newValue) -> processChosenChange(oldValue, newValue));
        gameProperty().addListener((observable, oldValue, newValue) -> processGameChange(oldValue, newValue));

        gridPane.prefWidthProperty().bindBidirectional(gridPane.prefHeightProperty());
    }

    private void processSelectionChange(BoardPanel oldValue, BoardPanel newValue) {
        if (newValue != null) {
            newValue.setSelected(true);
        }

        if (oldValue != null) {
            oldValue.setSelected(false);
        }
    }

    ObjectProperty<BoardPanel> chosenPositionProperty() {
        return chosenPosition;
    }

    private void processChosenChange(BoardPanel oldValue, BoardPanel newValue) {
        //if there is no current chosen
        if (oldValue == null && newValue != null) {
            //if new chosen is not empty and is active
            if (newValue.getFigureView() != null && newValue.getFigureView().isActive()) {
                newValue.setChosen(true);
            } else {
                chosenPositionProperty().set(null);
            }

            //if there is a previous chosen but not itself
        } else if (oldValue != null && newValue != null && oldValue != newValue) {

            //if new chosen is not empty and old chosen and new chosen are from same player, delegating the chosen status to new chosen
            if (newValue.getFigureView() != null && newValue.getFigureView().getFigure().isWhite() == oldValue.getFigureView().getFigure().isWhite()) {
                oldValue.setChosen(false);
                newValue.setChosen(true);
            } else {
                //if new chosen is from another player, triggering a moveTo on the new chosen
                newValue.moveTo(oldValue.getFigureView());

                oldValue.setChosen(false);
                newValue.setChosen(false);
                chosenPositionProperty().set(null);
            }
        } else if (oldValue != null) {
            oldValue.setChosen(false);
        }
    }

    ObjectProperty<ChessGame> gameProperty() {
        return game;
    }

    private void processGameChange(ChessGame oldValue, ChessGame newValue) {
        if (newValue != null) {
            buildBoard(newValue.getBoard());
            newValue.roundProperty().addListener(roundListener);
            newValue.madeMoveProperty().addListener(madeMoveListener);

            processBoardChange(true);
        }

        if (oldValue != null) {
            oldValue.roundProperty().removeListener(roundListener);
            oldValue.madeMoveProperty().removeListener(madeMoveListener);
        }
    }

    private void buildBoard(Board<Figure> board) {
        positionMap.values().forEach(BoardPanel::clear);
        figureViewMap.clear();

        positionMap.forEach(((position, boardPanel) -> {
            final Figure figure = board.figureAt(position);

            if (figure != null) {
                final FigureView figureView = new FigureView(figure, this);
                figureViewMap.put(figure, figureView);
                boardPanel.setFigure(figureView);
            }
        }));
        visualBoard = new VisualBoard(board, this);
        visualBoard.mirrorBoard();
    }

    private void processBoardChange(boolean madeMove) {
        if (madeMove) {
            moveShower.prepareMoves();
            moveAnimator.animateChange();
        }
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

    public VisualBoard getVisualBoard() {
        return visualBoard;
    }

    ObjectProperty<SideOrientation> orientationProperty() {
        return orientationProperty;
    }

    void addFigureView(FigureView view) {
        figureViewMap.put(view.getFigure(), view);
    }

    FigureView getFigureView(Position position) {
        return getPositionPane(position).getFigureView();
    }

    BoardPanel getPositionPane(Position position) {
        return positionMap.computeIfAbsent(position, this::createPanel);
    }

    Collection<BoardPanel> getFigurePositions() {
        return positionMap.values();
    }

    GridPane getGrid() {
        return gridPane;
    }

    void moveFocus(KeyEvent event) {
        final BoardPanel previousSelected = selectedPosition.get();
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

    void setSelectedPosition(BoardPanel selectedPosition) {
        this.selectedPosition.set(selectedPosition);
    }

    void setChosen() {
        final BoardPanel boardPanel = selectedPosition.get();
        if (boardPanel == null) {
            selectedPosition.set(getPositionPane(Position.get(1, 1)));
            setChosenPosition(selectedPosition.get());
        } else if (boardPanel == chosenPosition.get()) {
            setChosenPosition(null);
        } else {
            setChosenPosition(boardPanel);
        }
    }

    void setChosenPosition(BoardPanel chosenPosition) {
        if (chosenPosition != null) {

            final BoardPanel previousChosen = this.chosenPosition.get();
            if (previousChosen != null) {
                this.chosenPosition.set(chosenPosition);

            } else if (chosenPosition.getFigureView() != null) {
                this.chosenPosition.set(chosenPosition);
            }
        } else {
            this.chosenPosition.set(null);
        }
    }

    Text getBoardDescription(String s) {
        return descriptionMap.get(s);
    }

    private void processRoundChange() {
        Player atMove = getGame().getAtMove();
        Color notAtMove = Color.getEnemy(atMove.getColor());

        if (chess != null) {
            chess.showPlayerAtMove(atMove);
        }
        if (notAtMove != null) {
            gameProperty().get().getBoard().getFigures(notAtMove).forEach(figure -> getFigureView(figure).setActive(false));
        }

        if (getGame() instanceof MultiPlayerGame) {
            if (getChess().getClient().getPlayer().equals(atMove)) {
                gameProperty().get().getBoard().getFigures(atMove.getColor()).forEach(figure -> getFigureView(figure).setActive(true));
            }
        } else if (atMove.isHuman()) {
            gameProperty().get().getBoard().getFigures(atMove.getColor()).forEach(figure -> getFigureView(figure).setActive(true));
        }
    }

    ChessGame getGame() {
        return game.get();
    }

    FigureView getFigureView(Figure figure) {
        return figureViewMap.computeIfAbsent(figure, (k) -> {
            final FigureView figureView = new FigureView(k, this);
            Position position = getGame().getBoard().positionOf(k);

            if (position.isInBoard()) {
                getPositionPane(position).setFigure(figureView);
                return figureView;
            } else {
                return null;
            }
        });
    }

    ChessGameGui getChess() {
        return chess;
    }

    void setGame(ChessGame game) {
        this.game.set(game);
    }

    private void snapToOld(MouseDragEvent event) {
        final Object source = event.getGestureSource();
        if (source instanceof FigureView) {
            setToOldPosition((FigureView) source);
            ((FigureView) source).setDragging(false);
        }
    }

    private void setToOldPosition(FigureView view) {
        final Position position = getGame().getBoard().positionOf(view.getFigure());
        final BoardPanel positionPane = getPositionPane(position);
        positionPane.addCurrent();
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

    void drag(FigureView view, MouseEvent event) {
        final Cursor cursor = view.getCursor();
        view.saveCursor(cursor);
        view.setCursor(Cursor.CLOSED_HAND);

        final Parent parent = view.getParent();

        Bounds bounds = parent.localToScene(parent.getLayoutBounds());

        final double layoutX = bounds.getMinX();
        final double layoutY = bounds.getMinY();

        final double x = event.getSceneX();
        final double y = event.getSceneY();

        final double newX = x - layoutX - 20;
        final double newY = y - layoutY - 20;

        view.relocate(newX, newY);
    }

    private BoardPanel createPanel(Position k) {
        BoardPanel panel = new BoardPanel(k, this);
        panel.prefHeightProperty().bindBidirectional(panel.prefWidthProperty());
        NumberBinding min = Bindings.min(gridPane.widthProperty(), gridPane.heightProperty());
        panel.prefHeightProperty().bind(min.subtract(10).divide(8));
        gridPane.getChildren().add(panel);
        return panel;
    }
}

package chessGame.gui;


import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.Position;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 *
 */
public class FigureView extends ImageView {
    private final Figure figure;
    private final BoardGrid boardGrid;

    private final Cursor draggingCursor = Cursor.CLOSED_HAND;
    private final Cursor hoverCursor = Cursor.OPEN_HAND;


    private BooleanProperty active = new SimpleBooleanProperty();
    private final EventHandler<MouseEvent> onDragDetected;
    private Cursor savedCursor;
    private BooleanProperty dragging = new SimpleBooleanProperty();

    public FigureView(Figure figure, BoardGrid boardGrid) {
        this.figure = figure;
        this.boardGrid = boardGrid;

        setImage(figure.getImage());

        onDragDetected = event -> {
            setDragging(true);
            startFullDrag();

            if (!boardGrid.getGrid().getChildren().contains(this)) {
                boardGrid.getGrid().getChildren().add(this);
            }
            boardGrid.drag(this, event);
            boardGrid.setChosenPosition(boardGrid.getPositionPane(getPosition()));
            toFront();
        };
        initListener();
    }

    private void initListener() {
        hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && isActive()) {
                saveCursor(getCursor());
                setCursor(hoverCursor);
            } else {
                setCursor(getSavedCursor());
            }
        });

        activeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !getPosition().equals(Position.Bench)) {
                setOnDragDetected(onDragDetected);
            } else {
                setOnDragDetected(null);
            }
        });
        draggingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setMouseTransparent(true);
                setCursor(draggingCursor);
                setManaged(false);
            } else {
                setMouseTransparent(false);
                setCursor(getSavedCursor());
                setManaged(true);
            }
        });

        /*getFigure().positionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue != null && !isDragging()) {
                if (newValue != Position.Promoted) {
                    if (newValue != Position.Bench) {
                        final FigurePosition oldPane = boardGrid.getPositionPane(oldValue);
                        final FigurePosition newPane = boardGrid.getPositionPane(newValue);

                        //normal move, after this the next player is at move
                        final Transition transition = getTransition(this, oldPane, newPane, node -> {
                            oldPane.setFigure(null);
                            newPane.setFigure(this);
                            boardGrid.getBoard().atMoveFinished();
                        });
                        transition.play();
                    } else {

                        //benching move
                        final FigurePosition oldPane = boardGrid.getPositionPane(oldValue);
                        final Bench bench = boardGrid.getChess().getBench(getFigure().getPlayer());

                        final Transition transition = getTransition(this, oldPane, bench, node -> {
                            oldPane.setFigure(null);
                            boardGrid.getChess().showLostFigure(getFigure());
                        });
                        transition.play();
                    }
                } else {
                    final FigurePosition oldPane = boardGrid.getPositionPane(oldValue);
                    oldPane.setFigure(null);
                }
            }
        });*/
        boardGrid.getPositionPane(getPosition()).setFigure(this);
    }

    private Transition getTransition(Node node, Pane start, Pane goal, Consumer<Node> consumer) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);

        final Bounds local = start.localToScene(start.getBoundsInLocal());
        final Bounds bounds = goal.localToScene(goal.getBoundsInLocal());

        final double toX = bounds.getMinX() + (bounds.getWidth() / 2);
        final double fromX = local.getMinX() + (local.getWidth() / 2);
        final double translateX = toX - fromX;

        final double toY = bounds.getMinY() + (bounds.getHeight() / 2);
        final double fromY = local.getMinY() + (local.getHeight() / 2);
        final double translateY = toY - fromY - 5;

        transition.setToX(translateX);
        transition.setToY(translateY);

        //set moving "speed", with an base speed of 100 units per seconds or a max duration of 3 seconds
        final double xPow = Math.pow(translateX, 2);
        final double yPow = Math.pow(translateY, 2);
        final double distance = Math.sqrt(xPow + yPow);
        double duration = distance / 100d;
        duration = Math.min(duration, 3.0);
        transition.setDuration(Duration.seconds(duration));

        node.setTranslateY(-5);
        //important, else it will be hidden behind other positionPanes
        start.toFront();
        node.setEffect(new DropShadow(10, 0, 10, Color.BLACK));
        transition.setOnFinished(event -> {
            node.setEffect(null);
            consumer.accept(node);
            node.setManaged(true);

            node.setTranslateX(0);
            node.setTranslateY(0);
        });

        return transition;
    }

    @Override
    public void startFullDrag() {
        super.startFullDrag();
    }

    public Figure getFigure() {
        return figure;
    }

    public Position getPosition() {
        return figure.getPosition();
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setDragging(boolean dragging) {
        this.dragging.set(dragging);
    }

    public boolean isDragging() {
        return dragging.get();
    }

    public void saveCursor(Cursor cursor) {
        if (cursor != draggingCursor && cursor != hoverCursor) {
            savedCursor = cursor;
        }
    }

    public Cursor getSavedCursor() {
        return savedCursor;
    }

    @Override
    public String toString() {
        return "FigureView{" +
                "figure=" + figure +
                ", active=" + active.get() +
                ", dragging=" + dragging.get() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FigureView that = (FigureView) o;

        return getFigure().equals(that.getFigure());
    }

    @Override
    public int hashCode() {
        return getFigure().hashCode();
    }

    private BooleanProperty draggingProperty() {
        return dragging;
    }
}

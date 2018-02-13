package chessGame.gui;


import chessGame.mechanics.Position;
import chessGame.mechanics.Figure;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 *
 */
class FigureView extends ImageView {
    private final Figure figure;
    private final BoardGridManager boardGrid;

    private final Cursor draggingCursor = Cursor.CLOSED_HAND;
    private final Cursor hoverCursor = Cursor.OPEN_HAND;
    private final EventHandler<MouseEvent> onDragDetected;
    private final BooleanProperty active = new SimpleBooleanProperty();
    private Cursor savedCursor;
    private final BooleanProperty dragging = new SimpleBooleanProperty();

    FigureView(Figure figure, BoardGridManager boardGrid) {
        this.figure = figure;
        this.boardGrid = boardGrid;

        setImage(figure.getImage());
        setPreserveRatio(true);

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

    Position getPosition() {
        return boardGrid.getGame().getBoard().positionOf(figure);
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
                boardGrid.setChosenPosition(null);
                boardGrid.setChosenPosition(boardGrid.getPositionPane(getPosition()));

                setEffect(new DropShadow(10, 0, 10, Color.BLACK));
                setMouseTransparent(true);
                setCursor(draggingCursor);
                setManaged(false);
            } else {
                setMouseTransparent(false);
                setCursor(getSavedCursor());
                setManaged(true);
                setEffect(null);
            }
        });
    }

    boolean isActive() {
        return active.get();
    }

    void setActive(boolean active) {
        this.active.set(active);
    }

    void saveCursor(Cursor cursor) {
        if (cursor != draggingCursor && cursor != hoverCursor) {
            savedCursor = cursor;
        }
    }

    private Cursor getSavedCursor() {
        return savedCursor;
    }

    BooleanProperty activeProperty() {
        return active;
    }

    private BooleanProperty draggingProperty() {
        return dragging;
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
    public int hashCode() {
        return getFigure().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FigureView that = (FigureView) o;
        return getFigure().equals(that.getFigure());
    }

    Figure getFigure() {
        return figure;
    }

    boolean isDragging() {
        return dragging.get();
    }

    void setDragging(boolean dragging) {
        this.dragging.set(dragging);
    }
}

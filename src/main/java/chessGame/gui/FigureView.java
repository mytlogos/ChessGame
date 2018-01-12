package chessGame.gui;


import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.Position;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
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


    private BooleanProperty active = new SimpleBooleanProperty();
    private final EventHandler<MouseEvent> onDragDetected;
    private Cursor savedCursor;
    private BooleanProperty dragging = new SimpleBooleanProperty();

    FigureView(Figure figure, BoardGridManager boardGrid) {
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

    Figure getFigure() {
        return figure;
    }

    Position getPosition() {
        return figure.getPosition();
    }

    void setActive(boolean active) {
        this.active.set(active);
    }

    boolean isActive() {
        return active.get();
    }

    BooleanProperty activeProperty() {
        return active;
    }

    void setDragging(boolean dragging) {
        this.dragging.set(dragging);
    }

    boolean isDragging() {
        return dragging.get();
    }

    void saveCursor(Cursor cursor) {
        if (cursor != draggingCursor && cursor != hoverCursor) {
            savedCursor = cursor;
        }
    }

    private Cursor getSavedCursor() {
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

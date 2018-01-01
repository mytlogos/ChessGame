package chessGame.gui;


import chessGame.figures.Figure;
import chessGame.mechanics.Position;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

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
            toFront();
        };
        hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && isActive()) {
                saveCursor(getCursor());
                setCursor(hoverCursor);
            } else {
                setCursor(getSavedCursor());
            }
        });

        activeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
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

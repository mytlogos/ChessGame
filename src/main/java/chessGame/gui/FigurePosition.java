package chessGame.gui;

import chessGame.mechanics.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.Serializable;

/**
 *
 */
public class FigurePosition extends StackPane implements Serializable {
    private final Position position;
    private final BoardGrid boardGrid;
    private final Color color;

    private BooleanProperty enemy = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(ENEMY_PSEUDO_CLASS, get());
        }
    };

    private BooleanProperty empty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, get());
        }
    };


    private BooleanProperty hazard = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(HAZARD_PSEUDO_CLASS, get());
        }
    };

    private final static PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");
    private final static PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private final static PseudoClass ENEMY_PSEUDO_CLASS = PseudoClass.getPseudoClass("enemy");
    private final static PseudoClass HAZARD_PSEUDO_CLASS = PseudoClass.getPseudoClass("hazard");
    private ObjectProperty<FigureView> figureViewObjectProperty = new SimpleObjectProperty<>();

    public FigurePosition(Position position, BoardGrid boardGrid) {
        this.position = position;
        this.boardGrid = boardGrid;

        setPrefSize(100, 100);
        setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        setMinSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

        if (position.getRow() % 2 == 1) {
            if (position.getColumn() % 2 == 1) {
                this.color = Color.Black;
            } else {
                this.color = Color.White;
            }
        } else {
            if (position.getColumn() % 2 == 0) {
                this.color = Color.Black;
            } else {
                this.color = Color.White;
            }
        }
        color.setStyleClass(this);
        final Text e = new Text();
        e.toBack();
        figureViewProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                e.setText("besetzt");
            } else {
                e.setText("frei");
            }
        });
        getChildren().add(e);

        initListener();
        initHandler();
    }

    private void initHandler() {
        setOnMouseDragReleased(event -> {
            final FigureView gestureSource = (FigureView) event.getGestureSource();
            final FigureView previousView = figureViewProperty().get();

            if (!gestureSource.equals(previousView)) {
                final Position to = getPosition();
                final PositionChange currentChange = new PositionChange(gestureSource.getPosition(), to);

                final Move second;

                if (previousView != null) {
                    final PositionChange previousChange = new PositionChange(previousView.getPosition(), to);
                    second = new Move(previousView.getFigure(), previousChange);
                } else {
                    second = null;
                }

                final Move move = new Move(gestureSource.getFigure(), currentChange);
                try {
                    boardGrid.getBoard().makeMove(new PlayerMove(move, second));
                } catch (IllegalMoveException ignored) {
                    //todo for now ignore it
                }
                setEffect(null);
            } else {
                if (!getChildren().contains(gestureSource)) {
                    getChildren().add(gestureSource);
                }
            }

            gestureSource.setDragging(false);
            boardGrid.getGrid().getChildren().remove(gestureSource);
        });
    }

    private void initListener() {
        figureViewProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                getChildren().remove(oldValue);
            }

            if (newValue != null) {
                getChildren().add(newValue);
            }
        });

        hoverProperty().addListener((observable, oldValue, newValue) -> {
            final FigureView paneFigure = getFigure();

            final Parent parent = getParent();

            if (parent instanceof GridPane && paneFigure != null) {
                if (newValue) {
                    RoundManager.showPositions(paneFigure.getFigure(), boardGrid);
                } else if (!paneFigure.isDragging()) {
                    RoundManager.disableEffects(paneFigure.getFigure(), boardGrid);
                }
            }
        });
    }

    public ObjectProperty<FigureView> figureViewProperty() {
        return figureViewObjectProperty;
    }

    public void setFigure(FigureView figure) {
        FigureView old = figureViewObjectProperty.get();

        if (old != null && figure != null && old != figure && old.getFigure().getPlayer() == figure.getFigure().getPlayer()) {
            throw new IllegalArgumentException();
        }
    }

    public void setEnemy() {
        resetEffect();
        this.enemy.set(true);
    }

    public void setEmpty() {
        resetEffect();
        this.empty.set(true);
    }

    public void setHazard() {
        resetEffect();
        this.hazard.set(true);
    }

    public void resetEffect() {
        this.enemy.set(false);
        this.empty.set(false);
        this.hazard.set(false);
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public FigureView getFigure() {
        return figureViewProperty().get();
    }

    @Override
    public String toString() {
        return "FigurePosition{" +
                "position=" + position +
                ", figure=" + figureViewProperty().get() +
                '}';
    }

    public void addCurrent() {
        if (!getChildren().contains(getFigure())) {
            getChildren().add(getFigure());
        }
    }

    public enum Color {
        Black("board-tile-black"),
        White("board-tile-white"),;

        private final String styleClass;

        Color(String styleClass) {
            this.styleClass = styleClass;
        }

        void setStyleClass(FigurePosition position) {
            position.getStyleClass().add(styleClass);
        }
    }

}

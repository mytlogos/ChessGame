package chessGame.gui;

import chessGame.mechanics.*;
import chessGame.mechanics.figures.Figure;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 */
public class FigurePosition extends StackPane implements Serializable {
    private final Position position;
    private final BoardGrid boardGrid;

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


    private BooleanProperty selected = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
        }
    };


    private BooleanProperty chosen = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(CHOSEN_PSEUDO_CLASS, get());
        }
    };

    private final static PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");
    private final static PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private final static PseudoClass ENEMY_PSEUDO_CLASS = PseudoClass.getPseudoClass("enemy");
    private final static PseudoClass HAZARD_PSEUDO_CLASS = PseudoClass.getPseudoClass("hazard");
    private final static PseudoClass CHOSEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("chosen");

    private ObjectProperty<FigureView> figureViewObjectProperty = new SimpleObjectProperty<>();

    public FigurePosition(Position position, BoardGrid boardGrid) {
        Objects.requireNonNull(position);
        Objects.requireNonNull(boardGrid);

        this.position = position;
        this.boardGrid = boardGrid;

        setPrefSize(100, 100);
        setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        setMinSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

        setColor(position);

        initListener();
        initHandler();
    }

    void moveTo(FigureView newFigureView) {
        final FigureView previousView = figureViewProperty().get();

        if (!newFigureView.equals(previousView)) {
            final Figure figure = newFigureView.getFigure();

            if (figure != null) {
                RoundManager.disableEffects(figure, boardGrid);
            }

            final Position to = getPosition();
            final PositionChange currentChange = new PositionChange(newFigureView.getPosition(), to);

            Move second = null;

            if (previousView != null) {
                if (previousView.getFigure().getPlayer().equals(newFigureView.getFigure().getPlayer())) {
                    boardGrid.getPositionPane(newFigureView.getPosition()).addCurrent();
                } else {
                    final PositionChange previousChange = new PositionChange(previousView.getPosition(), Position.Bench);
                    second = new Move(previousView.getFigure(), previousChange);
                }
            }

            final Move move = new Move(newFigureView.getFigure(), currentChange);
            try {
                boardGrid.getBoard().makeMove(new PlayerMove(move, second));
                boardGrid.setChosenPosition(null);
            } catch (IllegalMoveException ignored) {
                System.out.println("illegal");
                boardGrid.getPositionPane(newFigureView.getPosition()).addCurrent();
                //todo make a popup depicting the infringement of rules
            }
        } else {
            addCurrent();
        }
    }

    private void setColor(Position position) {
        if (position.getRow() % 2 == 1) {
            if (position.getColumn() % 2 == 1) {
                getStyleClass().add("board-tile-black");
            } else {
                getStyleClass().add("board-tile-white");
            }
        } else {
            if (position.getColumn() % 2 == 0) {
                getStyleClass().add("board-tile-black");
            } else {
                getStyleClass().add("board-tile-white");
            }
        }
    }

    private void initHandler() {
        setOnMouseClicked(event -> {
            boardGrid.setSelectedPosition(this);

            if (isChosen()) {
                boardGrid.setChosenPosition(null);
            } else {
                if (getFigureView() != null) {
                    if (getFigureView().isActive()) {
                        boardGrid.setChosenPosition(this);
                    }
                } else {
                    boardGrid.setChosenPosition(this);
                }
            }
        });

        setOnMouseDragReleased(event -> {
            final FigureView gestureSource = (FigureView) event.getGestureSource();
            moveTo(gestureSource);

            gestureSource.setDragging(false);
            boardGrid.getBoard().atMoveFinished();
            boardGrid.getGrid().getChildren().remove(gestureSource);
            event.consume();
        });
    }

    private void initListener() {
        figureViewProperty().addListener((observable, oldFigure, newFigure) -> {
            System.out.println("old: " + oldFigure + " new " + newFigure);
            if (oldFigure != null) {
                getChildren().remove(oldFigure);
            }

            if (newFigure != null) {
                getChildren().add(newFigure);
                newFigure.activeProperty().addListener((observable1, oldValue1, newValue1) -> {
                    if (!newValue1) {
                        selected.set(false);
                    }
                });
            }
        });

        hoverProperty().addListener((observable, oldValue, newValue) -> {
            final FigureView paneFigure = getFigureView();

            if (paneFigure != null) {
                if (newValue) {
                    RoundManager.showPositions(paneFigure.getFigure(), boardGrid);
                } else if (!paneFigure.isDragging() && !isChosen()) {
                    RoundManager.disableEffects(paneFigure.getFigure(), boardGrid);
                }
            }
        });

        chosenProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                RoundManager.showPositions(getFigureView().getFigure(), boardGrid);
            } else if (getFigureView() != null){
                RoundManager.disableEffects(getFigureView().getFigure(), boardGrid);
            }
        });
    }

    public boolean isChosen() {
        return chosen.get();
    }

    public BooleanProperty chosenProperty() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen.set(chosen);
    }

    public ObjectProperty<FigureView> figureViewProperty() {
        return figureViewObjectProperty;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setFigure(FigureView figure) {
        FigureView old = getFigureView();

        if (old != null && figure != null && old != figure && old.getFigure().getPlayer() == figure.getFigure().getPlayer()) {
            throw new IllegalArgumentException();
        }
        figureViewProperty().set(figure);
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
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
        this.hazard.set(true);
    }

    public void resetEffect() {
        this.enemy.set(false);
        this.empty.set(false);
        this.hazard.set(false);
    }

    public Position getPosition() {
        return position;
    }

    public FigureView getFigureView() {
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
        if (!getChildren().contains(getFigureView())) {
            getChildren().add(getFigureView());
        }
    }
}

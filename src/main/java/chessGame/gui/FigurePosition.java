package chessGame.gui;

import chessGame.mechanics.*;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
class FigurePosition extends StackPane implements Serializable {
    private final Position position;
    private final BoardGridManager boardGrid;

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

    private Map<Figure, PlayerMove> acceptableMoves = new HashMap<>();

    private PlayerMove castling;
    private List<PlayerMove> promotions = new ArrayList<>();
    private ObjectProperty<Figure> showOff = new SimpleObjectProperty<>();

    FigurePosition(Position position, BoardGridManager boardGrid) {
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

    Figure getShowOff() {
        return showOff.get();
    }

    ObjectProperty<Figure> showOffProperty() {
        return showOff;
    }

    void setShowOff(Figure showOff) {
        if (showOff != null && (acceptableMoves.containsKey(showOff) || (showOff.getType() == FigureType.PAWN && !promotions.isEmpty()) || (showOff.getType() == FigureType.KING && castling != null))) {
            this.showOff.set(showOff);
        } else {
            this.showOff.set(null);
        }
    }

    void moveTo(FigureView newFigureView) {
        final FigureView previousView = figureViewProperty().get();

        if (!newFigureView.equals(previousView)) {
            final Figure figure = newFigureView.getFigure();

            PlayerMove move;

            if (figure.getType() == FigureType.PAWN && !promotions.isEmpty()) {
                //check user for promotion
                PromotionDialog dialog = new PromotionDialog(promotions);
                final Optional<PlayerMove> optional = dialog.showAndWait();

                //if promotion declined, try normal pawn move
                move = optional.orElse(acceptableMoves.get(figure));

            } else if (figure.getType() == FigureType.KING && castling != null) {
                //check user for castling
                move = castling;
            } else {
                move = acceptableMoves.get(figure);
            }

            //important to call this before making move, else figure will not be removed from old position
            final Position position = newFigureView.getPosition();
            try {
                if (move != null) {
                    boardGrid.getBoard().makeMove(move);

                    if (newFigureView.isDragging()) {
                        boardGrid.getPositionPane(position).setFigure(null);
                        boardGrid.setChosenPosition(null);
                        setFigure(newFigureView);
                    }
                } else {
                    boardGrid.getPositionPane(position).addCurrent();
                }
            } catch (IllegalMoveException ignored) {
                System.out.println("illegal");
                boardGrid.getPositionPane(position).addCurrent();
                //todo make a popup depicting the infringement of rules
            }
        } else {
            addCurrent();
        }
    }

    void setCastling(PlayerMove castling) {
        this.castling = castling;
    }

    void setPromotion(List<PlayerMove> promotions) {
        this.promotions.addAll(promotions);
    }

    void addAcceptableMove(Figure figure, PlayerMove move) {
        acceptableMoves.put(figure, move);
    }

    void addAcceptableMove(Map<Figure, PlayerMove> move) {
        acceptableMoves.putAll(move);
    }

    void clearAcceptableMoves() {
        castling = null;
        promotions.clear();
        acceptableMoves.clear();
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
            boardGrid.getGrid().getChildren().remove(gestureSource);
            event.consume();
        });
    }

    private void initListener() {
        figureViewProperty().addListener((observable, oldFigure, newFigure) -> {
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
                    boardGrid.getFigurePositions().forEach(figurePosition -> figurePosition.setShowOff(paneFigure.getFigure()));
                } else if (!paneFigure.isDragging() && !isChosen()) {
                    boardGrid.getFigurePositions().forEach(figurePosition -> figurePosition.setShowOff(null));
                }
            }
        });

        showOffProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                setEmpty();
                //todo check for type of move
            } else {
                resetEffect();
            }
        });
    }

    private boolean isChosen() {
        return chosen.get();
    }

    private BooleanProperty chosenProperty() {
        return chosen;
    }

    void setChosen(boolean chosen) {
        this.chosen.set(chosen);
    }

    private ObjectProperty<FigureView> figureViewProperty() {
        return figureViewObjectProperty;
    }

    boolean isSelected() {
        return selected.get();
    }

    BooleanProperty selectedProperty() {
        return selected;
    }

    void setFigure(FigureView figure) {
        figureViewProperty().set(figure);
    }

    void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    void setEnemy() {
        resetEffect();
        this.enemy.set(true);
    }

    void setEmpty() {
        resetEffect();
        this.empty.set(true);
    }

    void setHazard() {
        this.hazard.set(true);
    }

    void resetEffect() {
        this.enemy.set(false);
        this.empty.set(false);
        this.hazard.set(false);
    }

    Position getPosition() {
        return position;
    }

    FigureView getFigureView() {
        return figureViewProperty().get();
    }

    @Override
    public String toString() {
        return "FigurePosition{" +
                "position=" + position +
                ", figure=" + figureViewProperty().get() +
                '}';
    }

    void addCurrent() {
        if (!getChildren().contains(getFigureView())) {
            getChildren().add(getFigureView());
        }
    }

    public void clear() {
        getChildren().clear();
        setFigure(null);
    }
}

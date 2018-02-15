package chessGame.gui;

import chessGame.mechanics.Figure;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;
import chessGame.mechanics.move.PlayerMove;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.layout.StackPane;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
class BoardPanel extends StackPane implements Serializable {
    private final static PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");
    private final static PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private final static PseudoClass ENEMY_PSEUDO_CLASS = PseudoClass.getPseudoClass("enemy");
    private final static PseudoClass HAZARD_PSEUDO_CLASS = PseudoClass.getPseudoClass("hazard");
    private final static PseudoClass CHOSEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("chosen");


    private final BooleanProperty enemy = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(ENEMY_PSEUDO_CLASS, get());
        }
    };
    private final BooleanProperty empty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, get());
        }
    };
    private final BooleanProperty hazard = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(HAZARD_PSEUDO_CLASS, get());
        }
    };
    private final BooleanProperty selected = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
        }
    };
    private final BooleanProperty chosen = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(CHOSEN_PSEUDO_CLASS, get());
        }
    };
    private final ObjectProperty<FigureView> figureViewObjectProperty = new SimpleObjectProperty<>();

    private final Position position;
    private final BoardGridManager boardGrid;

    private final Map<Figure, PlayerMove> acceptableMoves = new HashMap<>();
    private final List<PlayerMove> promotions = new ArrayList<>();
    private final List<Figure> promoteAblePawns = new ArrayList<>();
    private final ObjectProperty<Figure> showOff = new SimpleObjectProperty<>();
    private PlayerMove castling;

    BoardPanel(Position position, BoardGridManager boardGrid) {
        Objects.requireNonNull(position);
        Objects.requireNonNull(boardGrid);

        this.position = position;
        this.boardGrid = boardGrid;
        setColor(position);

        initListener();
        initHandler();
    }

    @Override
    public String toString() {
        return "BoardPanel{" +
                "position=" + position +
                ", figure=" + figureViewProperty().get() +
                '}';
    }

    private ObjectProperty<FigureView> figureViewProperty() {
        return figureViewObjectProperty;
    }

    public void clear() {
        getChildren().clear();
        setFigureEmpty();
    }

    void setFigureEmpty() {
        FigureView figureView = getFigureView();
        if (figureView != null) {
            getChildren().remove(figureView);
        }
        figureViewProperty().set(null);
    }

    void setFigure(FigureView figure) {
        Objects.requireNonNull(figure);
        figureViewProperty().set(figure);
    }

    Figure getShowOff() {
        return showOff.get();
    }

    void setShowOff(Figure showOff) {
        if (showOff != null && isAcceptable(showOff)) {
            this.showOff.set(showOff);
        } else {
            this.showOff.set(null);
        }
    }

    private boolean isAcceptable(Figure showOff) {
        //if figure is part of the normal acceptable mainMoves
        return acceptableMoves.containsKey(showOff) ||
                //if pawn is promotable
                (showOff.is(FigureType.PAWN) && !promotions.isEmpty() && promotions.get(0).getColor().equals(showOff.getColor())) && promoteAblePawns.contains(showOff) ||
                //if king is castleAble
                (showOff.is(FigureType.KING) && castling != null && castling.getColor().equals(showOff.getColor()));
    }

    void moveTo(FigureView newFigureView) {
        final FigureView previousView = figureViewProperty().get();

        if (!newFigureView.equals(previousView)) {
            final Figure figure = newFigureView.getFigure();

            PlayerMove move;

            if (figure.is(FigureType.PAWN) && !promotions.isEmpty()) {
                //remove newFigureView from grid to prevent unwanted shifts of the board
                boardGrid.getGrid().getChildren().remove(newFigureView);

                //save dragging value, because opening the dialog will reset the dragging value as the mouse leaves the grid
                boolean dragging = newFigureView.isDragging();

                //check user for promotion
                PromotionDialog dialog = new PromotionDialog(promotions);
                final Optional<PlayerMove> optional = dialog.showAndWait();

                //if promotion declined, try normal pawn move
                move = optional.orElse(acceptableMoves.get(figure));

                newFigureView.setDragging(dragging);
            } else if (figure.is(FigureType.KING) && castling != null) {
                //check user for castling
                move = castling;
            } else {
                move = acceptableMoves.get(figure);
            }

            //important to call this before making move, else figure will not be removed from old position
            final Position position = newFigureView.getPosition();
            if (move != null) {
                boardGrid.getGame().makeMove(move);

                if (newFigureView.isDragging() && !move.isPromotion()) {
                    boardGrid.getPositionPane(position).setFigureEmpty();
                    setFigure(newFigureView);
                }
                boardGrid.setChosenPosition(null);
            } else {
                boardGrid.getPositionPane(position).addCurrent();
            }
        } else {
            addCurrent();
        }
    }

    void setCastling(PlayerMove castling) {
        this.castling = castling;
    }

    void setPromotion(List<PlayerMove> promotions, Figure figure) {
        this.promotions.addAll(promotions);
        this.promoteAblePawns.add(figure);
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
        promoteAblePawns.clear();
        acceptableMoves.clear();
    }

    boolean isSelected() {
        return selected.get();
    }

    void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    BooleanProperty selectedProperty() {
        return selected;
    }

    void setEnemy() {
        resetEffect();
        this.enemy.set(true);
    }

    void resetEffect() {
        this.enemy.set(false);
        this.empty.set(false);
        this.hazard.set(false);
    }

    void setEmpty() {
        resetEffect();
        this.empty.set(true);
    }

    void setHazard() {
        this.hazard.set(true);
    }

    Position getPosition() {
        return position;
    }

    FigureView getFigureView() {
        return figureViewProperty().get();
    }

    void addCurrent() {
        if (!getChildren().contains(getFigureView())) {
            getChildren().add(getFigureView());
        }
    }

    private ObjectProperty<Figure> showOffProperty() {
        return showOff;
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

                if (!getChildren().contains(newFigure)) {
                    getChildren().add(newFigure);
                }

                if (oldFigure != null) {
                    oldFigure.fitHeightProperty().unbind();
                }
                newFigure.fitHeightProperty().bind(heightProperty().subtract(heightProperty().divide(5)));

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

    void setChosen(boolean chosen) {
        this.chosen.set(chosen);
    }

    private BooleanProperty chosenProperty() {
        return chosen;
    }
}

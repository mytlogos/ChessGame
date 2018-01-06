package chessGame.gui;

import chessGame.mechanics.*;
import chessGame.mechanics.figures.Figure;
import javafx.animation.*;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Coordinates the {@link PlayerMove} at the gui layer.
 */
class MoveMaker {
    private final BoardGrid grid;
    private final ChangeListener<PlayerMove> lastMoveListener = getLastMoveListener();

    MoveMaker(BoardGrid grid) {
        this.grid = grid;

        grid.boardProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.lastMoveProperty().addListener(lastMoveListener);
            }
            if (oldValue != null) {
                //to prevent the old board from affecting the gui just in case
                oldValue.lastMoveProperty().removeListener(lastMoveListener);
            }
        });
    }

    private ChangeListener<PlayerMove> getLastMoveListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                makeMove(newValue);
            }
        };
    }

    private void makeMove(PlayerMove move) {
        if (move.isPromotion()) {
            promote(move);
        } else if (move.isCastlingMove()) {
            castle(move);
        } else {
            makeNormalMove(move);
        }
    }

    private void makeNormalMove(PlayerMove move) {
        final Move secondaryMove = move.getSecondaryMove();

        final Move mainMove = move.getMainMove();

        final Transition mainTransition = getNormalTransition(mainMove);

        final Transition secondaryTransition = doStrikeMove(secondaryMove, mainMove, mainTransition.getTotalDuration());

        final FigurePosition currentPane = grid.getPositionPane(mainMove.getChange().getFrom());
        currentPane.toFront();


        ParallelTransition transition;
        if (secondaryTransition != null) {
            transition = new ParallelTransition(mainTransition, secondaryTransition);
        } else {
            transition = new ParallelTransition(mainTransition);
        }
        transition.setOnFinished(event -> grid.getBoard().atMoveFinished());
        transition.play();
    }

    private void castle(PlayerMove move) {
        if (move.getSecondaryMove() == null) {
            throw new NullPointerException("sekundärer zug für Rochade ist null");
        }

        final Move secondaryMove = move.getSecondaryMove();
        final Move mainMove = move.getMainMove();

        if (!mainMove.getFigure().getPlayer().equals(secondaryMove.getFigure().getPlayer())) {
            throw new IllegalStateException("rochade von figuren unterschiedlicher spieler ist nicht erlaubt");
        }


        BooleanProperty main = new SimpleBooleanProperty();
        BooleanProperty secondary = new SimpleBooleanProperty();

        final Transition mainTransition = getNormalTransition(mainMove);
        final Transition secondaryTransition = getNormalTransition(secondaryMove);

        finish(main, secondary);

        ParallelTransition transition = new ParallelTransition(mainTransition, secondaryTransition);
        transition.play();
    }

    private void promote(PlayerMove move) {
        final Move mainMove = move.getMainMove();

        final Figure figure = mainMove.getFigure();
        final PositionChange change = mainMove.getChange();

        final FigurePosition currentPane = grid.getPositionPane(change.getFrom());
        final FigurePosition nextPane = grid.getPositionPane(change.getTo());

        final FigureView figureView = grid.getFigureView(figure);

        if (!figureView.equals(currentPane.getFigureView())) {
            throw new IllegalStateException("board ist nicht synchron");
        }

        final Move promotionMove = move.getPromotionMove();

        if (promotionMove == null || !promotionMove.getChange().getTo().equals(mainMove.getChange().getTo())) {
            throw new IllegalStateException("promotionMove ist null oder hat nicht dasselbe Ziel wie der Hauptzug!");
        }

        final FigureView promotedView = grid.getFigureView(promotionMove.getFigure());

        final Transition mainTransition = getTransition(figureView, currentPane, nextPane, node -> {
            currentPane.setFigure(null);
            nextPane.setFigure(promotedView);
        });

        final Transition strikeTransition = doStrikeMove(move.getSecondaryMove(), mainMove, mainTransition.getTotalDuration());

        ParallelTransition transition;
        if (strikeTransition != null) {
            transition = new ParallelTransition(mainTransition, strikeTransition);
        } else {
            transition = new ParallelTransition(mainTransition);
        }
        transition.setOnFinished(event -> grid.getBoard().atMoveFinished());
        transition.play();
    }

    private Transition doStrikeMove(Move strikeMove, Move mainMove, Duration totalDuration) {
        //check for benching move
        if (strikeMove != null) {
            final Figure strikedFigure = strikeMove.getFigure();
            final Player atMovePlayer = mainMove.getFigure().getPlayer();

            if (!strikedFigure.getPlayer().equals(atMovePlayer)) {
                if (!strikeMove.getChange().getTo().equals(Position.Bench)) {
                    throw new IllegalStateException("Sekundärzug ist kein Zug wo eine Figur geschlagen wird, obwohl es als normal gekennzeichnet ist");
                }

                final FigureView secondaryFigureView = grid.getFigureView(strikedFigure);


                final FigurePosition currentPosition = grid.getPositionPane(strikeMove.getChange().getFrom());
                final LostFigureItem bench = grid.getChess().getBench(atMovePlayer).getContainer(strikedFigure.getType());

                if (!secondaryFigureView.equals(currentPosition.getFigureView())) {
                    throw new IllegalStateException("board ist nicht synchron");
                }

                final Transition secondaryTransition = getTransition(secondaryFigureView, currentPosition, bench,
                        node -> {
                            bench.increment();
                            currentPosition.getChildren().remove(secondaryFigureView);
                        });

                secondaryFigureView.setEffect(null);

                final Duration duration = totalDuration.subtract(Duration.seconds(0.3));
                final PauseTransition pauseTransition = new PauseTransition(duration);
                pauseTransition.setOnFinished(event -> {
                    currentPosition.toFront();
                    secondaryFigureView.setEffect(new DropShadow(10, 0, 10, Color.BLACK));
                    secondaryFigureView.setLayoutY(-5);
                });

                //first wait before own figure is shortly before enemy figure, then start moving away
                return new SequentialTransition(pauseTransition, secondaryTransition);
            } else {
                throw new IllegalStateException("sekundär zug ist vom selben spieler obwohl es keine Rochade ist");
            }
        }
        return null;
    }

    private void finish(BooleanProperty... properties) {
        BooleanExpression expression = null;
        for (BooleanProperty property : properties) {
            if (expression == null) {
                expression = BooleanBinding.booleanExpression(property);
            } else {
                expression = expression.and(property);
            }
        }
        if (expression != null) {
            expression.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    grid.getBoard().atMoveFinished();
                }
            });
        }
    }

    private Transition getNormalTransition(Move move) {
        final Figure figure = move.getFigure();
        final PositionChange change = move.getChange();

        final FigurePosition currentPane = grid.getPositionPane(change.getFrom());
        final FigurePosition nextPane = grid.getPositionPane(change.getTo());

        final FigureView figureView = grid.getFigureView(figure);

        if (!figureView.equals(currentPane.getFigureView())) {
            throw new IllegalStateException("board ist nicht synchron");
        }

        return getTransition(figureView, currentPane, nextPane, node -> {
            currentPane.setFigure(null);
            nextPane.setFigure(figureView);
        });
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
        duration = Math.min(duration, 2.0);
        duration = Math.abs(duration);

        transition.setDuration(Duration.seconds(duration));

        node.setTranslateY(-5);
        //important, else it will be hidden behind other positionPanes
        start.toFront();
        node.setEffect(new DropShadow(10, 0, 10, Color.BLACK));
        transition.setOnFinished(event -> {
            node.setEffect(null);

            node.setTranslateX(0);
            node.setTranslateY(0);

            consumer.accept(node);
        });

        return transition;
    }

    private Transition getHorizontalTransition(Node node, Pane start, Pane goal, Consumer<Node> consumer) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);

        final Bounds local = start.localToScene(start.getBoundsInLocal());
        final Bounds bounds = goal.localToScene(goal.getBoundsInLocal());

        final double toX = bounds.getMinX() + (bounds.getWidth() / 2);
        final double fromX = local.getMinX() + (local.getWidth() / 2);
        final double translateX = toX - fromX;


        transition.setToX(translateX);
        transition.setToY(0);

        //set moving "speed", with an base speed of 100 units per seconds or a max duration of 3 seconds
        double duration = translateX / 100d;
        duration = Math.min(duration, 2.0);
        duration = Math.abs(duration);
        transition.setDuration(Duration.seconds(duration));

        //important, else it will be hidden behind other positionPanes
        start.toFront();
        transition.setOnFinished(event -> {
            node.setEffect(null);

            node.setTranslateX(0);
            node.setTranslateY(0);

            consumer.accept(node);
        });

        return transition;
    }
}

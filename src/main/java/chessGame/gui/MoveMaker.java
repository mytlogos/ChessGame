package chessGame.gui;

import chessGame.mechanics.*;
import chessGame.mechanics.figures.Figure;
import javafx.animation.*;
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
    private final BoardGridManager grid;
    private final ChangeListener<PlayerMove> lastMoveListener = getLastMoveListener();

    MoveMaker(BoardGridManager grid) {
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
        final ParallelTransition transition;
        if (move.isPromotion()) {
            transition = promote(move);
        } else if (move.isCastlingMove()) {
            transition = castle(move);
        } else {
            transition = makeNormalMove(move);
        }
        transition.setOnFinished(event -> grid.getBoard().atMoveFinished());
        transition.play();
    }

    private ParallelTransition makeNormalMove(PlayerMove move) {
        final Move secondaryMove = move.getSecondaryMove();

        final Move mainMove = move.getMainMove();

        final Transition mainTransition = getNormalTransition(mainMove);


        final FigurePosition currentPane = grid.getPositionPane(mainMove.getFrom());
        currentPane.toFront();


        ParallelTransition transition;
        if (secondaryMove != null) {

            if (grid.getFigureView(move.getMainMove().getFigure()).isDragging()) {
                final Transition secondaryTransition = doStrikeMove(secondaryMove, mainMove, Duration.INDEFINITE);
                transition = new ParallelTransition(secondaryTransition);
            } else {
                final Transition secondaryTransition = doStrikeMove(secondaryMove, mainMove, mainTransition.getTotalDuration());
                transition = new ParallelTransition(mainTransition, secondaryTransition);
            }
        } else {
            if (grid.getFigureView(move.getMainMove().getFigure()).isDragging()) {
                transition = new ParallelTransition();
            } else {
                transition = new ParallelTransition(mainTransition);
            }
        }
        return transition;
    }

    private ParallelTransition castle(PlayerMove move) {
        if (move.getSecondaryMove() == null) {
            throw new NullPointerException("sekundärer zug für Rochade ist null");
        }

        final Move secondaryMove = move.getSecondaryMove();
        final Move mainMove = move.getMainMove();

        if (!mainMove.getFigure().getPlayer().equals(secondaryMove.getFigure().getPlayer())) {
            throw new IllegalStateException("rochade von figuren unterschiedlicher spieler ist nicht erlaubt");
        }


        final Transition mainTransition = getNormalTransition(mainMove);
        final Transition secondaryTransition = getNormalTransition(secondaryMove);

        ParallelTransition transition;

        if (grid.getFigureView(mainMove.getFigure()).isDragging()) {
            transition = new ParallelTransition(secondaryTransition);
        } else {
            transition = new ParallelTransition(mainTransition, secondaryTransition);
        }
        return transition;
    }

    private ParallelTransition promote(PlayerMove move) {
        final Move mainMove = move.getMainMove();

        final Figure figure = mainMove.getFigure();

        final FigurePosition currentPane = grid.getPositionPane(mainMove.getFrom());
        final FigurePosition nextPane = grid.getPositionPane(mainMove.getTo());

        final FigureView figureView = grid.getFigureView(figure);

        if (!figureView.equals(currentPane.getFigureView())) {
            throw new IllegalStateException("board ist nicht synchron");
        }

        final Move promotionMove = move.getPromotionMove();

        if (promotionMove == null || !promotionMove.getTo().equals(mainMove.getTo())) {
            throw new IllegalStateException("promotionMove ist null oder hat nicht dasselbe Ziel wie der Hauptzug!");
        }

        final FigureView promotedView = grid.getFigureView(promotionMove.getFigure());

        final Transition mainTransition = getTransition(figureView, currentPane, nextPane, node -> {
            currentPane.setFigure(null);
            nextPane.setFigure(promotedView);
        });


        ParallelTransition transition;

        if (move.getSecondaryMove() != null) {
            if (grid.getFigureView(move.getMainMove().getFigure()).isDragging()) {
                final Transition strikeTransition = doStrikeMove(move.getSecondaryMove(), mainMove, Duration.INDEFINITE);
                transition = new ParallelTransition(strikeTransition);
            } else {
                final Transition strikeTransition = doStrikeMove(move.getSecondaryMove(), mainMove, mainTransition.getTotalDuration());
                transition = new ParallelTransition(mainTransition, strikeTransition);
            }
        } else {
            if (grid.getFigureView(move.getMainMove().getFigure()).isDragging()) {
                transition = new ParallelTransition();
            } else {
                transition = new ParallelTransition(mainTransition);
            }
        }
        return transition;
    }

    private Transition doStrikeMove(Move strikeMove, Move mainMove, Duration totalDuration) {
        final Figure strikedFigure = strikeMove.getFigure();
        final Player atMovePlayer = mainMove.getFigure().getPlayer();

        if (!strikedFigure.getPlayer().equals(atMovePlayer)) {
            if (!strikeMove.getTo().equals(Position.Bench)) {
                throw new IllegalStateException("Sekundärzug ist kein Zug wo eine Figur geschlagen wird, obwohl es als normal gekennzeichnet ist");
            }

            final FigureView secondaryFigureView = grid.getFigureView(strikedFigure);


            final FigurePosition currentPosition = grid.getPositionPane(strikeMove.getFrom());
            final Bench.LostFigureItem bench = grid.getChess().getBench(atMovePlayer).getContainer(strikedFigure.getType());

            if (!secondaryFigureView.equals(currentPosition.getFigureView())) {
                throw new IllegalStateException("board ist nicht synchron");
            }

            final Transition secondaryTransition = getTransition(secondaryFigureView, currentPosition, bench,
                    node -> {
                        bench.increment();
                        currentPosition.getChildren().remove(secondaryFigureView);
                    });

            secondaryFigureView.setEffect(null);

            final Duration duration = totalDuration.isIndefinite() ? Duration.ZERO : totalDuration.subtract(Duration.seconds(0.3));
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

    private Transition getNormalTransition(Move move) {
        final Figure figure = move.getFigure();

        final FigurePosition currentPane = grid.getPositionPane(move.getFrom());
        final FigurePosition nextPane = grid.getPositionPane(move.getTo());

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

        final Bounds startBounds = start.localToScene(start.getBoundsInLocal());
        final Bounds goalBounds = goal.localToScene(goal.getBoundsInLocal());

        final double toX = goalBounds.getMinX() + (goalBounds.getWidth() / 2);
        final double fromX = startBounds.getMinX() + (startBounds.getWidth() / 2);
        final double translateX = toX - fromX;

        final double toY = goalBounds.getMinY() + (goalBounds.getHeight() / 2);
        final double fromY = startBounds.getMinY() + (startBounds.getHeight() / 2);
        final double translateY = toY - fromY - 5;

        transition.setToX(translateX);
        transition.setToY(translateY);

        //set moving "speed", with an base speed of 100 units per seconds or a max duration of 2 seconds
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
}

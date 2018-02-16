package chessGame.gui;

import chessGame.mechanics.Figure;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;
import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.move.Move;
import chessGame.mechanics.move.PlayerMove;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Coordinates the {@link PlayerMove} at the gui layer.
 */
class MoveAnimator {
    private final BoardGridManager grid;

    MoveAnimator(BoardGridManager grid) {
        this.grid = grid;
    }

    void animateChange() {
        ChessGame game = grid.getGame();
        if (game.isRedo()) {
            for (PlayerMove move : game.getRedoQueue()) {
                redo(move);
            }
        } else {
            PlayerMove lastMove = game.getLastMove();
            if (lastMove != null) {
                makeMove(lastMove);
            }
        }
    }

    private void redo(PlayerMove lastMove) {
        final Transition transition;
        if (lastMove.isPromotion()) {
            transition = redoPromote(lastMove);
        } else if (lastMove.isCastlingMove()) {
            transition = redoCastle(lastMove);
        } else {
            transition = redoNormalMove(lastMove);
        }
        transition.setOnFinished(event -> finishAnimation());
        transition.play();
    }

    private Transition redoPromote(PlayerMove lastMove) {
        final Move mainMove = lastMove.getMainMove();
        //a promotion needs a promotionMove
        final Move promotionMove = lastMove.getPromotionMove().orElseThrow(() -> new NullPointerException("promotion move is null"));

        System.out.println("Redoing " + mainMove + " and " + promotionMove);

        if (mainMove.getTo() != Position.Promoted) {
            throw new IllegalStateException("mainMove not promoted on Promotion!");
        }

        final BoardPanel currentPane = grid.getPositionPane(promotionMove.getTo());
        final BoardPanel nextPane = grid.getPositionPane(mainMove.getFrom());

        //exchange promotedView for the pawn (removing promoted)
        FigureView promotedView = currentPane.getFigureView();
        currentPane.getChildren().remove(promotedView);

        Figure figure = grid.getGame().getBoard().figureAt(mainMove.getFrom());

        if (!figure.is(FigureType.PAWN)) {
            throw new IllegalStateException("Only a Pawn should have been promoted");
        }

        //set pawnView in exchange for the promoted View
        final FigureView figureView = grid.getFigureView(figure);
        currentPane.setFigure(figureView);

        final Transition mainTransition = getTransition(figureView, currentPane, nextPane, false, () -> normalTransitionFinished(currentPane, nextPane, figureView));

        return getTotalRedoTransition(lastMove, figureView, mainTransition);
    }

    private Transition getTotalRedoTransition(PlayerMove playerMove, FigureView figureView, Transition mainTransition) {
        return playerMove.getSecondaryMove()
                .map(move -> getRedoStrikeTransition(playerMove.getMainMove(), figureView, mainTransition, redoStrikeMove(move, playerMove.getMainMove())))
                .orElse(new SequentialTransition(mainTransition));
    }

    private Transition getRedoStrikeTransition(Move mainMove, FigureView figureView, Transition mainTransition, Transition strikeTransition) {
        Duration totalDuration = strikeTransition.getTotalDuration();

        final Duration duration = totalDuration.isIndefinite() ? Duration.ZERO : totalDuration.subtract(Duration.seconds(0.3));
        final PauseTransition pauseTransition = new PauseTransition(duration);

        BoardPanel currentPanel = grid.getPositionPane(mainMove.getTo());
        pauseTransition.setOnFinished(event -> finishPause(figureView, currentPanel));

        //remove effect first from getTransition, will be added after the pauseTransition is over
        figureView.setEffect(null);

        //first wait before enemy figure is shortly before own figure, then start moving away
        SequentialTransition transition = new SequentialTransition(pauseTransition, mainTransition);
        //redoStrike transition and redoMove are running parallel
        return new ParallelTransition(strikeTransition, transition);
    }

    private Transition redoStrikeMove(Move strikeMove, Move mainMove) {
        final chessGame.mechanics.Color atMovePlayer = mainMove.getColor();

        if (!strikeMove.getColor().equals(atMovePlayer)) {
            if (!strikeMove.getTo().equals(Position.Bench)) {
                throw new IllegalStateException("Sekundärzug ist kein Zug wo eine Figur geschlagen wird, obwohl es als normal gekennzeichnet ist");
            }

            //get figure which was defeated
            FigureView defeatedView = grid.getVisualBoard().figureAt(strikeMove.getFrom());

            //get panel as target and bench as beginning
            final BoardPanel currentPanel = grid.getPositionPane(strikeMove.getFrom());
            final PlayerBench.LostFigureItem bench = grid.getChess().getBench(atMovePlayer.isWhite()).getContainer(strikeMove.getFigure());

            TranslateTransition transition = new TranslateTransition();
            transition.setNode(defeatedView);

            Translate translate = new Translate(bench, currentPanel).invoke();
            getDistanceDuration(translate);

            bench.decrement();
            currentPanel.setFigure(defeatedView);
            bench.getChildren().remove(defeatedView);

            transition.setFromX(-translate.getTranslateX());
            transition.setFromY(-translate.getTranslateY());

            transition.setToX(0);
            transition.setToY(0);

            //set moving "speed", with an base speed of 100 units per seconds or a max duration of 2 seconds
            double duration = getDistanceDuration(translate);
            transition.setDuration(Duration.seconds(duration));

            //to create an effect of "lifting the figure up"
            defeatedView.setTranslateY(-5);
            defeatedView.setEffect(new DropShadow(10, 0, 10, Color.BLACK));

            //important, else it will be hidden behind other positionPanes
            bench.toFront();

            transition.setOnFinished(event -> transitionFinished(defeatedView, () -> {}));
            return transition;
        } else {
            throw new IllegalStateException("sekundär zug ist vom selben spieler obwohl es keine Rochade ist");
        }
    }

    private Transition redoCastle(PlayerMove lastMove) {
        //castling needs a secondary Move for Rook
        final Move secondaryMove = lastMove.getSecondaryMove().orElseThrow(() -> new NullPointerException("sekundärer zug für Rochade ist null"));
        final Move mainMove = lastMove.getMainMove();

        if (!mainMove.getColor().equals(secondaryMove.getColor())) {
            throw new IllegalStateException("rochade von figuren unterschiedlicher spieler ist nicht erlaubt");
        }

        //reverse transition, from goal to beginning of the move
        final Transition mainTransition = getNormalTransition(mainMove.getTo(), mainMove.getFrom());
        //reverse transition, from goal to beginning of the move
        final Transition secondaryTransition = getNormalTransition(secondaryMove.getFrom(), secondaryMove.getTo());
        //castling is running parallel
        return new ParallelTransition(mainTransition, secondaryTransition);
    }


    private Transition redoNormalMove(PlayerMove lastMove) {
        final Move mainMove = lastMove.getMainMove();

        //from goal to beginning
        final Transition mainTransition = getNormalTransition(mainMove.getTo(), mainMove.getFrom());

        final BoardPanel currentPane = grid.getPositionPane(mainMove.getTo());
        //set current pane to front to prevent hiding behind other panes
        currentPane.toFront();

        return getTotalRedoTransition(lastMove, currentPane.getFigureView(), mainTransition);
    }

    private void makeMove(PlayerMove lastMove) {
        final Transition transition;
        if (lastMove.isPromotion()) {
            transition = promote(lastMove);
        } else if (lastMove.isCastlingMove()) {
            transition = castle(lastMove);
        } else {
            transition = makeNormalMove(lastMove);
        }
        transition.setOnFinished(event -> finishAnimation());
        transition.play();
    }

    private void finishAnimation() {
        grid.getVisualBoard().mirrorBoard();
        grid.getGame().nextRound();
    }

    private Transition makeNormalMove(PlayerMove playerMove) {

        final Move mainMove = playerMove.getMainMove();
        final Transition mainTransition = getNormalTransition(mainMove.getFrom(), mainMove.getTo());

        return getTotalMakeMoveTransition(playerMove, mainMove, mainTransition);
    }

    private Transition getStrikeLessTransition(Move mainMove, Transition mainTransition) {
        return grid.getVisualBoard().figureAt(mainMove.getFrom()).isDragging() ? new SequentialTransition() : new SequentialTransition(mainTransition);
    }

    private Transition getStrikeTransition(Move mainMove, Transition mainTransition, Move move) {
        if (grid.getVisualBoard().figureAt(mainMove.getFrom()).isDragging()) {
            return new SequentialTransition(doStrikeMove(move, mainMove));
        } else {
            return new ParallelTransition(mainTransition, doStrikeMove(move, mainMove));
        }
    }

    private Transition promote(PlayerMove playerMove) {
        final Move mainMove = playerMove.getMainMove();
        final Move promotionMove = playerMove.getPromotionMove().orElseThrow(() -> new NullPointerException("promotion move is null"));

        final BoardPanel currentPane = grid.getPositionPane(mainMove.getFrom());
        final BoardPanel nextPane = grid.getPositionPane(promotionMove.getTo());

        final FigureView figureView = grid.getVisualBoard().figureAt(mainMove.getFrom());

        if (!figureView.equals(currentPane.getFigureView())) {
            throw new IllegalStateException("board ist nicht synchron");
        }

        if (mainMove.getTo() != Position.Promoted) {
            throw new IllegalStateException("mainMove not promoted on Promotion!");
        }

        Figure promotedFigure = grid.getGame().getBoard().figureAt(promotionMove.getTo());
        final FigureView promotedView = new FigureView(promotedFigure, grid);
        grid.addFigureView(promotedView);

        final Transition mainTransition = getTransition(figureView, currentPane, nextPane, true, () -> promotionTransitionFinished(currentPane, nextPane, figureView, promotedView));

        //if figureView is dragging remove the pawn and add the promotedView because there will be no animation for this part
        if (figureView.isDragging()) {
            nextPane.getChildren().remove(figureView);
            nextPane.setFigure(promotedView);
        }

        return getTotalMakeMoveTransition(playerMove, mainMove, mainTransition);
    }

    private void promotionTransitionFinished(BoardPanel currentPane, BoardPanel nextPane, FigureView figureView, FigureView promotedView) {
        nextPane.getChildren().remove(figureView);
        normalTransitionFinished(currentPane, nextPane, promotedView);
    }

    private Transition getTotalMakeMoveTransition(PlayerMove playerMove, Move mainMove, Transition mainTransition) {
        return playerMove.getSecondaryMove()
                .map(move -> getStrikeTransition(mainMove, mainTransition, move))
                .orElse(getStrikeLessTransition(mainMove, mainTransition));
    }

    private Transition castle(PlayerMove move) {
        final Move secondaryMove = move.getSecondaryMove().orElseThrow(() -> new NullPointerException("sekundärer zug für Rochade ist null"));

        final Move mainMove = move.getMainMove();

        if (!mainMove.getColor().equals(secondaryMove.getColor())) {
            throw new IllegalStateException("rochade von figuren unterschiedlicher spieler ist nicht erlaubt");
        }

        final Transition mainTransition = getNormalTransition(mainMove.getFrom(), mainMove.getTo());
        final Transition secondaryTransition = getNormalTransition(secondaryMove.getFrom(), secondaryMove.getTo());

        return grid.getVisualBoard().figureAt(mainMove.getFrom()).isDragging() ?
                new SequentialTransition(secondaryTransition) :
                new ParallelTransition(mainTransition, secondaryTransition);
    }

    private Transition doStrikeMove(Move strikeMove, Move mainMove) {
        final chessGame.mechanics.Color atMovePlayer = mainMove.getColor();

        if (!strikeMove.getColor().equals(atMovePlayer)) {
            if (!strikeMove.getTo().equals(Position.Bench)) {
                throw new IllegalStateException("Sekundärzug ist kein Zug wo eine Figur geschlagen wird, obwohl es als normal gekennzeichnet ist");
            }

            final FigureView secondaryFigureView = grid.getVisualBoard().figureAt(strikeMove.getFrom());


            final BoardPanel currentPosition = grid.getPositionPane(strikeMove.getFrom());
            final PlayerBench.LostFigureItem bench = grid.getChess().getBench(atMovePlayer.isWhite()).getContainer(strikeMove.getFigure());

            final Transition secondaryTransition = getTransition(secondaryFigureView, currentPosition, bench, false,
                    () -> strikeTransitionFinished(secondaryFigureView, currentPosition, bench));

            secondaryFigureView.setEffect(null);

            final PauseTransition pauseTransition = getPauseTransition(secondaryFigureView, currentPosition);

            //first wait before own figure is shortly before enemy figure, then start moving away
            return new SequentialTransition(pauseTransition, secondaryTransition);
        } else {
            throw new IllegalStateException("sekundär zug ist vom selben spieler obwohl es keine Rochade ist");
        }
    }

    private PauseTransition getPauseTransition(FigureView secondaryFigureView, BoardPanel currentPosition) {
        final Duration duration = Duration.INDEFINITE.isIndefinite() ?
                Duration.ZERO :
                Duration.INDEFINITE.subtract(Duration.seconds(0.3));

        final PauseTransition pauseTransition = new PauseTransition(duration);

        pauseTransition.setOnFinished(event -> finishPause(secondaryFigureView, currentPosition));
        return pauseTransition;
    }

    private void finishPause(FigureView secondaryFigureView, BoardPanel currentPosition) {
        currentPosition.toFront();
        secondaryFigureView.setEffect(new DropShadow(10, 0, 10, Color.BLACK));
        secondaryFigureView.setLayoutY(-5);
    }

    private void strikeTransitionFinished(FigureView secondaryFigureView, BoardPanel currentPosition, PlayerBench.LostFigureItem bench) {
        bench.increment();
        currentPosition.getChildren().remove(secondaryFigureView);
    }

    private Transition getNormalTransition(Position from, Position to) {
        final BoardPanel currentPane = grid.getPositionPane(from);
        final BoardPanel nextPane = grid.getPositionPane(to);

        final FigureView figureView = grid.getVisualBoard().figureAt(from);

        if (!figureView.equals(currentPane.getFigureView())) {
            throw new IllegalStateException("board ist nicht synchron");
        }

        return getTransition(figureView, currentPane, nextPane, true, () -> normalTransitionFinished(currentPane, nextPane, figureView));
    }

    private void normalTransitionFinished(BoardPanel current, BoardPanel next, FigureView figureView) {
        if (current.getFigureView() == figureView) {
            current.setFigureEmpty();
        }
        next.setFigure(figureView);
    }


    private Transition getTransition(Node node, Pane start, Pane goal, boolean reverse, Runnable runnable) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);

        Translate translate = new Translate(start, goal).invoke();

        if (reverse) {
            goal.getChildren().add(node);

            transition.setFromX(-translate.getTranslateX());
            transition.setFromY(-translate.getTranslateY());
            transition.setToX(0);
            transition.setToY(0);
            //important, else it will be hidden behind other positionPanes
            goal.toFront();
            start.toBack();
        } else {
            transition.setToX(translate.getTranslateX());
            transition.setToY(translate.getTranslateY());

            //important, else it will be hidden behind other positionPanes
            start.toFront();
        }

        //set moving "speed", with an base speed of 100 units per seconds or a max duration of 2 seconds
        double duration = getDistanceDuration(translate);

        transition.setDuration(Duration.seconds(duration));

        //to create an effect of "lifting the figure up"
        node.setTranslateY(-5);
        node.setEffect(new DropShadow(10, 0, 10, Color.BLACK));

        transition.setOnFinished(event -> transitionFinished(node, runnable));
        return transition;
    }

    private double getDistanceDuration(Translate translate) {
        final double xPow = Math.pow(translate.getTranslateX(), 2);
        final double yPow = Math.pow(translate.getTranslateY(), 2);
        final double distance = Math.sqrt(xPow + yPow);
        double duration = distance / 100d;
        duration = Math.min(duration, 2.0);
        duration = Math.abs(duration);
        return duration;
    }

    private void transitionFinished(Node node, Runnable runnable) {
        node.setEffect(null);

        node.setTranslateX(0);
        node.setTranslateY(0);

        runnable.run();
    }

    private static class Translate {
        private final Pane start;
        private final Pane goal;
        private double translateX;
        private double translateY;

        Translate(Pane start, Pane goal) {
            this.start = start;
            this.goal = goal;
        }

        double getTranslateX() {
            return translateX;
        }

        double getTranslateY() {
            return translateY;
        }

        Translate invoke() {
            final Bounds startBounds = start.localToScene(start.getBoundsInLocal());
            final Bounds goalBounds = goal.localToScene(goal.getBoundsInLocal());

            final double toX = goalBounds.getMinX() + (goalBounds.getWidth() / 2);
            final double fromX = startBounds.getMinX() + (startBounds.getWidth() / 2);
            translateX = toX - fromX;

            final double toY = goalBounds.getMinY() + (goalBounds.getHeight() / 2);
            final double fromY = startBounds.getMinY() + (startBounds.getHeight() / 2);
            translateY = toY - fromY - 5;
            return this;
        }
    }
}

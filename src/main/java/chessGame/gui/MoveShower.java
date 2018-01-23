package chessGame.gui;

import chessGame.mechanics.*;
import chessGame.mechanics.Board;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shows the possible PlayerMoves
 */
class MoveShower {
    private final BoardGridManager grid;
    private ChangeListener<Number> roundListener;

    MoveShower(BoardGridManager grid) {
        this.grid = grid;
        init();
    }

    private void init() {
        roundListener = (observable, oldValue, newValue) -> prepareMoves();

        grid.gameProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.roundProperty().addListener(roundListener);
            }
            if (oldValue != null) {
                //to prevent the old board from affecting the gui just in case
                oldValue.roundProperty().removeListener(roundListener);
            }
        });

        grid.chosenPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue == null) {
                showMoves(newValue);
            } else if (newValue == null && oldValue != null) {
                if (!oldValue.isHover()) {
                    grid.getFigurePositions().forEach(figurePosition -> figurePosition.setShowOff(null));
                }
            }
        });
    }

    private void prepareMoves() {
        Game game = grid.getGame();

        final List<PlayerMove> movesWhite = MoveGenerator.getAllowedMoves(game.getWhite(), game);
        final List<PlayerMove> allowedMoves = MoveGenerator.getAllowedMoves(game.getBlack(), game);

        allowedMoves.addAll(movesWhite);

        final Map<FigurePosition, Map<Figure, List<PlayerMove>>> map = allowedMoves.
                stream().
                collect(Collectors.groupingBy(
                        this::getPositionPane,
                        HashMap::new,
                        Collectors.groupingBy(move -> move.getMainMove().getFigure())));

        grid.getFigurePositions().forEach(FigurePosition::clearAcceptableMoves);


        map.forEach((key, value) -> {
            if (value != null) {
                value.forEach(((figure, moves) -> {
                    if (figure.getType() == FigureType.PAWN) {
                        setPawnMove(key, figure, moves);

                    } else if (figure.getType() == FigureType.KING) {
                        setKingMove(key, figure, moves);

                    } else if (moves.size() == 1) {
                        key.addAcceptableMove(figure, moves.get(0));
                    }
                }));
            }
        });
    }

    private void showMoves(FigurePosition position) {
        final FigureView figureView = position.getFigureView();
        if (figureView != null) {
            final Figure figure = figureView.getFigure();
            grid.getFigurePositions().forEach(figurePosition -> figurePosition.setShowOff(figure));
        }
    }

    private void setPawnMove(FigurePosition key, Figure figure, List<PlayerMove> moves) {
        final List<PlayerMove> promotions = moves.stream().filter(PlayerMove::isPromotion).collect(Collectors.toList());
        moves.removeAll(promotions);

        if (moves.size() == 1) {
            key.addAcceptableMove(figure, moves.get(0));
        }

        key.setPromotion(promotions);
    }

    private void setKingMove(FigurePosition key, Figure figure, List<PlayerMove> moves) {
        if (moves.size() == 2) {
            final PlayerMove firstMove = moves.get(0);
            final PlayerMove secondMove = moves.get(1);

            if (firstMove.isCastlingMove() && !secondMove.isCastlingMove()) {
                key.setCastling(firstMove);
                key.addAcceptableMove(figure, secondMove);
            } else if (secondMove.isCastlingMove() && !firstMove.isCastlingMove()) {
                key.setCastling(secondMove);
                key.addAcceptableMove(figure, firstMove);
            }
        } else if (moves.size() == 1) {
            final PlayerMove move = moves.get(0);

            if (move.isCastlingMove()) {
                key.setCastling(move);
            } else {
                key.addAcceptableMove(figure, move);
            }
        }
    }

    void showArrow(Figure figure, Pane goal) {

    }

    private FigurePosition getPositionPane(PlayerMove playerMove) {
        final Position to = playerMove.getPromotionMove().map(Move::getTo).orElse(playerMove.getMainMove().getTo());
        return grid.getPositionPane(to);
    }

}

package chessGame.gui;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.PlayerMove;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Shows the possible PlayerMoves
 */
class MoveShower {
    private final BoardGridManager grid;
    private ChangeListener<Player> atMoveChangeListener;

    MoveShower(BoardGridManager grid) {
        this.grid = grid;
        init();
    }

    private void init() {
        atMoveChangeListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                prepareMoves();
            }
        };
        grid.boardProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.atMovePlayerProperty().addListener(atMoveChangeListener);
            }
            if (oldValue != null) {
                //to prevent the old board from affecting the gui just in case
                oldValue.atMovePlayerProperty().removeListener(atMoveChangeListener);
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
        final Board board = grid.getBoard();
        final List<PlayerMove> movesWhite = board.getGenerator().getAllowedMoves(board.getWhite());
        final List<PlayerMove> allowedMoves = board.getGenerator().getAllowedMoves(board.getBlack());

        allowedMoves.addAll(movesWhite);
        final Map<FigurePosition, Map<Figure, List<PlayerMove>>> map = allowedMoves.
                stream().
                collect(Collectors.groupingBy(
                        move -> grid.getPositionPane(move.getMainMove().getTo()),
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

    private void showMoves(FigurePosition position) {
        final FigureView figureView = position.getFigureView();
        if (figureView != null) {
            final Figure figure = figureView.getFigure();
            grid.getFigurePositions().forEach(figurePosition -> figurePosition.setShowOff(figure));
        }
    }

    void showArrow(Figure figure, Pane goal) {

    }

}

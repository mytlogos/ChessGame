package chessGame.gui;

import chessGame.mechanics.Figure;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;
import chessGame.mechanics.board.FigureBoard;
import chessGame.mechanics.game.Game;
import chessGame.mechanics.move.Move;
import chessGame.mechanics.move.MoveForGenerator;
import chessGame.mechanics.move.PlayerMove;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shows the possible PlayerMoves
 */
class MoveShower {
    private final BoardGridManager grid;

    MoveShower(BoardGridManager grid) {
        this.grid = grid;
        init();
    }

    private void init() {
        grid.chosenPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue == null) {
                showMoves(newValue);
            } else if (newValue == null && oldValue != null) {
                if (!oldValue.isHover()) {
                    grid.getFigurePositions().forEach(boardPanel -> boardPanel.setShowOff(null));
                }
            }
        });
    }

    private void showMoves(BoardPanel position) {
        final FigureView figureView = position.getFigureView();
        if (figureView != null) {
            final Figure figure = figureView.getFigure();
            grid.getFigurePositions().forEach(boardPanel -> boardPanel.setShowOff(figure));
        }
    }

    void prepareMoves() {
        Game game = grid.getGame();
        FigureBoard board = game.getBoard();

        final List<PlayerMove> movesWhite = MoveForGenerator.getAllowedMoves(game.getWhite().getColor(), game);
        final List<PlayerMove> movesBlack = MoveForGenerator.getAllowedMoves(game.getBlack().getColor(), game);

        final List<PlayerMove> allowedMoves = new ArrayList<>();
        allowedMoves.addAll(movesWhite);
        allowedMoves.addAll(movesBlack);

        final Map<BoardPanel, Map<Figure, List<PlayerMove>>> map = allowedMoves.
                stream().
                collect(Collectors.groupingBy(
                        this::getPositionPane,
                        HashMap::new,
                        Collectors.groupingBy(move -> board.figureAt(move.getMainMove().getFrom()))));

        grid.getFigurePositions().forEach(BoardPanel::clearAcceptableMoves);


        map.forEach((key, value) -> {
            if (value != null) {
                value.forEach(((figure, moves) -> {
                    if (figure.is(FigureType.PAWN)) {
                        setPawnMove(key, figure, moves);

                    } else if (figure.is(FigureType.KING)) {
                        setKingMove(key, figure, moves);

                    } else if (moves.size() == 1) {
                        key.addAcceptableMove(figure, moves.get(0));
                    } else {
                        System.err.println("too many moves: " + moves);
                    }
                }));
            }
        });
    }

    private void setPawnMove(BoardPanel key, Figure figure, List<PlayerMove> moves) {
        final List<PlayerMove> promotions = moves.stream().filter(PlayerMove::isPromotion).collect(Collectors.toList());
        moves.removeAll(promotions);

        if (moves.size() == 1) {
            key.addAcceptableMove(figure, moves.get(0));
        }

        key.setPromotion(promotions, figure);
    }

    private void setKingMove(BoardPanel key, Figure figure, List<PlayerMove> moves) {
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

    private BoardPanel getPositionPane(PlayerMove playerMove) {
        final Position to = playerMove.getPromotionMove().map(Move::getTo).orElse(playerMove.getMainMove().getTo());
        return grid.getPositionPane(to);
    }

}

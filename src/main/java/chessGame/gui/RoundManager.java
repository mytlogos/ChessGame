package chessGame.gui;

import chessGame.figures.Figure;
import chessGame.mechanics.Position;

import java.util.List;

/**
 *
 */
public class RoundManager {
    private final BoardGrid board;

    RoundManager(BoardGrid board) {
        this.board = board;
        initManager();
    }

    private void initManager() {
        board.getBoard().atMoveProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.getFigures().forEach(figure -> board.getFigure(figure).setActive(false));
            }
            if (newValue != null) {
                newValue.getFigures().forEach(figure -> board.getFigure(figure).setActive(true));
            }
        });
    }



    static void showPositions(Figure figure, BoardGrid board) {
        final List<Position> allowedPositions = figure.getAllowedPositions();
        allowedPositions.stream().filter(Position::isEmpty).map(board::getPositionPane).forEach(FigurePosition::setEmpty);
        allowedPositions.stream().filter(Position::isEnemy).map(board::getPositionPane).forEach(FigurePosition::setEnemy);
    }

    static void disableEffects(Figure figure, BoardGrid board) {
        final List<Position> allowedPositions = figure.getAllowedPositions();
        allowedPositions.stream().map(board::getPositionPane).forEach(FigurePosition::resetEffect);
    }
}

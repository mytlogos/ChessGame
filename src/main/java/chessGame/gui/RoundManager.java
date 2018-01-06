package chessGame.gui;

import chessGame.mechanics.Game;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.Position;

import java.util.List;

/**
 *
 */
public class RoundManager {
    private final BoardGrid board;
    private Game game;

    RoundManager(BoardGrid board) {
        this.board = board;
        initManager();
    }

    private void initManager() {
        board.boardProperty().addListener((observable, oldValue, newValue) -> newValue.atMovePlayerProperty().addListener((observable1, playerNotAtMove, playerAtMove) -> {
            if (playerNotAtMove != null) {
                playerNotAtMove.getFigures().forEach(figure -> board.getFigureView(figure).setActive(false));
            }
            if (playerAtMove != null) {
                playerAtMove.getFigures().forEach(figure -> board.getFigureView(figure).setActive(true));
            }
        }));
    }



    static void showPositions(Figure figure, BoardGrid board) {
        final List<Position> allowedPositions = figure.getAllowedPositions();
        allowedPositions.stream().map(board::getPositionPane).forEach(FigurePosition::setEnemy);
        allowedPositions.stream().filter(Position::isEmpty).map(board::getPositionPane).forEach(FigurePosition::setEmpty);
    }

    static void disableEffects(Figure figure, BoardGrid board) {
        final List<Position> allowedPositions = figure.getAllowedPositions();
        allowedPositions.stream().map(board::getPositionPane).forEach(FigurePosition::resetEffect);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}

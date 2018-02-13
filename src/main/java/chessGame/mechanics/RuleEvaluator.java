package chessGame.mechanics;

import chessGame.mechanics.board.Board;
import chessGame.mechanics.game.Game;
import chessGame.mechanics.move.MoveForGenerator;
import chessGame.mechanics.move.MoveHistory;
import chessGame.mechanics.move.PlayerMove;

import java.util.List;

import static chessGame.mechanics.FigureType.BISHOP;
import static chessGame.mechanics.FigureType.KNIGHT;

/**
 *
 */
public abstract class RuleEvaluator {
    public enum End {
        WIN,
        DRAW,
        LOSS,
        NONE
    }


    public static boolean isDraw(Game game) {
        return checkEndGame(game, game.getWhite().getColor()) == End.DRAW || checkEndGame(game, game.getBlack().getColor()) == End.DRAW;
    }

    /**
     * Checks if a Conclusion to the Game is made.
     *
     * @param game
     * @param player
     * @return returns the endGame conclusion as {@link End}, not null
     * */
    public static End checkEndGame(Game game, Color player) {
        Board board = game.getBoard();

        Color enemy = Color.getEnemy(player);
        List<PlayerMove> enemyMoves = MoveForGenerator.getAllowedMoves(enemy, game);
        List<PlayerMove> playerMoves = MoveForGenerator.getAllowedMoves(player, game);

        if (playerMoves.isEmpty() && !enemyMoves.isEmpty()) {
            Figure king = board.getKing(player.isWhite());
            return MoveForGenerator.isInCheck(king, board) ? End.LOSS : End.DRAW;

        } else if (enemyMoves.isEmpty() && !playerMoves.isEmpty()) {
            Figure king = board.getKing(enemy.isWhite());

            //player at move has no legal moves, but king is not in check results in a draw
            return MoveForGenerator.isInCheck(king, board) ? End.WIN : End.DRAW;
        //if both players can make no legal moves or a checkmate is not possible
        } else if (enemyMoves.isEmpty() || checkImpossibleCheckMate(game)) {
            return End.DRAW;
        }
        return End.NONE;
    }

    /**
     * Checks for Impossible CheckMates.
     * Would result in a Draw.
     *
     * @param game
     * @return false if it is possible to checkMate, else true
     */
    private static boolean checkImpossibleCheckMate(Game game) {
        Board board = game.getBoard();

        List<Figure> whiteFigures = board.getFigures(game.getWhite().getColor());
        List<Figure> blackFigures = board.getFigures(game.getBlack().getColor());

        //if both only have one figure, namely a king, no checkmate possible
        if (whiteFigures.size() == 1 && blackFigures.size() == 1) {
            return true;
        }

        //black has only king and white either king and bishop or king and knight
        if (blackFigures.size() == 1 && whiteFigures.size() == 2) {
            Figure firstFigure = whiteFigures.get(0);
            Figure secondFigure = whiteFigures.get(1);

            return firstFigure.is(BISHOP) || secondFigure.is(BISHOP) || firstFigure.is(KNIGHT) || secondFigure.is(KNIGHT);

            //white has only king and black either king and bishop or king and knight
        } else if (blackFigures.size() == 2 && whiteFigures.size() == 1) {
            Figure firstFigure = blackFigures.get(0);
            Figure secondFigure = blackFigures.get(1);

            return firstFigure.is(BISHOP) || secondFigure.is(BISHOP) || firstFigure.is(KNIGHT) || secondFigure.is(KNIGHT);

            //both players have a number of bishops on the same color
        } else /*if (blackFigures.size() == whiteFigures.size()) {
            Figure figure = blackFigures.get(0);
            Figure figure1 = blackFigures.get(1);

        }*/
        return false;
    }

    /**
     * The Threefold Repetition Rule says that a Set of Positions (A Board)
     * can only occur up to 3 times?
     *
     * @return zero if this rule is not implicated, else -500 for draw
     * @see <a href="https://en.wikipedia.org/wiki/Threefold_repetition">Threefold Repetition</a>
     */
    private static boolean checkThreeRepRule(Game game) {
        //Threefold repetition rule: if a set of positions occur tree times a draw can be made
        return game.getHistory().checkOccurrences() >= 3;
    }

    /**
     * Checks for the Fifty-Move-Rule.
     * A'"move" consists of a player completing their turn followed by the opponent completing their turn".
     *
     * @param history history of the Game
     * @return true if this rule is
     * @see <a href="https://en.wikipedia.org/wiki/Fifty-move_rule">Fifty-Move-Rule</a>
     */
    private static boolean checkFiftyMovesRule(MoveHistory history) {
        return history.size() >= 100 && history.lastHundred().stream().anyMatch(RuleEvaluator::fiftyMovesCheck);
    }

    /**
     * Check if move is a Strike or a Pawn-Move.
     *
     * @param playerMove
     * @return true if this rule is followed.
     */
    private static boolean fiftyMovesCheck(PlayerMove playerMove) {
        return playerMove.isStrike() || playerMove.getMainMove().getFigure() == FigureType.PAWN;
    }

    public static boolean canClaimDraw(Game game) {
        return checkFiftyMovesRule(game.getHistory()) || checkThreeRepRule(game);
    }

    public static boolean isWin(Game game, Color color) {
        return checkEndGame(game, color) == End.WIN;
    }
}

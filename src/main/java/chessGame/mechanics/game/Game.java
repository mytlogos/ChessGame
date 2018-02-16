package chessGame.mechanics.game;

import chessGame.mechanics.Color;
import chessGame.mechanics.Figure;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Player;
import chessGame.mechanics.board.FigureBoard;
import chessGame.mechanics.move.MoveHistory;
import chessGame.mechanics.move.PlayerMove;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface Game {

    List<PlayerMove> getAllowedMoves();

    MoveHistory getHistory();

    /**
     * Gets the Bench of the Player.
     * A Bench holds the beaten Figures of the enemy.
     *
     * @return a Map of player and their figures they beat
     */
    Map<Color, Map<FigureType, List<Figure>>> getBench();

    Map<Color, List<Figure>> getPromoted();

    Player getWhite();

    Player getBlack();

    void makeMove(PlayerMove move);

    void singlePlyRedo();

    void addPromoted(Figure figure);

    void addBench(Figure figure);

    PlayerMove getLastMove();

    BitSet getSnapShot();

    Figure removeFromBench(Color player, FigureType figure);

    Figure removeFromPromoted(Color player);

    Player getAtMove();

    Color getAtMoveColor();

    FigureBoard getBoard();

    ReadOnlyStringProperty timeProperty();

}

package chessGame.mechanics.game;

import chessGame.mechanics.move.PlayerMove;

import java.util.List;

/**
 *  Interface for usage in the Engine.
 */
public interface SimulationGame extends Game{
    void setAllowedMoves(List<PlayerMove> allowedMoves);
}

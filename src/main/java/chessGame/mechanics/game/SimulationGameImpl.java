package chessGame.mechanics.game;

import chessGame.mechanics.move.PlayerMove;

import java.util.List;

/**
 *
 */
public class SimulationGameImpl extends GameImpl implements SimulationGame {

    SimulationGameImpl(GameImpl game) {
        super(game);
    }

    @Override
    public List<PlayerMove> getAllowedMoves() {
        return allowedMoves;
    }

    @Override
    public void setAllowedMoves(List<PlayerMove> allowedMoves) {
        this.allowedMoves = allowedMoves;
    }
}

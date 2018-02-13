package chessGame.engine;

import chessGame.mechanics.move.PlayerMove;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SnapshotItem {
    private List<PlayerMove> whiteMoves = new ArrayList<>();
    private List<PlayerMove> blackMoves = new ArrayList<>();
    private int evaluation;
}

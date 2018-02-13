package chessGame.mechanics.board;

import chessGame.mechanics.move.PlayerMove;

/**
 *
 */
public interface BoardHasher {
    void hashBoard();

    void forwardHash(PlayerMove move);

    void backWardHash(PlayerMove move);
}

package chessGame.engine;

import chessGame.mechanics.move.PlayerMove;

/**
 *
 */
class Entry {
    private long hashKey;
    private int evaluation;
    private PlayerMove bestMove;
    private int depth;
    private int age;
    private boolean used;
    private Bound bound;

    Entry(long hashKey, int evaluation, PlayerMove bestMove, int depth, Bound bound) {
        this.hashKey = hashKey;
        this.evaluation = evaluation;
        this.bestMove = bestMove;
        this.depth = depth;
        this.bound = bound;
    }

    public Bound getBound() {
        return bound;
    }

    boolean isUsed() {
        return used;
    }

    long getHashKey() {
        return hashKey;
    }

    int getEvaluation() {
        return evaluation;
    }

    PlayerMove getBestMove() {
        return bestMove;
    }

    int getDepth() {
        return depth;
    }

    void setUsed(boolean used) {
        this.used = used;
    }

    int getAge() {
        return age;
    }

    enum Bound {
        EXACT,
        LOWER,
        UPPER
    }

}

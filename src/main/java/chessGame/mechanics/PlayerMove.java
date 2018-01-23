package chessGame.mechanics;

import chessGame.engine.EngineMove;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;

import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public class PlayerMove implements Cloneable {
    private Move mainMove;
    private Move secondaryMove;
    private Move promotionMove;
    private Type type = Type.NORMAL;
    private boolean strike;
    private BoardSnapShot boardSnap;

    public PlayerMove(Move mainMove, Move secondaryMove) {
        Objects.requireNonNull(mainMove);
        this.mainMove = mainMove;
        this.secondaryMove = secondaryMove;

        strike = secondaryMove != null && !getPlayer().equals(secondaryMove.getFigure().getPlayer());
    }

    public Player getPlayer() {
        final Figure figure = mainMove.getFigure();
        return figure.getPlayer();
    }

    public static PlayerMove PromotionMove(Move pawnMove, Move strike, Move promotion) {
        Objects.requireNonNull(pawnMove);
        Objects.requireNonNull(promotion);

        if (checkPromotionConditions(pawnMove, strike, promotion)) {
            return null;
        }

        final PlayerMove playerMove = new PlayerMove(pawnMove, strike);
        playerMove.type = Type.PROMOTION;
        playerMove.promotionMove = promotion;
        return playerMove;
    }

    private static boolean checkPromotionConditions(Move pawnMove, Move strike, Move promotion) {
        return false;
    }

    public static PlayerMove CastlingMove(Move kingMove, Move rookMove) {
        Objects.requireNonNull(kingMove);
        Objects.requireNonNull(rookMove);

        if (checkCastlingConditions(kingMove, rookMove)) {
            return null;
        }

        final PlayerMove playerMove = new PlayerMove(kingMove, rookMove);
        playerMove.type = Type.CASTLING;
        return playerMove;
    }

    private static boolean checkCastlingConditions(Move kingMove, Move rookMove) {
        final Figure kingMoveFigure = kingMove.getFigure();
        final Figure rookMoveFigure = rookMove.getFigure();

        return kingMoveFigure.getType() != FigureType.KING ||
                rookMoveFigure.getType() != FigureType.ROOK ||
                !kingMoveFigure.getPlayer().equals(rookMoveFigure.getPlayer()) ||
                Math.abs(kingMove.getTo().getColumn() - rookMove.getTo().getColumn()) != 1;
    }

    public boolean checkMove(Board board) {
        return getMainMove().getFigure().checkBoard(board) &&
                getSecondaryMove().map(move -> move.getFigure().checkBoard(board)).orElse(true) &&
                getPromotionMove().map(move -> move.getFigure().checkBoard(board)).orElse(true);
    }

    public Move getMainMove() {
        return mainMove;
    }

    public Optional<Move> getSecondaryMove() {
        return Optional.ofNullable(secondaryMove);
    }

    public Optional<Move> getPromotionMove() {
        return Optional.ofNullable(isPromotion() ? promotionMove : null);
    }

    public boolean isPromotion() {
        return type == Type.PROMOTION;
    }

    final protected void setPromotionMove(Move promotionMove) {
        this.promotionMove = promotionMove;
    }

    public boolean isNormal() {
        return type == Type.NORMAL;
    }

    public boolean isCastlingMove() {
        return type == Type.CASTLING;
    }

    final public EngineMove engineClone(Board board, Game game) {
        final PlayerMove clone = clone(board, game);
        return new EngineMove(clone, board);
    }

    final public PlayerMove clone(Board board, Game game) {
        final PlayerMove clone = clone();
        if (clone == null) {
            return null;
        }
        clone.mainMove = getMainMove().clone(board, game,false);
        getSecondaryMove().ifPresent(move -> clone.secondaryMove = move.clone(board,game, false));
        getPromotionMove().ifPresent(move -> clone.promotionMove = move.clone(board,game, true));
        return clone;
    }

    void setBoardSnap(BoardSnapShot boardSnap) {
        this.boardSnap = boardSnap;
    }

    BoardSnapShot getBoardSnap() {
        return boardSnap;
    }

    public boolean isStrike() {
        return strike;
    }

    @Override
    public int hashCode() {
        int result = getMainMove() != null ? getMainMove().hashCode() : 0;
        result = 31 * result + getSecondaryMove().map(Move::hashCode).orElse(0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !PlayerMove.class.isAssignableFrom(o.getClass())) return false;

        PlayerMove that = (PlayerMove) o;

        if (getMainMove() != null ? !getMainMove().equals(that.getMainMove()) : that.getMainMove() != null)
            return false;
        return getSecondaryMove().isPresent() == that.getSecondaryMove().isPresent()
                && Objects.equals(getSecondaryMove().orElse(null), that.getSecondaryMove().orElse(null));
    }

    @Override
    final protected PlayerMove clone() {
        try {
            return (PlayerMove) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "PlayerMove{" +
                "mainMove=" + mainMove +
                ", secondaryMove=" + getSecondaryMove().map(Move::toString).orElse(null) +
                ", type=" + type +
                '}';
    }

    final protected void setType(PlayerMove move) {
        this.type = move.type;
    }

    private enum Type {
        NORMAL,
        CASTLING,
        PROMOTION,
    }

}

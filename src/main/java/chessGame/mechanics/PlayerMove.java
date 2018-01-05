package chessGame.mechanics;

import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;

import java.util.Objects;

/**
 *
 */
public final class PlayerMove implements Cloneable {
    private Move mainMove;
    private Move secondaryMove;
    private Move promotionMove;
    private Type type = Type.NORMAL;

    public PlayerMove(Move move, Move secondaryMove) {
        this.mainMove = move;
        this.secondaryMove = secondaryMove;
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

    private static boolean checkPromotionConditions(Move pawnMove, Move strike, Move promotion) {
        return false;
    }

    private static boolean checkCastlingConditions(Move kingMove, Move rookMove) {
        final Figure kingMoveFigure = kingMove.getFigure();
        final Figure rookMoveFigure = rookMove.getFigure();

        return kingMoveFigure.getType() != FigureType.KING ||
                rookMoveFigure.getType() != FigureType.ROOK ||
                !kingMoveFigure.getPlayer().equals(rookMoveFigure.getPlayer()) ||
                Math.abs(kingMove.getChange().getTo().getColumn() - rookMove.getChange().getTo().getColumn()) != 1;
    }

    public boolean isNormal() {
        return type == Type.NORMAL;
    }

    public Player getPlayer() {
        return mainMove.getFigure().getPlayer();
    }

    public Move getPromotionMove() {
        return isPromotion() ? promotionMove : null;
    }

    public boolean isPromotion() {
        return type == Type.PROMOTION;
    }

    public boolean isCastlingMove() {
        return type == Type.CASTLING;
    }

    public Move getMainMove() {
        return mainMove;
    }

    public Move getSecondaryMove() {
        return secondaryMove;
    }

    final public PlayerMove clone(Board board) {
        final PlayerMove clone = clone();
        if (clone == null) {
            return null;
        }
        clone.mainMove = getMainMove().clone(board);
        final Move secondaryMove = getSecondaryMove();

        if (secondaryMove != null) {
            clone.secondaryMove = secondaryMove.clone(board);
        }

        final Move promotionMove = getPromotionMove();

        if (promotionMove != null) {
            clone.promotionMove = promotionMove.clone(board);
        }
        return clone;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerMove that = (PlayerMove) o;

        if (getMainMove() != null ? !getMainMove().equals(that.getMainMove()) : that.getMainMove() != null) return false;
        return getSecondaryMove() != null ? getSecondaryMove().equals(that.getSecondaryMove()) : that.getSecondaryMove() == null;
    }

    @Override
    public int hashCode() {
        int result = getMainMove() != null ? getMainMove().hashCode() : 0;
        result = 31 * result + (getSecondaryMove() != null ? getSecondaryMove().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlayerMove{" +
                "first=" + getMainMove() +
                ", secondaryMove=" + getSecondaryMove() +
                '}';
    }

    private enum Type {
        NORMAL,
        CASTLING,
        PROMOTION,
    }

}

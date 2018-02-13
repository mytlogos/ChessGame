package chessGame.mechanics.move;

import chessGame.mechanics.Color;
import chessGame.mechanics.FigureType;
import chessGame.mechanics.Position;

import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public class PlayerMove implements Cloneable {
    private final Move mainMove;
    private final Move secondaryMove;
    private final boolean strike;
    private Move promotionMove;
    private Type type = Type.NORMAL;

    public PlayerMove(Move mainMove, Move secondaryMove) {
        Objects.requireNonNull(mainMove);
        this.mainMove = mainMove;
        this.secondaryMove = secondaryMove;

        strike = secondaryMove != null && !getColor().equals(secondaryMove.getColor());
    }

    public Color getColor() {
        return mainMove.getColor();
    }

    public static PlayerMove PromotionMove(Move pawnMove, Move strike, Move promotion) {
        Objects.requireNonNull(pawnMove);
        Objects.requireNonNull(promotion);

        if (pawnMove.getTo() != Position.Promoted) {
            throw new IllegalStateException();
        }

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
            System.err.println("Condition returns null");
            return null;
        }

        final PlayerMove playerMove = new PlayerMove(kingMove, rookMove);
        playerMove.type = Type.CASTLING;
        return playerMove;
    }

    private static boolean checkCastlingConditions(Move kingMove, Move rookMove) {
        final FigureType kingMoveFigure = kingMove.getFigure();
        final FigureType rookMoveFigure = rookMove.getFigure();

        return kingMoveFigure != FigureType.KING ||
                rookMoveFigure != FigureType.ROOK ||
                !kingMove.getColor().equals(rookMove.getColor()) ||
                Math.abs(kingMove.getTo().getColumn() - rookMove.getTo().getColumn()) != 1;
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

    public boolean isStrike() {
        return strike;
    }

    public boolean isWhite() {
        return getMainMove().isWhite();
    }

    public Move getMainMove() {
        return mainMove;
    }

    @Override
    public int hashCode() {
        int result = getMainMove() != null ? getMainMove().hashCode() : 0;
        result = 31 * result + getSecondaryMove().map(Move::hashCode).orElse(0);
        return result;
    }

    public Optional<Move> getSecondaryMove() {
        return Optional.ofNullable(secondaryMove);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !PlayerMove.class.isAssignableFrom(o.getClass())) return false;

        PlayerMove that = (PlayerMove) o;

        return (getMainMove() != null ? getMainMove().equals(that.getMainMove()) : that.getMainMove() == null)
                && getSecondaryMove().isPresent() == that.getSecondaryMove().isPresent() &&
                Objects.equals(getSecondaryMove().orElse(null), that.getSecondaryMove().orElse(null));
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

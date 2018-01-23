package chessGame.mechanics;

import chessGame.engine.Difficulty;

import java.util.Objects;

/**
 *
 */
public final class Player implements Cloneable {
    private PlayerType type;
    private boolean human = true;
    private Difficulty difficulty;

    public Player(PlayerType type) {
        Objects.requireNonNull(type);
        this.type = type;
    }

    public static Player getBlack() {
        return new Player(PlayerType.BLACK);
    }

    public static Player getWhite() {
        return new Player(PlayerType.WHITE);
    }

    public void setAI() {
        this.human = false;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isWhite() {
        return type == PlayerType.WHITE;
    }

    public PlayerType getType() {
        return type;
    }

    public boolean isHuman() {
        return human;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;
        return type == player.type;
    }

    @Override
    public final Player clone() {
        try {
            return (Player) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Player{" +
                "type=" + type +
                ", human=" + human +
                '}';
    }

    /**
     *
     */
    public enum PlayerType {
        WHITE,
        BLACK,;
    }
}

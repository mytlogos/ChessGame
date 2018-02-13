package chessGame.mechanics;

import chessGame.engine.Difficulty;

import java.util.Objects;

/**
 *
 */
public final class Player implements Cloneable {
    private final Color type;
    private boolean human = true;
    private Difficulty difficulty;

    private Player(Color type) {
        Objects.requireNonNull(type);
        this.type = type;
    }

    public static Player getBlack() {
        return new Player(Color.BLACK);
    }

    public static Player getWhite() {
        return new Player(Color.WHITE);
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
        return type == Color.WHITE;
    }

    public Color getColor() {
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
    public String toString() {
        return "Player{" +
                "type=" + type +
                ", human=" + human +
                '}';
    }

}

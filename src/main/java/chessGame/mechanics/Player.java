package chessGame.mechanics;

import chessGame.engine.Difficulty;
import chessGame.mechanics.figures.Figure;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
public final class Player implements Cloneable {
    private PlayerType type;
    private boolean human = true;
    private Difficulty difficulty;
    private List<Figure> figures = new ArrayList<>();

    public static Player getBlack() {
        return new Player(PlayerType.BLACK);
    }

    public static Player getWhite() {
        return new Player(PlayerType.WHITE);
    }

    public Player(PlayerType type) {
        Objects.requireNonNull(type);
        this.type = type;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setFigures(Collection<Figure> figures) {
        this.figures.clear();
        this.figures.addAll(figures);
    }

    public Collection<Figure> getFigures() {
        return figures;
    }

    public void setAI() {
        this.human = false;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isWhite() {
        return type == PlayerType.WHITE;
    }

    @Override
    public String toString() {
        return "Player{" +
                "type=" + type +
                ", human=" + human +
                '}';
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
            final Player clone = (Player) super.clone();
            clone.figures = new ArrayList<>();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
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

    /**
     *
     */
    public enum PlayerType {
        WHITE,
        BLACK,;
    }
}

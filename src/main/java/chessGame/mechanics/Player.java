package chessGame.mechanics;

import chessGame.engine.Difficulty;
import chessGame.mechanics.figures.Figure;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Objects;

/**
 *
 */
public class Player {
    private PlayerType type;
    private boolean human = true;
    private Difficulty difficulty;
    private ObservableList<Figure> figures = FXCollections.observableArrayList();

    public Player(PlayerType type) {
        Objects.requireNonNull(type);
        this.type = type;
        figures.addListener((ListChangeListener<? super Figure>) observable -> {
            if (observable.next()) {
                if (observable.getAddedSubList().contains(null)) {
                    System.out.println("hi");
                }
            }
        });
    }

    public void setFigures(Collection<Figure> figures) {
        this.figures.setAll(figures);
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

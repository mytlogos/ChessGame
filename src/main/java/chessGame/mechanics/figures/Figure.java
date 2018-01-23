package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a Chess Figure on a Board.
 * <p>
 * A Figure can have the same hashCode but is not necessarily equal.
 * This class overrides {@link #hashCode()} but not {@link #equals(Object)}.
 */
public abstract class Figure implements Serializable, Comparable<Figure>, Cloneable {
    private final FigureType type;
    private transient ObjectProperty<Position> position = new SimpleObjectProperty<>();
    private Player player;
    private transient Board board;

    private transient Image image;

    Figure(Position position, Player player, FigureType type, Board board) {
        this.board = board;
        this.player = player;
        this.type = type;
        this.position.set(position);

        initListener();
    }

    public boolean is(FigureType type) {
        return type == this.type;
    }

    private void initListener() {
        positionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
            }
        });
    }

    public ObjectProperty<Position> positionProperty() {
        return position;
    }

    public List<Position> getAllowedPositions() {
        return PositionGenerator.getAllowedPositions(this, board);
    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getPlayer().hashCode();
        return result;
    }

    public boolean checkBoard(Board board) {
        return board == this.board;
    }

    public Position getPosition() {
        return position.get();
    }

    public void setPosition(Position position) {
        this.position.set(position);
    }

    public FigureType getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    final public Figure clone() {
        try {
            final Figure clone = (Figure) super.clone();
            clone.position = new SimpleObjectProperty<>();
            clone.board = board;
            clone.initListener();
            clone.position.set(getPosition());
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "position=" + position.get() +
                ", player=" + player +
                ", type=" + type +
                '}';
    }

    @Override
    public int compareTo(Figure o) {
        if (o == this) return 0;
        if (o == null || !getClass().equals(o.getClass())) return -1;
        if (getPlayer() != o.getPlayer()) return -1;
        return getPosition().compareTo(o.getPosition());
    }

    public Image getImage() {
        if (image == null) {
            image = getType().getImage(getPlayer().getType());
        }
        return image;
    }

    public Figure clone(Board board) {
        final Figure clone = clone();
        if (clone != null) {
            clone.position = new SimpleObjectProperty<>(getPosition());
            clone.board = board;
            return clone;
        }
        return null;
    }
}

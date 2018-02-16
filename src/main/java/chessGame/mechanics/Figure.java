package chessGame.mechanics;

import javafx.scene.image.Image;

import java.io.Serializable;

/**
 * A class representing a Chess Figure on a Board<Figure>.
 * <p>
 * A Figure can have the same hashCode but is not necessarily equal.
 * This class overrides {@link #hashCode()} but not {@link #equals(Object)}.
 */
public final class Figure implements Serializable, Cloneable {
    private final FigureType type;
    private final Color player;

    private transient Image image;

    Figure(Color color, FigureType type) {
        this.player = color;
        this.type = type;
    }

    public boolean is(FigureType type) {
        return type == this.type;
    }


    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getColor().hashCode();
        return result;
    }


    public FigureType getType() {
        return type;
    }

    public char getNotation() {
        char notation = type.getNotation();
        return isWhite() ? notation : Character.toLowerCase(notation);
    }

    public Color getColor() {
        return player;
    }

    public boolean isWhite() {
        return getColor() == Color.WHITE;
    }

    @Override
    public String toString() {
        return "Figure{" +
                "type=" + type +
                ", player=" + player +
                '}';
    }

    public Image getImage() {
        if (image == null) {
            image = getType().getImage(getColor());
        }
        return image;
    }

}

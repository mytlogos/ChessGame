package chessGame.mechanics.figures;

import chessGame.mechanics.Board;
import chessGame.mechanics.Player;
import chessGame.mechanics.Position;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
public abstract class Figure implements Serializable, Comparable<Figure>, Cloneable {
    private transient ObjectProperty<Position> position = new SimpleObjectProperty<>();

    private static int counter;
    private final int id;
    private final FigureType type;
    private final Player player;
    transient Board board;

    private transient Image image;

    Figure(Position position, Player player, FigureType type, Board board) {
        this.board = board;
        this.player = player;
        this.type = type;
        this.position.set(position);
        positionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.equals(Position.Bench)) {
                    board.getBench().add(this);
                }
                if (oldValue != null && oldValue.equals(Position.Bench)) {
                    board.getBench().remove(this);
                }
            }
        });
        id = counter++;
    }

    public abstract List<Position> getAllowedPositions();

    public Position getPosition() {
        return position.get();
    }

    public void setPosition(Position position) {
        this.position.set(position);
    }

    public ObjectProperty<Position> positionProperty() {
        return position;
    }

    public Player getPlayer() {
        return player;
    }

    public FigureType getType() {
        return type;
    }

    final List<Position> getVertical(int max) {
        List<Position> positions = new ArrayList<>();

        final int row = getPosition().getRow();
        final int column = getPosition().getColumn();

        boolean backward = true;
        boolean forward = true;

        for (int i = 1; i < max + 1; i++) {
            if (backward) {
                backward = addPosition(row - i, column, positions);
            }
            if (forward) {
                forward = addPosition(row + i, column, positions);
            }
        }
        return positions;
    }

    final List<Position> getHorizontal(int max) {
        List<Position> positions = new ArrayList<>();

        final int row = getPosition().getRow();
        final int column = getPosition().getColumn();

        boolean left = true;
        boolean right = true;

        for (int i = 1; i < max + 1; i++) {

            if (left) {
                left = addPosition(row, column - i, positions);
            }
            if (right) {
                right = addPosition(row, column + i, positions);
            }
        }
        return positions;
    }

    final List<Position> getDiagonal(int max) {
        List<Position> positions = new ArrayList<>();

        final int row = getPosition().getRow();
        final int column = getPosition().getColumn();

        boolean rightForward = true;
        boolean leftForward = true;
        boolean rightBackward = true;
        boolean leftBackward = true;

        for (int i = 1; i < max + 1; i++) {
            if (leftBackward) {
                leftBackward = addPosition(row - i, column - i, positions);
            }
            if (rightBackward) {
                rightBackward = addPosition(row - i, column + i, positions);
            }
            if (rightForward) {
                rightForward = addPosition(row + i, column + i, positions);
            }
            if (leftForward) {
                leftForward = addPosition(row + i, column - i, positions);
            }
        }
        return positions;
    }

    final boolean addPosition(int newRow, int newColumn, Collection<Position> positions) {
        if (!Position.isInBoard(newRow, newColumn)) {
            return false;
        }
        Position position = Position.get(newRow, newColumn);
        final Figure figure = board.getFigure(position);

        if (figure == null) {
            positions.add(position);
            return true;
        } else {
            if (!figure.getPlayer().equals(getPlayer())) {
                positions.add(position);
            }
            return false;
        }
    }

    private void checkPositionState(Position position) {
        final Figure figure = board.getFigure(position);

        if (figure != null) {
            position.setEnemy(figure.getPlayer() != getPlayer());
            position.setEmpty(false);
        }
    }

    List<Position> checkPositions(Collection<Position> positions) {
        final List<Position> positionList = positions.
                stream().
                filter(Position::isInBoard).
                filter(((Predicate<Position>) Position::isAlly).negate()).
                collect(Collectors.toList());
        positionList.forEach(this::checkPositionState);

        return getPosition().equals(Position.Bench) ? new ArrayList<>() : positionList;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Figure figure = (Figure) o;

        return getPosition().equals(figure.getPosition())
                && getType() == figure.getType()
                && getPlayer().equals(figure.getPlayer());
    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getPlayer().hashCode();
        result = 31 * result + id;
        return result;
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
            image = getType().getImage(getPlayer());
        }
        return image;
    }

    final public Figure clone(Board board) {
        final Figure clone = clone();
        if (clone != null) {
            clone.board = board;
            return clone;
        }
        return null;
    }

    @Override
    final public Figure clone() {
        try {
            final Figure clone = (Figure) super.clone();
            clone.position = new SimpleObjectProperty<>(getPosition());
            clone.board = board;
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

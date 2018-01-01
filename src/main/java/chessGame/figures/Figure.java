package chessGame.figures;

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
public abstract class Figure implements Serializable, Comparable<Figure> {
    private transient ObjectProperty<Position> position = new SimpleObjectProperty<>();

    private final FigureType type;
    private final Player player;
    final transient Board board;

    private transient Image image;

    Figure(Position position, Player player, FigureType type, Board board) {
        this.board = board;
        this.position.set(position);
        this.player = player;
        this.type = type;
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

    List<Position> getVertical(int max) {
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

    List<Position> getHorizontal(int max) {
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

    List<Position> getDiagonal(int max) {
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

    private boolean addPosition(int newRow, int newColumn, Collection<Position> positions) {
        final Position position = new Position(newRow, newColumn);

        if (checkPositionIndex(position)) {
            final Figure figure = board.getFigure(position);
            positions.add(position);
            return figure == null;
        }
        return true;
    }

    void checkPositionState(Position position) {
        final Figure figure = board.getFigure(position);

        if (figure != null) {
            position.setEnemy(figure.getPlayer() != getPlayer());
            position.setEmpty(false);
        }
    }

    boolean checkPositionIndex(Position position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    List<Position> checkPositions(Collection<Position> positions) {
        final List<Position> positionList = positions.stream().filter(this::checkPositionIndex).filter(((Predicate<Position>) Position::isAlly).negate()).collect(Collectors.toList());
        positionList.forEach(this::checkPositionState);
        return positionList;
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

        if (!getPosition().equals(figure.getPosition())) return false;
        if (getType() != figure.getType()) return false;
        if (getPlayer() != figure.getPlayer()) return false;
        return board.equals(figure.board);
    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getPlayer().hashCode();
        result = 31 * result + board.hashCode();
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
}

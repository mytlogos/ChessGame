package chessGame;

import chessGame.figures.Figure;
import chessGame.mechanics.Position;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.ImageView;

/**
 *
 */
public class PositionPane extends Control {
    private Figure figure;

    public PositionPane(int row, int column) {
        this.position = new Position(row, column);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    private Position position;

    public Figure setFigure(Figure figure) {
        Figure old = this.figure;

        if (old != null && old != figure && old.getPlayer() == figure.getPlayer()) {
            throw new IllegalArgumentException();
        }

        if (old != null) {
            getChildren().remove(new ImageView(old.getImage()));
        }

        if (figure != null) {
            getChildren().add(new ImageView(figure.getImage()));
            figure.setPosition(getPosition());
        }
        this.figure = figure;
        return old;
    }

    public Position getPosition() {
        return position;
    }

    public Figure getFigure() {
        return figure;
    }

    @Override
    public String toString() {
        return "FigurePosition{" +
                "position=" + position +
                ", figure=" + figure +
                '}';
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PositionPaneSkin(this);
    }
}

package chessGame.gui.multiplayer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.shape.*;

/**
 *
 */
class Bubble extends Path {
    private DoubleProperty height = new SimpleDoubleProperty();
    private DoubleProperty width = new SimpleDoubleProperty();

    Bubble() {
        LineTo leftVertical = new LineTo(10, 20);
        ArcTo leftArc = new ArcTo();
        LineTo lowerHorizontal = new LineTo();
        ArcTo rightDownArc = new ArcTo();
        LineTo rightVertical = new LineTo();
        ArcTo rightUpArc = new ArcTo();

        getElements().add(new MoveTo(0,0));
        getElements().add(new LineTo(10,10));
        getElements().add(leftVertical);
        getElements().add(leftArc);
        getElements().add(lowerHorizontal);
        getElements().add(rightDownArc);
        getElements().add(rightVertical);
        getElements().add(rightUpArc);
        getElements().add(new ClosePath());

        leftVertical.setX(10);
        leftVertical.yProperty().bind(heightProperty().subtract(10));

        leftArc.setX(20);
        leftArc.yProperty().bind(heightProperty());

        lowerHorizontal.yProperty().bind(heightProperty());
        lowerHorizontal.xProperty().bind(widthProperty().subtract(10));

        rightDownArc.yProperty().bind(heightProperty().subtract(10));
        rightDownArc.xProperty().bind(widthProperty());

        rightVertical.setY(10);
        rightVertical.xProperty().bind(widthProperty());

        rightUpArc.xProperty().bind(widthProperty().subtract(10));
        rightUpArc.setY(0);

        setRadii(leftArc);
        setRadii(rightDownArc);
        setRadii(rightUpArc);
    }

    private void setRadii(ArcTo arc) {
        arc.setRadiusX(10);
        arc.setRadiusY(10);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public DoubleProperty widthProperty() {
        return width;
    }
}

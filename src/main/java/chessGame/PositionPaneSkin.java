package chessGame;

import javafx.geometry.Insets;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;

/**
 *
 */
public class PositionPaneSkin extends SkinBase<PositionPane> {
    private StackPane pane;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    PositionPaneSkin(PositionPane control) {
        super(control);
        pane = new StackPane();
        getChildren().add(pane);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Insets insets = pane.getInsets();
        return super.computeMinWidth(pane.getHeight(), insets.getTop(), insets.getRight(), insets.getBottom(), insets.getLeft());
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Insets insets = pane.getInsets();
        return super.computeMinWidth(pane.getWidth(), insets.getTop(), insets.getRight(), insets.getBottom(), insets.getLeft());
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Insets insets = pane.getInsets();
        return super.computeMinWidth(pane.getMaxWidth(), insets.getTop(), insets.getRight(), insets.getBottom(), insets.getLeft());
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Insets insets = pane.getInsets();
        return super.computeMinWidth(pane.getMaxHeight(), insets.getTop(), insets.getRight(), insets.getBottom(), insets.getLeft());    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Insets insets = pane.getInsets();
        return super.computeMinWidth(pane.getPrefWidth(), insets.getTop(), insets.getRight(), insets.getBottom(), insets.getLeft());
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Insets insets = pane.getInsets();
        return super.computeMinWidth(pane.getPrefHeight(), insets.getTop(), insets.getRight(), insets.getBottom(), insets.getLeft());
    }
}

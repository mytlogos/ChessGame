package chessGame.gui;

import chessGame.mechanics.PlayerMove;
import chessGame.mechanics.figures.Figure;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.SegmentedButton;

import java.util.List;

/**
 *
 */
class PromotionDialog extends Dialog<PlayerMove> {

    private final List<PlayerMove> promotions;

    PromotionDialog(List<PlayerMove> promotions) {
        if (!promotions.stream().allMatch(PlayerMove::isPromotion)) {
            throw new IllegalArgumentException("Es ist ein Zug dabei der keine Beförderung eines Bauerns ist.");
        }
        this.promotions = promotions;
        init();
    }


    private void init() {
        SegmentedButton button = new SegmentedButton();
        promotions.forEach(move -> {
            final Figure figure = move.getPromotionMove().orElseThrow(() -> new IllegalStateException("promotion is null")).getFigure();
            final Image image = figure.getType().getImage(figure.getPlayer().getType());

            final ToggleButton toggleButton = getToggleButton(image, move);
            button.getButtons().add(toggleButton);
        });
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }

    private ToggleButton getToggleButton(Image image, PlayerMove move) {
        final ToggleButton toggleButton = new ToggleButton();
        toggleButton.setGraphic(new ImageView(image));
        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            setResult(move);
            close();
        });

        return toggleButton;
    }


}

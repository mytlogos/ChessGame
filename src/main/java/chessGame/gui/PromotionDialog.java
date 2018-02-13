package chessGame.gui;

import chessGame.mechanics.FigureType;
import chessGame.mechanics.move.PlayerMove;
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
public class PromotionDialog extends Dialog<PlayerMove> {

    private final List<PlayerMove> promotions;

    public PromotionDialog(List<PlayerMove> promotions) {
        if (!promotions.stream().allMatch(PlayerMove::isPromotion)) {
            throw new IllegalArgumentException("Es ist ein Zug dabei der keine Beförderung eines Bauerns ist.");
        }
        this.promotions = promotions;
        init();
    }


    private void init() {
        SegmentedButton button = new SegmentedButton();

        promotions.forEach(move -> {
            final FigureType figure = move.getPromotionMove().orElseThrow(() -> new IllegalStateException("promotion is null")).getFigure();
            final Image image = figure.getImage(move.getColor());

            final ToggleButton toggleButton = getToggleButton(image, move);
            toggleButton.setPrefSize(50, 50);
            button.getButtons().add(toggleButton);
        });

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().setContent(button);
        setResultConverter(type-> null);
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

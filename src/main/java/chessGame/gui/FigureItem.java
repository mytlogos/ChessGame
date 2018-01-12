package chessGame.gui;

import chessGame.mechanics.Player;
import chessGame.mechanics.figures.FigureType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 *
 */
class FigureItem extends HBox{

    @FXML
    private Text lostFigureCounter;

    @FXML
    private ImageView lostFigureView;

    private IntegerProperty timesLost = new SimpleIntegerProperty();

    FigureItem(FigureType figure, Player.PlayerType player) {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lostFigureItem.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ignored) {
        }
        visibleProperty().bind(timesLost.isNotEqualTo(0));
        lostFigureCounter.textProperty().bind(timesLost.asString());
        lostFigureView.setImage(figure.getImage(player));
    }

    void reset() {
        timesLost.set(0);
    }

    int getTimesLost() {
        return timesLost.get();
    }

    ReadOnlyIntegerProperty timesLostProperty() {
        return timesLost;
    }

    void increment() {
        timesLost.set(timesLost.get() + 1);
    }
}

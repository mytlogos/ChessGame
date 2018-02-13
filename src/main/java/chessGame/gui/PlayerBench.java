package chessGame.gui;

import chessGame.mechanics.Color;
import chessGame.mechanics.FigureType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class PlayerBench extends VBox {

    @FXML
    private Text playerName;

    @FXML
    private VBox lostFigureContainer;

    private Color player;
    private final Map<FigureType, LostFigureItem> lostFigureItemMap = new HashMap<>();

    public PlayerBench() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/playerStatistics.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ignored) {
        }
        setBackground(null);
    }

    LostFigureItem getContainer(FigureType figureType) {
        return lostFigureItemMap.get(figureType);
    }

    void setPlayer(Color player) {
        this.player = player;
        init();

        if (player == Color.WHITE) {
            playerName.setText("Spieler Weiß");
        } else {
            playerName.setText("Spieler Schwarz");
        }
    }

    private void init() {
        Color type = player == Color.BLACK ? Color.WHITE : Color.BLACK;

        for (FigureType figureType : FigureType.values()) {
            final LostFigureItem item = new LostFigureItem(figureType, type);
            lostFigureItemMap.put(figureType, item);
            lostFigureContainer.getChildren().add(item);
        }
    }

    void reset() {
        lostFigureItemMap.values().forEach(LostFigureItem::reset);
    }

    class LostFigureItem extends HBox {

        private final FigureType figure;
        private final Color player;
        @FXML
        private Text lostFigureCounter;

        @FXML
        private ImageView lostFigureView;

        private final IntegerProperty timesLost = new SimpleIntegerProperty();

        LostFigureItem(FigureType figure, Color player) {
            this.figure = figure;
            this.player = player;
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

        void decrement() {
            timesLost.set(timesLost.get() - 1);
        }

        @Override
        public String toString() {
            return "LostFigureItem{" +
                    "figure=" + figure +
                    ", player=" + player +
                    ", timesLost=" + timesLost +
                    '}';
        }
    }
}

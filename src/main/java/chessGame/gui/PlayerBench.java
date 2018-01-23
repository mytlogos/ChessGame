package chessGame.gui;

import chessGame.mechanics.Player;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;
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

    private Player.PlayerType player;
    private Map<FigureType, LostFigureItem> lostFigureItemMap = new HashMap<>();

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

    void defeatedFigure(Figure figure) {
        lostFigureItemMap.get(figure.getType()).increment();
    }

    LostFigureItem getContainer(FigureType figureType) {
        return lostFigureItemMap.get(figureType);
    }

    void setPlayer(Player.PlayerType player) {
        this.player = player;
        init();

        if (player == Player.PlayerType.WHITE) {
            playerName.setText("Spieler Weiß");
        } else {
            playerName.setText("Spieler Schwarz");
        }
    }

    private void init() {
        Player.PlayerType type = player == Player.PlayerType.BLACK ? Player.PlayerType.WHITE : Player.PlayerType.BLACK;

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

        @FXML
        private Text lostFigureCounter;

        @FXML
        private ImageView lostFigureView;

        private IntegerProperty timesLost = new SimpleIntegerProperty();

        LostFigureItem(FigureType figure, Player.PlayerType player) {
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
}

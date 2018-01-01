package chessGame.gui;

import chessGame.figures.Figure;
import chessGame.figures.FigureType;
import chessGame.mechanics.Player;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class PlayerStatistics extends VBox {

    @FXML
    private Text playerName;

    @FXML
    private VBox lostFigureContainer;

    private Player player;

    public PlayerStatistics() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/playerStatistics.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ignored) {
        }
    }

    private Map<FigureType, LostFigureItem> lostFigureItemMap = new HashMap<>();

    public void loseFigure(Figure figure) {
        if (lostFigureItemMap.containsKey(figure.getType())) {
            lostFigureItemMap.get(figure.getType()).increment();
        } else {
            final LostFigureItem item = new LostFigureItem(figure);
            lostFigureItemMap.put(figure.getType(), item);
            lostFigureContainer.getChildren().add(item);
        }
    }

    public void setPlayer(Player player) {
        this.player = player;

        if (player == Player.BLACK) {
            playerName.setText("Spieler Schwarz");
        } else {
            playerName.setText("Spieler Weiß");

        }
    }
}

package chessGame.gui;

import chessGame.mechanics.Player;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Bench extends VBox {

    @FXML
    private Text playerName;

    @FXML
    private VBox lostFigureContainer;

    private Player player;

    public Bench() {
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

    public Node getContainer() {
        return lostFigureContainer;
    }

    public void setPlayer(Player player) {
        this.player = player;

        if (player.isWhite()) {
            playerName.setText("Spieler Weiß");
        } else {
            playerName.setText("Spieler Schwarz");
        }
    }

    public void reset() {
        lostFigureContainer.getChildren().clear();
        lostFigureItemMap.clear();
    }
}

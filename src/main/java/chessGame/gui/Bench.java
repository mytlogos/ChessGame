package chessGame.gui;

import chessGame.mechanics.Player;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class Bench extends VBox {

    @FXML
    private Text playerName;

    @FXML
    private VBox lostFigureContainer;

    private Player.PlayerType player;

    public Bench() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/playerStatistics.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ignored) {
        }
        setBackground(null);
    }

    private void init() {
        Player.PlayerType type = player == Player.PlayerType.BLACK ? Player.PlayerType.WHITE : Player.PlayerType.BLACK;

        for (FigureType figureType : FigureType.values()) {
            final LostFigureItem item = new LostFigureItem(figureType, type);
            lostFigureItemMap.put(figureType, item);
            lostFigureContainer.getChildren().add(item);
        }
    }

    private Map<FigureType, LostFigureItem> lostFigureItemMap = new HashMap<>();

    public void defeatedFigure(Figure figure) {
        lostFigureItemMap.get(figure.getType()).increment();
    }

    public LostFigureItem getContainer(FigureType figureType) {
        return lostFigureItemMap.get(figureType);
    }

    public void setPlayer(Player.PlayerType player) {
        this.player = player;
        init();

        if (player == Player.PlayerType.WHITE) {
            playerName.setText("Spieler Weiß");
        } else {
            playerName.setText("Spieler Schwarz");
        }
    }

    public void reset() {
        lostFigureItemMap.values().forEach(LostFigureItem::reset);
    }
}

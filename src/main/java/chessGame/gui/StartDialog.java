package chessGame.gui;

import chessGame.engine.Difficulty;
import chessGame.mechanics.ChessGame;
import chessGame.mechanics.Game;
import chessGame.mechanics.Player;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

/**
 *
 */
class StartDialog extends Dialog<Game> {

    private final ComboBox<Player.PlayerType> playerTypeBox = new ComboBox<>();
    private final ComboBox<Difficulty> difficultyBox = new ComboBox<>();

    private final Text playerText = new Text("Spieler: ");
    private final Text difficultyText = new Text("Schwierigkeitsgrad: ");
    private final ComboBox<PlayMode> playModeComboBox = new ComboBox<>();

    StartDialog() {
        final DialogPane pane = getDialogPane();
        pane.setPrefSize(200, 200);
        final ButtonType start = new ButtonType("Start", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().add(start);
        pane.getButtonTypes().add(new ButtonType("Schließen", ButtonBar.ButtonData.CANCEL_CLOSE));
        pane.lookupButton(start).disableProperty().bind(playModeComboBox.getSelectionModel().selectedItemProperty().isNull());

        pane.setContent(getContent());
        setResultConverter(param -> {
            if (param.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                final PlayMode item = playModeComboBox.getSelectionModel().getSelectedItem();
                final List<Player> players = item.getPlayer(this);
                return new ChessGame(players);
            }
            return null;
        });
    }

    private Pane getContent() {
        VBox content = new VBox();
        content.setSpacing(10);

        GridPane pane = new GridPane();
        pane.setHgap(5);
        pane.setVgap(5);

        content.getChildren().add(playModeComboBox);
        content.getChildren().add(pane);

        playModeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            pane.getChildren().clear();

            if (newValue == PlayMode.HUMAN_VS_AI) {
                pane.add(playerText, 0, 0);
                pane.add(playerTypeBox, 1, 0);
                pane.add(difficultyText, 0, 1);
                pane.add(difficultyBox, 1, 1);
            } else if (newValue == PlayMode.AI_VS_AI) {

            }
        });

        playModeComboBox.getItems().addAll(PlayMode.values());
        playModeComboBox.getSelectionModel().select(PlayMode.HUMAN_VS_AI);

        playerTypeBox.getItems().addAll(Player.PlayerType.values());
        playerTypeBox.getSelectionModel().select(Player.PlayerType.WHITE);

        difficultyBox.getItems().addAll(Difficulty.values());
        difficultyBox.getSelectionModel().select(Difficulty.EASY);
        return content;
    }

    private enum PlayMode {
        HUMAN_VS_HUMAN {
            @Override
            List<Player> getPlayer(StartDialog dialog) {
                return List.of(new Player(Player.PlayerType.BLACK), new Player(Player.PlayerType.WHITE));
            }

        },
        HUMAN_VS_AI {
            @Override
            List<Player> getPlayer(StartDialog dialog) {
                final Difficulty item = dialog.difficultyBox.getSelectionModel().getSelectedItem();

                final Player.PlayerType playerType = dialog.playerTypeBox.getSelectionModel().getSelectedItem();
                Player human = new Player(playerType);
                Player ai;

                if (playerType == Player.PlayerType.WHITE) {
                    ai = new Player(Player.PlayerType.BLACK);
                    ai.setDifficulty(item);
                } else {
                    ai = new Player(Player.PlayerType.WHITE);
                    ai.setDifficulty(item);
                }
                ai.setAI();
                return List.of(human, ai);
            }

        },
        AI_VS_AI {
            @Override
            List<Player> getPlayer(StartDialog dialog) {
                Player player1 = new Player(Player.PlayerType.WHITE);
                player1.setAI();

                Player player2 = new Player(Player.PlayerType.BLACK);
                player2.setAI();
                return List.of(player1, player2);
            }

        };

        abstract List<Player> getPlayer(StartDialog dialog);
    }

}

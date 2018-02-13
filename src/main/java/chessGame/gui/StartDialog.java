package chessGame.gui;

import chessGame.engine.Difficulty;
import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.game.ChessGameImpl;
import chessGame.mechanics.Color;
import chessGame.mechanics.game.Game;
import chessGame.mechanics.Player;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 *
 */
class StartDialog extends Dialog<ChessGame> {

    private final ComboBox<Color> playerTypeBox = new ComboBox<>();
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
                PlayerSet set = item.getPlayer(this);
                return new ChessGameImpl(set.getBlack(), set.getWhite());
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

        playerTypeBox.getItems().addAll(Color.values());
        playerTypeBox.getSelectionModel().select(Color.WHITE);

        difficultyBox.getItems().addAll(Difficulty.values());
        difficultyBox.getSelectionModel().select(Difficulty.EASY);
        return content;
    }

    private enum PlayMode {
        HUMAN_VS_HUMAN {
            @Override
            PlayerSet getPlayer(StartDialog dialog) {
                return new PlayerSet(Player.getBlack(), Player.getWhite());
            }

        },
        HUMAN_VS_AI {
            @Override
            PlayerSet getPlayer(StartDialog dialog) {
                final Difficulty item = dialog.difficultyBox.getSelectionModel().getSelectedItem();

                final Color color = dialog.playerTypeBox.getSelectionModel().getSelectedItem();

                Player human;
                Player ai;

                if (color == Color.WHITE) {
                    human = Player.getWhite();
                    ai = Player.getBlack();
                    ai.setDifficulty(item);
                } else {
                    human = Player.getBlack();
                    ai = Player.getWhite();
                    ai.setDifficulty(item);
                }
                ai.setAI();
                return new PlayerSet(human, ai);
            }

        },
        AI_VS_AI {
            @Override
            PlayerSet getPlayer(StartDialog dialog) {
                Player player1 = Player.getWhite();
                player1.setAI();
                player1.setDifficulty(Difficulty.EASY);

                Player player2 = Player.getBlack();
                player2.setAI();
                player2.setDifficulty(Difficulty.EASY);
                return new PlayerSet(player1, player2);
            }

        };

        abstract PlayerSet getPlayer(StartDialog dialog);
    }

    private static class PlayerSet {
        private Player white;
        private Player black;

        PlayerSet(Player player1, Player player2) {
            if (player1.isWhite()) {
                this.white = player1;
                this.black = player2;
            } else {
                this.white = player2;
                this.black = player1;
            }
        }

        Player getWhite() {
            return white;
        }

        Player getBlack() {
            return black;
        }
    }


}

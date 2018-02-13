package chessGame.gui;

import chessGame.mechanics.Color;
import chessGame.mechanics.Player;
import chessGame.mechanics.game.ChessGame;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Optional;

/**
 *
 */
public class ChessGameGui {
    private final Path arrow = getArrow();
    private final ObjectProperty<ChessGame> currentGame = new SimpleObjectProperty<>();
    @FXML
    private VBox whitePlayer;
    @FXML
    private VBox blackPlayer;
    @FXML
    private Button pauseBtn;
    @FXML
    private Button gameChangerBtn;
    @FXML
    private Text timer;
    @FXML
    private PlayerBench blackPlayerController;
    @FXML
    private PlayerBench whitePlayerController;
    @FXML
    private Pane blackPlayerArrow;
    @FXML
    private Pane whitePlayerArrow;
    @FXML
    private Pane root;
    @FXML
    private GridPane boardGrid;
    @FXML
    private Button redoBtn;
    @FXML
    private VBox chessContainer;

    private BoardGridManager manager;
    private HistoryDisplay historyDisplay;

    public void initialize() {
        whitePlayerController.setPlayer(Color.WHITE);
        blackPlayerController.setPlayer(Color.BLACK);

        manager = new BoardGridManager(this);
        timer.setText("0:00");
        pauseBtn.setDisable(true);

        historyDisplay = new HistoryDisplay();
        chessContainer.getChildren().add(historyDisplay);

        boardGrid.widthProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue));

        historyDisplay.prefWidthProperty().bind(boardGrid.widthProperty());
        historyDisplay.maxWidthProperty().bind(boardGrid.widthProperty());

        historyDisplay.gameProperty().bind(currentGame);
        manager.gameProperty().bind(currentGame);

        redoBtn.disableProperty().bind(currentGame.isNull());
    }

    public void openSettings() {
        Settings settings = new Settings();
        manager.orientationProperty().bind(settings.whiteOrientationProperty());
        Stage stage = new Stage();
        stage.setScene(new Scene(settings));
        stage.show();
    }

    public void exit() {
        Platform.exit();
    }

    void showPlayerAtMove(Player player) {
        blackPlayerArrow.getChildren().clear();
        whitePlayerArrow.getChildren().clear();
        if (!player.isWhite()) {
            blackPlayerArrow.getChildren().add(arrow);

        } else if (player.isWhite()) {
            whitePlayerArrow.getChildren().add(arrow);
        }
    }

    @FXML
    void redo() {
        currentGame.get().redo();
    }

    GridPane getBoardGrid() {
        return boardGrid;
    }

    PlayerBench getBench(boolean white) {
        if (white) {
            return whitePlayerController;
        } else {
            return blackPlayerController;
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode().isArrowKey()) {
            manager.moveFocus(event);
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            manager.setChosen();
        }
    }

    private Path getArrow() {
        final Path path = new Path();

        LineTo lineTo = new LineTo(-5, 0);
        LineTo lineTo1 = new LineTo(-5, 15);
        LineTo lineTo2 = new LineTo(-10, 15);
        LineTo lineTo3 = new LineTo(0, 25);
        LineTo lineTo4 = new LineTo(10, 15);
        LineTo lineTo5 = new LineTo(5, 15);
        LineTo lineTo6 = new LineTo(5, 0);
        LineTo lineTo7 = new LineTo(0, 0);

        path.getElements().add(new MoveTo(0, 0));
        path.getElements().add(lineTo);
        path.getElements().add(lineTo1);
        path.getElements().add(lineTo2);
        path.getElements().add(lineTo3);
        path.getElements().add(lineTo4);
        path.getElements().add(lineTo5);
        path.getElements().add(lineTo6);
        path.getElements().add(lineTo7);
        path.setStrokeWidth(2);
        path.setFill(javafx.scene.paint.Color.BLACK);
        return path;
    }

    @FXML
    private void changeGame() {
        if (currentGame.get() == null) {
            initGame();
        } else {
            if (currentGame.get().isFinished()) {
                initGame();
            } else if (currentGame.get().isRunning()) {
                currentGame.get().setPaused(true);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Möchten sie wirklich aufgeben?");
                final Optional<ButtonType> optional = alert.showAndWait();

                optional.ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        initGame();
                    } else {
                        currentGame.get().setPaused(false);
                    }
                });
            } else {
                timer.textProperty().unbind();
                timer.textProperty().bind(currentGame.get().timeProperty());
                currentGame.get().setRunning(true);
            }
        }
    }

    private void initGame() {
        Dialog<ChessGame> gameDialog = new StartDialog();
        final Optional<ChessGame> game = gameDialog.showAndWait();

        currentGame.set(game.orElse(null));

        final ChessGame current = currentGame.get();
        if (current == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Es konnte kein neues Spiel erstellt werden");
            alert.show();
        } else {
            pauseBtn.disableProperty().bind(current.runningProperty().not());

            current.runningProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    gameChangerBtn.setText("Aufgeben");
                } else {
                    gameChangerBtn.setText("Neues Spiel");
                }
            });

            current.setRunning(true);
            timer.textProperty().bind(current.timeProperty());

            whitePlayerController.reset();
            blackPlayerController.reset();

            current.finishedProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("finished " + newValue);
                if (newValue) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    if (current.isWon()) {
                        String player = current.getWinner().isWhite() ? "Weiß" : "Schwarz";
                        alert.setContentText("Spieler " + player + " hat gewonnen.");
                    } else if (current.isDraw()) {
                        alert.setContentText("Es ist ein unentschieden.");
                    }
                    alert.show();
                }
            });

        }
    }

    @FXML
    private void pause() {
        if (!currentGame.get().isPaused()) {
            currentGame.get().setPaused(true);
            pauseBtn.setText("Fortsetzen");
        } else {
            currentGame.get().setPaused(false);
            pauseBtn.setText("Pausieren");
        }
    }
}

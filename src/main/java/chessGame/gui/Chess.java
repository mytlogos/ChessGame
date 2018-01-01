package chessGame.gui;

import chessGame.mechanics.Board;
import chessGame.mechanics.Game;
import chessGame.mechanics.Player;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.util.Optional;

/**
 *
 */
public class Chess {
    @FXML
    private Button pauseBtn;

    @FXML
    private Button gameChangerBtn;

    @FXML
    private Text timer;

    @FXML
    private VBox blackPlayer;

    @FXML
    private PlayerStatistics blackPlayerController;

    @FXML
    private VBox whitePlayer;

    @FXML
    private PlayerStatistics whitePlayerController;

    @FXML
    private Group blackPlayerArrow;

    @FXML
    private Group whitePlayerArrow;

    @FXML
    private Pane root;

    @FXML
    private GridPane boardGrid;

    private BoardGrid board;
    private RoundManager manager;
    private Game current;

    public void initialize() {
        blackPlayerController.setPlayer(Player.BLACK);
        whitePlayerController.setPlayer(Player.WHITE);

        board = new BoardGrid(this, new Board());
        timer.setText("0:00");
        manager = new RoundManager(board);
        pauseBtn.setDisable(true);
    }

    private final Path arrow = getArrow();

    public void showPlayerAtMove(Player player) {
        if (player == Player.BLACK) {
            whitePlayerArrow.getChildren().clear();
            blackPlayerArrow.getChildren().add(arrow);

        } else if (player == Player.WHITE) {
            blackPlayerArrow.getChildren().clear();
            whitePlayerArrow.getChildren().add(arrow);
        } else {
            blackPlayerArrow.getChildren().clear();
            whitePlayerArrow.getChildren().clear();
        }
    }

    public GridPane getBoardGrid() {
        return boardGrid;
    }

    public void setTimer(String time) {
        timer.setText(time);
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
        path.setFill(Color.BLACK);
        return path;
    }

    @FXML
    private void changeGame() {
        if (current == null) {
            current = new Game(board);
            pauseBtn.disableProperty().bind(current.runningProperty().not());
            current.finishedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    gameChangerBtn.setText("Neues Spiel");
                } else {
                    gameChangerBtn.setText("Aufgeben");
                }
            });

            current.runningProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    gameChangerBtn.setText("Aufgeben");
                } else {
                    gameChangerBtn.setText("Neues Spiel");
                }
            });

            current.setRunning(true);
            timer.textProperty().bind(current.timeProperty());
        } else {
            if (current.isFinished()) {
                current.restart();
                timer.textProperty().unbind();
                timer.textProperty().bind(current.timeProperty());
            } else if (current.isRunning()) {
                current.setPaused(true);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Möchten sie wirklich aufgeben?");
                final Optional<ButtonType> optional = alert.showAndWait();

                optional.ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        current.restart();
                    } else {
                        current.setPaused(false);
                    }
                });
            } else {
                timer.textProperty().unbind();
                timer.textProperty().bind(current.timeProperty());
                current.setRunning(true);
            }
        }
    }

    @FXML
    private void pause() {
        if (!current.isPaused()) {
            current.setPaused(true);
            pauseBtn.setText("Fortsetzen");
        } else {
            current.setPaused(false);
            pauseBtn.setText("Pausieren");
        }
    }
}

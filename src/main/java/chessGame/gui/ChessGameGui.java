package chessGame.gui;

import chessGame.gui.multiplayer.ChatWindow;
import chessGame.gui.multiplayer.MultiPlayerWindow;
import chessGame.mechanics.Color;
import chessGame.mechanics.Player;
import chessGame.mechanics.game.ChessGame;
import chessGame.multiplayer.MultiPlayerGame;
import chessGame.multiplayer.PlayerClient;
import chessGame.settings.SetAble;
import chessGame.settings.SetAbleEntry;
import chessGame.settings.SetAbleManager;
import chessGame.settings.Settings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.Modality;

import java.io.IOException;
import java.util.Optional;

/**
 *
 */
public class ChessGameGui implements SetAble {
    private final Path arrow = getArrow();
    private final ObjectProperty<ChessGame> currentGame = new SimpleObjectProperty<>();

    @FXML
    private MenuBar menuBar;

    @FXML
    private chessGame.gui.MenuBar menuBarController;

    @FXML
    private Button multiPlayBtn;

    @FXML
    private HBox topContainer;
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

    private BooleanProperty autoLogin = new SimpleBooleanProperty();
    private BooleanProperty historyDisplayed = new SimpleBooleanProperty();

    private SetAbleManager setAbleManager;
    private BoardGridManager manager;
    private HistoryDisplay historyDisplay;
    private PlayerClient client = null;

    public ChessGameGui() {
        Settings.getSettings().register(this);
    }

    private ObjectProperty<ChessGame> currentGameProperty() {
        return currentGame;
    }

    ChessGame getCurrentGame() {
        return currentGameProperty().get();
    }

    public void initialize() {
        menuBarController.setMain(this);

        whitePlayerController.setPlayer(Color.WHITE);
        blackPlayerController.setPlayer(Color.BLACK);

        manager = new BoardGridManager(this);
        timer.setText("0:00");
        pauseBtn.setDisable(true);

        boardGrid.widthProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue));

        initHistory();

        manager.gameProperty().bind(currentGameProperty());

        redoBtn.disableProperty().bind(currentGameProperty().isNull());

        if (autoLogin.get()) {
            try {
                client = new PlayerClient();
            } catch (IOException e) {
                System.out.println("login failed");
                e.printStackTrace();
            }
        }
    }

    private void initHistory() {
        historyDisplay = new HistoryDisplay();
        historyDisplay.prefWidthProperty().bind(boardGrid.widthProperty());
        historyDisplay.maxWidthProperty().bind(boardGrid.widthProperty());
        historyDisplay.gameProperty().bind(currentGameProperty());

        historyDisplayed.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chessContainer.getChildren().add(historyDisplay);
            } else {
                chessContainer.getChildren().remove(historyDisplay);
            }
        });
    }

    @Override
    public SetAbleManager getManager() {
        if (setAbleManager == null) {
            SetAbleEntry loginEntry = new SetAbleEntry("autoLogin", autoLogin);
            SetAbleEntry historyEntry = new SetAbleEntry("historyDisplayed", historyDisplayed);
            setAbleManager = new SetAbleManager(loginEntry, historyEntry);
        }
        return setAbleManager;
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
        currentGameProperty().get().redo();
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

    PlayerClient getClient() {
        return client;
    }

    @FXML
    private void startMultiPlay() throws IOException {
        if (client == null) {
            client = new PlayerClient();
            root.getScene().getWindow().setOnCloseRequest(event -> client.closeClient());
        }

        Dialog<MultiPlayerGame> dialog = getMultiGameDialog();
        Optional<MultiPlayerGame> multiPlayerGame = dialog.showAndWait();

        if (multiPlayerGame.isPresent()) {
            MultiPlayerGame game = multiPlayerGame.get();
            currentGameProperty().set(game);
            configPreGameSetting(game);

            ChatWindow window = new ChatWindow(client);
            window.show();
        } else {
            showNoNewGameAlert();
        }
    }

    private Dialog<MultiPlayerGame> getMultiGameDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/onlineWindow.fxml"));
        Pane root = loader.load();
        MultiPlayerWindow controller = loader.getController();
        controller.setClient(client);

        Dialog<MultiPlayerGame> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.initModality(Modality.NONE);
        dialog.getDialogPane().setContent(root);

        client.gameProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                MultiPlayerGame game = client.getGame();

                if (game != null && (!game.isRunning() && !game.isFinished())) {
                    dialog.setResult(game);
                }

                dialog.close();
            }
        });

        dialog.setResultConverter(param -> {
            MultiPlayerGame game = client.getGame();

            if (game != null && (game.isRunning() || game.isFinished())) {
                return null;
            }
            return game;
        });
        return dialog;
    }

    void configPreGameSetting(ChessGame current) {
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

    private void showNoNewGameAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("Es konnte kein neues Spiel erstellt werden");
        alert.show();
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
        if (currentGameProperty().get() == null) {
            initGame();
        } else {
            if (currentGameProperty().get().isFinished()) {
                initGame();
            } else if (currentGameProperty().get().isRunning()) {
                currentGameProperty().get().setPaused(true);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Möchten sie wirklich aufgeben?");
                final Optional<ButtonType> optional = alert.showAndWait();

                optional.ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        initGame();
                    } else {
                        currentGameProperty().get().setPaused(false);
                    }
                });
            } else {
                timer.textProperty().unbind();
                timer.textProperty().bind(currentGameProperty().get().timeProperty());
                currentGameProperty().get().setRunning(true);
            }
        }
    }

    void setCurrentGame(ChessGame currentGame) {
        this.currentGameProperty().set(currentGame);
    }

    private void initGame() {
        Dialog<ChessGame> gameDialog = new StartDialog();
        final Optional<ChessGame> game = gameDialog.showAndWait();

        ChessGame chessGame = game.orElse(null);
        setCurrentGame(chessGame);

        if (chessGame == null) {
            showNoNewGameAlert();
        } else {
            configPreGameSetting(chessGame);
        }
    }

    @FXML
    private void pause() {
        if (!currentGameProperty().get().isPaused()) {
            currentGameProperty().get().setPaused(true);
            pauseBtn.setText("Fortsetzen");
        } else {
            currentGameProperty().get().setPaused(false);
            pauseBtn.setText("Pausieren");
        }
    }
}

package chessGame.gui;

import chessGame.multiplayer.Chat;
import chessGame.multiplayer.MultiPlayer;
import chessGame.multiplayer.PlayerClient;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import org.controlsfx.control.StatusBar;

import java.time.Instant;

/**
 *
 */
public class OnlineWindow {
    @FXML
    private ListView<MultiPlayer> onlinePlayersView;

    @FXML
    private ListView<Chat.Message> chatWindow;

    @FXML
    private StatusBar statusBar;

    @FXML
    private Text playerName;

    @FXML
    private Button hostButton;

    @FXML
    private TextField messageField;
    private PlayerClient client;

    public void setClient(PlayerClient client) {
        this.client = client;

        playerName.textProperty().bind(client.playerNameProperty());

        ObservableList<Chat.Message> messages = this.client.getAllChat().getMessages();
        Bindings.bindContent(chatWindow.getItems(), messages);
        Bindings.bindContent(onlinePlayersView.getItems(), client.getOnlinePlayerList());
    }

    public void initialize() {
        chatWindow.setCellFactory(param -> new MessageCell());
        onlinePlayersView.setItems(FXCollections.observableArrayList(this::extractMultiPlayerObservables));

        onlinePlayersView.setCellFactory(param -> new ListCell<MultiPlayer>() {

            @Override
            protected void updateItem(MultiPlayer item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String content;
                    if (item.isHosting()) {
                        content = "Hosting";
                        setGraphic(new Button());
                    } else if (item.isInGame()) {
                        content = "InGame";
                        setGraphic(null);

                    } else {
                        content = "Idle";
                        setGraphic(null);

                    }
                    setText(content + " " + item.getName());
                }
            }
        });

        onlinePlayersView.setOnMouseClicked(this::acceptHosting);
//        Platform.runLater(() -> onlinePlayersView.getScene().getWindow().setOnCloseRequest(event -> client.closeClient()));
    }

    private Observable[] extractMultiPlayerObservables(MultiPlayer param) {
        Observable[] observables = new Observable[2];
        observables[0] = param.hostingProperty();
        observables[1] = param.inGameProperty();
        return observables;
    }

    @FXML
    private void sendMessage() {
        String text = messageField.getText();

        if (text.isEmpty()) {
            return;
        }

        if (text.contains("$")) {
            Popup popup = new Popup();
            StackPane pane = new StackPane();
            pane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
            pane.setPrefSize(100, 100);
            pane.getChildren().add(new Text("Der Charakter $ ist nicht erlaubt!"));
            popup.getContent().add(pane);
            popup.setAutoHide(true);
            Bounds bounds = messageField.localToScreen(messageField.getBoundsInLocal());
            popup.show(messageField, bounds.getMinX() - 100, bounds.getMinY() - 100);
            return;
        }

        long epochMilli = Instant.now().toEpochMilli();
        String playerName = client.getPlayerName();

        Chat.Message message = new Chat.Message(epochMilli, text, playerName);
        client.writeMessage(message);
        messageField.clear();
    }

    @FXML
    private void startHosting() {
        if (client.isHost()) {
            client.stopHosting();
            hostButton.setText("Host Game");
        } else {
            hostButton.setText("Stop Hosting");
            client.startHost();
        }
    }

    private void acceptHosting(MouseEvent event) {
        MultiPlayer item = onlinePlayersView.getSelectionModel().getSelectedItem();

        if (event.getClickCount() >= 2) {
//            onlinePlayersView.edit(onlinePlayersView.getItems().indexOf(item));
            // todo implement editing of playerName
        } else {

            if (item == null || !item.isHosting()) {
                return;
            }

            client.acceptHost(item.getName());
        }

    }
}

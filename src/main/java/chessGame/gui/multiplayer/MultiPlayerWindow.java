package chessGame.gui.multiplayer;

import chessGame.multiplayer.Chat;
import chessGame.multiplayer.MultiPlayer;
import chessGame.multiplayer.PlayerClient;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;

import java.time.Instant;

/**
 *
 */
public class MultiPlayerWindow {
    @FXML
    private ListView<MultiPlayer> onlinePlayersView;

    @FXML
    private ListView<Chat.Message> chatWindow;

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
        chatWindow.setItems(messages);
        onlinePlayersView.setItems(client.getOnlinePlayerList());
    }

    public void initialize() {
        chatWindow.setCellFactory(param -> new ChatBubble());
        onlinePlayersView.setCellFactory(param -> new MultiPlayerCell());
        onlinePlayersView.setOnMouseClicked(this::acceptHosting);
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

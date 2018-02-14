package chessGame.gui;

import chessGame.multiplayer.Chat;
import chessGame.multiplayer.PlayerClient;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Instant;

/**
 *
 */
public class ChatWindow {

    private final Stage stage;
    private final PlayerClient client;

    @FXML
    private TextField messageField;

    @FXML
    private ListView<Chat.Message> chatView;

    ChatWindow(PlayerClient client) throws IOException {
        this.client = client;

        stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
        loader.setController(this);
        Pane load = loader.load();

        stage.setScene(new Scene(load));
        stage.setAlwaysOnTop(true);
        stage.titleProperty().bind(client.playerNameProperty().concat(" -Chat"));

        ObservableList<Chat.Message> messages = client.getGameChat().getMessages();
        Bindings.bindContent(chatView.getItems(), messages);
        chatView.setCellFactory(param -> new MessageCell());
    }

    void show() {
        stage.show();
    }

    void close() {
        stage.close();
    }

    @FXML
    private void sendMessage() {
        String text = messageField.getText();

        if (text.isEmpty()) {
            return;
        }

        //at the moment text is not allowed to contain the separator for messages between server/client
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
}

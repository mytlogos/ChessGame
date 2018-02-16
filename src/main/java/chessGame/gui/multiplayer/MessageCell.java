package chessGame.gui.multiplayer;

import chessGame.multiplayer.Chat;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 */
class MessageCell extends ListCell<Chat.Message> {
    private HBox container;
    private Text timeStamp;
    private Text content;
    private Text name;


    MessageCell() {
        container = new HBox();
        container.setSpacing(5);
    }


    @Override
    protected void updateItem(Chat.Message item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            LocalDateTime time = Instant.ofEpochMilli(item.getTimeStamp()).atZone(ZoneOffset.systemDefault()).toLocalDateTime();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM. HH:mm:ss");
            String stamp = time.format(formatter);

            setText("[" + stamp + "] " + item.getPlayerName() + ": " + item.getContent());
        }
    }
}

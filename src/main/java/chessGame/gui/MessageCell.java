package chessGame.gui;

import chessGame.multiplayer.Chat;
import javafx.scene.control.ListCell;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 *
 */
public class MessageCell extends ListCell<Chat.Message> {

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

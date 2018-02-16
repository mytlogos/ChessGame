package chessGame.gui.multiplayer;

import chessGame.multiplayer.Chat;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 *
 */
class ChatBubble extends ListCell<Chat.Message> {

    private final StackPane container = new StackPane();
    private final Bubble bubble = new Bubble();
    private Text name = new Text();
    private Text time = new Text();
    private Text content = new Text();
    private final VBox messageContainer;

    ChatBubble() {
        messageContainer = new VBox();

        HBox nameContainer = new HBox();
        HBox timeContainer = new HBox();

        messageContainer.getChildren().add(nameContainer);
        messageContainer.getChildren().add(content);
        messageContainer.getChildren().add(timeContainer);

        container.getChildren().add(bubble);
        container.getChildren().add(messageContainer);
        nameContainer.getChildren().add(name);
        timeContainer.getChildren().add(time);

        messageContainer.setSpacing(10);
        messageContainer.setPadding(new Insets(10));
        StackPane.setMargin(messageContainer, new Insets(0, 0, 0, 10));

        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        VBox.setVgrow(nameContainer, Priority.NEVER);
        VBox.setVgrow(timeContainer, Priority.NEVER);
        timeContainer.setAlignment(Pos.BOTTOM_RIGHT);

        content.wrappingWidthProperty().bind(widthProperty().subtract(50));

        bubble.setRotationAxis(new Point3D(0, 1, 0));
        bubble.widthProperty().bind(messageContainer.widthProperty().add(10));
        bubble.heightProperty().bind(messageContainer.heightProperty());

        bubble.setStroke(null);
        bubble.setFill(Color.DODGERBLUE);
        bubble.setOpacity(0.5);

        setPrefWidth(0);
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setBackground(null);
            }
        });
    }

    @Override
    protected void updateItem(Chat.Message item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setGraphic(container);
            bindMessage(item);
        }
    }

    private void setBubbleAlignmentRight(boolean alignmentRight) {
        bubble.setRotate(alignmentRight ? 180 : 0);
        Insets insets;

        if (alignmentRight) {
            insets = new Insets(0, 10, 0, 0);
        } else {
            insets = new Insets(0, 0, 0, 10);
        }
        StackPane.setMargin(messageContainer, insets);
    }

    private void bindMessage(Chat.Message message) {
        name.textProperty().unbind();
        name.textProperty().bind(message.nameProperty());

        long timeStamp = message.getTimeStamp();
        String timeString = Instant.
                ofEpochMilli(timeStamp).
                atZone(ZoneId.systemDefault()).
                toLocalTime().
                format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        time.setText(timeString);
        content.setText(message.getContent());

        setBubbleAlignmentRight(message.isOwnMessage());
    }
}

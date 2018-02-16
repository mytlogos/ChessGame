package chessGame.multiplayer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import java.util.Comparator;
import java.util.Objects;

/**
 *
 */
public class Chat {
    private final ObservableList<Message> messages = FXCollections.observableArrayList(this::extractObservables);
    private final SortedList<Message> sortedMessages = messages.sorted(Comparator.comparingLong(Message::getTimeStamp));

    public ObservableList<Message> getMessages() {
        return sortedMessages;
    }

    public void addMessage(Message message) {
        Platform.runLater(() -> messages.add(message));
    }

    public void renameUser(String oldName, String newName) {
        messages.stream().filter(message -> message.getPlayerName().equals(oldName)).forEach(message -> message.setPlayerName(newName));
    }

    private Observable[] extractObservables(Message message) {
        Observable[] observables = new Observable[1];
        observables[0] = message.nameProperty();
        return observables;
    }

    public static class Message implements Comparable<Message> {
        private long timeStamp;
        private String message;
        private StringProperty name = new SimpleStringProperty();
        private boolean isOwnMessage = false;

        public Message(long timeStamp, String message, String playerName) {
            this.timeStamp = timeStamp;
            this.message = message;
            this.name.set(playerName);
        }

        public boolean isOwnMessage() {
            return isOwnMessage;
        }

        public void setOwnMessage(String playerName) {
            isOwnMessage = getPlayerName().equals(playerName);
        }

        public String getPlayerName() {
            return name.get();
        }

        private void setPlayerName(String name) {
            Objects.requireNonNull(name);
            Platform.runLater(() -> this.name.set(name));
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public String getContent() {
            return message;
        }

        @Override
        public int compareTo(Message o) {
            return (int) (timeStamp - o.timeStamp);
        }

        public ReadOnlyStringProperty nameProperty() {
            return name;
        }
    }
}


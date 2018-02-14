package chessGame.multiplayer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import java.util.Comparator;

/**
 *
 */
public class Chat {
    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    private final SortedList<Message> sortedMessages = messages.sorted(Comparator.comparingLong(Message::getTimeStamp));

    public ObservableList<Message> getMessages() {
        return sortedMessages;
    }

    public void addMessage(Message message) {
        Platform.runLater(()-> messages.add(message));
    }

    public static class Message implements Comparable<Message> {
        private long timeStamp;
        private String message;
        private String playerName;

        public Message(long timeStamp, String message, String playerName) {
            this.timeStamp = timeStamp;
            this.message = message;
            this.playerName = playerName;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public String getContent() {
            return message;
        }

        public String getPlayerName() {
            return playerName;
        }

        @Override
        public int compareTo(Message o) {
            return (int) (timeStamp - o.timeStamp);
        }
    }
}


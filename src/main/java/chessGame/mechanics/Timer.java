package chessGame.mechanics;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.util.Duration;

/**
 *
 */
class Timer {
    private Timeline timeline;
    private StringProperty time = new SimpleStringProperty();
    private IntegerProperty second = new SimpleIntegerProperty();
    private IntegerProperty minute = new SimpleIntegerProperty();
    private IntegerProperty hour = new SimpleIntegerProperty();

    Timer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), this::increment));
        timeline.setCycleCount(Timeline.INDEFINITE);
        bind();
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public ReadOnlyStringProperty timeProperty() {
        return time;
    }

    private void bind() {
        time.bind(Bindings.createStringBinding(()->{
            String time;
            final int hour = this.hour.get();
            final int minute = this.minute.get();
            final int second = this.second.get();

            time = getPuffedUp(second);
            time = getPuffedUp(minute) + ":" + time;
            return hour + ":" + time;
        },second));
    }

    private String getPuffedUp(int second) {
        String time;
        if (second < 10) {
            time = "0" + String.valueOf(second);
        } else {
            time = String.valueOf(second);
        }
        return time;
    }

    private void increment(ActionEvent event) {
        final int s = second.get();
        if (s == 59) {
            final int m = this.minute.get();
            if (m == 59) {
                minute.set(0);
                hour.set(hour.get() + 1);
            } else {
                minute.set(m + 1);
            }
            second.set(0);
        } else {
            second.set(s + 1);
        }
    }
}

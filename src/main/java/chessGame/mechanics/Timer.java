package chessGame.mechanics;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.util.Duration;

/**
 *
 */
public final class Timer {
    private final Duration duration;
    private Timeline timeline;
    private StringProperty time = new SimpleStringProperty();
    private BooleanProperty timeUp = new SimpleBooleanProperty();
    private ObjectProperty<Duration> timeDuration = new SimpleObjectProperty<>();

    public Timer() {
        this(Duration.INDEFINITE);
    }

    public Timer(Duration duration) {
        if (duration.isIndefinite() || duration.equals(Duration.ZERO)) {
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), this::increment));
            this.duration = Duration.ZERO;
        } else {
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), this::decrement));
            this.duration = duration;
        }
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeDuration.set(this.duration);
        bind();
    }


    private void bind() {
        time.bind(Bindings.createStringBinding(() -> {
            String time;
            Duration duration = timeDuration.get();
            long seconds = (long) duration.toSeconds();
            long second = seconds % 60;
            long minute = seconds / 60;
            long hour = minute / 60;

            time = getPuffedUp(second);
            time = getPuffedUp(minute) + ":" + time;
            return hour + ":" + time;
        }, timeDuration));
    }

    private String getPuffedUp(long second) {
        String time;
        if (second < 10) {
            time = "0" + String.valueOf(second);
        } else {
            time = String.valueOf(second);
        }
        return time;
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public ReadOnlyBooleanProperty timeUpProperty() {
        return timeUp;
    }

    public void restart() {
        timeUp.set(false);
        timeDuration.set(duration);
        timeline.playFromStart();
    }

    public ReadOnlyStringProperty timeProperty() {
        return time;
    }

    private void decrement(ActionEvent actionEvent) {
        Duration duration = timeDuration.get();
        Duration newDuration = duration.subtract(Duration.seconds(1));
        timeDuration.set(newDuration);

        if (newDuration.lessThanOrEqualTo(Duration.ZERO)) {
            stop();
            timeUp.set(true);
        }
    }

    private void increment(ActionEvent event) {
        Duration duration = timeDuration.get();
        Duration newDuration = duration.add(Duration.seconds(1));
        timeDuration.set(newDuration);
    }
}

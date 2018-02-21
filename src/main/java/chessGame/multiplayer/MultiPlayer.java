package chessGame.multiplayer;

import chessGame.mechanics.Color;
import chessGame.mechanics.Player;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 */
public class MultiPlayer extends Player {
    private String name;
    private BooleanProperty inGame = new SimpleBooleanProperty();
    private BooleanProperty hosting = new SimpleBooleanProperty();

    MultiPlayer(String name) {
        super(Color.WHITE);
        this.name = name;
    }

    public MultiPlayer(String name, Color color) {
        super(color);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isInGame() {
        return inGame.get();
    }

    public BooleanProperty inGameProperty() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        Platform.runLater(()->this.inGame.set(inGame));
    }

    public boolean isHosting() {
        return hosting.get();
    }

    public BooleanProperty hostingProperty() {
        return hosting;
    }

    public void setHosting(boolean hosting) {
        Platform.runLater(() -> this.hosting.set(hosting));
    }

    @Override
    public void setType(Color type) {
        super.setType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiPlayer that = (MultiPlayer) o;

        return getName().equals(that.getName());
    }

    @Override
    public String toString() {
        return "MultiPlayer{" +
                "type=" + getColor() + "," +
                "name='" + getName() + '\'' +
                '}';
    }
}

package chessGame.gui.multiplayer;

import chessGame.multiplayer.MultiPlayer;
import javafx.scene.control.ListCell;

/**
 *
 */
class MultiPlayerCell extends ListCell<MultiPlayer> {
    @Override
    protected void updateItem(MultiPlayer item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            String content;
            if (item.isHosting()) {
                content = "Hosting";
            } else if (item.isInGame()) {
                content = "InGame";
            } else {
                content = "Idle";
            }
            setText(content + " " + item.getName());
        }
    }
}

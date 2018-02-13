package chessGame.gui;

import com.sun.org.apache.bcel.internal.generic.NEW;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 *
 */
public class Settings extends HBox {
    @FXML
    private ComboBox<SideOrientation> whiteOrientation;


    Settings() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ReadOnlyObjectProperty<SideOrientation> whiteOrientationProperty() {
        return whiteOrientation.getSelectionModel().selectedItemProperty();
    }

    public void initialize() {
        whiteOrientation.getItems().addAll(SideOrientation.values());
        whiteOrientation.getSelectionModel().select(SideOrientation.UP);
    }
}

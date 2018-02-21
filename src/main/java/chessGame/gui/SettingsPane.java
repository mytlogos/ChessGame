package chessGame.gui;

import chessGame.settings.Settings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 *
 */
public class SettingsPane extends VBox {
    @FXML
    private ComboBox<SideOrientation> whiteOrientation;

    @FXML
    private CheckBox historyDisplayCheck;

    @FXML
    private CheckBox autoLoginCheck;

    @FXML
    private Slider animateSpeedSlider;

    @FXML
    private ComboBox<?> styleBox;

    @FXML
    private Pane blackTileView;

    @FXML
    private ColorPicker blackColorPicker;

    @FXML
    private Pane whiteTileView;

    @FXML
    private ColorPicker whiteColorPicker;

    SettingsPane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SettingsPane get() {
        return null;
    }

    public void initialize() {
        whiteOrientation.getItems().addAll(SideOrientation.values());
        whiteOrientation.getSelectionModel().select(SideOrientation.UP);
    }

    ReadOnlyObjectProperty<SideOrientation> whiteOrientationProperty() {
        return whiteOrientation.getSelectionModel().selectedItemProperty();
    }

    @FXML
    private void reset() {
        Settings.getSettings().resetToDefault();
    }

    @FXML
    private void close() {

    }

    @FXML
    private void save() {

    }
}

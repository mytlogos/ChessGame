package chessGame.gui;

import chessGame.data.GameConverter;
import chessGame.data.GameFileManager;
import chessGame.mechanics.game.ChessGame;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class MenuBar {

    @FXML
    private javafx.scene.control.MenuBar root;

    @FXML
    private Menu openGameMenu;

    private GameFileManager manager = new GameFileManager();
    private ChessGameGui gameGui;


    public void initialize() {

    }

    void setMain(ChessGameGui gameGui) {
        this.gameGui = gameGui;
    }

    @FXML
    private void openSettings() {
        SettingsPane settings = new SettingsPane();
        Stage stage = new Stage();
        stage.setScene(new Scene(settings));
        stage.show();
    }

    @FXML
    private void exit() {
        Window window = root.getScene().getWindow();
        window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void openAbout() {

    }

    @FXML
    private void openGameChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(manager.savedGamesDirectory));
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Portable Game Notation", "*.pgn");
        chooser.getExtensionFilters().add(filter);
        chooser.setSelectedExtensionFilter(filter);

        File file = chooser.showOpenDialog(root.getScene().getWindow());

        if (file != null) {
            loadGame(file);
        }
    }

    private void loadGame(File file) {
        String content = manager.loadFile(file);

        try {
            ChessGame game = GameConverter.convert(content);
            gameGui.setCurrentGame(game);
            gameGui.configPreGameSetting(game);
        } catch (ParseException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Konvertieren der Datei in ein Spiel ist fehlgeschlagen. Möglicher invalider Inhalt.");
            alert.show();
        }

    }

    @FXML
    private void openScenarios() {
        Collection<File> files = manager.loadScenarios();
        //todo present them in panels with images or small chessboard as preview with title
    }

    @FXML
    private void saveGame() {
        ChessGame game = gameGui.getCurrentGame();

        File file;
        if (game != null && (file = manager.save(game)) != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Das Spiel wurde in " + file.getAbsolutePath() + " gespeichert");
            alert.show();
        }
    }

    @FXML
    private void loadFiles() {
        openGameMenu.getItems().clear();

        List<MenuItem> items = manager.loadAvailable().stream().map(this::createItem).collect(Collectors.toList());
        openGameMenu.getItems().addAll(items);
    }

    private MenuItem createItem(File file) {
        MenuItem item = new MenuItem();
        item.setOnAction(event -> loadGame(file));
        String name = file.getName();
        item.setText(name.substring(0, name.indexOf(".pgn")));
        return item;
    }
}

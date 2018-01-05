package chessGame.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 */
public class Start extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Pane pane = FXMLLoader.load(getClass().getResource("/fxml/chess.fxml"));
        pane.setPrefSize(1000,1000);
        pane.getStylesheets().add("/css/chess.css");
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

}

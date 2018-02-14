package chessGame.gui;

import chessGame.multiplayer.PlayerClient;
import chessGame.multiplayer.Server;
import chessGame.multiplayer.SocketWrapper;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

import static chessGame.multiplayer.PlayerClient.isServerHost;

/**
 *
 */
public class Start extends Application {

    public static void main(String[] args) {
        new Thread(Start::startServer).start();
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        SvgImageLoaderFactory.install();

        final Pane pane = FXMLLoader.load(getClass().getResource("/fxml/chess.fxml"));
        pane.setPrefSize(1000, 1000);
        pane.getStylesheets().add("/css/chess.css");
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();

    }

    private static void startServer() {
        try {
            if (isServerHost()) {
                try {
                    Socket socket = new Socket(PlayerClient.serverHost, PlayerClient.port);
                    new SocketWrapper(socket).logOutClient();
                    System.out.println("Server is up");
                } catch (IOException e) {
                    System.out.println("Server is not up");
                    Server.startServer();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

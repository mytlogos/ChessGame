package chessGame.gui;

import chessGame.figures.Figure;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 *
 */
public class LostFigureItem extends HBox{

    @FXML
    private Text lostFigureCounter;

    @FXML
    private ImageView lostFigureView;

    private int timesLost = 0;

    public LostFigureItem(Figure figure) {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lostFigureItem.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ignored) {
        }
        timesLost++;
        lostFigureCounter.setText(String.valueOf(timesLost));
        lostFigureView.setImage(figure.getImage());
    }

    public void increment() {
        lostFigureCounter.setText(String.valueOf(++timesLost));
    }
}

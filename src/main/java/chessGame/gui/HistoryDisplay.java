package chessGame.gui;

import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.move.Move;
import chessGame.mechanics.move.MoveHistory;
import chessGame.mechanics.move.PlayerMove;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 *
 */
public class HistoryDisplay extends VBox {
    private ObjectProperty<ChessGame> game = new SimpleObjectProperty<>();
    private ChangeListener<Number> roundListener = (observable, oldValue, newValue) -> processRoundChange(newValue);
    private TableView<String> historyRoundsView = new TableView<>();

    HistoryDisplay() {
        game.addListener((observable, oldValue, newValue) -> bindGame(newValue, oldValue));
        getChildren().add(historyRoundsView);
        setMaxHeight(200);

        initTable();
    }

    private void initTable() {
        historyRoundsView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        historyRoundsView.getColumns().add(getIndexColumn());

        TableColumn<String, String> whiteColumn = new TableColumn<>("Weiß");
        whiteColumn.setCellValueFactory(p -> {
            String value = p.getValue();
            int separator = value.indexOf('%');
            return separator != -1 ?
                    new SimpleStringProperty(value.substring(0, separator))
                    : new SimpleStringProperty(value);
        });

        TableColumn<String, String> blackColumn = new TableColumn<>("Schwarz");
        blackColumn.setCellValueFactory(p -> {
            String value = p.getValue();
            int separator = value.indexOf('%');
            return new SimpleStringProperty(value.substring(separator + 1, value.length()));
        });

        historyRoundsView.getColumns().add(whiteColumn);
        historyRoundsView.getColumns().add(blackColumn);
    }

    private void bindGame(ChessGame newValue, ChessGame oldValue) {
        newValue.roundProperty().addListener(roundListener);

        if (oldValue != null) {
            oldValue.roundProperty().removeListener(roundListener);
        }

        resetHistory();
    }

    /**
     * Sets the IndexColumn to increment the value by one, to match
     * the position of the row in the tableView.
     */
    private TableColumn<String, Number> getIndexColumn() {
        TableColumn<String, Number> indexColumn = new TableColumn<>("Runde");
        indexColumn.setPrefWidth(50);
        indexColumn.setMaxWidth(50);
        indexColumn.setMinWidth(50);
        indexColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>((historyRoundsView.getItems().indexOf(p.getValue()) + 1)));
        return indexColumn;
    }

    private void resetHistory() {
        historyRoundsView.getItems().clear();
        MoveHistory history = getGame().getHistory();

        for (int i = 0; i < history.size(); i++) {
            int round = getRound(i + 1);
            PlayerMove move = history.moveAtPly(i);

            String moveAsString = getMoveAsString(move);
            addItem(moveAsString, move.isWhite());
        }
    }

    ChessGame getGame() {
        return game.get();
    }

    private int getRound(Number newRound) {
        return (newRound.intValue() + 1) >> 1;
    }

    private String getMoveAsString(PlayerMove move) {
        Move mainMove = move.getMainMove();

        String result;

        if (move.isCastlingMove()) {
            result = "Castle to " + mainMove.getTo().notation();
        } else {
            if (move.isPromotion()) {
                Move promotion = move.getPromotionMove().get();
                result = mainMove.getFigure() + " " + mainMove.getFrom().notation() + " -> " + promotion.getTo().notation() + " promoted to " + promotion.getFigure();
            } else {
                result = mainMove.getFigure() + " " + mainMove.getFrom().notation() + " -> " + mainMove.getTo().notation();
            }

            if (move.isStrike()) {
                result += " x " + move.getSecondaryMove().get().getFigure();
            }
        }
        return result;
    }

    private void addItem(String s, boolean white) {
        ObservableList<String> items = historyRoundsView.getItems();
        int lastIndex = items.size() - 1;

        if (white) {
            historyRoundsView.getItems().add(s);
        } else {
            String last = items.get(lastIndex);
            items.remove(lastIndex);
            last += "%" + s;
            items.add(last);
        }
        historyRoundsView.scrollTo(lastIndex);
    }

    ObjectProperty<ChessGame> gameProperty() {
        return game;
    }

    private void processRoundChange(Number newRound) {
        int round = getRound(newRound);
        PlayerMove lastMove = getGame().getLastMove();

        if (historyRoundsView.getItems().size() < getGame().getHistory().size()) {
            if (lastMove != null) {
                String moveAsString = getMoveAsString(lastMove);
                addItem(moveAsString, lastMove.isWhite());
            }
        } else {
            resetHistory();
        }
    }
}

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
    private TableView<String> historyRoundsView = new TableView<>();
    private ChangeListener<Number> roundListener = (observable, oldValue, newValue) -> processRoundChange();

    HistoryDisplay() {
        game.addListener((observable, oldValue, newValue) -> bindGame(newValue, oldValue));
        getChildren().add(historyRoundsView);
        setMaxHeight(200);

        initTable();
    }

    private void bindGame(ChessGame newValue, ChessGame oldValue) {
        if (newValue != null) {
            newValue.roundProperty().addListener(roundListener);
        }

        if (oldValue != null) {
            oldValue.roundProperty().removeListener(roundListener);
        }

        resetHistory();
    }

    private void initTable() {
        historyRoundsView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyRoundsView.setSortPolicy(null);
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
            return new SimpleStringProperty(separator == -1 ? "" : value.substring(separator + 1, value.length()));
        });

        whiteColumn.setSortable(false);
        blackColumn.setSortable(false);
        historyRoundsView.getColumns().add(whiteColumn);
        historyRoundsView.getColumns().add(blackColumn);
    }

    private void resetHistory() {
        historyRoundsView.getItems().clear();
        ChessGame game = getGame();

        if (game == null) {
            return;
        }

        MoveHistory history = game.getHistory();

        for (int i = 0; i < history.size(); i++) {
            PlayerMove move = history.moveAtPly(i);

            String moveAsString = getMoveAsString(move);
            addItem(moveAsString, move.isWhite());
        }
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
        indexColumn.setSortable(false);
        indexColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>((historyRoundsView.getItems().indexOf(p.getValue()) + 1)));
        return indexColumn;
    }

    ChessGame getGame() {
        return game.get();
    }

    private String getMoveAsString(PlayerMove move) {
        Move mainMove = move.getMainMove();

        String result;

        if (move.isCastlingMove()) {
            result = "Castle to " + mainMove.getTo().notation();
        } else {
            if (move.isPromotion()) {
                Move promotion = move.getPromotionMove().orElseThrow(NullPointerException::new);
                result = mainMove.getFigure() + " " + mainMove.getFrom().notation() + " -> " + promotion.getTo().notation();
            } else {
                result = mainMove.getFigure() + " " + mainMove.getFrom().notation() + " -> " + mainMove.getTo().notation();
            }

            if (move.isStrike()) {
                result += " x " + move.getSecondaryMove().orElseThrow(NullPointerException::new).getFigure();
            }

            if (move.isPromotion()) {
                Move promotion = move.getPromotionMove().orElseThrow(NullPointerException::new);
                result += " promoted to " + promotion.getFigure();
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

    private void processRoundChange() {
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

package chessGame.gui;

import chessGame.mechanics.Position;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.function.Function;

/**
 *
 */
public enum SideOrientation {
    LEFT(Position::getRow, Position::getColumn, Function.identity(), i-> 0, i-> 0, Function.identity()),
    RIGHT(position -> 9 - position.getRow(), Position::getColumn, i -> 9 - i, i -> 0,i -> 0, Function.identity()),
    UP(Position::getColumn, Position::getRow, i->0, Function.identity(), Function.identity(), i->0),
    DOWN(Position::getColumn, position -> 10 - position.getRow(), i -> 0, i -> 10 - i, Function.identity(), i -> 0),;

    private final Function<Position, Integer> columnPanel;
    private final Function<Position, Integer> rowPanel;
    private final Function<Integer, Integer> rowColumnDesc;
    private final Function<Integer, Integer> rowRowDesc;
    private final Function<Integer, Integer> columnColumnDesc;
    private final Function<Integer, Integer> columnRowDesc;

    SideOrientation(Function<Position, Integer> columnPanel, Function<Position, Integer> rowPanel, Function<Integer, Integer> rowColumnDesc, Function<Integer, Integer> rowRowDesc, Function<Integer, Integer> columnColumnDesc, Function<Integer, Integer> columnRowDesc) {
        this.columnPanel = columnPanel;
        this.rowPanel = rowPanel;
        this.rowColumnDesc = rowColumnDesc;
        this.rowRowDesc = rowRowDesc;
        this.columnColumnDesc = columnColumnDesc;
        this.columnRowDesc = columnRowDesc;
    }

    void changeOrientation(BoardGridManager manager) {
        for (int panel = 0; panel < 64; panel++) {
            final Position position = Position.get(panel);

            BoardPanel boardPanel = manager.getPositionPane(position);
            GridPane.setConstraints(boardPanel, columnPanel.apply(position), rowPanel.apply(position));
        }

        for (int index = 1; index <= 8; index++) {
            String rowText = "" + (index);

            Text rowDescription = manager.getBoardDescription(rowText);

            final String columnText = String.valueOf((char) (index - 1 + 'A'));
            Text columnDescription = manager.getBoardDescription(columnText);

            GridPane.setConstraints(columnDescription, columnColumnDesc.apply(index), columnRowDesc.apply(index));
            GridPane.setConstraints(rowDescription, rowColumnDesc.apply(index), rowRowDesc.apply(index));
        }
    }
}

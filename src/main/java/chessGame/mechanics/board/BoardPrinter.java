package chessGame.mechanics.board;

import chessGame.mechanics.Figure;
import chessGame.mechanics.Position;

/**
 *
 */
public class BoardPrinter {
    public static void print(Board<Figure> board) {

        for (int row = 1; row < 9; row++) {
            StringBuilder builder = new StringBuilder();

            for (int column = 1; column < 9; column++) {
                final Position position = Position.get(row, column);

                String text;

                final Figure figure = board.figureAt(position);
                if (figure != null) {
                    final String name = figure.getType().name();
                    text = name.substring(0, 2) + "(" + (figure.isWhite() ? "W" : "S") + ")";
                } else {
                    text = "0";
                }
                text = String.format("%8s", text);
                builder.append(text);
            }
            System.out.println(builder.toString());
            System.out.println();
        }
    }
}

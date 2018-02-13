package chessGame.mechanics;

/**
 *
 */
public enum Color {
    WHITE,
    BLACK,;

    public static Color getEnemy(Color color) {
        return color == BLACK ? WHITE : BLACK;
    }

    public boolean isWhite() {
        return this == WHITE;
    }
}

package chessGame.mechanics.figures;

import chessGame.mechanics.Player;
import javafx.scene.image.Image;

/**
 *
 */
public enum FigureType {
    PAWN(100, "/img/blackPawn.png", "/img/whitePawn.png"),
    ROOK(310, "/img/blackRook.png", "/img/whiteRook.png"),
    KNIGHT(320, "/img/blackKnight.png", "/img/whiteKnight.png"),
    BISHOP(460, "/img/blackBishop.png", "/img/whiteBishop.png"),
    QUEEN(900, "/img/blackQueen.png", "/img/whiteQueen.png"),
    KING(0, "/img/blackKing.png", "/img/whiteKing.png");

    private final int worth;
    private final String black;
    private final String white;

    FigureType(int worth, String black, String white) {
        this.worth = worth;
        this.black = black;
        this.white = white;
    }

    public int getWorth() {
        return worth;
    }

    public Image getImage(Player.PlayerType player) {
        if (player == Player.PlayerType.BLACK) {
            return new Image(getClass().getResource(black).toExternalForm());
        } else {
            return new Image(getClass().getResource(white).toExternalForm());
        }
    }
}

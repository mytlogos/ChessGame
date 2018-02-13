package chessGame.mechanics;

import javafx.scene.image.Image;

/**
 *
 */
public enum FigureType {
    PAWN(1, "/img/blackPawn.png", "/img/whitePawn.png",'P'),
    ROOK(3.1, "/img/blackRook.png", "/img/whiteRook.png",'R'),
    KNIGHT(3.2, "/img/blackKnight.png", "/img/whiteKnight.png",'N'),
    BISHOP(4.6, "/img/blackBishop.png", "/img/whiteBishop.png",'B'),
    QUEEN(9, "/img/blackQueen.png", "/img/whiteQueen.png",'Q'),
    KING(0, "/img/blackKing.png", "/img/whiteKing.png",'K');

    private final double worth;
    private final String black;
    private final String white;
    private final char notation;

    FigureType(double worth, String black, String white, char notation) {
        this.worth = worth;
        this.black = black;
        this.white = white;
        this.notation = notation;
    }

    public char getNotation() {
        return notation;
    }

    public double getWorth() {
        return worth;
    }

    public Figure create(Color player) {
        return new Figure(player, this);
    }

    public Image getImage(Color player) {
        if (player == Color.BLACK) {
            return new Image(getClass().getResource(black).toExternalForm());
        } else {
            return new Image(getClass().getResource(white).toExternalForm());
        }
    }
}

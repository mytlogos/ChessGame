package chessGame.mechanics.figures;

import chessGame.mechanics.Player;
import javafx.scene.image.Image;

/**
 *
 */
public enum FigureType {
    PAWN {
        @Override
        public Image getImage(Player player) {
            if (!player.isWhite()) {
                return  new Image(getClass().getResource("/img/blackPawn.png").toExternalForm());
            } else {
                return  new Image(getClass().getResource("/img/whitePawn.png").toExternalForm());
            }
        }
    },
    ROOK {
        @Override
        public Image getImage(Player player) {
            if (!player.isWhite()) {
                return new Image(getClass().getResource("/img/blackRook.png").toExternalForm());
            } else {
                return new Image(getClass().getResource("/img/whiteRook.png").toExternalForm());
            }
        }
    },
    KNIGHT {
        @Override
        public Image getImage(Player player) {
            if (!player.isWhite()) {
                return new Image(getClass().getResource("/img/blackKnight.png").toExternalForm());
            } else {
                return new Image(getClass().getResource("/img/whiteKnight.png").toExternalForm());
            }
        }
    },
    BISHOP {
        @Override
        public Image getImage(Player player) {
            if (!player.isWhite()) {
                return  new Image(getClass().getResource("/img/blackBishop.png").toExternalForm());
            } else {
                return new Image(getClass().getResource("/img/whiteBishop.png").toExternalForm());
            }
        }
    },
    QUEEN {
        @Override
        public Image getImage(Player player) {
            if (!player.isWhite()) {
                return  new Image(getClass().getResource("/img/blackQueen.png").toExternalForm());
            } else {
                return  new Image(getClass().getResource("/img/whiteQueen.png").toExternalForm());
            }
        }
    },
    KING {
        @Override
        public Image getImage(Player player) {
            if (!player.isWhite()) {
                return new Image(getClass().getResource("/img/blackKing.png").toExternalForm());
            } else {
                return new Image(getClass().getResource("/img/whiteKing.png").toExternalForm());
            }
        }
    };

    public abstract Image getImage(Player player);
}

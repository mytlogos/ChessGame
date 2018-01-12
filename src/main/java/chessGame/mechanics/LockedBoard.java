package chessGame.mechanics;

import java.util.List;

/**
 *
 */
public final class LockedBoard extends AbstractBoard {
    private boolean locked = false;

    LockedBoard(Player white, Player black) {
        super(white, black);
    }

    @Override
    public void makeMove(PlayerMove playerMove) throws IllegalMoveException {
        if (locked) {
            throw new IllegalStateException("Board is locked");
        }
        super.makeMove(playerMove);
        locked = true;
    }

    public boolean isLocked() {
        return locked;
    }

    void addPlayerChangeListener() {
        atMovePlayerProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                final List<PlayerMove> allowedMoves = getGenerator().getAllowedMoves(newValue);
                allowedMoves.stream().filter(move -> !move.getPlayer().equals(newValue)).forEach(System.err::println);
                getAllowedMoves().setAll(allowedMoves);

                final AtMove atMove = new AtMove(newValue, allowedMoves);
                this.atMove.set(atMove);

                if (getAllowedMoves().isEmpty()) {
                    atMovePlayerProperty().set(null);
                    this.atMove.set(null);
                }
            }
        });
    }
}

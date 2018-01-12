package chessGame.mechanics;

import chessGame.mechanics.figures.*;

import java.util.*;

/**
 *
 */
public final class Board extends AbstractBoard implements Cloneable {
    private AtMove saveForPausing;
    private Game game;

    public Board(Player white, Player black, Game game) {
        super(white, black);
        this.game = game;
        this.generator = new MoveGenerator(this);

        game.pausedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                atMoveProperty().set(getSaveForPausing());
            }
        });

        atMovePlayerProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                //check if next player has only one figure, it is implied that his last figure is the king
                if (newValue.getFigures().size() == 1 ) {
                    game.setLoser(newValue);
                }

                final List<PlayerMove> allowedMoves = getGenerator().getAllowedMoves(newValue);
                allowedMoves.stream().filter(move -> move.getMainMove().getFigure().getPlayer() != newValue).forEach(System.err::println);
                getAllowedMoves().setAll(allowedMoves);

                final AtMove atMove = new AtMove(newValue, allowedMoves);
                if (game.isPaused()) {
                    this.saveForPausing = atMove;
                } else {
                    this.atMove.set(atMove);

                    if (getAllowedMoves().isEmpty()) {
                        atMovePlayerProperty().set(null);
                        this.atMove.set(null);
                    }
                }
            }
        });
        buildBoard();
    }

    private AtMove getSaveForPausing() {
        return saveForPausing;
    }

    public void buildBoard() {
        boardMap.clear();

        Collection<Figure> whiteFigures = new ArrayList<>();
        Collection<Figure> blackFigures = new ArrayList<>();

        setPositions(Rook::new, 1, 1, whiteFigures, blackFigures);
        setPositions(Knight::new, 1, 2, whiteFigures, blackFigures);
        setPositions(Bishop::new, 1, 3, whiteFigures, blackFigures);

        setFigure(Queen::new, 1, 4, white, whiteFigures);
        setFigure(King::new, 1, 5, white, whiteFigures);
        setFigure(Queen::new, 8, 4, black, blackFigures);
        setFigure(King::new, 8, 5, black, blackFigures);

        setPositions(Bishop::new, 1, 6, whiteFigures, blackFigures);
        setPositions(Knight::new, 1, 7, whiteFigures, blackFigures);
        setPositions(Rook::new, 1, 8, whiteFigures, blackFigures);

        for (int i = 0; i < 8; i++) {
            setPositions(Pawn::new, 2, i + 1, whiteFigures, blackFigures);
        }
        this.white.setFigures(whiteFigures);
        this.black.setFigures(blackFigures);
    }

    @Override
    public String toString() {
        return "Board{" +
                "boardMap=" + boardMap +
                ", lastMove=" + getLastMove() +
                ", atMovePlayer=" + getAtMovePlayer() +
                '}';
    }

    private void setPositions(TriFunction<Position, Player, Board, Figure> figureFunction, int row, int column, Collection<Figure> whiteFigures, Collection<Figure> figures) {
        setFigure(figureFunction, row, column, getWhite(), whiteFigures);

        row = 9 - row;
        column = 9 - column;

        setFigure(figureFunction, row, column, getBlack(), figures);
    }

    private void setFigure(TriFunction<Position, Player, Board, Figure> figureFunction, int row, int column, Player player, Collection<Figure> figures) {

        final Position position = Position.get(row, column);
        Figure figure = figureFunction.apply(position, player, this);

        if (player.isWhite() && figure.getType() == FigureType.KING) {

        }

        setPosition(figure, position);
        figures.add(figure);
    }


    @Override
    public LockedBoard cloneBoard() {
        return super.cloneBoard();
    }
}

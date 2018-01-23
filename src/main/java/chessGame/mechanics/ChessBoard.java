package chessGame.mechanics;

import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.King;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class ChessBoard implements Board {
    private Figure[] board = new Figure[64];

    private King whiteKing;
    private King blackKing;

    @Override
    public void setFigure(Figure figure, Position position) {
        Objects.requireNonNull(figure);
        check();

        if (figure instanceof King) {
            if (figure.getPlayer().isWhite()) {
                whiteKing = (King) figure;
            } else {
                blackKing = (King) figure;
            }
        }

        final int index = convert(position);
        Figure previous = board[index];

        if (previous == null) {
            board[index] = figure;
            figure.setPosition(position);
            check();
        } else {
            throw new IllegalArgumentException();
        }
        check();
    }

    @Override
    public void setEmpty(Position position) {
        check();
        int index = convert(position);
        Figure figure = board[index];

        if (figure == null) {
            System.out.println("setting a position empty which is empty: " + position);
        }

        board[index] = null;
        check();
    }

    @Override
    public Figure figureAt(Position position) {
        check();
        int index = convert(position);
        Figure figure = board[index];
        check();
        return figure;
    }

    @Override
    public List<Figure> getFigures(Player player) {
        check();
        Map<Player, List<Figure>> playerFigures = getPlayerFigures();
        check();
        return playerFigures.get(player);
    }

    @Override
    public Map<Player, List<Figure>> getPlayerFigures() {
        check();
        Map<Player, List<Figure>> collect = Arrays.stream(board).filter(Objects::nonNull).collect(Collectors.groupingBy(Figure::getPlayer));
        check();
        return collect;
    }

    @Override
    public List<Figure> getFigures() {
        return new ArrayList<>(getBoardMap().values());
    }

    @Override
    public Map<Position, Figure> getBoardMap() {
        check();
        Map<Position, Figure> map = new TreeMap<>();

        for (int i = 0; i < board.length; i++) {
            final Figure figure = board[i];

            if (figure != null) {
                final Position convert = convert(i);
                map.put(convert, figure);
            }
        }
        check();
        return map;
    }

    @Override
    public BoardSnapShot getSnapShot() {
        check();
        String[] typeBoard = new String[64];

        for (int i = 0; i < board.length; i++) {
            Figure figure = board[i];

            if (figure != null) {
                typeBoard[i] = figure.getType() + "(" + figure.getPlayer().getType() + ")";
            }
        }
        check();
        return new BoardSnapShot(typeBoard);
    }

    @Override
    public King getKing(Player player) {
        check();

        King king = player.isWhite() ? whiteKing : blackKing;
        if (figureAt(king.getPosition()) != king) {
            System.out.print("");
        }

        check();
        return king;
    }

    @Override
    public boolean isEmptyAt(Position position) {
        return figureAt(position) == null;
    }

    @Override
    public Board copy() {
        return null;
    }

    private void check() {
        Map<Position, Figure> map = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            Position position = convert(i);
            Figure figure = board[i];

            if (figure != null && !figure.getPosition().equals(position)) {
                map.put(position, figure);
            }
        }

        if (!map.isEmpty()) {
            System.out.print("");
        }
    }

    private int convert(Position position) {
        if (!position.isInBoard()) {
            throw new IllegalArgumentException("Nur Positionen innerhalb eines Board erlaubt!");
        }
        int rowIndex = position.getRow() - 1;
        int columnIndex = position.getColumn() - 1;
        return rowIndex * 8 + columnIndex;
    }

    private Position convert(int index) {
        int rowIndex = index / 8;
        int columnIndex = index % 8;
        return Position.get(rowIndex + 1, columnIndex + 1);
    }
}

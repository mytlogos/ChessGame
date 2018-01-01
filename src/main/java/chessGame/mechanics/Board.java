package chessGame.mechanics;

import chessGame.figures.*;
import chessGame.gui.TriFunction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class Board{
    private Map<Position, ObjectProperty<Figure>> board = new TreeMap<>();
    private ObjectProperty<PlayerMove> lastMove = new SimpleObjectProperty<>();
    private ObjectProperty<Player> atMove = new SimpleObjectProperty<>();
    private ObjectProperty<Figure> fromProperty;

    public Board() {
        buildBoard();
    }

    public void buildBoard() {
        board.values().forEach(value -> value.set(null));

        Collection<chessGame.figures.Figure> white = new ArrayList<>();
        Collection<chessGame.figures.Figure> black = new ArrayList<>();

        setPositions(Rook::new, 0, 0, white, black);
        setPositions(Knight::new, 0, 1, white, black);
        setPositions(Bishop::new, 0, 2, white, black);
        setPositions(Queen::new, 0, 3, white, black);
        setPositions(King::new, 0, 4, white, black);
        setPositions(Bishop::new, 0, 5, white, black);
        setPositions(Knight::new, 0, 6, white, black);
        setPositions(Rook::new, 0, 7, white, black);

        for (int i = 0; i < 8; i++) {
            setPositions(Pawn::new, 1, i, white, black);
        }
        Player.BLACK.setFigures(black);
        Player.WHITE.setFigures(white);
    }

    public ObjectProperty<Figure> figureObjectProperty(Position position) {
        return board.computeIfAbsent(position, (k) -> new SimpleObjectProperty<>());
    }

    public Figure getFigure(Position position) {
        return figureObjectProperty(position).get();
    }

    public void makeMove(PlayerMove playerMove) throws IllegalMoveException {
        final Move secondMove = playerMove.getSecondMove();

        if (secondMove != null) {
            makeMove(secondMove);
        }

        makeMove(playerMove.getMove());
        atMove.set(getNotAtMove());
    }

    public ObjectProperty<PlayerMove> lastMoveProperty() {
        return lastMove;
    }

    public PlayerMove getLastMove() {
        return lastMove.get();
    }

    public Map<Figure, List<Position>> getPossibleEnemyPositions() {
        final List<Figure> figures = getPlayerFiguresMap().get(getNotAtMove());
        return figures.stream().collect(Collectors.toMap(Function.identity(), Figure::getAllowedPositions));
    }

    public Player getNotAtMove() {
        final ArrayList<Player> list = new ArrayList<>(List.of(Player.values()));
        list.remove(atMove.get());
        return list.get(0);
    }

    public List<PlayerMove> getAllowedMoves() {
        final Map<Player, List<Figure>> playerListMap = getPlayerFiguresMap();

        final List<Figure> figures = playerListMap.get(atMove.get());

        final Figure king = figures.stream().collect(Collectors.toMap(Figure::getType, Function.identity())).get(FigureType.KING);

        if (king == null) {
            throw new IllegalStateException();
        } else {
            final List<Figure> dangerFigures = getCheckFigure(king.getPosition());

            if (!dangerFigures.isEmpty()) {
                //todo get moves for getting out of check
            } else {
                return figures.stream().flatMap(figure -> {
                    final List<Position> allowedPositions = figure.getAllowedPositions();
                    return allowedPositions.stream().map(position -> {
                        PlayerMove playerMove = null;

                        final Figure boardFigure = figureObjectProperty(position).get();

                        final Move move = new Move(figure, new PositionChange(figure.getPosition(), position));
                        if (boardFigure == null) {
                            playerMove = new PlayerMove(move, null);
                        } else if (boardFigure.getPlayer() != figure.getPlayer()) {
                            playerMove = new PlayerMove(move, new Move(boardFigure, new PositionChange(boardFigure.getPosition(), Position.Bench)));
                        }
                        return playerMove;
                    });
                }).filter(Objects::nonNull).collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    public Player getAtMove() {
        return atMove.get();
    }

    public ObjectProperty<Player> atMoveProperty() {
        return atMove;
    }

    @Override
    public String toString() {
        return "Board{" +
                "board=" + board +
                ", lastMove=" + lastMove +
                ", atMove=" + atMove .get()+
                ", fromProperty=" + fromProperty +
                '}';
    }

    private void makeMove(Move move) throws IllegalMoveException {
        final PositionChange change = move.getChange();
        fromProperty = figureObjectProperty(change.getFrom());

        final Figure figure = move.getFigure();

        if (!figure.equals(fromProperty.get())) {
            throw new IllegalMoveException();
        } else {
            fromProperty.set(null);
            final Position to = change.getTo();
            figureObjectProperty(to).set(figure);
            figure.setPosition(to);
        }
    }

    private boolean isChecked(Position position) {
        return getCheckFigure(position) != null;
    }

    private List<Figure> getCheckFigure(Position position) {
        return getPossibleEnemyPositions().
                entrySet().
                stream().
                filter(entry -> entry.getValue().contains(position)).
                map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private Map<Player, List<Figure>> getPlayerFiguresMap() {
        return board.
                values().
                stream().
                map(ObjectProperty::get).
                filter(Objects::nonNull).
                collect(Collectors.groupingBy(Figure::getPlayer));
    }

    private void setPositions(TriFunction<Position, Player, Board, Figure> figureFunction, int row, int column, Collection<Figure> white, Collection<Figure> black) {
        setFigure(figureFunction, row, column, Player.WHITE, white);

        row = 7 - row;
        column = 7 - column;

        setFigure(figureFunction, row, column, Player.BLACK, black);
    }

    private void setFigure(TriFunction<Position, Player, Board, Figure> figureFunction, int row, int column, Player player, Collection<Figure> figures) {
        final Position position = Position.get(row, column);

        Figure figure = figureFunction.apply(position, player, this);

        figures.add(figure);
        figureObjectProperty(position).set(figure);
    }
}

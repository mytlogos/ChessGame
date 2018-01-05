package chessGame.mechanics;

import chessGame.gui.TriFunction;
import chessGame.mechanics.figures.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class Board implements Cloneable {
    private final Player white;
    private final Player black;
    private MoveGenerator manager;

    private List<Figure> bench = new ArrayList<>();
    private Map<Position, ObjectProperty<Figure>> board = new TreeMap<>();

    private ObjectProperty<PlayerMove> lastMove = new SimpleObjectProperty<>();
    private ObjectProperty<Player> atMovePlayer = new SimpleObjectProperty<>();
    private ObjectProperty<Figure> defeatedFigure = new SimpleObjectProperty<>();

    private ObjectProperty<AtMove> atMove = new SimpleObjectProperty<>();
    private AtMove saveForPausing;

    private ObservableList<PlayerMove> allowedMoves = FXCollections.observableArrayList();
    private final Game game;

    public Board(Player white, Player black, Game game) {
        this.game = game;
        this.manager = new MoveGenerator(this);
        if (!white.isWhite()) {
            throw new IllegalArgumentException("Spieler Weiß ist schwarz");
        }

        if (black.isWhite()) {
            throw new IllegalArgumentException("Spieler Schwarz ist weiß");
        }

        this.white = white;
        this.black = black;

        game.pausedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                atMoveProperty().set(getSaveForPausing());
            }
        });

        atMovePlayerProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                final List<PlayerMove> allowedMoves = getManager().getAllowedMoves(newValue);
                allowedMoves.stream().filter(move -> move.getMainMove().getFigure().getPlayer() != newValue).forEach(System.err::println);
                getAllowedMoves().setAll(allowedMoves);

                final AtMove atMove = new AtMove(newValue, allowedMoves);
                if (game.isPaused()) {
                    this.saveForPausing = atMove;
                } else {
                    this.atMove.set(atMove);

                    if (getAllowedMoves().isEmpty()) {
                        final List<PlayerMove> moves = getManager().getAllowedMoves(newValue);
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


    public AtMove getAtMove() {
        return atMove.get();
    }

    public ObjectProperty<AtMove> atMoveProperty() {
        return atMove;
    }

    public Collection<Figure> figures() {
        return board.values().stream().map(ObservableObjectValue::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void buildBoard() {
        board.values().forEach(value -> value.set(null));

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
        BoardPrinter.print(this);
    }

    public List<Figure> getBench() {
        return bench;
    }

    public ObjectProperty<Figure> figureObjectProperty(Position position) {
        if (position != null && position.isInBoard()) {
            return board.computeIfAbsent(position, this::initFigureProperty);
        } else {
            return new SimpleObjectProperty<>();
        }
    }

    private ObjectProperty<Figure> initFigureProperty(Position position) {
        final SimpleObjectProperty<Figure> property = new SimpleObjectProperty<>();

        property.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.setPosition(position);
            }
        });
        return property;
    }

    public Figure getFigure(Position position) {
        final Figure figure = figureObjectProperty(position).get();
//        if (figure != null && !figure.getPosition().equals(position)) {
//            figure.setPosition(position);
//        }
        return figure;
    }

    public void makeMove(PlayerMove playerMove) throws IllegalMoveException {
        if (!getAllowedMoves().contains(playerMove)) {
            System.err.println("illegal: " + playerMove);
            throw new IllegalMoveException();
        }
        System.out.println("Bench: " + bench);
        System.out.println("Before");
        BoardPrinter.print(this);
        move(playerMove);
        System.out.println("After");
        BoardPrinter.print(this);
    }

    void move(PlayerMove playerMove) throws IllegalMoveException {
        if (playerMove.isCastlingMove()) {
            castle(playerMove);
        } else if (playerMove.isPromotion()) {
            promote(playerMove);
        } else {
            final Move secondMove = playerMove.getSecondaryMove();

            if (secondMove != null) {
                makeMove(secondMove);
            }

            makeMove(playerMove.getMainMove());
        }
    }

    public void atMoveFinished() {
        System.out.println("finished");
        atMovePlayer.set(getNotAtMove());
    }

    private void promote(PlayerMove playerMove) throws IllegalMoveException {
        final Move mainMove = playerMove.getMainMove();
        playerMove.getPlayer().getFigures().remove(mainMove.getFigure());
        mainMove.getFigure().setPosition(Position.Promoted);

        final Move secondMove = playerMove.getSecondaryMove();

        if (secondMove != null) {
            makeMove(secondMove);
        }

        final Move promotionMove = playerMove.getPromotionMove();
        if (promotionMove == null || promotionMove.getFigure() == null) {
            throw new IllegalMoveException("promotion is null");
        }
        setPosition(promotionMove.getFigure(), promotionMove.getChange().getTo());
        playerMove.getPlayer().getFigures().add(promotionMove.getFigure());
    }

    private void castle(PlayerMove playerMove) throws IllegalMoveException {
        final Move mainMove = playerMove.getMainMove();
        final Move secondaryMove = playerMove.getSecondaryMove();

        makeMove(mainMove);
        makeMove(secondaryMove);
    }

    public Player getWhite() {
        return white;
    }

    public Player getBlack() {
        return black;
    }

    public ObjectProperty<PlayerMove> lastMoveProperty() {
        return lastMove;
    }

    public PlayerMove getLastMove() {
        return lastMove.get();
    }


    public Player getNotAtMove() {
        return getAtMovePlayer() == null ? getWhite() : getAtMovePlayer().equals(getWhite()) ? getBlack() : getWhite();
    }

    public Player getEnemy(Player player) {
        return getWhite().equals(player) ? getBlack() : getBlack().equals(player) ? getWhite() : null;
    }

    public MoveGenerator getManager() {
        return manager;
    }

    public Player getAtMovePlayer() {
        return atMovePlayer.get();
    }

    public ObjectProperty<Player> atMovePlayerProperty() {
        return atMovePlayer;
    }

    @Override
    public String toString() {
        return "Board{" +
                "board=" + board +
                ", lastMove=" + getLastMove() +
                ", atMovePlayer=" + getAtMovePlayer() +
                '}';
    }

    public void setPosition(Figure figure, Position position) {
//        if (figure != null) {
//            figure.setPosition(position);
//        }
        figureObjectProperty(position).set(figure);
    }

    private void makeMove(Move move) throws IllegalMoveException {
        final PositionChange change = move.getChange();

        final Figure figure = move.getFigure();
        final Position from = change.getFrom();

        final Figure boardFigure = getFigure(from);

        if (!figure.equals(boardFigure)) {
            System.err.println("Move: " + move + " Previous " + boardFigure);
            throw new IllegalMoveException();
        } else {
            setPosition(null, from);
            final Position to = change.getTo();

            if (to.equals(Position.Bench)) {
                if (figure.getType() == FigureType.KING) {
                    throw new IllegalMoveException("König darf nicht geschlagen werden");
                }
                figure.setPosition(Position.Bench);
                defeatedFigure.set(figure);
            } else {
                setPosition(figure, to);
            }
        }
    }

    public Figure getDefeatedFigure() {
        return defeatedFigure.get();
    }

    public ObjectProperty<Figure> defeatedFigureProperty() {
        return defeatedFigure;
    }

    private void setPositions(TriFunction<Position, Player, Board, Figure> figureFunction, int row, int column, Collection<Figure> whiteFigures, Collection<Figure> figures) {
        setFigure(figureFunction, row, column, getWhite(), whiteFigures);

        row = 9 - row;
        column = 9 - column;

        setFigure(figureFunction, row, column, getBlack(), figures);
    }

    private void setFigure(TriFunction<Position, Player, Board, Figure> figureFunction, int row, int column, Player player, Collection<Figure> figures) {
        final Position position = Position.get(row, column);

        if (row != 1 && row != 2 && row != 7 && row != 8) {
            System.out.println(position);
        }
        Figure figure = figureFunction.apply(position, player, this);
        setPosition(figure, position);
        figures.add(figure);
    }

    @Override
    final public Board clone() {
        try {
            final Board clone = (Board) super.clone();
            clone.board = new TreeMap<>();
            board.forEach((k, v) -> {
                final Figure figure = v.get();
                if (figure == null) {
                    clone.setPosition(null, k);
                } else {
                    final Figure clonedFigure = figure.clone(clone);
                    clone.setPosition(clonedFigure, k);
                }
            });

            clone.manager = new MoveGenerator(clone);
            clone.atMovePlayer = new SimpleObjectProperty<>(getAtMovePlayer());
            clone.defeatedFigure = new SimpleObjectProperty<>(getDefeatedFigure());
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Board board1 = (Board) o;

        if (!getWhite().equals(board1.getWhite())) return false;
        if (!getBlack().equals(board1.getBlack())) return false;
        return board.equals(board1.board);
    }

    @Override
    public int hashCode() {
        int result = getWhite().hashCode();
        result = 31 * result + getBlack().hashCode();
        result = 31 * result + board.hashCode();
        return result;
    }

    public ObservableList<PlayerMove> getAllowedMoves() {
        return allowedMoves;
    }
}

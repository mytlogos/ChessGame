package chessGame.mechanics;

import chessGame.mechanics.figures.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class AbstractBoard implements Cloneable {
    Player white;
    Player black;
    MoveGenerator generator;
    PositionGenerator positioner;

    List<Figure> bench = new ArrayList<>();
    Map<Position, Figure> boardMap = new TreeMap<>();

    ObjectProperty<PlayerMove> lastMove = new SimpleObjectProperty<>();
    ObjectProperty<Player> atMovePlayer = new SimpleObjectProperty<>();

    ObjectProperty<AtMove> atMove = new SimpleObjectProperty<>();

    ObservableList<PlayerMove> allowedMoves = FXCollections.observableArrayList();

    AbstractBoard(Player white, Player black) {
        if (!white.isWhite()) {
            throw new IllegalArgumentException("Spieler Weiß ist schwarz");
        }

        if (black.isWhite()) {
            throw new IllegalArgumentException("Spieler Schwarz ist weiß");
        }
        this.white = white;
        this.black = black;
    }

    public AtMove getAtMove() {
        return atMove.get();
    }

    public ObjectProperty<AtMove> atMoveProperty() {
        return atMove;
    }

    public Collection<Figure> figures() {
        return boardMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<Figure> getBench() {
        return bench;
    }


    public Figure getFigure(Position position) {
        return boardMap.get(position);
    }

    public void makeMove(PlayerMove playerMove) throws IllegalMoveException {
        if (!getAllowedMoves().contains(playerMove)) {
            throw new IllegalMoveException("dieser zug ist nicht erlaubt für " + playerMove.getPlayer());
        } else if (getLastMove() != null && getLastMove().getPlayer().equals(playerMove.getPlayer())) {
            throw new IllegalMoveException("ein spieler darf nicht zweimal hintereinander ziehen!");
        }

        move(playerMove);
        setLastMove(playerMove);
    }

    private void setLastMove(PlayerMove lastMove) {
        this.lastMove.set(lastMove);
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
        atMovePlayer.set(getNotAtMove());
    }

    void promote(PlayerMove playerMove) throws IllegalMoveException {
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
        setPosition(promotionMove.getFigure(), promotionMove.getTo());
        playerMove.getPlayer().getFigures().add(promotionMove.getFigure());
    }

    void castle(PlayerMove playerMove) throws IllegalMoveException {
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

    public MoveGenerator getGenerator() {
        return generator;
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
                "boardMap=" + boardMap +
                ", lastMove=" + getLastMove() +
                ", atMovePlayer=" + getAtMovePlayer() +
                '}';
    }

    void setPosition(Figure figure, Position position) {
        if (figure != null) {
            figure.setPosition(position);
            boardMap.put(position, figure);
        } else {
            boardMap.remove(position);
        }
    }

    void makeMove(Move move) throws IllegalMoveException {
        final Figure figure = move.getFigure();
        final Position from = move.getFrom();

        final Figure boardFigure = getFigure(from);

        if (!figure.equals(boardFigure)) {
            throw new IllegalMoveException("boardMap is not synched with its figure: different position, boardMap has: " + boardFigure + " for " + figure);
        } else {
            setPosition(null, from);
            final Position to = move.getTo();

            if (to.equals(Position.Bench)) {
                if (figure.getType() == FigureType.KING) {
                    throw new IllegalMoveException("König darf nicht geschlagen werden");
                }
                figure.setPosition(Position.Bench);
            } else {
                setPosition(figure, to);
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !AbstractBoard.class.isAssignableFrom(o.getClass())) return false;

        AbstractBoard otherBoard = (AbstractBoard) o;
        return boardMap.equals(otherBoard.boardMap);
    }

    public Map<Position, Figure> getBoard() {
        return boardMap;
    }

    @Override
    public int hashCode() {
        return boardMap.hashCode();
    }

    public LockedBoard cloneBoard() {
        final LockedBoard clone = new LockedBoard(white, black);
        clone.boardMap = new TreeMap<>();

        clone.white = white.clone();
        clone.black = black.clone();

        boardMap.forEach((k, v) -> {
//                final Figure figure = v.get();
            if (v == null) {
                clone.setPosition(null, k);
            } else {
                final Figure clonedFigure = v.clone(clone);
                clone.setPosition(clonedFigure, k);
            }
        });

        final Map<Player, List<Figure>> figures = clone.figures().stream().collect(Collectors.groupingBy(Figure::getPlayer));
        clone.white.setFigures(figures.get(white));
        clone.black.setFigures(figures.get(black));

        clone.generator = new MoveGenerator(clone);
        clone.lastMove = new SimpleObjectProperty<>(getLastMove());
        clone.atMovePlayer = new SimpleObjectProperty<>(getAtMovePlayer());
        clone.atMove = new SimpleObjectProperty<>(getAtMove());
        clone.bench = new ArrayList<>(bench);
        clone.allowedMoves = FXCollections.observableArrayList(allowedMoves);

        clone.addPlayerChangeListener();
        return clone;
    }

    public ObservableList<PlayerMove> getAllowedMoves() {
        return allowedMoves;
    }
}


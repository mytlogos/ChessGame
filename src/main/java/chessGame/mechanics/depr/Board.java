package chessGame.mechanics.depr;

import chessGame.mechanics.*;
import chessGame.mechanics.figures.*;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public final class Board implements Cloneable {
   /* private Player white;
    private Player black;
    private King whiteKing;
    private King blackKing;
    private Map<Position, Figure> boardMap = new TreeMap<>();
    private List<Figure> bench = new ArrayList<>();
    private List<Figure> promoted = new ArrayList<>();
    private MoveHistory moveHistory = new MoveHistory();
    private List<PlayerMove> allowedMoves = new ArrayList<>();

    private ObjectProperty<PlayerMove> lastMove = new SimpleObjectProperty<>();
    private ObjectProperty<Player> atMovePlayer = new SimpleObjectProperty<>();
    private IntegerProperty round = new SimpleIntegerProperty();

    private BooleanProperty simulate = new SimpleBooleanProperty();

    Board(Player white, Player black, Game game) {
        if (!white.isWhite() || black.isWhite()) {
            throw new IllegalArgumentException("Spielerfarben stimmen nicht: Weiß " + white + " Schwarz " + black);
        }

        this.white = white;
        this.black = black;

        initListener(game);
        buildBoard();
    }

    public ReadOnlyIntegerProperty roundProperty() {
        return round;
    }

    public Collection<Figure> figures() {
        return boardMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Figure getFigure(Position position) {
        Objects.requireNonNull(position);
        return boardMap.get(position);
    }

    public void makeMove(PlayerMove playerMove) throws IllegalMoveException {
        if (!getAllowedMoves().contains(playerMove)) {
            throw new IllegalMoveException("der zug " + playerMove + " ist nicht erlaubt für " + getAtMovePlayer());
        } else if (getLastMove() != null && getLastMove().getPlayer().equals(playerMove.getPlayer())) {
            throw new IllegalMoveException("ein spieler darf nicht zweimal hintereinander ziehen!");
        }

        move(playerMove);
    }

    public void atMoveFinished() {
        atMovePlayer.set(getNotAtMove());
    }

    public Player getNotAtMove() {
        return getAtMovePlayer() == null ? getWhite() : getAtMovePlayer().equals(getWhite()) ? getBlack() : getWhite();
    }

    public Player getAtMovePlayer() {
        return atMovePlayer.get();
    }

    public Player getWhite() {
        return white;
    }

    public Player getBlack() {
        return black;
    }

    public Player getEnemy(Player player) {
        return getWhite().equals(player) ? getBlack() : getBlack().equals(player) ? getWhite() : null;
    }

    public List<Figure> getFigures(Player player) {
        return figures().stream().collect(Collectors.groupingBy(Figure::getPlayer)).get(player);
    }

    public Map<Position, Figure> getBoard() {
        return boardMap;
    }

    @Override
    public int hashCode() {
        return boardMap.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !Board.class.isAssignableFrom(o.getClass())) return false;

        Board otherBoard = (Board) o;
        return boardMap.equals(otherBoard.boardMap);
    }

    @Override
    public Board clone() {
        final Board clone;

        try {
            clone = (Board) super.clone();
        } catch (CloneNotSupportedException e) {
            //should never happen, implements Cloneable
            return null;
        }
        clone.boardMap = new TreeMap<>();

        boardMap.forEach((k, v) -> {
            if (v != null) {
//                final Figure clonedFigure = v.clone(clone);
                final Figure clonedFigure = v;
                clone.setPosition(clonedFigure, k);

                if (clonedFigure.getType() == FigureType.KING) {
                    if (v.getPlayer().isWhite()) {
                        clone.whiteKing = (King) clonedFigure;
                    } else {
                        clone.blackKing = (King) clonedFigure;
                    }
                }
            }
        });

        clone.atMovePlayer = new SimpleObjectProperty<>(getAtMovePlayer());
//        clone.bench = getBench().stream().map(figure -> figure.clone(clone)).collect(Collectors.toList());
//        clone.promoted = promoted.stream().map(figure -> figure.clone(clone)).collect(Collectors.toList());
//        clone.allowedMoves = new ArrayList<>();
//        clone.moveHistory = moveHistory.clone(clone, game);
        clone.lastMove = new SimpleObjectProperty<>(clone.moveHistory.getLast());

        return clone;
    }

    @Override
    public String toString() {
        return "Board{" +
                "boardMapSize=" + boardMap.size() +
                ", lastMove=" + getLastMove() +
                ", atMovePlayer=" + getAtMovePlayer() +
                '}';
    }

    public PlayerMove getLastMove() {
        return lastMove.get();
    }

    private void setLastMove(PlayerMove lastMove) {
        this.lastMove.set(lastMove);
    }

    private void setPosition(Figure figure, Position position) {
        if (figure != null) {
            if (boardMap.values().contains(figure)) {
                throw new IllegalStateException("a figure can only occur once in a board");
            }
            if (boardMap.containsKey(position)) {
                throw new IllegalStateException("a figure should not be replaced!");
            }
            final Figure put = boardMap.put(position, figure);
            if (put != null) {
                throw new IllegalStateException("a figure should not be replaced!");
            }
            figure.setPosition(position);
        } else {
            final Figure remove = boardMap.remove(position);

        }
    }

    public List<Figure> getBench() {
        return bench;
    }

    private boolean isPlaying() {
        return !simulate.get();
    }

    public void setPlaying(boolean simulate) {
        this.simulate.set(simulate);
    }

    public List<PlayerMove> getAllowedMoves() {
        if (allowedMoves.isEmpty()) {
            if (Platform.isFxApplicationThread()) {
                setPlaying(false);
            }
        }
        return allowedMoves;
    }

    *//**
     * This Method reverses a {@link PlayerMove} in reverse order.
     * First the mainMove, then the promotionMove and at last the secondaryMove
     * will be undone.
     *//*
    public void redo() {
        final PlayerMove lastMove = lastMoveProperty().get();

        if (moveHistory.getLast() != lastMove) {
            throw new IllegalStateException("incongruous move history!");
        }
        if (lastMove != null) {
            final Move mainMove = lastMove.getMainMove();

            //first redo mainMove
            redo(mainMove);

            //then remove promoted
            lastMove.getPromotionMove().ifPresent(this::redo);
            //so that a defeated figure can take the place
            lastMove.getSecondaryMove().ifPresent(this::redo);

            moveHistory.removeLast();

            final PlayerMove playerMove;
            if (!getMovesHistory().isEmpty()) {
                playerMove = moveHistory.getLast();
            } else {
                playerMove = null;
            }
            lastMoveProperty().set(playerMove);
        }

        checkBoard();
        final Player player = lastMove == null ? getWhite() : lastMove.getPlayer();
        atMovePlayerProperty().set(player);
    }

    public ObjectProperty<PlayerMove> lastMoveProperty() {
        return lastMove;
    }

    private void redo(Move move) {
        checkBoard();
        final Figure figure = move.getFigure();
        final Position from = move.getFrom();
        final Position to = move.getTo();

        if (to == Position.Bench) {
            getBench().remove(figure);
            checkState();
        } else if (to == Position.Promoted) {
            getPromoted().remove(figure);
            checkState();
        } else {
            setPosition(null, to);
            checkState();
        }

        if (from != null) {
            setPosition(figure, from);
        }
        checkBoard();
    }

    public MoveHistory getMovesHistory() {
        return moveHistory;
    }

    private void checkBoard() {
        if (boardMap.size() + bench.size() < 32) {
            throw new IllegalStateException("Es dürfen nicht weniger als 32 Figuren auf der Bank und dem Brett vorhanden sein!");
        }
        checkState();
    }

    private void checkState() {
        List<Figure> board = boardMap.values().stream().filter(figure -> !figure.getPosition().isInBoard()).collect(Collectors.toList());
        List<Figure> bench = getBench().stream().filter(figure -> figure.getPosition() != Position.Bench).collect(Collectors.toList());
        List<Figure> promoted = getPromoted().stream().filter(figure -> figure.getPosition() != Position.Promoted).collect(Collectors.toList());

        if (!board.isEmpty() || !bench.isEmpty() || !promoted.isEmpty()) {
            System.out.println();
        }
    }

    public ObjectProperty<Player> atMovePlayerProperty() {
        return atMovePlayer;
    }

    public List<Figure> getPromoted() {
        return promoted;
    }

    public void buildBoard() {
        boardMap.clear();
        System.out.println("building");
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
    }

    public King getKing(Player player) {
        return player.isWhite() ? whiteKing : blackKing;
    }

    void move(PlayerMove playerMove) throws IllegalMoveException {
        checkBoard();
        if (playerMove.isCastlingMove()) {
            castle(playerMove);
        } else if (playerMove.isPromotion()) {
            promote(playerMove);
        } else {
            final Move secondMove = playerMove.getSecondaryMove().orElse(null);

            if (secondMove != null) {
                makeMove(secondMove);
            }

            makeMove(playerMove.getMainMove());
        }
        setLastMove(playerMove);
        checkBoard();
    }

    private void nextRound() {
        final PlayerMove lastMove = getLastMove();
        if (lastMove != null) {
            if (getAtMovePlayer().equals(lastMove.getPlayer())) {
                throw new IllegalStateException("ein spieler darf nicht zweimal hintereinander ziehen");
            }
        } else if (!getAtMovePlayer().equals(white)) {
            throw new IllegalStateException("weiß muss anfangen");
        }
        round.set(getMovesHistory().size() + 1);
    }

    private void promote(PlayerMove playerMove) throws IllegalMoveException {
        final Move mainMove = playerMove.getMainMove();
        final Figure figure = mainMove.getFigure();

        //remove figure from old position, now empty
        setPosition(null, mainMove.getFrom());

        if (figure.getType() != FigureType.PAWN) {
            throw new IllegalArgumentException("only pawns can be promoted!");
        } else {
            figure.setPosition(Position.Promoted);
            if (!getPromoted().contains(figure)) {
                //save figure to promoted
                getPromoted().add(figure);
            } else {
                throw new IllegalArgumentException("A figure can be promoted only once not twice");
            }
        }

        final Move secondMove = playerMove.getSecondaryMove().orElse(null);

        //strike enemy if allowed
        if (secondMove != null) {
            makeMove(secondMove);
        }

        //set promoted figure to new position
        final Move promotionMove = playerMove.getPromotionMove().orElseThrow(() -> new IllegalStateException("promotion move is null"));
        setPosition(promotionMove.getFigure(), promotionMove.getTo());
    }

    private void castle(PlayerMove playerMove) throws IllegalMoveException {
        final Move mainMove = playerMove.getMainMove();
        final Move secondaryMove = playerMove.getSecondaryMove().orElseThrow(() -> new IllegalStateException("rook move for castling is null"));

        makeMove(mainMove);
        makeMove(secondaryMove);
    }

    private void makeMove(Move move) throws IllegalMoveException {
        final Figure figure = move.getFigure();

        final Position from = move.getFrom();

        final Figure boardFigure = getFigure(from);

        if (!figure.equals(boardFigure)) {
            throw new IllegalMoveException("boardMap is not synched with its figure: different position, boardMap has: " + boardFigure + " for " + figure);
        } else {
            setPosition(null, from);
            final Position to = move.getTo();

            if (to.equals(Position.Bench)) {
                if (!getBench().contains(figure)) {
                    getBench().add(figure);
                } else {
                    throw new IllegalArgumentException("A figure can be benched only once not twice");
                }

                if (figure.getType() == FigureType.KING) {
                    throw new IllegalMoveException("König darf nicht geschlagen werden");
                }
                figure.setPosition(Position.Bench);
            } else {
                setPosition(figure, to);
            }
        }
    }

    private void initListener(Game game) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException();
        }

        lastMoveProperty().addListener((observable, oldValue, newValue) -> processLastMove(newValue));
        atMovePlayerProperty().addListener((observable, oldValue, newValue) -> processMovingPlayer(game, newValue));
        game.pausedProperty().addListener((observable, oldValue, newValue) -> processPaused(newValue));
    }

    private void processMovingPlayer(Game game, Player newValue) {
        if (newValue != null) {
            //check if next player has only one figure, it is implied that his last figure is the king
            if (isPlaying() && getFigures(newValue).size() == 1) {
                game.setLoser(newValue);
            }

            final List<PlayerMove> allowedMoves = getGenerator().getAllowedMoves(newValue, game);
            allowedMoves.stream().filter(move -> move.getMainMove().getFigure().getPlayer() != newValue).forEach(System.err::println);
            allowedMoves.stream().filter(move -> !move.checkMove(this)).forEach(System.err::println);

            if (isPlaying()) {
                setPlaying(false);
            } else {
                setPlaying(true);
            }

            getAllowedMoves().clear();
            getAllowedMoves().addAll(allowedMoves);

            if (getAllowedMoves().isEmpty()) {
                atMovePlayerProperty().set(null);
            }

            processPaused(game.isPaused());
        }
    }

    private void processPaused(boolean paused) {
        if (!paused && isPlaying()) {
            nextRound();
        }
    }

    private void processLastMove(PlayerMove newValue) {
        if (newValue != null) {
            if (getMovesHistory().isEmpty()) {
                getMovesHistory().add(newValue);
            } else if (!moveHistory.getLast().getPlayer().equals(newValue.getPlayer())) {
                getMovesHistory().add(newValue);
            }
        }
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

        if (figure.getType() == FigureType.KING) {
            if (player.isWhite()) {
                whiteKing = (King) figure;
            } else {
                blackKing = (King) figure;
            }
        }

        setPosition(figure, position);
        figures.add(figure);
    }*/
}


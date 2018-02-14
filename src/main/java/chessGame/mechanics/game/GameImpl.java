package chessGame.mechanics.game;

import chessGame.mechanics.*;
import chessGame.mechanics.Timer;
import chessGame.mechanics.board.*;
import chessGame.mechanics.move.MoveForGenerator;
import chessGame.mechanics.move.MoveHistory;
import chessGame.mechanics.move.MoveMaker;
import chessGame.mechanics.move.PlayerMove;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.util.Duration;

import java.util.*;

/**
 *
 */
public class GameImpl implements Game {
    final Player white;
    final Player black;
    List<PlayerMove> allowedMoves;
    Player atMove;
    Timer timer;
    private Board board;
    private MoveHistory history;
    private Map<Color, Map<FigureType, List<Figure>>> bench = new HashMap<>();
    private Map<Color, List<Figure>> promoted = new HashMap<>();
    private BitSet snapShot;

    private boolean movesValid = false;

    private BoardHasher hasher;


    GameImpl(Player black, Player white, Duration duration) {
        Objects.requireNonNull(black);
        Objects.requireNonNull(white);


        if (black.isWhite() && !white.isWhite()) {
            this.white = black;
            this.black = white;
        } else if (!black.isWhite() && white.isWhite()) {
            this.black = black;
            this.white = white;
        } else {
            throw new IllegalArgumentException("Color of Player are not legal: White: " + white + " Black: " + black);
        }


        board = new ArrayBoard();
        timer = new chessGame.mechanics.Timer(duration);

        BoardInitiator.initiate(this);
        history = new MoveHistory(this);

        hasher = new ZobristHasher(this);
        hasher.hashBoard();
        snapShot = BoardEncoder.encode(this);
    }

    GameImpl(Board board, BitSet set) {
        Objects.requireNonNull(board);

        this.black = Player.getBlack();
        this.white = Player.getWhite();

        this.board = board;
        timer = new chessGame.mechanics.Timer(Duration.INDEFINITE);

        history = new MoveHistory(this);
        hasher = new ZobristHasher(this);
        hasher.hashBoard();
        snapShot = set;
    }

    GameImpl(GameImpl game) {
        history = new MoveHistory(this);
        history = game.getHistory();
        bench = game.getBench();
        promoted = game.getPromoted();
        board = game.getBoard();

        atMove = game.getAtMove();
        white = game.getWhite();
        black = game.getBlack();
        allowedMoves = game.getAllowedMoves();

        timer = game.timer;
        movesValid = game.movesValid;
        hasher = game.hasher;
        snapShot = game.snapShot;
    }

    @Override
    public List<PlayerMove> getAllowedMoves() {
        if (!movesValid) {
            allowedMoves = MoveForGenerator.getAllowedMoves(getAtMove().getColor(), this);
            movesValid = true;
        }
        return allowedMoves;
    }

    @Override
    public MoveHistory getHistory() {
        return history;
    }

    @Override
    public Map<Color, Map<FigureType, List<Figure>>> getBench() {
        return bench;
    }

    @Override
    public Map<Color, List<Figure>> getPromoted() {
        return promoted;
    }

    @Override
    public Player getWhite() {
        return white;
    }

    @Override
    public Player getBlack() {
        return black;
    }

    @Override
    public void makeMove(PlayerMove move) {
        if (!getAtMoveColor().equals(move.getColor())) {
            throw new IllegalArgumentException("Move of wrong player " + getAtMove() + " Move: " + move);
        }
        MoveMaker.makeSafeMove(move, getBoard(), this);

        Player enemy = getEnemy(getAtMove());
        setAtMove(enemy);

        getHasher().forwardHash(move);
        BoardEncoder.updateForward(snapShot, this, move);

        getHistory().add(move);
    }

    @Override
    public void singlePlyRedo() {
        PlayerMove last = history.getLast();

        Player atMove = getAtMove();
        setAtMove(getEnemy(atMove));

        this.getHistory().removeLast();

        getHasher().backWardHash(last);
        BoardEncoder.updateBackward(snapShot, this, last);

        MoveMaker.redo(getBoard(), this, last);
    }

    @Override
    public void addPromoted(Figure figure) {
        List<Figure> pawns = getPromoted().computeIfAbsent(figure.getColor(), k -> new ArrayList<>());
        if (!pawns.contains(figure)) {
            pawns.add(figure);
        } else {
            throw new IllegalArgumentException("A Figure can only be promoted once");
        }

        //should never happen
        if (getPromoted().size() > 2) {
            throw new IllegalStateException("Es können nur zwei Spieler vorhanden sein!");
        }
    }

    @Override
    public void addBench(Figure figure) {
        Color enemy = Color.getEnemy(figure.getColor());
        Map<FigureType, List<Figure>> typeListMap = bench.computeIfAbsent(enemy, k -> new HashMap<>());

        List<Figure> figures = typeListMap.computeIfAbsent(figure.getType(), k -> new ArrayList<>());

        if (!figures.contains(figure)) {
            figures.add(figure);
        } else {
            throw new IllegalArgumentException("A figure can be benched only once not twice");
        }

        //should never happen
        if (bench.size() > 2) {
            throw new IllegalStateException("Es können nur zwei Spieler vorhanden sein!");
        }
    }

    @Override
    public PlayerMove getLastMove() {
        return history.getLast();
    }

    @Override
    public BitSet getSnapShot() {
        return (BitSet) snapShot.clone();
    }

    @Override
    public Figure removeFromBench(Color player, FigureType figureType) {
        Color enemy = Color.getEnemy(player);

        Map<FigureType, List<Figure>> types = getBench().get(enemy);
        List<Figure> figures = types.get(figureType);

        if (figures.isEmpty()) {
            return null;
        }

        Figure figure = figures.get(figures.size() - 1);

        if (figure == null) {
            throw new NullPointerException();
        }

        figures.remove(figure);
        return figure;
    }

    @Override
    public Figure removeFromPromoted(Color player) {
        List<Figure> figures = getPromoted().get(player);

        if (figures.isEmpty()) {
            return null;
        }

        Figure figure = figures.get(figures.size() - 1);
        figures.remove(figure);
        return figure;
    }

    @Override
    public Player getAtMove() {
        return atMove == null ? white : atMove;
    }

    @Override
    public Color getAtMoveColor() {
        return getAtMove().getColor();
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public ReadOnlyStringProperty timeProperty() {
        return timer.timeProperty();
    }

    public void setAtMove(Player player) {
        movesValid = false;
        atMove = player;
        BoardEncoder.setAtMove(getSnapShot(), player.getColor());
    }

    Player getEnemy(Player player) {
        return player.isWhite() ? getBlack() : getWhite();
    }

    private BoardHasher getHasher() {
        return hasher;
    }

    Timer getTimer() {
        return timer;
    }
}

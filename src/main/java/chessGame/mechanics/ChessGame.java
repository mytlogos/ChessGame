package chessGame.mechanics;

import chessGame.engine.Engine;
import chessGame.engine.EngineWorker;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.King;
import chessGame.mechanics.figures.Pawn;
import javafx.beans.property.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ChessGame implements Game {
    private MoveHistory history = new MoveHistory();
    private IntegerProperty round = new SimpleIntegerProperty(-1);

    private Map<Player, List<Figure>> bench = new HashMap<>();
    private Map<Player, List<Pawn>> promoted = new HashMap<>();

    private Board board;
    private Player atMove;
    private Player white;
    private Player black;

    private ObjectProperty<PlayerMove> lastMove = new SimpleObjectProperty<>();

    private Timer timer;
    private Player loser;

    private BooleanProperty running = new SimpleBooleanProperty();
    private BooleanProperty paused = new SimpleBooleanProperty();
    private BooleanProperty finished = new SimpleBooleanProperty();

    private List<PlayerMove> allowedMoves;
    private boolean movesValid = false;

    private final Map<Player, Engine> engineMap;

    public ChessGame(List<Player> players) {
        this(players, Duration.INDEFINITE);
    }

    public ChessGame(List<Player> players, Duration duration) {
        final Player player1 = players.get(0);
        final Player player2 = players.get(1);

        if (player1.isWhite() && !player2.isWhite()) {
            white = player1;
            black = player2;
        } else if (player2.isWhite() && !player1.isWhite()) {
            white = player2;
            black = player1;
        } else {
            throw new IllegalArgumentException();
        }
        engineMap = EngineWorker.getEngineWorker().getEngines(this);

        board = new ChessBoard();
        timer = new Timer(duration);

        BoardInitiator.initiate(board, this);
        initListener();
    }

    private void initListener() {
        pausedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
            } else {
                nextRound();
                timer.start();
            }
        });

        runningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !isFinished()) {
                System.out.println("starting");
                timer = new Timer();
                timer.start();
                atMove = white;
                nextRound();
            } else {
                setFinished(true);
                System.out.println(timer.timeProperty().get());
            }
        });

        finishedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.stop();
                System.out.println("winner: " + getWinner());
            }
        });
    }

    public BooleanProperty pausedProperty() {
        return paused;
    }

    public void nextRound() {
        if (!isPaused()) {
            setRound(getHistory().size());
            Engine engine = engineMap.get(getAtMove());

            //start engine only after all round listeners are processed, else concurrency problems on Board will happen,
            //because it is mutable
            if (engine != null) {
                engine.processRound(getRound());
            }
        }
    }

    @Override
    public Game copy() {
        return null;
    }

    @Override
    public void start() {
        timer.start();
    }

    @Override
    public List<PlayerMove> getAllowedMoves() {
        if (!movesValid) {
            allowedMoves = MoveGenerator.getAllowedMoves(getAtMove(), this);
            movesValid = true;
        }
        return allowedMoves;
    }

    @Override
    public MoveHistory getHistory() {
        return history;
    }

    @Override
    public Map<Player, List<Figure>> getBench() {
        return bench;
    }

    @Override
    public Map<Player, List<Pawn>> getPromoted() {
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
    public IntegerProperty roundProperty() {
        return round;
    }

    @Override
    public boolean makeMove(PlayerMove move) throws IllegalMoveException {
        if (!getAtMove().equals(move.getPlayer())) {
            throw new IllegalArgumentException("Move of wrong player " + getAtMove() + " Move: " + move);
        }
        boolean safeMove = MoveMaker.makeSafeMove(move, getBoard(), this);
        getHistory().add(move, getBoard());
        return safeMove;
    }

    @Override
    public boolean redo() {
        boolean redo = simulateRedo();
        nextRound();
        return redo;
    }

    @Override
    public boolean simulateRedo() {
        PlayerMove last = history.getLast();

        if (last == null) {
            return true;
        }

        Player atMove = getAtMove();
        setAtMove(getEnemy(atMove));

        boolean redo = MoveMaker.redo(board, this, this.getLastMove());
        this.getHistory().removeLast();
        return redo;
    }

    @Override
    public int getRound() {
        return round.get();
    }

    @Override
    public void setRound(int round) {
        this.round.set(round);
    }

    @Override
    public void addPromoted(Pawn figure) {
        List<Pawn> pawns = promoted.computeIfAbsent(figure.getPlayer(), k -> new ArrayList<>());
        if (!pawns.contains(figure)) {
            pawns.add(figure);
        } else {
            throw new IllegalArgumentException("A Pawn can only be promoted once");
        }

        //should never happen
        if (promoted.size() > 2) {
            throw new IllegalStateException("Es können nur zwei Spieler vorhanden sein!");
        }
    }

    @Override
    public void addBench(Figure figure) {
        Player enemy = getEnemy(figure.getPlayer());
        List<Figure> figures = bench.computeIfAbsent(enemy, k -> new ArrayList<>());
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
    public void removeFromBench(Figure figure) {
        Player enemy = getEnemy(figure.getPlayer());
        bench.get(enemy).remove(figure);
    }

    @Override
    public void removeFromPromoted(Pawn figure) {
        promoted.get(figure.getPlayer()).remove(figure);
    }

    @Override
    public ObjectProperty<PlayerMove> lastMoveProperty() {
        return lastMove;
    }

    @Override
    public Player getAtMove() {
        return atMove == null ? white : atMove;
    }

    public BooleanProperty finishedProperty() {
        return finished;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public void atMoveFinished() {
        simulateAtMoveFinished();
        lastMoveProperty().set(getHistory().getLast());
    }

    @Override
    public void simulateAtMoveFinished() {
        Player enemy = getEnemy(getAtMove());
        setAtMove(enemy);
    }

    public void setAtMove(Player player) {
        movesValid = false;
        atMove = player;
    }

    public Player getLoser() {
        return loser;
    }

    public void setLoser(Player player) {
        this.loser = player;
        setRunning(false);
    }

    @Override
    public Player getEnemy(Player player) {
        return player == null ? white :  /*getHistory().isEmpty() ? white :
                getHistory().getLast().getPlayer().isWhite() ? black :
                        white :*/
                player.isWhite() ? black :
                        white;
    }

    public boolean isFinished() {
        return finished.get();
    }

    public void setFinished(boolean finished) {
        this.finished.set(finished);
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public ReadOnlyStringProperty timeProperty() {
        return timer.timeProperty();
    }

    public boolean isWon() {
        return isFinished() && loser != null;
    }

    public Player getWinner() {
        return loser == null ? null : loser.isWhite() ?  black : white;
    }

    public boolean isDraw() {
        return isFinished() && loser == null;
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void setPaused(boolean paused) {
        this.paused.set(paused);
    }

    @Override
    public void decideEnd() {
        Player atMove = getAtMove();
        King king = board.getKing(atMove);

        //checks for staleMate
        if (MoveGenerator.isInCheck(king, board, this)) {
            setLoser(atMove);
        } else {
            //staleMate results in draw
            setLoser(null);
        }
    }
}

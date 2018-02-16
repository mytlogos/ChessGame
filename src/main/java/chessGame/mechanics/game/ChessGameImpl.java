package chessGame.mechanics.game;

import chessGame.engine.AlphaBetaExtendedEngine;
import chessGame.engine.Engine;
import chessGame.engine.EngineWorker;
import chessGame.mechanics.Figure;
import chessGame.mechanics.Player;
import chessGame.mechanics.RuleEvaluator;
import chessGame.mechanics.board.Board;
import chessGame.mechanics.board.FigureBoard;
import chessGame.mechanics.move.PlayerMove;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Map;
import java.util.Queue;

/**
 *
 */
public class ChessGameImpl extends GameImpl implements ChessGame {
    private final Map<Player, Engine> engineMap;

    private final Queue<PlayerMove> redoQueue = new ArrayDeque<>();
    private final BooleanProperty madeMove = new SimpleBooleanProperty();
    private Player loser;
    private IntegerProperty round = new SimpleIntegerProperty(-1);
    private BooleanProperty running = new SimpleBooleanProperty();
    private BooleanProperty paused = new SimpleBooleanProperty();
    private BooleanProperty finished = new SimpleBooleanProperty();

    private boolean redo = false;

    public ChessGameImpl() {
        this(Player.getBlack(), Player.getWhite());
    }

    public ChessGameImpl(Player black, Player white) {
        this(black, white, Duration.INDEFINITE);
    }

    public ChessGameImpl(Player black, Player white, Duration duration) {
        super(black, white, duration);

        engineMap = EngineWorker.getEngineWorker().getEngines(this);
        initListener();
    }

    private void initListener() {
        pausedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                stop();
            } else {
                nextRound();
                start();
            }
        });

        runningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !isFinished()) {
                System.out.println("starting");
                timer = new chessGame.mechanics.Timer();
                atMove = white;

                start();
                nextRound();
            } else {
                setFinished();
                System.out.println("Game Duration " + timer.timeProperty().get());
            }
        });

        finishedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                stop();
                System.out.println("winner: " + getWinner());
            }
        });
    }

    private BooleanProperty pausedProperty() {
        return paused;
    }

    private void stop() {
        getTimer().stop();
    }

    private void setFinished() {
        this.finished.set(true);
    }

    public ChessGameImpl(FigureBoard board, BitSet set) {
        super(board, set);
        engineMap = EngineWorker.getEngineWorker().getEngines(this);
        initListener();
    }

    protected ChessGameImpl(ChessGameImpl game) {
        super(game);

        round = game.roundProperty();

        loser = game.getLoser();

        redoQueue.addAll(game.getRedoQueue());
        running = game.runningProperty();
        paused = game.pausedProperty();
        finished = game.finishedProperty();

        engineMap = game.engineMap;
    }

    private Player getLoser() {
        return loser;
    }

    public void setLoser(Player player) {
        this.loser = player;
        setRunning(false);
        Engine whiteEngine = engineMap.get(white);

        if (whiteEngine instanceof AlphaBetaExtendedEngine) {
            System.out.println("Average Duration White " + ((AlphaBetaExtendedEngine) whiteEngine).duration.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            System.out.println("Average CutOffRate White " + ((AlphaBetaExtendedEngine) whiteEngine).cutOffRates.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        }

        Engine blackEngine = engineMap.get(black);

        if (blackEngine instanceof AlphaBetaExtendedEngine) {
            System.out.println("Average Duration Black " + ((AlphaBetaExtendedEngine) blackEngine).duration.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            System.out.println("Average CutOffRate Black " + ((AlphaBetaExtendedEngine) blackEngine).cutOffRates.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        }
    }

    public boolean isFinished() {
        return finished.get();
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

    public boolean isWon() {
        return isFinished() && loser != null;
    }

    public Player getWinner() {
        return loser == null ? null : loser.isWhite() ? black : white;
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

        RuleEvaluator.End end = RuleEvaluator.checkEndGame(getSimulation(), atMove.getColor());

        if (end == RuleEvaluator.End.WIN) {
            setLoser(getEnemy(atMove));
        } else if (end == RuleEvaluator.End.LOSS) {
            setLoser(atMove);
        } else if (end == RuleEvaluator.End.DRAW) {
            setLoser(null);
        }
    }

    @Override
    public void redo() {
        PlayerMove playerMove = getLastMove();
        if (playerMove != null) {
            setRedo(true);

            Player player = playerMove.isWhite() ? getWhite() : getBlack();
            Player enemy = getEnemy(player);

            if (player.isHuman() && enemy.isHuman()) {
                //if last move player is human and at move player is human
                //redo two moves
                //todo request redo from enemy
                //as long as it is not implemented redo needs to be on false
                setRedo(false);
            } else if (!player.isHuman() && enemy.isHuman()) {
                //if last move player is not human and at move player is human
                //redo two moves

                //first redo on board, then redo on gui board
                singlePlyRedo();
                redoQueue.add(playerMove);

                PlayerMove lastMove = getLastMove();

                if (lastMove != null) {
                    //first redo on board, then redo on gui board
                    singlePlyRedo();
                    redoQueue.add(lastMove);
                }
                madeMove.set(true);
            } else if (!player.isHuman() && !enemy.isHuman()) {
                //if last move player is not human and at move player is not human
                // redo only one move
                Engine engine = engineMap.get(enemy);
                engine.cancel();

                singlePlyRedo();
                redoQueue.add(playerMove);
                madeMove.set(true);
            } else {
                //if last move player is human but at move player is not
                // redo only one move
                Engine engine = engineMap.get(enemy);
                engine.cancel();

                singlePlyRedo();
                redoQueue.add(playerMove);
                madeMove.set(true);
            }
        }
    }

    @Override
    public void start() {
        getTimer().start();
    }

    @Override
    public boolean isRedo() {
        return redo;
    }

    private void setRedo(boolean redo) {
        this.redo = redo;
    }

    public void nextRound() {
        if (!isPaused()) {
            madeMove.set(false);
            redoQueue.clear();
            decideEnd();

            if (!isFinished()) {
                setRound(getHistory().size());
                Engine engine = engineMap.get(getAtMove());

                //start engine only after all round listeners are processed, else concurrency problems on Board<Figure> will happen,
                //because it is mutable
                if (engine != null) {
                    engine.processRound(getRound());
                }
            }
        }
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
    public SimulationGame getSimulation() {
        return new SimulationGameImpl(this);
    }

    @Override
    public BooleanProperty madeMoveProperty() {
        return madeMove;
    }

    @Override
    public IntegerProperty roundProperty() {
        return round;
    }

    public BooleanProperty finishedProperty() {
        return finished;
    }

    @Override
    public Queue<PlayerMove> getRedoQueue() {
        return redoQueue;
    }

    @Override
    public void makeMove(PlayerMove move) {
        super.makeMove(move);
        setRedo(false);
        madeMove.set(true);
    }


}

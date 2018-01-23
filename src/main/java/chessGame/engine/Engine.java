package chessGame.engine;

import chessGame.mechanics.*;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Base Class for the Chess Engine.
 * Needs to be initialised from the JavaFX-ApplicationThread.
 * It listens to the {@link Game#roundProperty()} and makes a {@link PlayerMove}
 * upon an Update.
 */
public class Engine extends Service<PlayerMove> {
    final Game game;
    final Player player;
    int maxDepth;

    private final Random random = new Random();
    private Map<EngineMove, SearchItem> searchItemMap = Collections.synchronizedMap(new HashMap<>());
    private long nanoTime;

    Engine(Game game, Player player, int maxDepth) {
        this(game, player);
        this.maxDepth = maxDepth;
    }

    @Override
    public void start() {
        nanoTime = System.nanoTime();
        super.start();
    }

    Engine(Game game, Player player) {
        Objects.requireNonNull(game);
        Objects.requireNonNull(player);

        if (!Platform.isFxApplicationThread()) {
            throw new IllegalThreadStateException("Wurde nicht vom FX-Thread initialisiert");
        }

        this.game = game;
        this.player = player;


    }

    public void processRound(Number newValue) {
        System.out.println("new round" + newValue + "|" + player);
        if (newValue != null && Objects.equals(game.getAtMove(), player)) {
            System.out.println("starting up");
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(this::startEngine);
            } else {
                startEngine();
            }
        }
    }

    private void startEngine() {
        if (getState() == State.FAILED || getState() == State.SUCCEEDED) {
            restart();
        } else if (getState() == State.READY) {
            start();
        } else {
            throw new IllegalStateException("Engine wurde nicht in einem Anfangs oder EndZustand gestartet: War in Zustand: " + getState());
        }
    }

    @Override
    protected void succeeded() {
        long time = System.nanoTime();
        double seconds = Duration.millis(java.time.Duration.ofNanos(time - nanoTime).toMillis()).toSeconds();
        System.out.println("succeeded in " + seconds + " Seconds");

        if (!Platform.isFxApplicationThread()) {
            System.out.print("");
        }

        final PlayerMove playerMove = getValue();
        if (playerMove == null) {
            //null means player has no valid moves anymore, but does not result automatically in ones loss, can still claim draw
            game.decideEnd();
        } else {
            try {
                //make a move of this gameBoard on this gameBoard
                game.makeMove(playerMove);
                game.atMoveFinished();
                game.nextRound();
            } catch (IllegalMoveException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Engine versuchte einen illegalen Zug durchzuführen: " + playerMove);
                alert.show();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void failed() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("Engine für Spieler " + player.getType() + " crashed");
        alert.show();
        getException().printStackTrace();
    }

    @Override
    protected Task<PlayerMove> createTask() {
        return new Task<>() {
            @Override
            protected PlayerMove call() throws Exception {
                return getChoice();
            }
        };
    }

    PlayerMove getChoice() {
        searchItemMap.values().forEach(SearchItem::resetLevel);

        final List<PlayerMove> moves = MoveGenerator.getAllowedMoves(player, game).
                stream().
                map(move -> move.engineClone(game.getBoard(), game)).
                collect(Collectors.toList());


//        final SearchItem searchItem = search(moves, maxDepth);
        final PlayerMove searchItem = chooseMove(moves);
        System.out.println(searchItemMap.size());
        System.out.println(searchItem);
        //return null or a move on the gameBoard
//        return searchItem == null ? null : searchItem.getMove().engineClone(game.getBoard());
        return searchItem;
    }

    /**
     * Chooses a {@link PlayerMove} from a List per Random.
     *
     * @param moves moves to choose from
     * @return a PlayerMove or null if List is empty
     */
    PlayerMove chooseMove(List<PlayerMove> moves) {
        final int anInt = random.nextInt(moves.size());
        return moves.isEmpty() ? null : moves.get(anInt);
    }
}

/* *//**
 * Starts the pseudo implementation of the NegaMax-Algorithm.
 *
 * @param moves
 * @param maxDepth
 * @return
 * @param playerMove move to evaluate
 * @param parent     parent SearchItem
 * @param depth      maxDepth of the search, representing the player atMove,
 * @param maxDepth   maximal Search maxDepth
 * @param playerMove move to evaluate
 * @param parent     parent SearchItem
 * @param depth      maxDepth of the search, representing the player atMove,
 * @param maxDepth   maximal Search maxDepth
 * @see #search(EngineMove, SearchItem, int, int)
 * <p>
 * A pseudo-derived Implementation of the NegaMax-Algorithm.
 * @see <a href="https://de.wikipedia.org/wiki/Minimax-Algorithmus#Variante:_Der_Negamax-Algorithmus">Negamax-Algorithm Wikipedia</a>
 * <p>
 * A pseudo-derived Implementation of the NegaMax-Algorithm.
 * @see <a href="https://de.wikipedia.org/wiki/Minimax-Algorithmus#Variante:_Der_Negamax-Algorithmus">Negamax-Algorithm Wikipedia</a>
 *//*
    private SearchItem search(List<EngineMove> moves, int maxDepth) {
        return moves.stream().parallel().
                map(move -> search(move, null, 0, maxDepth)).
                filter(Objects::nonNull).
                max(Comparator.comparingInt(SearchItem::getTotalScore)).
                orElse(null);
    }

    *//**
 * A pseudo-derived Implementation of the NegaMax-Algorithm.
 *
 * @param playerMove move to evaluate
 * @param parent     parent SearchItem
 * @param depth      maxDepth of the search, representing the player atMove,
 * @param maxDepth   maximal Search maxDepth
 * @see <a href="https://de.wikipedia.org/wiki/Minimax-Algorithmus#Variante:_Der_Negamax-Algorithmus">Negamax-Algorithm Wikipedia</a>
 *//*
    private SearchItem search(EngineMove playerMove, SearchItem parent, int depth, int maxDepth) {
        if (depth == maxDepth) {
            return null;
        }
        final Board before = playerMove.getBoard();

        return createNewItem(playerMove, parent, depth, maxDepth, before);
        //if it was evaluated once, use it as starting point, else evaluate new
*//*
        if (searchItemMap.containsKey(playerMove)) {
            return useEvaluatedItem(playerMove, parent, maxDepth, after);
        } else {
            return createNewItem(playerMove, parent, maxDepth, maxDepth, before, after);
        }
*//*
    }

    private Board getAfterMoveBoard(EngineMove playerMove) {
        final Board after = playerMove.getBoard().clone();

        final PlayerMove clonedMove = playerMove.clone(after);
        try {
            //should never be null
            after.makeMove(clonedMove);
        } catch (IllegalMoveException ignored) {
            //should never happen, because the playerMove is selected from legal moves
            return null;
        }
        return after;
    }

    private SearchItem useEvaluatedItem(EngineMove playerMove, SearchItem parent, int maxDepth) {
        System.out.println("evaluated once");
        final SearchItem searchItem = searchItemMap.get(playerMove);

        final int itemDepth = searchItem.getDepth();

        if (itemDepth < maxDepth && !searchItem.isSearching()) {
            final Board after = getAfterMoveBoard(playerMove);
            if (after == null) return null;
            System.out.println("maxDepth not enough");
            after.getAllowedMoves().
                    stream().parallel().
                    map(move -> move.engineClone(after)).
                    forEach(move -> search(move, searchItem, itemDepth + 1, maxDepth));
        }

        if (parent == null) {
            return searchItem;
        } else {
            parent.addChildren(searchItem);
            return null;
        }
    }

    private SearchItem createNewItem(EngineMove playerMove, SearchItem parent, int depth, int maxDepth, Board before) {
        final Board after = getAfterMoveBoard(playerMove);
        if (after == null) return null;

        final int rating;

        //if maxDepth is even, startPlayer is atMove, else enemy is at move
        if (depth % 2 == 0) {
            rating = evaluateBoard(before, after, playerMove);
        } else {
            rating = -evaluateBoard(before, after, playerMove);
        }

        final SearchItem searchItem = new SearchItem(before, after, depth, playerMove, rating);

//        searchItemMap.put(playerMove, searchItem);

        if (parent != null) {
            parent.addChildren(searchItem);
        }

        after.getAllowedMoves().
                stream().parallel().
                map(move -> move.engineClone(after)).
                forEach(move -> search(move, searchItem, depth + 1, maxDepth));

        searchItem.setSearchingFinished();
        return searchItem;
    }


    private int evaluateBoard(Board before, Board after, PlayerMove move) {
        final int whiteSum = getDominationSum(before, Board::getWhite);
        final int blackSum = getDominationSum(before, Board::getBlack);

        final int afterWhiteSum = getDominationSum(after, Board::getWhite);
        final int afterBlackSum = getDominationSum(after, Board::getBlack);

        final int dominationBefore = difference(move.getPlayer().isWhite(), blackSum, whiteSum);
        final int dominationAfter = difference(move.getPlayer().isWhite(), afterBlackSum, afterWhiteSum);

        //evaluate own move for striking a figure
        final int strike = evaluateStrike(move);

        //check for moves the enemy can do for striking one of the own figure
        final int enemySum = after.getAllowedMoves().stream().mapToInt(this::evaluateStrike).sum();

        int result = strike - enemySum;

        if (after.getAllowedMoves().isEmpty()) {
            result = result + 1000;
        }

        //how many fields this player dominates with his own figures,
        //e.g. how many fields he could theoretically move to
        final int dominationDifference = dominationAfter - dominationBefore;

        return result + dominationDifference;
    }

    private int evaluateStrike(PlayerMove playerMove) {
        if (playerMove.isNormal()) {
            return playerMove.getSecondaryMove().map(move -> {
                final Figure figure = move.getFigure();

                final int size = figure.getAllowedPositions().size();
                final int worth = figure.getType().getWorth();
                return size * worth;
            }).orElse(0);

        }
        return 0;
    }

    private int difference(boolean isWhite, int blackSum, int whiteSum) {
        return isWhite ? whiteSum - blackSum : blackSum - whiteSum;
    }

    private int getDominationSum(Board board, Function<Board, Player> playerFunction) {
        final List<Figure> figures = getPlayerFigures(playerFunction.apply(board), board);
        return figures.stream().mapToInt(figure -> figure.getAllowedPositions().size()).sum();
    }

    private List<Figure> getPlayerFigures(Player player, Board board) {
        final Map<Player, List<Figure>> playerFigures = board.figures().stream().collect(Collectors.groupingBy(Figure::getPlayer));
        return playerFigures.get(player);
    }
}*/

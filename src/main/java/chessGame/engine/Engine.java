package chessGame.engine;

import chessGame.mechanics.*;
import chessGame.mechanics.figures.Figure;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Base Class for the Chess Engine.
 * Needs to be initialised from the JavaFX-ApplicationThread.
 * It listens to the {@link Board#atMoveProperty()} and makes a {@link PlayerMove}
 * upon an Update.
 */
class Engine extends Service<PlayerMove> {
    final Game game;
    final Player player;
    private Map<EngineMove, SearchItem> searchItemMap = Collections.synchronizedMap(new HashMap<>());
    private int maxDepth;
    private final Random random = new Random();

    Engine(Game game, Player player) {
        Objects.requireNonNull(game);
        Objects.requireNonNull(player);

        if (!Platform.isFxApplicationThread()) {
            throw new IllegalThreadStateException("Wurde nicht vom FX-Thread initialisiert");
        }

        this.game = game;
        this.player = player;

        game.getBoard().atMoveProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getPlayer() == player) {
                System.out.println("starting up");
                if (getState() == State.FAILED || getState() == State.SUCCEEDED) {
                    restart();
                } else if (getState() == State.READY) {
                    start();
                } else {
                    throw new IllegalStateException("Engine wurde nicht in einem Anfangs oder EndZustand gestartet: War in Zustand: " + getState());
                }
            }
        });

        setOnSucceeded(event -> {
            final PlayerMove playerMove = getValue();
            if (playerMove == null) {
                game.setLoser(this.player);
            } else {
                try {
                    final Board board = game.getBoard();
                    //make a move of this gameBoard on this gameBoard
                    board.makeMove(playerMove);
                } catch (IllegalMoveException e) {
                    //todo show error popup
                    e.printStackTrace();
                }
            }
        });
        setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Engine für Spieler " + player.getType() + " crashed");
            alert.show();
            event.getSource().getException().printStackTrace();
        });
    }

    Engine(Game game, Player player, int maxDepth) {
        this(game, player);
        this.maxDepth = maxDepth;
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

    PlayerMove getChoice() {
        searchItemMap.values().forEach(SearchItem::resetLevel);

        final List<EngineMove> moves = game.
                getBoard().
                getGenerator().
                getAllowedMoves(player).stream().
                map(move -> move.engineClone(game.getBoard())).
                collect(Collectors.toList());

        final SearchItem searchItem = search(moves, maxDepth);
        System.out.println(searchItemMap.size());
        System.out.println(searchItem);
        //return null or a move on the gameBoard
        return searchItem == null ? null : searchItem.getMove().engineClone(game.getBoard());
    }

    private SearchItem search(List<EngineMove> moves, int maxDepth) {
        return moves.stream().parallel().
                map(move -> search(move, null, 0, maxDepth)).
                filter(Objects::nonNull).
                max(Comparator.comparingInt(SearchItem::getTotalScore)).
                orElse(null);
    }

    /**
     * An derived Implementation of the NegaMax-Algorithm.
     *
     * @param playerMove move to evaluate
     * @param parent     parent SearchItem
     * @param depth      maxDepth of the search, representing the player atMove,
     * @param maxDepth   maximal Search maxDepth
     * @see <a href="https://de.wikipedia.org/wiki/Minimax-Algorithmus#Variante:_Der_Negamax-Algorithmus">Negamax-Algorithm Wikipedia</a>
     */
    private SearchItem search(EngineMove playerMove, SearchItem parent, int depth, int maxDepth) {
        if (depth == maxDepth) {
            return null;
        }
        final AbstractBoard before = playerMove.getBoard();
        final LockedBoard after = before.cloneBoard();

        if (!playerMove.getBoard().equals(after)) {
            System.out.println();
        }

        final PlayerMove clonedMove = playerMove.clone(after);
        try {
            //should never be null
            after.makeMove(clonedMove);
            after.atMoveFinished();
        } catch (IllegalMoveException ignored) {
            //should never happen, because the playerMove is selected from legal moves
            return null;
        }

        return createNewItem(playerMove, parent, depth, maxDepth, before, after);
        //if it was evaluated once, use it as starting point, else evaluate new
/*
        if (searchItemMap.containsKey(playerMove)) {
            return useEvaluatedItem(playerMove, parent, maxDepth, after);
        } else {
            return createNewItem(playerMove, parent, maxDepth, maxDepth, before, after);
        }
*/
    }

    private SearchItem useEvaluatedItem(EngineMove playerMove, SearchItem parent, int maxDepth, LockedBoard after) {
        System.out.println("evaluated once");
        final SearchItem searchItem = searchItemMap.get(playerMove);

        final int itemDepth = searchItem.getDepth();

        if (itemDepth < maxDepth && !searchItem.isSearching()) {
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

    private SearchItem createNewItem(EngineMove playerMove, SearchItem parent, int depth, int maxDepth, AbstractBoard before, LockedBoard after) {
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


    private int evaluateBoard(AbstractBoard before, LockedBoard after, PlayerMove move) {
        final int whiteSum = getDominationSum(before, AbstractBoard::getWhite);
        final int blackSum = getDominationSum(before, AbstractBoard::getBlack);

        final int afterWhiteSum = getDominationSum(after, AbstractBoard::getWhite);
        final int afterBlackSum = getDominationSum(after, AbstractBoard::getBlack);

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

    private int evaluateStrike(PlayerMove move) {
        if (move.isNormal()) {
            final Move secondaryMove = move.getSecondaryMove();
            if (secondaryMove == null) {
                return 0;
            }

            final Figure figure = secondaryMove.getFigure();

            final int size = figure.getAllowedPositions().size();
            final int worth = figure.getType().getWorth();
            return size * worth;
        }
        return 0;
    }

    private int difference(boolean isWhite, int blackSum, int whiteSum) {
        return isWhite ? whiteSum - blackSum : blackSum - whiteSum;
    }

    private int getDominationSum(AbstractBoard board, Function<AbstractBoard, Player> playerFunction) {
        final List<Figure> figures = getPlayerFigures(playerFunction.apply(board), board);
        return figures.stream().mapToInt(figure -> figure.getAllowedPositions().size()).sum();
    }

    private List<Figure> getPlayerFigures(Player player, AbstractBoard board) {
        final Map<Player, List<Figure>> playerFigures = board.figures().stream().collect(Collectors.groupingBy(Figure::getPlayer));
        return playerFigures.get(player);
    }
}

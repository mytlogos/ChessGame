package chessGame.engine;

import chessGame.mechanics.*;
import chessGame.mechanics.board.FigureBoard;
import chessGame.mechanics.board.BoardSnapShot;
import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.move.MoveForGenerator;
import chessGame.mechanics.move.PlayerMove;
import chessGame.mechanics.move.PositionGenerator;

import java.util.*;
import java.util.function.Function;

/**
 *
 */
public class PseudoEngine extends Engine {
    private final Map<EngineMove, SearchItem> searchItemMap = Collections.synchronizedMap(new HashMap<>());


    PseudoEngine(ChessGame game, Player player, int maxDepth) {
        super(game, player, maxDepth);
    }

    PseudoEngine(ChessGame game, Player player) {
        super(game, player);
    }

    @Override
    PlayerMove getChoice() {
        searchItemMap.values().forEach(SearchItem::resetLevel);

        //removed cloning
        final List<PlayerMove> moves = new ArrayList<>(MoveForGenerator.getAllowedMoves(player.getColor(), game));


//        final SearchItem searchItem = search(moves, maxDepth);
        final PlayerMove searchItem = chooseMove(moves);
        System.out.println(searchItemMap.size());
        System.out.println(searchItem);
        //return null or a move on the gameBoard
//        return searchItem == null ? null : searchItem.getMove().engineClone(game.getBoard());
        return searchItem;
    }

    /**
     * Starts the pseudo implementation of the NegaMax-Algorithm.
     *
     * @param moves
     * @param maxDepth
     * @param maxDepth maximal Search maxDepth
     * @return
     * @see #search(EngineMove, SearchItem, int, int)
     * <p>
     * A pseudo-derived Implementation of the NegaMax-Algorithm.
     * @see <a href="https://de.wikipedia.org/wiki/Minimax-Algorithmus#Variante:_Der_Negamax-Algorithmus">Negamax-Algorithm Wikipedia</a>
     * <p>
     * A pseudo-derived Implementation of the NegaMax-Algorithm.
     * @see <a href="https://de.wikipedia.org/wiki/Minimax-Algorithmus#Variante:_Der_Negamax-Algorithmus">Negamax-Algorithm Wikipedia</a>
     */
    private SearchItem search(List<EngineMove> moves, int maxDepth) {
        return moves.stream().parallel().
                map(move -> search(move, null, 0, maxDepth)).
                filter(Objects::nonNull).
                max(Comparator.comparingInt(SearchItem::getTotalScore)).
                orElse(null);
    }

    /**
     * A pseudo-derived Implementation of the NegaMax-Algorithm.
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
        final BoardSnapShot before = playerMove.getBoard();

        return createNewItem(playerMove, parent, depth, maxDepth, before);
        //if it was evaluated once, use it as starting point, else evaluate new

        /*if (searchItemMap.containsKey(playerMove)) {
            return useEvaluatedItem(playerMove, parent, maxDepth);
        } else {
            return createNewItem(playerMove, parent, maxDepth, maxDepth, before);
        }*/
    }

    private SearchItem useEvaluatedItem(EngineMove playerMove, SearchItem parent, int maxDepth) {
        System.out.println("evaluated once");
        final SearchItem searchItem = searchItemMap.get(playerMove);

        final int itemDepth = searchItem.getDepth();

        if (itemDepth < maxDepth && !searchItem.isSearching()) {
            System.out.println("maxDepth not enough");
        }

        if (parent == null) {
            return searchItem;
        } else {
            parent.addChildren(searchItem);
            return null;
        }
    }

    private SearchItem createNewItem(EngineMove playerMove, SearchItem parent, int depth, int maxDepth, BoardSnapShot before) {
        final int rating;

        //if maxDepth is even, startPlayer is atMove, else enemy is at move
        /*if (depth % 2 == 0) {
            rating = evaluateBoard(before, after, playerMove);
        } else {
            rating = -evaluateBoard(before, after, playerMove);
        }

        final SearchItem searchItem = new SearchItem(before, after, depth, playerMove, rating);

//        searchItemMap.put(playerMove, searchItem);

        if (parent != null) {
            parent.addChildren(searchItem);
        }

        searchItem.setSearchingFinished();*/
        return null;
    }


    private double evaluateBoard(FigureBoard before, FigureBoard after, PlayerMove move) {
//        final int whiteSum = getDominationSum(before, Board<Figure>::getWhite);
//        final int blackSum = getDominationSum(before, Board<Figure>::getBlack);
//
//        final int afterWhiteSum = getDominationSum(after, Board<Figure>::getWhite);
//        final int afterBlackSum = getDominationSum(after, Board<Figure>::getBlack);
//
//        final int dominationBefore = difference(move.getColor().isWhite(), blackSum, whiteSum);
//        final int dominationAfter = difference(move.getColor().isWhite(), afterBlackSum, afterWhiteSum);

        //evaluate own move for striking a figure
        final double strike = evaluateStrike(move);

        //check for moves the enemy can do for striking one of the own figure
//        final int enemySum = after.getAllowedMoves().stream().mapToInt(this::evaluateStrike).sum();

        double result = strike;

//        if (after.getAllowedMoves().isEmpty()) {
//            result = result + 1000;
//        }

        //how many fields this player dominates with his own figures,
        //e.g. how many fields he could theoretically move to
//        final int dominationDifference = dominationAfter - dominationBefore;

        return result;
    }

    private double evaluateStrike(PlayerMove playerMove) {
        if (playerMove.isNormal()) {
            return playerMove.getSecondaryMove().map(move -> {
                FigureType figure = move.getFigure();

//                int size = figure.getAllowedPositions().size();
                double worth = figure.getWorth();
                return worth;
            }).orElse(0d);

        }
        return 0;
    }

    private int difference(boolean isWhite, int blackSum, int whiteSum) {
        return isWhite ? whiteSum - blackSum : blackSum - whiteSum;
    }

    private int getDominationSum(FigureBoard board, Function<ChessGame, Color> playerFunction) {
        final List<Figure> figures = board.getFigures(playerFunction.apply(game));
        return figures.stream().mapToInt(figure -> PositionGenerator.getAllowedPositions(figure, board).size()).sum();

    }

}

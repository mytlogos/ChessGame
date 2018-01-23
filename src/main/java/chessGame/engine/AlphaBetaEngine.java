package chessGame.engine;

import chessGame.mechanics.*;
import chessGame.mechanics.Board;
import chessGame.mechanics.Game;
import chessGame.mechanics.figures.Figure;
import chessGame.mechanics.figures.FigureType;
import chessGame.mechanics.figures.King;
import impl.org.controlsfx.tools.MathTools;
import javafx.beans.property.ObjectProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
class AlphaBetaEngine extends Engine {
    private PlayerMove best;

    AlphaBetaEngine(Game game, Player player, int maxDepth) {
        super(game, player, maxDepth);
    }

    @Override
    PlayerMove getChoice() {
        final Board board = game.getBoard();

        final List<PlayerMove> allowedMoves = MoveGenerator.getAllowedMoves(game.getAtMove(), game);

        if (allowedMoves.isEmpty()) {
            return null;
        }
        //use clone to avoid triggering unwanted things
//        final Board clone = board.clone();
        //use simulate to avoid triggering the listener bound to gui
//        board.setPlaying(true);
        int rating = alphaBeta(board, maxDepth, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        System.out.println("Rating " + rating +  " for " + best);
        if (best == null) {
            System.out.println("failed?");
        }
        System.out.println("counted: " + alphaBetaCounter);
        double moveAv = movesTime.stream().mapToLong(Long::longValue).average().orElse(0);
        double redoAv = redosTime.stream().mapToLong(Long::longValue).average().orElse(0);
        double evalAv = evalTime.stream().mapToLong(Long::longValue).average().orElse(0);

        BigDecimal moves = BigDecimal.valueOf(moveAv / 1_000_000d).setScale(3, RoundingMode.HALF_EVEN);
        BigDecimal redos = BigDecimal.valueOf(redoAv / 1_000_000d).setScale(3, RoundingMode.HALF_EVEN);
        BigDecimal evals = BigDecimal.valueOf(evalAv  / 1_000_000d).setScale(3, RoundingMode.HALF_EVEN);

        moveAv = moves.doubleValue();
        redoAv = redos.doubleValue();
        evalAv = evals.doubleValue();
        System.out.println("Moves Average: " + moveAv + " ms | RedoAverage: " + redoAv + " ms  | EvalAverage: " + evalAv + " ms");

        return best == null ? chooseMove(allowedMoves) : best;
    }

    private int alphaBetaCounter;
    private List<Long> movesTime = new ArrayList<>();
    private List<Long> redosTime = new ArrayList<>();
    private List<Long> evalTime = new ArrayList<>();

    private int alphaBeta(Board board, int maxDepth, int depth, int alpha, int beta) {
        alphaBetaCounter++;

        final List<PlayerMove> moves = new ArrayList<>(game.getAllowedMoves());
        if (depth == 0 || moves.isEmpty()) {
            long before = System.nanoTime();
            int evaluate = evaluate(board);
            long after = System.nanoTime();
            evalTime.add(after - before);
            return evaluate;
        }


        //sort the moves after the values of their figures
        moves.sort(Comparator.comparingInt(this::worth));

        int max = alpha;
        for (PlayerMove move : moves) {
            try {
                long beforeMove = System.nanoTime();
                game.makeMove(move);
                long afterMove = System.nanoTime();
                movesTime.add(afterMove - beforeMove);
                game.simulateAtMoveFinished();
                int worth = -alphaBeta(board, maxDepth, depth - 1, negate(beta), negate(max));
                long beforeRedo = System.nanoTime();
                game.simulateRedo();
                long afterRedo = System.nanoTime();
                redosTime.add(afterRedo - beforeRedo);

                if (worth > max) {
                    max = worth;

                    if (max >= beta) {
                        break;
                    }
                    if (depth == maxDepth) {
                        best = move;
                    }
                }
            } catch (IllegalMoveException e) {
                e.printStackTrace();
                return Integer.MIN_VALUE;
            }
        }
        return max;
    }

    /**
     * Returns the negated Integer of the Parameter.
     * Treats the negate of {@link Integer#MIN_VALUE} as
     * {@link Integer#MAX_VALUE} to prevent overflow.
     *
     * @param i value to negate
     * @return 'negated' value
     */
    private int negate(int i) {
        return i == Integer.MIN_VALUE ? Integer.MAX_VALUE : -i;
    }

    /**
     * @param playerMove
     * @return
     */
    private int worth(PlayerMove playerMove) {
        final Move mainMove = playerMove.getMainMove();
        int worth = mainMove.getFigure().getType().getWorth();

        final Integer secondaryWorth = playerMove.getSecondaryMove().map(move -> move.getFigure().getType().getWorth()).orElse(0);
        worth += secondaryWorth;

        final Integer promotionWorth = playerMove.getPromotionMove().map(move -> move.getFigure().getType().getWorth()).orElse(0);
        worth += promotionWorth;

        double worthInPawnUnits = worth / 100d;
        return (int) Math.round(worthInPawnUnits);
    }

    /**
     * Evaluates the Board according to the sum of the player figures worth, subtracting the worth
     * of their figures on the bench.
     * A Board which evaluates to a Draw will be subtracted 500 points, if it evaluates to a Win
     * 2000 points will be added.
     *
     * @param board board to evaluate
     * @return board evaluation in point of view of the drawing/moving player
     */
    private int evaluate(Board board) {
        final List<Figure> blackFigures = board.getFigures(game.getBlack());
        final List<Figure> whiteFigures = board.getFigures(game.getWhite());

        final Player atMovePlayer = game.getAtMove();
        Player player = atMovePlayer == null ? game.getEnemy(game.getLastMove().getPlayer()) : atMovePlayer;

        final int figureWorthDiff = getFigureWorthDiff(blackFigures, whiteFigures, player);
        final int benchWorthDiff = getBenchWorthDiff(player);
        int eval = figureWorthDiff + benchWorthDiff;

//        List<Figure> atMoveFigures = board.getFigures(player);
//        List<Figure> notAtMoveFigures = board.getFigures(board.getEnemy(player));

//        List<Position> atMovePosition = getAllPositions(board, atMoveFigures);
//        List<Position> notAtMovePosition = getAllPositions(board, notAtMoveFigures);

//        int checkSum = atMoveFigures.stream().filter(figure -> notAtMovePosition.contains(figure.getPosition())).mapToInt(figure -> figure.getType().getWorth()).sum();
//        int guardSum = atMoveFigures.stream().filter(figure -> notAtMovePosition.contains(figure.getPosition())).mapToInt(figure -> figure.getType().getWorth()).sum();

        int fiftyMoves = checkFiftyMovesRule(game.getHistory());
        eval += fiftyMoves;

        int threeRep = checkThreeRepRule(game.getHistory(), board);
        eval += threeRep;

        if (MoveGenerator.getAllowedMoves(game.getEnemy(player), game).isEmpty()) {
            King king = board.getKing(player);

            //player at move has no legal moves, but king is not in check results in a draw
            if (MoveGenerator.isInCheck(king, board, game)) {
                int winBonus = 2000;
                eval += winBonus;
            } else {
                int drawMali = -500;
                eval += drawMali;
            }
        }
        return eval;
    }

    private int getBenchWorthDiff(Player atMovePlayer) {
        final int benchWorthDiff;

        if (!game.getBench().isEmpty()) {
            final Map<Player, Integer> playerBenchWorth = game.
                    getBench().
                    entrySet().
                    stream().
                    collect(Collectors.toMap(Map.Entry::getKey, this::getSum));

            final Integer atMoveBenchWorthInt = playerBenchWorth.get(atMovePlayer);
            final Integer notAtMoveBenchWorthInt = playerBenchWorth.get(game.getEnemy(game.getAtMove()));

            final int atMoveBenchWorth = atMoveBenchWorthInt == null ? 0 : atMoveBenchWorthInt;
            final int notAtMoveBenchWorth = notAtMoveBenchWorthInt == null ? 0 : notAtMoveBenchWorthInt;

            benchWorthDiff = atMoveBenchWorth - notAtMoveBenchWorth;
        } else {
            benchWorthDiff = 0;
        }
        double benchWorthDiffInPawns = benchWorthDiff / 100d;
        return (int) Math.round(benchWorthDiffInPawns);
    }

    private Integer getSum(Map.Entry<Player, List<Figure>> entry) {
        return entry.getValue().stream().mapToInt(figure -> figure.getType().getWorth()).sum();
    }

    private int getFigureWorthDiff(List<Figure> blackFigures, List<Figure> whiteFigures, Player player) {
        int figureWorthDiff;
        final Integer blackFiguresWorth = blackFigures.stream().mapToInt(figure -> figure.getType().getWorth()).sum();
        final Integer whiteFiguresWorth = whiteFigures.stream().mapToInt(figure -> figure.getType().getWorth()).sum();

        if (player.isWhite()) {
            figureWorthDiff = whiteFiguresWorth - blackFiguresWorth;
        } else {
            figureWorthDiff = blackFiguresWorth - whiteFiguresWorth;
        }
        double figureWorthDiffInPawns = figureWorthDiff / 100d;
        return (int) Math.round(figureWorthDiffInPawns);
    }

    /**
     * The Threefold Repetition Rule says that a Set of Positions (A Board)
     * can only occur up to 3 times?
     *
     *
     * @param history move history of the game, not null
     * @param board
     * @return
     * @see <a href="https://en.wikipedia.org/wiki/Threefold_repetition">Threefold Repetition</a>
     */
    private int checkThreeRepRule(MoveHistory history, Board board) {
        //Threefold repetition rule: if a set of positions occur tree times a draw can be made
        if (history.checkOccurrences(board) >= 3) {
            return -500;
        }
        return 0;
    }

    /**
     * Checks for the Fifty-Move-Rule.
     *  A'"move" consists of a player completing their turn followed by the opponent completing their turn".
     *
     * @param history history of the Game
     * @return a value evaluating this Rule, -500 if this rule is not followed, else zero.
     * @see <a href="https://en.wikipedia.org/wiki/Fifty-move_rule">Fifty-Move-Rule</a>
     */
    private int checkFiftyMovesRule(MoveHistory history) {
        if (history.size() < 100) {
            return 0;
        }
        return history.lastHundred().stream().anyMatch(fiftyMovesPredicate()) ? 0 : -500;
    }

    /**
     * Check if move is a Strike or a Pawn-Move.
     *
     * @return true if this rule is followed.
     */
    private Predicate<PlayerMove> fiftyMovesPredicate() {
        return move -> move.isStrike() || move.getMainMove().getFigure().getType() == FigureType.PAWN;
    }
}

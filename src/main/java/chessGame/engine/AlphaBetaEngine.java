package chessGame.engine;

import chessGame.mechanics.*;
import chessGame.mechanics.board.Board;
import chessGame.mechanics.game.ChessGameImpl;
import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.game.SimulationGame;
import chessGame.mechanics.game.SimulationGameImpl;
import chessGame.mechanics.move.*;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
class AlphaBetaEngine extends Engine {
    private PlayerMove best;
    private int alphaBetaCounter;

    AlphaBetaEngine(ChessGame game, Player player, int maxDepth) {
        super(game, player, maxDepth);
    }

    @Override
    PlayerMove getChoice() {
        long nanoTime = System.nanoTime();

        final List<PlayerMove> allowedMoves = game.getAllowedMoves();
        SimulationGame game = this.game.getSimulation();

        if (allowedMoves.isEmpty()) {
            return null;
        }
        int rating = alphaBeta(game, maxDepth, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        long time = System.nanoTime();
        double seconds = Duration.millis(java.time.Duration.ofNanos(time - nanoTime).toMillis()).toSeconds();

        printInfo(rating, seconds);

        return best == null ? chooseMove(allowedMoves) : best;
    }

    private void printInfo(int rating, double seconds) {
        System.out.println("Rating " + rating + " for " + best);
        if (best == null) {
            System.out.println("failed?");
        }
        System.out.println("counted: " + alphaBetaCounter);
        System.out.println("succeeded in " + seconds + " seconds");
        alphaBetaCounter = 0;
    }

    private int alphaBeta(SimulationGame game, int maxDepth, int depth, int alpha, int beta) {
        alphaBetaCounter++;
        final List<PlayerMove> moves = new ArrayList<>(MoveForGenerator.getAllowedMoves(game.getAtMove().getColor(), game));

        if (moves.isEmpty()) {
            return evaluate(game);
        }

        if (depth == 0) {
            return evaluate(game);
        }

        //sort the moves after the values of their figures
        moves.sort(Comparator.comparingDouble(this::worth).reversed());

        int max = alpha;
        for (PlayerMove move : moves) {
            game.setAllowedMoves(moves);
            game.makeMove(move);

            int worth = -alphaBeta(game, maxDepth, depth - 1, negate(beta), negate(max));
            game.singlePlyRedo();

            if (worth > max) {
                max = worth;

                if (max >= beta) {
                    break;
                }
                if (depth == maxDepth) {
                    best = move;
                }
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
     *
     *
     * @param playerMove
     * @return
     */
    private double worth(PlayerMove playerMove) {
        final Move mainMove = playerMove.getMainMove();
        double worth = mainMove.getFigure().getWorth();

        final Double secondaryWorth = playerMove.getSecondaryMove().map(move -> move.getFigure().getWorth()).orElse(0d);
        worth = worth + secondaryWorth * 10;

        final Double promotionWorth = playerMove.getPromotionMove().map(move -> move.getFigure().getWorth()).orElse(0d);
        worth += promotionWorth;

        return worth;
    }

    /**
     * Evaluates the Board according to the sum of the player figures worth, subtracting the worth
     * of their figures on the bench.
     * A Board which evaluates to a Draw will be subtracted 500 points, if it evaluates to a Win
     * 2000 points will be added.
     *
     * @param game game with board to evaluate
     * @return board evaluation in point of view of the drawing/moving player
     */
    private int evaluate(SimulationGame game) {
        Board board = game.getBoard();

        Map<Color, List<Figure>> playerFigures = board.getPlayerFigures();

        final Color player = game.getAtMove().getColor();

        List<Figure> atMoveFigures = playerFigures.get(player);
        List<Figure> notAtMoveFigures = playerFigures.get(Color.getEnemy(player));


        final int figureWorthDiff = getFigureWorthDiff(atMoveFigures, notAtMoveFigures);
        final int benchWorthDiff = getBenchWorthDiff(player);
        int eval = figureWorthDiff + benchWorthDiff;

        List<Position> atMovePosition = getAllPositions(board, atMoveFigures);
        List<Position> notAtMovePosition = getAllPositions(board, notAtMoveFigures);

        //the sum of the figures which are on board and in check from enemy
        double atMoveCheckSum = positionSum(board, atMoveFigures, notAtMovePosition);
        double notAtMoveCheckSum = positionSum(board, atMoveFigures, atMovePosition);

        //the sum of the figures which are on board and are guarded by own
        double atMoveGuardSum = positionSum(board, atMoveFigures, atMovePosition);
        double notAtMoveGuardSum = positionSum(board, notAtMoveFigures, notAtMovePosition);

        int figureRelations = (int) (atMoveGuardSum - atMoveCheckSum + notAtMoveCheckSum - notAtMoveGuardSum);
        eval += figureRelations;


        //end game evaluation
        RuleEvaluator.End end = RuleEvaluator.checkEndGame(game, player);

        if (end == RuleEvaluator.End.WIN) {
            eval += winBonus;
        } else if (end == RuleEvaluator.End.DRAW) {
            eval += drawMali;
        }else {
            //game has not ended, but could end if draw can be claimed
            eval += RuleEvaluator.canClaimDraw(game) ? drawMali : 0;
        }
        return eval;
    }

    private double positionSum(Board board, List<Figure> atMoveFigures, List<Position> notAtMovePosition) {
        return atMoveFigures.stream().filter(figure -> notAtMovePosition.contains(board.positionOf(figure))).mapToDouble(figure -> figure.getType().getWorth()).sum();
    }

    private List<Position> getAllPositions(Board board, List<Figure> atMoveFigures) {
        return atMoveFigures.stream().flatMap(figure -> PositionGenerator.getAllowedPositions(figure, board).stream()).collect(Collectors.toList());
    }

    private int getBenchWorthDiff(Color atMovePlayer) {
        final double benchWorthDiff;

        if (!game.getBench().isEmpty()) {
            final Map<Color, Double> playerBenchWorth =
                    game.getBench().
                    entrySet().
                    stream().
                    collect(Collectors.toMap(Map.Entry::getKey, this::getSum));

            final Double atMoveBenchWorthInt = playerBenchWorth.get(atMovePlayer);
            final Double notAtMoveBenchWorthInt = playerBenchWorth.get(Color.getEnemy(game.getAtMove().getColor()));

            final double atMoveBenchWorth = atMoveBenchWorthInt == null ? 0 : atMoveBenchWorthInt;
            final double notAtMoveBenchWorth = notAtMoveBenchWorthInt == null ? 0 : notAtMoveBenchWorthInt;

            benchWorthDiff = atMoveBenchWorth - notAtMoveBenchWorth;
        } else {
            benchWorthDiff = 0;
        }
        return (int) Math.round(benchWorthDiff);
    }

    private Double getSum(Map.Entry<Color, Map<FigureType, List<Figure>>> entry) {
        return entry.getValue().values().stream().mapToDouble(list -> list.stream().mapToDouble(figure -> figure.getType().getWorth()).sum()).sum();
    }

    private int getFigureWorthDiff(List<Figure> atMoveFigures, List<Figure> notAtMoveFigures) {
        double figureWorthDiff;
        final Double atMoveFiguresWorth = atMoveFigures.stream().mapToDouble(figure -> figure.getType().getWorth()).sum();
        final Double notAtMoveFiguresWorth = notAtMoveFigures.stream().mapToDouble(figure -> figure.getType().getWorth()).sum();

        figureWorthDiff = atMoveFiguresWorth - notAtMoveFiguresWorth;
        return (int) Math.round(figureWorthDiff);
    }
}

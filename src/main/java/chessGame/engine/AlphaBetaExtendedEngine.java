package chessGame.engine;

import chessGame.mechanics.*;
import chessGame.mechanics.board.Board;
import chessGame.mechanics.game.ChessGame;
import chessGame.mechanics.game.SimulationGame;
import chessGame.mechanics.move.Move;
import chessGame.mechanics.move.MoveForGenerator;
import chessGame.mechanics.move.PlayerMove;
import chessGame.mechanics.move.PositionGenerator;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class AlphaBetaExtendedEngine extends Engine {
    private PlayerMove best;
    private int rating;
    private int alphaBetaCounter;
    private int quiescentCounter;

    private TranspositionTable table = new TranspositionTable();
    private int cutOffCounter;

    public List<Double> duration = new ArrayList<>();
    public List<Double> cutOffRates = new ArrayList<>();

    AlphaBetaExtendedEngine(ChessGame game, Player player, int maxDepth) {
        super(game, player, maxDepth);
    }

    @Override
    PlayerMove getChoice() {
        long nanoTime = System.nanoTime();
        table.resetUsage();
        best = null;

        final List<PlayerMove> allowedMoves = game.getAllowedMoves();

        SimulationGame game = this.game.getSimulation();

        if (allowedMoves.isEmpty()) {
            return null;
        }

//        alphaBetaTransPosition(game, maxDepth, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        iterativeDeepening(game, maxDepth);

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
        System.out.println("Cut off " + cutOffCounter);
        BigDecimal cutOffRate = BigDecimal.valueOf(cutOffCounter).divide(BigDecimal.valueOf(alphaBetaCounter), 1, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));
        System.out.println("Cut off rate " + cutOffRate);
        System.out.println("counted: " + alphaBetaCounter);
        System.out.println("counted silent: " + quiescentCounter);
        System.out.println("succeeded in " + seconds + " seconds");
        duration.add(seconds);
        cutOffRates.add(cutOffRate.doubleValue());

        alphaBetaCounter = 0;
        quiescentCounter = 0;
        cutOffCounter = 0;
        table.printInfo();
    }

    private void iterativeDeepening(SimulationGame game, int maxDepth) {
        for (int depth = 1; depth < maxDepth + 1; depth++) {
            alphaBetaTransPosition(game, depth, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    }

    private int alphaBeta(SimulationGame game, int maxDepth, int depth, int alpha, int beta) {
        alphaBetaCounter++;
        game.getBoard();

        if (depth == 0) {
//            PlayerMove lastMove = game.getLastMove();
//            return lastMove != null && lastMove.isStrike() ? quiescentSearch(game, alpha, beta) : evaluate(game);
            return evaluate(game);
        }

        final List<PlayerMove> moves = new ArrayList<>(MoveForGenerator.getAllowedMoves(game.getAtMoveColor(), game));

        if (moves.isEmpty()) {
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
                    cutOffCounter++;
                    break;
                }
                if (depth == maxDepth) {
                    best = move;
                    rating = worth;
                }
            }
        }
        return max;
    }

    private int alphaBetaTransPosition(SimulationGame game, int maxDepth, int depth, int alpha, int beta) {
        alphaBetaCounter++;

        Board board = game.getBoard();
        int originalAlpha = alpha;

        Entry entry = table.getEntry(board);

        if (entry != null) {
            if (entry.getDepth() >= depth) {
                if (entry.getBound() == Entry.Bound.EXACT) {
                    if (maxDepth == depth) {
                        best = entry.getBestMove();
                    }
                    return entry.getEvaluation();

                } else if (entry.getBound() == Entry.Bound.LOWER) {
                    alpha = Math.max(alpha, entry.getEvaluation());

                } else if (entry.getBound() == Entry.Bound.UPPER) {
                    beta = Math.min(beta, entry.getEvaluation());
                }

                if (alpha > beta) {
                    if (maxDepth == depth) {
                        best = entry.getBestMove();
                    }
                    return entry.getEvaluation();
                }
            }
        }

        if (depth == 0) {
//            PlayerMove lastMove = game.getLastMove();
//            return lastMove != null && lastMove.isStrike() ? quiescentSearch(game, alpha, beta) : evaluate(game);
            return evaluate(game);
        }

        final List<PlayerMove> moves = new ArrayList<>(MoveForGenerator.getAllowedMoves(game.getAtMoveColor(), game));

        if (moves.isEmpty()) {
            return evaluate(game);
        }

        //sort the moves after the values of their figures
        moves.sort(Comparator.comparingDouble(this::worth).reversed());
        sortMove(entry, moves);

        PlayerMove bestMove = null;

        int max = alpha;
        for (PlayerMove move : moves) {
            game.setAllowedMoves(moves);
            game.makeMove(move);

            int worth = -alphaBetaTransPosition(game, maxDepth, depth - 1, negate(beta), negate(max));
            game.singlePlyRedo();

            if (worth > max) {
                max = worth;
                bestMove = move;

                if (max >= beta) {
                    cutOffCounter++;
                    break;
                }

                if (depth == maxDepth) {
                    best = move;
                    rating = worth;
                }
            }
        }
        storeEntry(depth, beta, board, originalAlpha, bestMove, max);
        return max;
    }

    private void sortMove(Entry entry, List<PlayerMove> moves) {
        if (entry != null) {
            PlayerMove bestMove = entry.getBestMove();
            if (bestMove == null) {
                return;
            }

            if (moves.contains(bestMove)) {
                //remove bestMove
                moves.remove(bestMove);
                //to insert it as first item to examine
                moves.add(0, bestMove);
            } else {
                System.out.println();
            }
        }
    }

    private void storeEntry(int depth, int beta, Board board, int originalAlpha, PlayerMove bestMove, int max) {
        long hash = board.getHash();

        Entry.Bound bound;
        if (max <= originalAlpha) {
            bound = Entry.Bound.UPPER;
        } else if (max >= beta) {
            bound = Entry.Bound.LOWER;
        } else {
            bound = Entry.Bound.EXACT;
        }
        Entry newEntry = new Entry(hash, max, bestMove, depth, bound);
        table.add(newEntry);
    }

    private int quiescentSearch(SimulationGame game, int alpha, int beta) {
        quiescentCounter++;
        game.getBoard();

        if (!game.getLastMove().isStrike()) {
            return evaluate(game);
        }

        final List<PlayerMove> moves = new ArrayList<>(MoveForGenerator.getStrikes(game, game.getAtMoveColor()));

        if (moves.isEmpty()) {
            return evaluate(game);
        }

        //sort the moves after the values of their figures
        moves.sort(Comparator.comparingDouble(this::strikeWorth).reversed());

        int max = alpha;
        for (PlayerMove move : moves) {

            game.setAllowedMoves(moves);
            game.makeMove(move);

            int worth = -quiescentSearch(game, negate(beta), negate(max));
            game.singlePlyRedo();

            if (worth > max) {
                max = worth;

                if (max >= beta) {
                    break;
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
     * Worth of the the Strike.
     *
     * @param playerMove
     * @return
     */
    private double strikeWorth(PlayerMove playerMove) {
        final Double secondaryWorth = playerMove.getSecondaryMove().map(move -> move.getFigure().getWorth()).orElse(0d);

        if (secondaryWorth == 0) {
            return 0;
        }

        final Move mainMove = playerMove.getMainMove();
        double worth = mainMove.getFigure().getWorth();
        worth = 1 / worth;

        worth += secondaryWorth;

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

        Color atMoveColor = game.getAtMoveColor();
        Color enemy = Color.getEnemy(atMoveColor);

        List<Figure> atMoveFigures = playerFigures.get(atMoveColor);
        List<Figure> notAtMoveFigures = playerFigures.get(enemy);

        final int figureWorthDiff = getFigureWorthDiff(atMoveFigures, notAtMoveFigures);
        final int benchWorthDiff = getBenchWorthDiff(atMoveColor, enemy);
        int eval = figureWorthDiff + benchWorthDiff;

        List<Position> atMovePosition = getAllPositions(board, atMoveFigures);
        List<Position> notAtMovePosition = getAllPositions(board, notAtMoveFigures);

        //the sum of the figures which are on board and in check from enemy
        double atMoveCheckSum = positionSum(board, atMoveFigures, notAtMovePosition);
        double notAtMoveCheckSum = positionSum(board, notAtMoveFigures, atMovePosition);

        //the sum of the figures which are on board and are guarded by own
        double atMoveGuardSum = positionSum(board, atMoveFigures, atMovePosition);
        double notAtMoveGuardSum = positionSum(board, notAtMoveFigures, notAtMovePosition);

        int figureRelations = (int) (atMoveGuardSum - atMoveCheckSum + notAtMoveCheckSum - notAtMoveGuardSum);
        eval += figureRelations;


        //end game evaluation
        RuleEvaluator.End end = RuleEvaluator.checkEndGame(game, atMoveColor);

        if (end == RuleEvaluator.End.WIN) {
            eval += winBonus;
        } else if (end == RuleEvaluator.End.DRAW) {
            eval += drawMali;
        } else if (end == RuleEvaluator.End.LOSS) {
            eval -= winBonus;
        } else {
            //game has not ended, but could end if draw can be claimed
            eval += RuleEvaluator.canClaimDraw(game) ? drawMali : 0;
        }
        return eval;
    }

    private double positionSum(Board board, List<Figure> atMoveFigures, List<Position> notAtMovePosition) {
        return atMoveFigures.stream().filter(figure -> notAtMovePosition.contains(board.positionOf(figure))).mapToDouble(figure -> figure.getType().getWorth()).sum();
    }

    private List<Position> getAllPositions(Board board, List<Figure> atMoveFigures) {
        return atMoveFigures.stream().flatMap(figure -> PositionGenerator.getPositions(figure, board).stream()).collect(Collectors.toList());
    }

    private int getBenchWorthDiff(Color atMovePlayer, Color enemy) {
        final double benchWorthDiff;

        if (!game.getBench().isEmpty()) {
            final Map<Color, Double> playerBenchWorth = game.
                    getBench().
                    entrySet().
                    stream().
                    collect(Collectors.toMap(Map.Entry::getKey, this::getSum));

            final Double atMoveBenchWorthInt = playerBenchWorth.get(atMovePlayer);
            final Double notAtMoveBenchWorthInt = playerBenchWorth.get(enemy);

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

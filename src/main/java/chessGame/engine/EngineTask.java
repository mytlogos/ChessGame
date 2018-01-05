package chessGame.engine;

import chessGame.mechanics.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 *
 */
public class EngineTask extends Service<PlayerMove> {

    private final Game game;
    private final AtMove atMove;
    private final Random random = new Random();

    EngineTask(Game game, AtMove atMove) {
        Objects.requireNonNull(game);
        Objects.requireNonNull(atMove);

        this.game = game;
        this.atMove = atMove;
    }

    @Override
    protected Task<PlayerMove> createTask() {
        return new Task<>() {
            @Override
            protected PlayerMove call() {
                final List<PlayerMove> moves = atMove.getAllowedMoves();
                final int anInt = random.nextInt(moves.size());
                System.out.println("possible moves: ");
                moves.forEach(System.out::println);
                final PlayerMove move = moves.isEmpty() ? null : moves.get(anInt);
                System.out.println("choosing: " + move);
                return move;
            }
        };
    }
}

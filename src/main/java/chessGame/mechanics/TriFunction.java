package chessGame.mechanics;

/**
 * Functional Interface which accepts three arguments and returns a Result R.
 */
@FunctionalInterface
public interface TriFunction<S, T, U, R> {
    R apply(S s, T t, U u);
}

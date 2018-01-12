package chessGame.mechanics;

/**
 *
 */
public class IllegalMoveException extends Exception {

    IllegalMoveException() {
    }

    IllegalMoveException(String message) {
        super(message);
    }

    IllegalMoveException(String message, Throwable cause) {
        super(message, cause);
    }

    IllegalMoveException(Throwable cause) {
        super(cause);
    }

    IllegalMoveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

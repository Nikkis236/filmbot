package com.tg.filmbot.dao.connection;

/**
 * The type connection pool exception.
 *
 * @see Exception
 */
public class ConnectionPoolException extends Exception {

    public ConnectionPoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionPoolException(Throwable cause) {
        super(cause);
    }
}

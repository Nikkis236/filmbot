package com.tg.filmbot.dao.exception;

/**
 * The type Dao exception for invalid data.
 */
public class DataNotFoundException extends DAOException {
    public DataNotFoundException() {
        super();
    }

    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotFoundException(Throwable cause) {
        super(cause);
    }
}

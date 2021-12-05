package com.tg.filmbot.dao.connection;

import java.sql.Connection;

/**
 * The interface connection pool.
 */
public interface ConnectionPool {

    /**
     * Get connection from pool
     *
     * @return connection
     * @throws ConnectionPoolException when the connection cannot be taken
     */
    Connection getConnection() throws ConnectionPoolException;

    /**
     * Return the connection to the pool
     *
     * @param connection connection to return
     */
    void releaseConnection(Connection connection);

    /**
     * Initialize pool of connections
     *
     * @throws ConnectionPoolException when there are problems connecting to the database
     */
    void init(String bundle) throws ConnectionPoolException;

    /**
     * Dispose pool of connection
     *
     * @throws ConnectionPoolException when the connection pool cannot be disposed
     */
    void dispose() throws ConnectionPoolException;
}

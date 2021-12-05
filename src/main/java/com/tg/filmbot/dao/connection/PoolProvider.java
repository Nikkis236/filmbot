package com.tg.filmbot.dao.connection;

import com.tg.filmbot.dao.connection.impl.ConnectionPoolImpl;

/**
 * The class that serves as a provider for the pool of connections
 */
public final class PoolProvider {

    /**
     * Instance of {@link PoolProvider}
     */
    private static final ConnectionPool connectionPool = new ConnectionPoolImpl();

    private PoolProvider() {
    }

    public static ConnectionPool getConnectionPool() {
        return connectionPool;
    }
}

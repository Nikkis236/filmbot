package com.tg.filmbot.dao.connection.impl;

import com.tg.filmbot.dao.connection.ConnectionPool;
import com.tg.filmbot.dao.connection.ConnectionPoolException;
import com.tg.filmbot.dao.connection.resource.DBParameter;
import com.tg.filmbot.dao.connection.resource.DBResourceManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The class for working with connection pool
 *  @author Anton Brunner
 */
public class ConnectionPoolImpl implements ConnectionPool {


    private static final Logger logger = LogManager.getLogger(ConnectionPoolImpl.class);
    /**
     * Queue for storing free pool connections
     */
    private BlockingQueue<Connection> connectionPool;

    /**
     * Queue for for accounting for the given pool connections
     */
    private BlockingQueue<Connection> usedConnections;

    /**
     * Database driver name
     */
    private String driver;

    /**
     * URL to get the database connection
     */
    private String url;

    /**
     * Database user name to get the database connection
     */
    private String user;

    /**
     * Password to get the database connection
     */
    private String password;

    /**
     * Number of connections to create
     */
    private int size;

    public ConnectionPoolImpl()  {
    }

    @Override
    public void init(String bundleName)throws ConnectionPoolException {
        DBResourceManager resourceManager = DBResourceManager.getInstance();
        resourceManager.setBundle(bundleName);
        this.driver = resourceManager.getValue(DBParameter.DB_DRIVER);
        this.url = resourceManager.getValue(DBParameter.DB_URL);
        this.user = resourceManager.getValue(DBParameter.DB_USER);
        this.password = resourceManager.getValue(DBParameter.DB_PASSWORD);

        try {
            this.size = Integer.parseInt(resourceManager.getValue(DBParameter.DB_POOLSIZE));
        }catch (NumberFormatException e){
            logger.log(Level.WARN,"pool size error. Creating pool with size 10");
            this.size = 10;
        }
        try {
            Class.forName(driver);
            connectionPool = new ArrayBlockingQueue<>(size);
            usedConnections = new ArrayBlockingQueue<>(size);

            for(int i =0;i<size;i++){
                Connection connection = DriverManager.getConnection(url,user,password);
                connectionPool.add(connection);
            }
        } catch (SQLException sqlE) {
            throw new ConnectionPoolException("can't connect to db,  check url,user,password",sqlE);
        } catch (ClassNotFoundException e) {
            throw new ConnectionPoolException("can't find database driver class",e);
        }
    }

    @Override
    public Connection getConnection() throws ConnectionPoolException {
        Connection connection = null;
        try {
            connection = connectionPool.take();
            usedConnections.add(connection);
        } catch (InterruptedException e) {
            throw new ConnectionPoolException("can't take connection",e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(Connection connection) {
        if(connection!=null)
        {
            usedConnections.remove(connection);
            connectionPool.add(connection);
        }
    }

    /**
     * Dispose connection pool
     * @throws ConnectionPoolException
     */
    @Override
    public void dispose()throws ConnectionPoolException{
        try {
            clearConnectionQueue();
        } catch (SQLException throwables) {
            throw new ConnectionPoolException(throwables);
        }
    }
    /**
     * Clear connection queues
     *
     * @throws SQLException  if {@link SQLException} occurs
     */
    private void clearConnectionQueue() throws SQLException {
        closeConnectionQueue(connectionPool);
        closeConnectionQueue(usedConnections);
    }


    /**
     *  * Close connection queue
     * @param connectionPool queue {@link BlockingQueue} of {@link Connection} queue to close
     * @throws SQLException  if {@link SQLException} occurs
     */
    private void closeConnectionQueue(BlockingQueue<Connection> connectionPool) throws SQLException {

        Connection connection;
        while ((connection= connectionPool.poll())!=null){
            if(!connection.getAutoCommit()) {
               connection.commit();
            }
           connection.close();
        }

    }
}

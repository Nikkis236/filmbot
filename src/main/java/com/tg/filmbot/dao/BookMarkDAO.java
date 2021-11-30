package com.tg.filmbot.dao;

import com.tg.filmbot.dao.connection.ConnectionPool;
import com.tg.filmbot.dao.connection.ConnectionPoolException;
import com.tg.filmbot.dao.connection.PoolProvider;
import com.tg.filmbot.entity.Bookmark;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BookMarkDAO {

    private static final Logger logger = LogManager.getLogger(BookMarkDAO.class);

    private static final String SAVE = "insert into user_bookmark(chat_id, movie_id) values(?,?)";

    private final ConnectionPool connectionPool = PoolProvider.getConnectionPool();

    public void save(Bookmark bookmark) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(SAVE);
            preparedStatement.setString(1, bookmark.getChatId());
            preparedStatement.setString(2, bookmark.getMovieId());
            preparedStatement.execute();
        } catch (SQLException | ConnectionPoolException e) {
            logger.log(Level.ERROR, e);
        } finally {
            connectionPool.releaseConnection(connection);
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.log(Level.ERROR, e);
                }
            }
        }
    }
}

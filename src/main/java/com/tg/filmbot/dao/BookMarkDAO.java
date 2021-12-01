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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookMarkDAO {

    private static final Logger logger = LogManager.getLogger(BookMarkDAO.class);

    private static final String SAVE = "insert into bot.user_bookmark(chat_id, movie_id) values(?,?)";
    private static final String GET_MOVIE = "select distinct ub.movie_id from bot.user_BOOKMARK ub   where chat_id = ?";

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

    public List<String> getAllMovieByChat(String chatId){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(GET_MOVIE);
            preparedStatement.setString(1, chatId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return convertToMovies(resultSet);
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
        return Collections.emptyList();
    }

    private List<String> convertToMovies(ResultSet resultSet) throws SQLException {
        List<String> movies = new ArrayList<>();
        while (resultSet.next()){
            movies.add(resultSet.getString(1));
        }
        return movies;
    }
}

package com.tg.filmbot.dao;


/**
 * The class that serves as a provider for the dao layer
 */
public final class DAOProvider {

    /**
     * Instance of {@link DAOProvider}
     */
    private static final DAOProvider instance = new DAOProvider();
    private final BookMarkDAO bookMarkDAO = new BookMarkDAO();

    private DAOProvider() {
    }

    public static DAOProvider getInstance() {
        return instance;
    }

    public BookMarkDAO getBookmarkDAO() {
        return bookMarkDAO;
    }
}

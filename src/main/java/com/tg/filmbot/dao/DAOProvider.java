package com.tg.filmbot.dao;


/**
 * The class that serves as a provider for the dao layer
 */
public final class DAOProvider {

    /**
     * Instance of {@link DAOProvider}
     */
    private static DAOProvider instance = new DAOProvider();

    private DAOProvider(){}

    public static DAOProvider getInstance(){
        return instance;
    }

    private BookMarkDAO bookMarkDAO = new BookMarkDAO();


    public BookMarkDAO getBookmarkDAO() {return bookMarkDAO;}
}

package com.tg.filmbot.dao.connection.resource;

import java.util.ResourceBundle;

/**
 * The class resource manager for database
 */
public final class DBResourceManager {
    private static final String DEFAULT_BUNDLE = "db";


    /**
     * Instance of {@link DBResourceManager}
     */
    private final static DBResourceManager instance = new DBResourceManager();

    private ResourceBundle bundle;

    /**
     * Get instance of this class
     *
     * @return {@link DBResourceManager} instance
     */
    public static DBResourceManager getInstance() {
        return instance;
    }

    /**
     * Get parameter value by key
     *
     * @param key {@link String} parameter name
     * @return {@link String} parameter value
     */
    public String getValue(String key) {
        return bundle.getString(key);
    }


    /**
     * Set bundle
     *
     * @param bundleFile bundle file name
     */
    public void setBundle(String bundleFile) {
        bundle = ResourceBundle.getBundle(bundleFile);
    }
}

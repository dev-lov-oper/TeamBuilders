package com.campus.matching.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ================================
 *  DESIGN PATTERN: SINGLETON
 * ================================
 * DatabaseManager handles SQLite database connectivity using pure JDBC.
 *
 * Requirements:
 *  1. Private constructor to prevent direct instantiation.
 *  2. One static instance (INSTANCE) loaded lazily/eagerly.
 *  3. Static getInstance() method returning the unique instance.
 *  4. Clean JDBC connection management (no JPA / Hibernate).
 */
public final class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    // Single static instance (Singleton)
    private static DatabaseManager instance;

    // Database JDBC URL
    private final String dbUrl;

    // Private constructor prevents external instantiation
    private DatabaseManager() {
        this.dbUrl = resolveDatabaseUrl();
        try {
            // Explicitly load SQLite JDBC driver class
            Class.forName("org.sqlite.JDBC");
            LOGGER.info("SQLite JDBC Driver successfully loaded.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load SQLite JDBC Driver", e);
            throw new IllegalStateException("SQLite JDBC Driver missing from classpath", e);
        }
    }

    /**
     * Singleton instance retriever (Thread-safe double-checked locking).
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Resolves the TeamBuilders SQLite database path.
     * Uses the Java-owned data/team_builders.db database by default.
     */
    private String resolveDatabaseUrl() {
        String customPath = System.getProperty("db.path");
        if (customPath == null || customPath.isBlank()) {
            customPath = "data/team_builders.db";
        }

        File dbFile = new File(customPath);
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        LOGGER.info("Connecting to TeamBuilders SQLite DB at: " + dbFile.getAbsolutePath());
        return "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    /**
     * Creates and returns a fresh JDBC Connection to the SQLite database.
     *
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to obtain SQLite connection using URL: " + dbUrl, e);
            throw e;
        }
    }

    /**
     * Helper method to verify that database connection can be established.
     *
     * @return true if connection is valid, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "SQLite connection test failed", e);
            return false;
        }
    }

    public String getDbUrl() {
        return dbUrl;
    }
}

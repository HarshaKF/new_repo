package com.justglance.tuition;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DBConnection {
    // Static initializer for logging configuration
    static {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(Level.ALL);
        
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.ALL);
    }

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    
    // Configuration from environment variables or default values
    private static final String DRIVER_CLASS = System.getenv().getOrDefault("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/just_glance_tuition?useSSL=false&serverTimezone=UTC");
    private static final String USERNAME = System.getenv().getOrDefault("DB_USERNAME", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");
    
    private static final int MAX_POOL_SIZE = Integer.parseInt(System.getenv().getOrDefault("DB_MAX_POOL_SIZE", "10"));
    private static final int CONNECTION_TIMEOUT = Integer.parseInt(System.getenv().getOrDefault("DB_CONNECTION_TIMEOUT", "5"));
    
    private final BlockingQueue<Connection> connectionPool;
    private static volatile DBConnection instance;
    
    private DBConnection() {
        try {
            // Load JDBC driver
            Class.forName(DRIVER_CLASS);
            LOGGER.info("JDBC Driver loaded successfully: " + DRIVER_CLASS);
            
            connectionPool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);
            initializeConnectionPool();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load JDBC driver: " + DRIVER_CLASS, e);
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }
    
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }
    
    private void initializeConnectionPool() {
        int successfulConnections = 0;
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            try {
                Connection connection = createNewConnection();
                if (isConnectionValid(connection)) {
                    connectionPool.offer(connection);
                    successfulConnections++;
                    LOGGER.info("Successfully created and validated connection " + (successfulConnections));
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error creating connection", e);
            }
        }
        
        if (successfulConnections == 0) {
            LOGGER.severe("Unable to establish any database connections");
            throw new RuntimeException("No database connections could be established");
        }
        
        LOGGER.info("Connection pool initialized with " + successfulConnections + " connections");
    }
    
    private Connection createNewConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        props.setProperty("autoReconnect", "true");
        props.setProperty("useSSL", "false");
        
        LOGGER.info("Attempting to create new database connection");
        return DriverManager.getConnection(URL, props);
    }
    
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = connectionPool.poll();
            if (connection == null || !isConnectionValid(connection)) {
                LOGGER.warning("Creating a new connection as the polled connection is invalid");
                connection = createNewConnection();
            }
            return connection;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error obtaining connection from pool", e);
            throw new SQLException("Unable to obtain database connection", e);
        }
    }
    
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed() && isConnectionValid(connection)) {
                    if (!connectionPool.offer(connection)) {
                        LOGGER.warning("Connection pool is full. Closing the connection.");
                        connection.close();
                    } else {
                        LOGGER.info("Connection released back to the pool");
                    }
                } else {
                    LOGGER.warning("Closing invalid connection");
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error releasing connection", e);
            }
        }
    }
    
    private boolean isConnectionValid(Connection connection) {
        if (connection == null) {
            return false;
        }
        
        try {
            // Check if connection is closed
            if (connection.isClosed()) {
                LOGGER.warning("Connection is already closed");
                return false;
            }
            
            // Validate connection with a lightweight test
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SELECT 1");
            }
            
            // Additional isValid check
            return connection.isValid(CONNECTION_TIMEOUT);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Connection validation failed", e);
            return false;
        }
    }
    
    public void shutdown() {
        LOGGER.info("Shutting down connection pool");
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection during shutdown", e);
            }
        }
        LOGGER.info("Connection pool shutdown complete");
    }
    
    public static void demonstrateConnection() {
        DBConnection db = DBConnection.getInstance();
        Connection conn = null;
        try {
            conn = db.getConnection();
            LOGGER.info("Connection successfully obtained!");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error using connection", e);
        } finally {
            if (conn != null) {
                db.releaseConnection(conn);
            }
        }
    }
    
    public static void main(String[] args) {
        DBConnection dbConnection = DBConnection.getInstance();
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            LOGGER.info("Successfully connected to database!");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
        } finally {
            if (conn != null) {
                dbConnection.releaseConnection(conn);
            }
            dbConnection.shutdown();
        }
    }
}
package config.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import config.AppConfig;

/**
 * Utility class responsible for creating database connections.
 * 
 * This class centralizes all connection-related logic so it can be reused
 * across the application and maintained easily.
 */
public class DBConnection {

    /**
     * Establishes a connection to the database using the configuration
     * provided in AppConfig.
     *
     * Process:
     * - Loads the JDBC driver
     * - Sets connection timeout
     * - Attempts to connect to the database
     *
     * @return Connection object if successful
     * @throws RuntimeException if driver loading or connection fails
     */
    public static Connection connect() {
        try {
            // Load the JDBC driver class from AppConfig.
            // This ensures the driver is registered before attempting connection.
            Class.forName(AppConfig.DB_DRIVER);

            // Set how long the system will wait for a connection attempt.
            DriverManager.setLoginTimeout(AppConfig.DB_TIMEOUT);

            // Create and return the database connection using configured credentials.
            return DriverManager.getConnection(
                    AppConfig.DB_URL,
                    AppConfig.DB_USERNAME,
                    AppConfig.DB_PASSWORD);

        } catch (ClassNotFoundException e) {
            // Happens when the JDBC driver is not found in the classpath.
            System.err.println("ERROR: Database driver not found.");
            throw new RuntimeException("JDBC Driver loading failed.", e);

        } catch (SQLException e) {
            // Handle database-related errors such as invalid credentials or server issues.
            handleSQLException(e);
            throw new RuntimeException("Failed to establish database connection.", e);
        }
    }

    /**
     * Handles SQL exceptions by checking SQL state codes
     * and printing readable error messages.
     *
     * Known SQL State Codes:
     * 3D000 - Database does not exist
     * 28000 - Invalid credentials
     * 08S01 - Database unreachable or connection failure
     *
     * @param e the SQLException thrown during connection attempt
     */
    private static void handleSQLException(SQLException e) {
        // Extract SQL state code to identify the type of error
        String sqlState = e.getSQLState();

        // Determine specific error based on SQL state
        switch (sqlState) {
            case "3D000":
                System.err.println("ERROR: Database does not exist.");
                break;

            case "28000":
                System.err.println("ERROR: Invalid database credentials.");
                break;

            case "08S01":
                System.err.println("ERROR: Database server is offline or unreachable.");
                break;

            default:
                // Generic fallback for unhandled SQL errors
                System.err.println("SQL ERROR: " + e.getMessage());
                break;
        }

        // Print SQL state for debugging purposes
        System.err.println("SQL State: " + sqlState);
    }
}
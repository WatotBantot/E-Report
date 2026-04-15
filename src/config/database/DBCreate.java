package config.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import config.AppConfig;

/**
 * Utility class responsible for creating the database if it does not exist.
 * 
 * This class checks whether the database 'e_report' exists and creates it
 * automatically if it is missing.
 */
public class DBCreate {

    /**
     * Creates the database 'e_report' if it does not already exist.
     *
     * Process:
     * - Connects to the MySQL server (without specifying a database)
     * - Checks whether 'e_report' exists
     * - Creates the database if it does not exist
     *
     * @throws RuntimeException if a database connection or creation fails
     */
    public static void createDatabase() {
        // MySQL server URL (without specifying database)
        String url = "jdbc:mysql://localhost:3306";

        // Query to check if the database already exists
        String checkQuery = "SHOW DATABASES LIKE 'e_report';";

        // Query to create the database if it does not exist
        String createQuery = "CREATE DATABASE IF NOT EXISTS e_report;";

        // Use try-with-resources to automatically close Connection, Statement, and
        // ResultSet
        try (Connection con = DriverManager.getConnection(
                url,
                AppConfig.DB_USERNAME,
                AppConfig.DB_PASSWORD);
                Statement statement = con.createStatement();
                ResultSet rs = statement.executeQuery(checkQuery)) {

            // If a result is returned, database exists
            if (rs.next()) {
                System.out.println("Database 'e_report' already exists!");
            } else {
                // Create the database
                statement.executeUpdate(createQuery);
                System.out.println("Database 'e_report' has been created!");
            }

        } catch (SQLException e) {
            // Handle SQL errors
            handleSQLException(e);
            throw new RuntimeException("Failed to create database.", e);
        }
    }

    /**
     * Handles SQLExceptions by checking SQL State codes
     * and printing readable error messages.
     *
     * Common SQL State Codes:
     * 42000 - Syntax or permission error
     * 28000 - Authentication error (invalid credentials)
     * 08S01 - Database server unreachable or offline
     *
     * @param e the SQLException thrown during database operations
     */
    private static void handleSQLException(SQLException e) {
        // Extract SQL state to classify the error
        String sqlState = e.getSQLState();

        // Determine error type and print appropriate message
        switch (sqlState) {
            case "42000":
                System.err.println("ERROR: SQL syntax or permission issue.");
                break;

            case "28000":
                System.err.println("ERROR: Invalid credentials.");
                break;

            case "08S01":
                System.err.println("ERROR: Database server is offline.");
                break;

            default:
                // Generic fallback for unexpected SQL errors
                System.err.println("SQL ERROR: " + e.getMessage());
        }

        // Print SQL state for debugging purposes
        System.err.println("SQL State: " + sqlState);
    }
}
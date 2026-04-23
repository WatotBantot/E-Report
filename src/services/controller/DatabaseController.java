package services.controller;

import java.sql.Connection;

import javax.swing.JOptionPane;

import config.database.DBConnection;
import config.database.DBCreate;
import config.database.TBCreate;

/**
 * DatabaseController
 * 
 * Central controller for database initialization and connection management.
 * 
 * Responsibilities:
 * - Create the database if it doesn't exist
 * - Create all required tables
 * - Provide a reusable method to get a database connection
 */
public class DatabaseController {

    /**
     * Initializes the database system.
     * 
     * This includes:
     * 1. Creating the database if it does not exist
     * 2. Creating all required tables
     */
    public static void initializeDatabase() {
        System.out.println("[DatabaseController] Initializing database...");

        try {
            // Step 1: Ensure the database exists
            DBCreate.createDatabase();

            // Step 2: Connect to the database
            try (Connection con = DBConnection.connect()) {
                // Step 3: Create all tables
                TBCreate.createTables(con);
            }

            System.out.println("[DatabaseController] Database initialization complete!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Exception: Database initialization have failed!\nPlease check your database connection.",
                    "Database Exception",
                    JOptionPane.WARNING_MESSAGE);
            System.err.println("[DatabaseController] ERROR: Failed to initialize database!");
            e.printStackTrace();
        }
    }

    /**
     * Provides a database connection.
     * 
     * Usage:
     * try (Connection con = DatabaseController.getConnection()) { ... }
     * 
     * @return Connection to the configured database
     */
    public static Connection getConnection() {
        return DBConnection.connect();
    }

}
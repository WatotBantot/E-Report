package config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class responsible for creating all required database tables.
 * 
 * This class ensures that each table is created only if it does not already
 * exist.
 * It centralizes table creation logic for easy maintenance and initialization.
 */
public class TBCreate {

    /**
     * Creates all tables defined in AppConfig if they do not already exist.
     *
     * Tables created:
     * - User_Info
     * - Credential
     * - Complaint_Detail
     * - Complaint
     * - Complaint_Action
     * - Complaint_History_Detail
     * - Complaint_History
     *
     * @param con an active {@link Connection} to the database
     */
    public static void createTables(Connection con) {
        createTable(con, "User_Info", AppConfig.CREATE_USER_INFO_TABLE);
        createTable(con, "Credential", AppConfig.CREATE_CREDENTIAL_TABLE);
        createTable(con, "Complaint_Detail", AppConfig.CREATE_COMPLAINT_DETAIL_TABLE);
        createTable(con, "Complaint", AppConfig.CREATE_COMPLAINT_TABLE);
        createTable(con, "Complaint_Action", AppConfig.CREATE_COMPLAINT_ACTION_TABLE);
        createTable(con, "Complaint_History_Detail", AppConfig.CREATE_COMPLAINT_HISTORY_DETAIL_TABLE);
        createTable(con, "Complaint_History", AppConfig.CREATE_COMPLAINT_HISTORY_TABLE);
    }

    /**
     * Generic helper method to create a single table if it does not exist.
     *
     * Process:
     * - Checks whether the table exists using a "SHOW TABLES LIKE" query
     * - Executes the creation query if the table is missing
     *
     * @param con         an active {@link Connection} to the database
     * @param tableName   the name of the table to create
     * @param createQuery the SQL CREATE TABLE statement
     */
    private static void createTable(Connection con, String tableName, String createQuery) {
        // Query to check if the table already exists
        String checkQuery = "SHOW TABLES LIKE '" + tableName + "';";

        // Use try-with-resources for automatic closing of Statement and ResultSet
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(checkQuery)) {

            if (rs.next()) {
                // Table exists, no action needed
                System.out.println(tableName + " table already exists!");
            } else {
                // Table does not exist, create it
                stmt.executeUpdate(createQuery);
                System.out.println("Table " + tableName + " has been created!");
            }

        } catch (SQLException e) {
            // Print SQL error with table context for easier debugging
            System.err.println("Failed to create table " + tableName);
            System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
        }
    }
}
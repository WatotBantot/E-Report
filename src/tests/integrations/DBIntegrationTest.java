package tests.integrations;

import config.DBCreate;
import config.DBConnection;
import config.TBCreate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Integration test for the database setup process.
 * 
 * This test covers:
 * 1. Database creation
 * 2. Database connection
 * 3. Table creation and recreation
 */
public class DBIntegrationTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING INTEGRATION TESTS =====\n");

        testDatabaseCreation();
        testDatabaseConnection();
        testTableCreation();
        testTableRecreation();

        System.out.println("==========================");
        if (allTestsPassed) {
            System.out.println("🎉 ALL DATABASE INTEGRATION TESTS PASSED! 🎉");
        } else {
            System.out.println("❌ SOME INTEGRATION TESTS FAILED. See logs above. ❌");
        }
    }

    // ----------------------------
    // TEST DATABASE CREATION
    // ----------------------------
    public static void testDatabaseCreation() {
        System.out.println("[INTEGRATION TEST] Database Creation");

        try {
            DBCreate.createDatabase();
            System.out.println("-> PASS: Database creation handled successfully\n");
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception during database creation: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ----------------------------
    // TEST DATABASE CONNECTION
    // ----------------------------
    public static void testDatabaseConnection() {
        System.out.println("[INTEGRATION TEST] Database Connection");

        try (Connection con = DBConnection.connect()) {
            if (con != null) {
                System.out.println("-> PASS: Connection established successfully\n");
            } else {
                System.out.println("-> FAIL: Connection returned null\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception during connection: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ----------------------------
    // TEST TABLE CREATION
    // ----------------------------
    // ----------------------------
    // TEST TABLE CREATION (First Run)
    // ----------------------------
    public static void testTableCreation() {
        System.out.println("[INTEGRATION TEST] Table Creation (First Run)");

        try (Connection con = DBConnection.connect()) {

            // Check which tables exist before creation
            String[] tables = {
                    "User_Info",
                    "Credential",
                    "Complaint_Detail",
                    "Complaint",
                    "Complaint_Action",
                    "Complaint_History_Detail",
                    "Complaint_History"
            };
            boolean anyTableExistsBefore = false;

            for (String table : tables) {
                try (ResultSet rs = con.createStatement()
                        .executeQuery("SHOW TABLES LIKE '" + table + "'")) {
                    if (rs.next()) {
                        anyTableExistsBefore = true;
                        System.out.println("-> NOTE: Table " + table + " already exists before creation.");
                    }
                }
            }

            // Create tables
            TBCreate.createTables(con);

            // Verify all tables exist after creation
            boolean allExist = true;
            for (String table : tables) {
                try (ResultSet rs = con.createStatement()
                        .executeQuery("SHOW TABLES LIKE '" + table + "'")) {
                    if (!rs.next()) {
                        allExist = false;
                        System.out.println("-> FAIL: Table " + table + " does NOT exist after creation!");
                    }
                }
            }

            if (allExist) {
                if (anyTableExistsBefore) {
                    System.out.println("-> PASS: Table creation handled existing tables correctly\n");
                } else {
                    System.out.println("-> PASS: All tables created successfully\n");
                }
            } else {
                allTestsPassed = false;
            }

        } catch (SQLException e) {
            System.out.println("-> FAIL: Table creation verification failed: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ----------------------------
    // TEST TABLE RE-CREATION
    // ----------------------------
    public static void testTableRecreation() {
        System.out.println("[INTEGRATION TEST] Table Creation (Second Run)");

        // List of tables to verify
        String[] tables = {
                "User_Info",
                "Credential",
                "Complaint_Detail",
                "Complaint",
                "Complaint_Action",
                "Complaint_History_Detail",
                "Complaint_History"
        };

        try (Connection con = DBConnection.connect()) {
            // Attempt to create tables again
            TBCreate.createTables(con);
            System.out.println("-> Tables creation method executed.");

            boolean allExist = true;

            // Verify each table exists by querying the metadata
            for (String table : tables) {
                try {
                    // Using metadata instead of query for safer check
                    if (!con.getMetaData().getTables(null, null, table, null).next()) {
                        System.out.println("-> FAIL: Table missing: " + table);
                        allExist = false;
                    } else {
                        System.out.println("-> Table verified: " + table);
                    }
                } catch (SQLException e) {
                    System.out.println("-> FAIL: Could not verify table " + table + ": " + e.getMessage());
                    allExist = false;
                }
            }

            if (allExist) {
                System.out.println("-> PASS: All tables exist and recreation handled successfully.\n");
            } else {
                System.out.println("-> FAIL: Some tables are missing after recreation.\n");
                allTestsPassed = false;
            }

        } catch (SQLException e) {
            System.out.println("-> FAIL: Table recreation execution failed: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }
}
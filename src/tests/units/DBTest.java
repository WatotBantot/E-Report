package tests.units;

import config.database.*;
import java.sql.Connection;

public class DBTest {

    // This variable keeps track of whether any test has failed.
    private static boolean allTestsPassed = true;

    public static void main(String[] args) {

        System.out.println("===== STARTING TESTS =====\n");

        String dbUser = "root";
        String dbPass = "";

        testCreateDatabase(dbUser, dbPass);
        testConnectionSuccess();
        testTableCreation();
        testTableRecreation();

        System.out.println("==========================");
        if (allTestsPassed) {
            System.out.println("🎉 ALL TESTS PASSED! 🎉");
        } else {
            System.out.println("❌ SOME TESTS FAILED. See the logs above. ❌");
        }
    }

    // =========================
    // DBCreate TEST
    // =========================

    public static void testCreateDatabase(String user, String pass) {
        System.out.println("[TEST] Database Creation");

        try {
            DBCreate.createDatabase();
            System.out.println("-> PASS: Database creation handled properly\n");
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception during database creation: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // =========================
    // DBConnection TESTS
    // =========================

    public static void testConnectionSuccess() {
        System.out.println("[TEST] Connection SUCCESS");

        Connection con = DBConnection.connect();

        if (con != null) {
            System.out.println("-> PASS: Connection successful\n");
            // Always close connections when you're done with them!
            try {
                con.close();
            } catch (Exception ignored) {
            }
        } else {
            System.out.println("-> FAIL: Connection should have succeeded but returned null\n");
            allTestsPassed = false;
        }
    }

    // =========================
    // TBCreate TESTS
    // =========================

    public static void testTableCreation() {
        System.out.println("[TEST] Table Creation (First Run)");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("-> FAIL: Cannot test tables because DB connection failed\n");
            allTestsPassed = false;
            return;
        }

        try {
            TBCreate.createTables(con);

            // If the table doesn't exist, this will throw an exception!
            con.createStatement().executeQuery("SELECT * FROM Complaint LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Complaint_Action LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Complaint_Detail LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Complaint_History LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Complaint_History_Detail LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Credential LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM User_Info LIMIT 1");

            System.out.println("-> PASS: Tables created successfully\n");

        } catch (Exception e) {
            System.out.println("-> FAIL: Tables were NOT created! \nError: " + e.getMessage() + "\n");
            allTestsPassed = false;
        } finally {
            try {
                con.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void testTableRecreation() {
        System.out.println("[TEST] Table Creation (Second Run)");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("-> FAIL: Cannot test table recreation because DB connection failed\n");
            allTestsPassed = false;
            return;
        }

        try {
            TBCreate.createTables(con);

            // If the table doesn't exist, this line will crash and trigger the catch block
            // below!
            con.createStatement().executeQuery("SELECT * FROM Complaint LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Complaint_Action LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Complaint_Detail LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Complaint_History LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Complaint_History_Detail LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM Credential LIMIT 1");
            con.createStatement().executeQuery("SELECT * FROM User_Info LIMIT 1");

            System.out.println("-> PASS: Tables handled existing state correctly\n");

        } catch (Exception e) {
            System.out.println("-> FAIL: Tables do not exist after recreation run! \nError: " + e.getMessage() + "\n");
            allTestsPassed = false;
        } finally {
            try {
                con.close();
            } catch (Exception ignored) {
            }
        }
    }
}
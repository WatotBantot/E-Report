package tests.units;

import config.database.DBConnection;
import daos.AddUserDAO;
import models.UserInfo;
import models.Credential;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AddUserDAOTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING USER DAO TESTS =====\n");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("CRITICAL: Cannot run User DAO tests because database connection failed!");
            return;
        }

        AddUserDAO dao = new AddUserDAO();

        testAddUser(con, dao);
        testAddCredential(con, dao);

        try {
            con.close();
        } catch (Exception ignored) {
        }

        System.out.println("=============================");
        if (allTestsPassed) {
            System.out.println("ALL USER DAO TESTS PASSED!");
        } else {
            System.out.println("SOME USER DAO TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: Add User
    // ==========================================
    public static void testAddUser(Connection con, AddUserDAO dao) {
        System.out.println("[TEST] Add User");

        try {
            int initialCount = getTableRowCount(con, "User_Info");

            UserInfo ui = new UserInfo();
            ui.setFName("John");
            ui.setMName("Middle");
            ui.setLName("Doe");
            ui.setSex("M");
            ui.setContact("123456789");
            ui.setEmail("johndoe@example.com");
            ui.setHouseNum("42");
            ui.setStreet("Oak St.");
            ui.setPurok("Purok 4");

            dao.addUser(con, ui);

            int afterCount = getTableRowCount(con, "User_Info");

            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: User record successfully verified in database.\n");
            } else {
                System.out.println("-> FAIL: Row count did not increase! Expected " + (initialCount + 1) + " but got "
                        + afterCount + ".\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Add Credential
    // ==========================================
    public static void testAddCredential(Connection con, AddUserDAO dao) {
        System.out.println("[TEST] Add Credential");

        try {
            int initialCount = getTableRowCount(con, "Credential");

            Credential c = new Credential();
            c.setUsername("johndoe42");
            c.setPassword("securePassword123");
            c.setRole("User");
            c.setIsVerified(true);

            int dummyUserId = 1;

            dao.addCredential(con, dummyUserId, c);

            int afterCount = getTableRowCount(con, "Credential");

            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: Credential record successfully verified in database.\n");
            } else {
                System.out.println("-> FAIL: Row count did not increase! Expected " + (initialCount + 1) + " but got "
                        + afterCount + ".\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // HELPER METHOD: Counts rows in any table safely
    // ==========================================
    private static int getTableRowCount(Connection con, String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("[Warning] Could not get count for " + tableName + ". Assuming 0.");
        }
        return 0;
    }
}
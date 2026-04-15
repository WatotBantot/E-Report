package tests.units;

import config.database.DBConnection;
import daos.GetUserDAO;
import models.UserInfo;
import models.Credential;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetUserDAOTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING GET USER DAO TESTS =====\n");

        GetUserDAO dao = new GetUserDAO();

        testGetUser(dao);
        testGetCredential(dao);

        System.out.println("=============================");
        if (allTestsPassed) {
            System.out.println("ALL GET USER DAO TESTS PASSED!");
        } else {
            System.out.println("SOME GET USER DAO TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: Get User
    // ==========================================
    public static void testGetUser(GetUserDAO dao) {
        System.out.println("[TEST] Get User by ID");

        // We open an independent connection to find a user to test
        try (Connection con = DBConnection.connect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT User_Info.UI_ID, User_Info.first_name FROM USER_INFO INNER JOIN CREDENTIAL ON Credential.UI_ID = User_Info.UI_ID LIMIT 1")) {

            if (!rs.next()) {
                System.out.println("-> FAIL: No users found in database with credentials to run this test.\n");
                allTestsPassed = false;
                return;
            }

            int targetId = rs.getInt("UI_ID");
            String expectedName = rs.getString("first_name");

            // Run the target DAO method
            UserInfo ui = dao.getUser(DBConnection.connect(), targetId);

            // Verify the data came back and matches
            if (ui != null && expectedName.equals(ui.getFName())) {
                System.out.println(
                        "-> PASS: Successfully fetched user with ID " + targetId + " (" + ui.getFName() + ")\n");
            } else {
                System.out.println("-> FAIL: The returned user object was null or the data did not match.\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Get Credential
    // ==========================================
    public static void testGetCredential(GetUserDAO dao) {
        System.out.println("[TEST] Get Credential by Login");

        try (Connection con = DBConnection.connect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT username, password FROM CREDENTIAL LIMIT 1")) {

            if (!rs.next()) {
                System.out.println("-> FAIL: No credentials found in database to run this test.\n");
                allTestsPassed = false;
                return;
            }

            String targetUser = rs.getString("username");
            String targetPass = rs.getString("password");

            // Run the target DAO method
            Credential c = dao.getCredential(DBConnection.connect(), targetUser, targetPass);

            // Verify the data came back and matches
            if (c != null && targetUser.equals(c.getUsername())) {
                System.out.println("-> PASS: Successfully logged in and fetched credentials for " + targetUser + "\n");
            } else {
                System.out.println("-> FAIL: Credential check failed. Returned object was null or data mismatched.\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }
}
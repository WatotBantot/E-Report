package tests.units;

import config.database.DBConnection;
import daos.AddComplaintDAO;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import models.ComplaintAction;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

public class AddComplaintDAOTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING DAO TESTS =====\n");

        Connection con = DBConnection.connect();

        if (con == null) {
            System.out.println("CRITICAL: Cannot run DAO tests because database connection failed!");
            return;
        }

        // Run the tests
        testAddComplaint(con);
        testAddComplaintHistory(con);
        testAddComplaintAction(con);

        try {
            con.close();
        } catch (Exception ignored) {
        }

        System.out.println("=============================");
        if (allTestsPassed) {
            System.out.println("🎉 ALL DAO TESTS PASSED! 🎉");
        } else {
            System.out.println("SOME DAO TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: Add Complaint
    // ==========================================
    public static void testAddComplaint(Connection con) {
        System.out.println("[TEST] Add Complaint");

        File mockFile = null;
        try {
            // 1. Get the current count of rows in the database before we run the test
            int initialCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            // 2. Create mock data
            ComplaintDetail cd = new ComplaintDetail();
            cd.setCurrentStatus("Pending");
            cd.setSubject("Noise Complaint");
            cd.setType("Disturbance");
            cd.setDateTime(new Timestamp(System.currentTimeMillis()));
            cd.setStreet("Apple St.");
            cd.setPurok("Purok 1");
            cd.setLongitude(120.1234);
            cd.setLatitude(15.5678);
            cd.setPersonsInvolved("John Doe");
            cd.setDetails("Playing loud music at 2 AM.");

            // 3. Create a temporary dummy file to simulate image upload
            String mockFilePath = System.getProperty("user.dir") + "/test_evidence.jpg";
            mockFile = new File(mockFilePath);

            try (FileWriter writer = new FileWriter(mockFile)) {
                writer.write("Simulated image data");
            }

            // 4. Read file bytes and attach as BLOB
            byte[] fileBytes = Files.readAllBytes(mockFile.toPath());
            cd.setPhotoAttachmentBytes(fileBytes);

            int dummyUserId = 1;

            // 5. Run the target code
            AddComplaintDAO acDao = new AddComplaintDAO();
            acDao.addComplaint(con, dummyUserId, cd);

            // 6. Check the row count again
            int afterCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            // 7. Verify the row count increased by exactly 1
            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: Complaint record successfully verified in database.\n");
            } else {
                System.out.println("-> FAIL: Row count did not increase! Expected " + (initialCount + 1)
                        + " but got " + afterCount + ".\n");
                allTestsPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        } finally {
            // 8. Clean up the dummy test file
            if (mockFile != null && mockFile.exists()) {
                mockFile.delete();
            }
        }
    }

    // ==========================================
    // TEST: Add Complaint History
    // ==========================================
    public static void testAddComplaintHistory(Connection con) {
        System.out.println("[TEST] Add Complaint History");

        try {
            int initialCount = getTableRowCount(con, "Complaint_History_Detail");

            ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
            chd.setStatus("Under Investigation");
            chd.setProcess("Officer communicated with the persons involved for further investigation.");
            chd.setDateTimeUpdated(new Timestamp(System.currentTimeMillis()));
            chd.setUpdatedBy("Admin Officer");

            int dummyComplaintId = 1;

            AddComplaintDAO acDao = new AddComplaintDAO();
            acDao.addComplaintHistory(con, dummyComplaintId, chd);

            int afterCount = getTableRowCount(con, "Complaint_History_Detail");

            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: Complaint History successfully verified in database.\n");
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
    // TEST: Add Complaint Action
    // ==========================================
    public static void testAddComplaintAction(Connection con) {
        System.out.println("[TEST] Add Complaint Action");

        try {
            int initialCount = getTableRowCount(con, "Complaint_Action");

            ComplaintAction ca = new ComplaintAction();
            ca.setActionTaken("Gave a verbal warning to the offender.");
            ca.setRecommendation("Monitor the area for repeat offenses.");
            ca.setOIC("Officer Reyes");
            ca.setResolutionDateTime(new Timestamp(System.currentTimeMillis()));

            int dummyComplaintId = 1;

            AddComplaintDAO acDao = new AddComplaintDAO();
            acDao.addComplaintAction(con, dummyComplaintId, ca);

            int afterCount = getTableRowCount(con, "Complaint_Action");

            if (afterCount == initialCount + 1) {
                System.out.println("-> PASS: Complaint Action successfully verified in database.\n");
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
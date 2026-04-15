package tests.integrations;

import daos.AddUserDAO;
import daos.AddComplaintDAO;
import daos.GetUserDAO;
import daos.GetComplaintDAO;
import config.database.DBConnection;
import models.UserInfo;
import models.Credential;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import models.ComplaintAction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Integration test for DAOs: AddUserDAO, AddComplaintDAO, GetUserDAO,
 * GetComplaintDAO
 */
public class DAOIntegrationTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING DAO INTEGRATION TESTS =====\n");

        try (Connection con = DBConnection.connect()) {

            testAddUserAndCredential(con);
            testAddComplaintWorkflow(con);

        } catch (SQLException e) {
            System.out.println("❌ FAIL: Could not connect to DB - " + e.getMessage());
            allTestsPassed = false;
        }

        System.out.println("==========================");
        if (allTestsPassed) {
            System.out.println("🎉 ALL DAO INTEGRATION TESTS PASSED! 🎉");
        } else {
            System.out.println("❌ SOME DAO TESTS FAILED. See logs above. ❌");
        }
    }

    // ----------------------------
    // TEST ADD USER AND CREDENTIAL
    // ----------------------------
    public static void testAddUserAndCredential(Connection con) {
        System.out.println("[DAO TEST] Add User & Credential");

        try {
            // Create user
            UserInfo ui = new UserInfo();
            ui.setFName("John");
            ui.setMName("D");
            ui.setLName("Doe");
            ui.setSex("M");
            ui.setContact("09171234567");
            ui.setEmail("john.doe@example.com");
            ui.setHouseNum("123");
            ui.setStreet("Main St");
            ui.setPurok("Purok 1");

            AddUserDAO addUserDAO = new AddUserDAO();
            int userID = addUserDAO.addUser(con, ui);
            if (userID <= 0) {
                throw new Exception("User insertion failed");
            }
            System.out.println("-> PASS: User inserted with ID " + userID);

            // Add credential
            Credential cred = new Credential();
            cred.setUsername("johndoe");
            cred.setPassword("password123");
            cred.setRole("user");
            cred.setIsVerified(true);

            boolean credInserted = addUserDAO.addCredential(con, userID, cred);
            if (!credInserted)
                throw new Exception("Credential insertion failed");
            System.out.println("-> PASS: Credential inserted for user ID " + userID);

            // Retrieve and verify
            GetUserDAO getUserDAO = new GetUserDAO();
            UserInfo retrievedUI = getUserDAO.getUser(DBConnection.connect() ,userID);
            if (retrievedUI == null || !retrievedUI.getEmail().equals("john.doe@example.com")) {
                throw new Exception("Retrieved user does not match inserted user");
            }

            Credential retrievedCred = getUserDAO.getCredential(DBConnection.connect(), "johndoe", "password123");
            if (retrievedCred == null || retrievedCred.getUI_ID() != userID) {
                throw new Exception("Retrieved credential does not match inserted credential");
            }

            System.out.println("-> PASS: User and credential retrieval verified\n");

        } catch (Exception e) {
            System.out.println("-> FAIL: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ----------------------------
    // TEST ADD COMPLAINT WORKFLOW
    // ----------------------------
    public static void testAddComplaintWorkflow(Connection con) {
        System.out.println("[DAO TEST] Add Complaint Workflow");

        try {
            // Step 1: create user to attach complaint
            UserInfo ui = new UserInfo();
            ui.setFName("Alice");
            ui.setMName("B");
            ui.setLName("Smith");
            ui.setSex("F");
            ui.setContact("09171239876");
            ui.setEmail("alice.smith@example.com");
            ui.setHouseNum("456");
            ui.setStreet("Second St");
            ui.setPurok("Purok 2");

            AddUserDAO addUserDAO = new AddUserDAO();
            int userID = addUserDAO.addUser(con, ui);
            if (userID <= 0)
                throw new Exception("User insertion failed for complaint workflow");

            // Step 2: insert complaint
            ComplaintDetail cd = new ComplaintDetail();
            cd.setCurrentStatus("Open");
            cd.setSubject("Noise complaint");
            cd.setType("Public Disturbance");
            cd.setDateTime(new Timestamp(System.currentTimeMillis()));
            cd.setStreet("Second St");
            cd.setPurok("Purok 2");
            cd.setLongitude(123.456);
            cd.setLatitude(78.910);
            cd.setPersonsInvolved("Neighbor");
            cd.setDetails("Loud music every night");
            cd.setPhotoAttachmentBytes(null);

            AddComplaintDAO acDao = new AddComplaintDAO();
            int complaintID = acDao.addComplaint(con, userID, cd);
            if (complaintID <= 0)
                throw new Exception("Complaint insertion failed");

            System.out.println("-> PASS: Complaint inserted with ID " + complaintID);

            // Step 3: insert complaint history
            ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
            chd.setStatus("Open");
            chd.setProcess("Assigned");
            chd.setDateTimeUpdated(new Timestamp(System.currentTimeMillis()));
            chd.setUpdatedBy("Admin");
            
            int chdID = acDao.addComplaintHistory(con, complaintID, chd);
            if (chdID <= 0)
                throw new Exception("Complaint history insertion failed");
            System.out.println("-> PASS: Complaint history inserted with ID " + chdID);

            // Step 4: insert complaint action
            ComplaintAction ca = new ComplaintAction();
            ca.setActionTaken("Visited site");
            ca.setRecommendation("Warn neighbor");
            ca.setOIC("Officer A");
            ca.setResolutionDateTime(new Timestamp(System.currentTimeMillis()));

            boolean actionInserted = acDao.addComplaintAction(con, complaintID, ca);
            if (!actionInserted)
                throw new Exception("Complaint action insertion failed");
            System.out.println("-> PASS: Complaint action inserted for complaint ID " + complaintID);

            // Step 5: retrieve complaint and verify
            GetComplaintDAO getComplaintDAO = new GetComplaintDAO();
            ComplaintDetail retrievedCD = getComplaintDAO.getComplaint(con, userID, complaintID);
            if (retrievedCD == null || !retrievedCD.getSubject().equals("Noise complaint")) {
                throw new Exception("Retrieved complaint does not match inserted complaint");
            }

            List<ComplaintHistoryDetail> retrievedCHD = getComplaintDAO.getComplaintHistory(con, complaintID);
            if (retrievedCHD.isEmpty())
                throw new Exception("Complaint history not retrieved");

            ComplaintAction retrievedCA = getComplaintDAO.getComplaintAction(con, complaintID);
            if (retrievedCA == null || !retrievedCA.getOIC().equals("Officer A")) {
                throw new Exception("Complaint action not retrieved correctly");
            }

            System.out.println("-> PASS: Complaint workflow retrieval verified\n");

        } catch (Exception e) {
            System.out.println("-> FAIL: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }
}
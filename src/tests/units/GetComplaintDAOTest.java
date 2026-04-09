package tests.units;

import config.DBConnection;
import DAOs.GetComplaintDAO;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import models.ComplaintAction;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class GetComplaintDAOTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING CONSOLIDATED GET COMPLAINT DAO TESTS =====\n");

        GetComplaintDAO dao = new GetComplaintDAO();

        // Dynamically locate valid testing IDs from the database
        int[] complaintIds = findValidComplaintIds();
        int[] historyIds = findValidComplaintHistoryIds();
        int validActionCdId = findValidComplaintActionId();

        // 1. Run Test: Get Single Complaint
        if (complaintIds != null) {
            testGetComplaint(dao, complaintIds[0], complaintIds[1]);
            testGetAllComplaint(dao, complaintIds[0]);
        } else {
            System.out.println("[SKIP] Complaint tests skipped: No valid data found in DB.");
        }

        // 2. Run Test: Get Complaint History
        if (historyIds != null) {
            testGetComplaintHistory(dao, historyIds[0], historyIds[1]);
        } else {
            System.out.println("[SKIP] Complaint History test skipped: No valid data found in DB.");
        }

        // 3. Run Test: Get Complaint Action
        if (validActionCdId != -1) {
            testGetComplaintAction(dao, validActionCdId);
        } else {
            System.out.println("[SKIP] Complaint Action test skipped: No valid data found in DB.");
        }

        System.out.println("=============================");
        if (allTestsPassed) {
            System.out.println("ALL CONSOLIDATED GET DAO TESTS PASSED!");
        } else {
            System.out.println("SOME CONSOLIDATED GET DAO TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // TEST: Get Single Complaint
    // ==========================================
    public static void testGetComplaint(GetComplaintDAO dao, int uiId, int cdId) {
        System.out.println("[TEST] Get Single Complaint");

        try {
            ComplaintDetail cd = dao.getComplaint(uiId, cdId);
            if (cd != null) {
                System.out.println("-> PASS: Successfully fetched Complaint ID: " + cdId);
                System.out.println("   Subject: " + cd.getSubject());

                // Check the image BLOB
                byte[] photoBytes = cd.getPhotoAttachmentBytes(); // Updated to BLOB field
                if (photoBytes != null && photoBytes.length > 0) {
                    System.out
                            .println("   Photo Attachment: BLOB found in DB! (Size: " + photoBytes.length + " bytes)");
                } else {
                    System.out.println("   Photo Attachment: [No image attached]");
                }

                System.out.println();

            } else {
                System.out.println("-> FAIL: Returned complaint object was null.\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Get All Complaints
    // ==========================================
    public static void testGetAllComplaint(GetComplaintDAO dao, int uiId) {
        System.out.println("[TEST] Get All Complaints for User");
        try {
            List<ComplaintDetail> list = dao.getAllComplaint(uiId);
            if (list != null && !list.isEmpty()) {
                System.out.println(
                        "-> PASS: Successfully fetched " + list.size() + " complaint(s) for User ID: " + uiId + "\n");
            } else {
                System.out.println("-> FAIL: Returned list was null or empty.\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Get Complaint History
    // ==========================================
    public static void testGetComplaintHistory(GetComplaintDAO dao, int cdId, int chdId) {
        System.out.println("[TEST] Get Complaint History");
        try {
            List<ComplaintHistoryDetail> historyList = dao.getComplaintHistory(cdId);

            if (historyList != null && !historyList.isEmpty()) {
                System.out.println("-> PASS: Successfully fetched " + historyList.size()
                        + " history record(s) for History ID: " + chdId);

                for (int i = 0; i < historyList.size(); i++) {
                    ComplaintHistoryDetail chd = historyList.get(i);
                    System.out.println("   [Record " + (i + 1) + "]");
                    System.out.println("   Status: " + chd.getStatus());
                    System.out.println("   Process: " + chd.getProcess());
                    System.out.println("   Updated By: " + chd.getUpdatedBy());
                    System.out.println("   Date Updated: " + chd.getDateTimeUpdated());
                }
                System.out.println();
            } else {
                System.out.println("-> FAIL: Returned history list was null or empty.\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // TEST: Get Complaint Action
    // ==========================================
    public static void testGetComplaintAction(GetComplaintDAO dao, int cdId) {
        System.out.println("[TEST] Get Complaint Action");
        try {
            ComplaintAction ca = dao.getComplaintAction(cdId);
            if (ca != null) {
                System.out.println("-> PASS: Successfully fetched Action for Complaint ID: " + cdId);
                System.out.println("   Action Taken: " + ca.getActionTaken());
                System.out.println("   OIC: " + ca.getOIC() + "\n");
            } else {
                System.out.println("-> FAIL: Returned action object was null.\n");
                allTestsPassed = false;
            }
        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred: " + e.getMessage() + "\n");
            allTestsPassed = false;
        }
    }

    // ==========================================
    // ID DISCOVERY HELPERS (To prevent blind queries)
    // ==========================================
    private static int[] findValidComplaintIds() {
        try (Connection con = DBConnection.connect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT UI_ID, CD_ID FROM Complaint LIMIT 1")) {
            if (rs.next())
                return new int[] { rs.getInt("UI_ID"), rs.getInt("CD_ID") };
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int[] findValidComplaintHistoryIds() {
        try (Connection con = DBConnection.connect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT CD_ID, CHD_ID FROM Complaint_History LIMIT 1")) {
            if (rs.next())
                return new int[] { rs.getInt("CD_ID"), rs.getInt("CHD_ID") };
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int findValidComplaintActionId() {
        try (Connection con = DBConnection.connect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT CD_ID FROM Complaint_Action LIMIT 1")) {
            if (rs.next())
                return rs.getInt("CD_ID");
        } catch (Exception ignored) {
        }
        return -1;
    }
}
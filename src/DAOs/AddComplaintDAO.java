package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.ComplaintAction;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;

/**
 * DAO for adding complaints, complaint history, and actions.
 * All methods throw SQLException on failure, making them
 * safe to use in transactions.
 */
public class AddComplaintDAO {

    /**
     * Adds a complaint and links it to a user.
     * 
     * @param con    Active DB connection
     * @param userID User ID filing the complaint
     * @param cd     ComplaintDetail object
     * @return Auto-generated ComplaintDetail ID
     * @throws SQLException if insertion fails
     */
    public static int addComplaint(Connection con, int userID, ComplaintDetail cd) throws SQLException {
        String insertDetailSQL = "INSERT INTO Complaint_Detail "
                + "(current_status, subject, type, date_time, street, purok, longitude, latitude, persons_involved, details, photo_attachment) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?);";

        try (PreparedStatement stmtDetail = con.prepareStatement(insertDetailSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmtDetail.setString(1, cd.getCurrentStatus());
            stmtDetail.setString(2, cd.getSubject());
            stmtDetail.setString(3, cd.getType());
            stmtDetail.setTimestamp(4, cd.getDateTime());
            stmtDetail.setString(5, cd.getStreet());
            stmtDetail.setString(6, cd.getPurok());
            stmtDetail.setDouble(7, cd.getLongitude());
            stmtDetail.setDouble(8, cd.getLatitude());
            stmtDetail.setString(9, cd.getPersonsInvolved());
            stmtDetail.setString(10, cd.getDetails());
            stmtDetail.setString(11, cd.getPhotoAttachment());

            int rows = stmtDetail.executeUpdate();
            if (rows == 0)
                throw new SQLException("Failed to insert Complaint_Detail");

            try (ResultSet rs = stmtDetail.getGeneratedKeys()) {
                if (rs.next()) {
                    int cdID = rs.getInt(1);

                    String insertComplaintSQL = "INSERT INTO Complaint(CD_ID, UI_ID) VALUES(?,?);";
                    try (PreparedStatement stmtComplaint = con.prepareStatement(insertComplaintSQL)) {
                        stmtComplaint.setInt(1, cdID);
                        stmtComplaint.setInt(2, userID);
                        int complaintRows = stmtComplaint.executeUpdate();
                        if (complaintRows == 0)
                            throw new SQLException("Failed to insert Complaint");
                    }

                    return cdID;
                } else {
                    throw new SQLException("Failed to retrieve generated ComplaintDetail ID");
                }
            }
        }
    }

    /**
     * Adds complaint history linked to a complaint.
     * 
     * @param con         Active DB connection
     * @param complaintID Complaint ID to link
     * @param chd         ComplaintHistoryDetail object
     * @return Auto-generated ComplaintHistoryDetail ID
     * @throws SQLException if insertion fails
     */
    public static int addComplaintHistory(Connection con, int complaintID, ComplaintHistoryDetail chd)
            throws SQLException {
        String insertHistoryDetailSQL = "INSERT INTO Complaint_History_Detail "
                + "(status, process, date_time_updated, updated_by) VALUES (?,?,?,?);";

        try (PreparedStatement stmtHistoryDetail = con.prepareStatement(insertHistoryDetailSQL,
                Statement.RETURN_GENERATED_KEYS)) {
            stmtHistoryDetail.setString(1, chd.getStatus());
            stmtHistoryDetail.setString(2, chd.getProcess());
            stmtHistoryDetail.setTimestamp(3, chd.getDateTimeUpdated());
            stmtHistoryDetail.setString(4, chd.getUpdatedBy());

            int rows = stmtHistoryDetail.executeUpdate();
            if (rows == 0)
                throw new SQLException("Failed to insert Complaint_History_Detail");

            try (ResultSet rs = stmtHistoryDetail.getGeneratedKeys()) {
                if (rs.next()) {
                    int chdID = rs.getInt(1);

                    String insertHistorySQL = "INSERT INTO Complaint_History(CD_ID, CHD_ID) VALUES (?,?);";
                    try (PreparedStatement stmtHistory = con.prepareStatement(insertHistorySQL)) {
                        stmtHistory.setInt(1, complaintID);
                        stmtHistory.setInt(2, chdID);
                        int historyRows = stmtHistory.executeUpdate();
                        if (historyRows == 0)
                            throw new SQLException("Failed to insert Complaint_History");
                    }

                    return chdID;
                } else {
                    throw new SQLException("Failed to retrieve generated ComplaintHistoryDetail ID");
                }
            }
        }
    }

    /**
     * Adds an action related to a complaint.
     * 
     * @param con         Active DB connection
     * @param complaintID Complaint ID
     * @param ca          ComplaintAction object
     * @return true if insertion succeeds
     * @throws SQLException if insertion fails
     */
    public static boolean addComplaintAction(Connection con, int complaintID, ComplaintAction ca) throws SQLException {
        String insertActionSQL = "INSERT INTO Complaint_Action "
                + "(CD_ID, action_taken, recommendation, oic, resolution_date_time) VALUES (?,?,?,?,?);";

        try (PreparedStatement stmtAction = con.prepareStatement(insertActionSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmtAction.setInt(1, complaintID);
            stmtAction.setString(2, ca.getActionTaken());
            stmtAction.setString(3, ca.getRecommendation());
            stmtAction.setString(4, ca.getOIC());
            stmtAction.setTimestamp(5, ca.getResolutionDateTime());

            int rows = stmtAction.executeUpdate();
            if (rows == 0)
                throw new SQLException("Failed to insert Complaint_Action");

            return true;
        }
    }
}
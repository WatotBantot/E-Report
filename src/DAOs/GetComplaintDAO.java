package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import models.ComplaintAction;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;

public class GetComplaintDAO {

    // Map ResultSet to ComplaintDetail
    private ComplaintDetail mapResultSetToComplaintDetail(ResultSet rs) throws SQLException {
        ComplaintDetail cd = new ComplaintDetail();
        cd.setCurrentStatus(rs.getString("current_status"));
        cd.setSubject(rs.getString("subject"));
        cd.setType(rs.getString("type"));
        cd.setDateTime(rs.getTimestamp("date_time"));
        cd.setStreet(rs.getString("street"));
        cd.setPurok(rs.getString("purok"));
        cd.setLongitude(rs.getDouble("longitude"));
        cd.setLatitude(rs.getDouble("latitude"));
        cd.setPersonsInvolved(rs.getString("persons_involved"));
        cd.setDetails(rs.getString("details"));
        cd.setPhotoAttachment(rs.getString("photo_attachment"));
        return cd;
    }

    public ComplaintDetail getComplaint(int UI_ID, int CD_ID) {
        String query = """
                SELECT cd.CD_ID, cd.current_status, cd.subject, cd.type,
                       cd.date_time, cd.street, cd.purok, cd.longitude, cd.latitude,
                       cd.persons_involved, cd.details, cd.photo_attachment
                FROM Complaint c
                INNER JOIN Complaint_Detail cd ON cd.CD_ID = c.CD_ID
                WHERE c.UI_ID = ? AND cd.CD_ID = ?;
                """;

        try (Connection con = DBConnection.connect();
                PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, UI_ID);
            stmt.setInt(2, CD_ID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComplaintDetail(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving complaint detail from Complaint_Detail table");
            e.printStackTrace();
        }

        return null;
    }

    public List<ComplaintDetail> getAllComplaint(int UI_ID) {
        List<ComplaintDetail> cdList = new ArrayList<>();
        String query = """
                SELECT cd.CD_ID, cd.current_status, cd.subject, cd.type,
                       cd.date_time, cd.street, cd.purok, cd.longitude, cd.latitude,
                       cd.persons_involved, cd.details, cd.photo_attachment
                FROM Complaint_Detail cd
                INNER JOIN Complaint c ON c.CD_ID = cd.CD_ID
                WHERE c.UI_ID = ?;
                """;

        try (Connection con = DBConnection.connect();
                PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, UI_ID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cdList.add(mapResultSetToComplaintDetail(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving all complaints for UI_ID: " + UI_ID);
            e.printStackTrace();
        }

        return cdList;
    }

    // Map ResultSet to ComplaintHistoryDetail
    private ComplaintHistoryDetail mapResultSetToComplaintHistoryDetail(ResultSet rs) throws SQLException {
        ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
        chd.setStatus(rs.getString("status"));
        chd.setProcess(rs.getString("process"));
        chd.setDateTimeUpdated(rs.getTimestamp("date_time_updated"));
        chd.setUpdatedBy(rs.getString("updated_by"));
        return chd;
    }

    public List<ComplaintHistoryDetail> getComplaintHistory(int CD_ID) {
        List<ComplaintHistoryDetail> chdList = new ArrayList<>();
        String query = """
                SELECT chd.CHD_ID, chd.status, chd.process, chd.date_time_updated, chd.updated_by
                FROM Complaint_History_Detail chd
                INNER JOIN Complaint_History ch ON ch.CHD_ID = chd.CHD_ID
                WHERE ch.CD_ID = ?;
                """;

        try (Connection con = DBConnection.connect();
                PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, CD_ID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chdList.add(mapResultSetToComplaintHistoryDetail(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving complaint history for CD_ID: " + CD_ID);
            e.printStackTrace();
        }

        return chdList;
    }

    public ComplaintAction getComplaintAction(int CD_ID) {
        String query = """
                SELECT CD_ID, action_taken, recommendation, oic,
                       date_time_assigned, resolution_date_time
                FROM Complaint_Action
                WHERE CD_ID = ?;
                """;

        try (Connection con = DBConnection.connect();
                PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, CD_ID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ComplaintAction ca = new ComplaintAction();
                    ca.setActionTaken(rs.getString("action_taken"));
                    ca.setRecommendation(rs.getString("recommendation"));
                    ca.setOIC(rs.getString("oic"));
                    ca.setDateTimeAssigned(rs.getTimestamp("date_time_assigned"));
                    ca.setResolutionDateTime(rs.getTimestamp("resolution_date_time"));
                    return ca;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving complaint action for CD_ID: " + CD_ID);
            e.printStackTrace();
        }

        return null;
    }
}
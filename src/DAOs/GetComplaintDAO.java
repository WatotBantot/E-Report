package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.ComplaintAction;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;

public class GetComplaintDAO {
	
	// ===== SQL STRINGS =====
	private String queryComplaint, queryAllComplaints, queryHistory, queryAction;
	private String queryUserCount, queryTotalCount, queryStatusCount;
	
	public GetComplaintDAO(){
		// ===== INIT SQL =====
		queryComplaint = """
			SELECT cd.CD_ID, cd.current_status, cd.subject, cd.type,
				cd.date_time, cd.street, cd.purok, cd.longitude, cd.latitude,
				cd.persons_involved, cd.details, cd.photo_attachment
			FROM Complaint c
			INNER JOIN Complaint_Detail cd ON cd.CD_ID = c.CD_ID
			WHERE c.UI_ID = ? AND cd.CD_ID = ?;
			""";
			
		queryAllComplaints = """
			SELECT cd.CD_ID, cd.current_status, cd.subject, cd.type,
				cd.date_time, cd.street, cd.purok, cd.longitude, cd.latitude,
				cd.persons_involved, cd.details, cd.photo_attachment
			FROM Complaint_Detail cd
			INNER JOIN Complaint c ON c.CD_ID = cd.CD_ID
			WHERE c.UI_ID = ?;
			""";
			
		queryHistory = """
			SELECT chd.CHD_ID, chd.status, chd.process, chd.date_time_updated, chd.updated_by
			FROM Complaint_History_Detail chd
			INNER JOIN Complaint_History ch ON ch.CHD_ID = chd.CHD_ID
			WHERE ch.CD_ID = ?;
			""";
			
		queryAction = """
			SELECT CD_ID, action_taken, recommendation, oic,
				date_time_assigned, resolution_date_time
			FROM Complaint_Action
			WHERE CD_ID = ?;
			""";
			
		queryUserCount = """
			SELECT COUNT(C_ID) AS Total
			FROM Complaint
			WHERE UI_ID = ?;
			""";
			
		queryTotalCount = """
			SELECT COUNT(C_ID) AS Total
			FROM Complaint;
			""";
			
		queryStatusCount = """
			SELECT COUNT(*) AS Total
			FROM Complaint_Detail cd INNER JOIN Complaint c
			ON cd.cd_id = c.cd_id
			WHERE c.ui_id = ? AND cd.current_status = ?;
			""";
	}

	// ===== MAP RESULTSET TO COMPLAINTDETAIL =====
	private ComplaintDetail mapToComplaintDetail(ResultSet rs) throws SQLException {
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
		cd.setPhotoAttachmentBytes(rs.getBytes("photo_attachment"));
		return cd;
	}

	// ===== MAP RESULTSET TO COMPLAINTHISTORYDETAIL =====
	private ComplaintHistoryDetail mapToHistoryDetail(ResultSet rs) throws SQLException {
		ComplaintHistoryDetail chd = new ComplaintHistoryDetail();
		chd.setStatus(rs.getString("status"));
		chd.setProcess(rs.getString("process"));
		chd.setDateTimeUpdated(rs.getTimestamp("date_time_updated"));
		chd.setUpdatedBy(rs.getString("updated_by"));
		return chd;
	}

	public ComplaintDetail getComplaint(Connection con, int UI_ID, int CD_ID) {
		// ===== GET COMPLAINT =====
		try (PreparedStatement stmt = con.prepareStatement(queryComplaint)) {

			stmt.setInt(1, UI_ID);
			stmt.setInt(2, CD_ID);

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapToComplaintDetail(rs);
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving complaint detail from Complaint_Detail table");
			e.printStackTrace();
		}

		return null;
	}

	public List<ComplaintDetail> getAllComplaint(Connection con, int UI_ID) {
		List<ComplaintDetail> cdList = new ArrayList<>();
		
		// ===== GET ALL COMPLAINTS =====
		try (PreparedStatement stmt = con.prepareStatement(queryAllComplaints)) {

			stmt.setInt(1, UI_ID);

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					cdList.add(mapToComplaintDetail(rs));
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving all complaints for UI_ID: " + UI_ID);
			e.printStackTrace();
		}

		return cdList;
	}

	public List<ComplaintHistoryDetail> getComplaintHistory(Connection con, int CD_ID) {
		List<ComplaintHistoryDetail> chdList = new ArrayList<>();
		
		// ===== GET COMPLAINT HISTORY =====
		try (PreparedStatement stmt = con.prepareStatement(queryHistory)) {

			stmt.setInt(1, CD_ID);

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					chdList.add(mapToHistoryDetail(rs));
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving complaint history for CD_ID: " + CD_ID);
			e.printStackTrace();
		}

		return chdList;
	}

	public ComplaintAction getComplaintAction(Connection con, int CD_ID) {
		// ===== GET COMPLAINT ACTION =====
		try (PreparedStatement stmt = con.prepareStatement(queryAction)) {

			stmt.setInt(1, CD_ID);

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					// ===== MAP TO ACTION =====
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

	public int getUserTotalReportCount(Connection con, int UI_ID) {
		// ===== GET USER REPORT COUNT =====
		try (PreparedStatement stmt = con.prepareStatement(queryUserCount)) {

			stmt.setInt(1, UI_ID);

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("Total");
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving report count for UI_ID: " + UI_ID);
			e.printStackTrace();
		}

		return -1;
	}

	public int getTotalReportCount(Connection con) {
		// ===== GET TOTAL REPORT COUNT =====
		try (PreparedStatement stmt = con.prepareStatement(queryTotalCount)) {

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("Total");
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving report count");
			e.printStackTrace();
		}

		return -1;
	}

	public int getUserTotalStatusCount(Connection con, int UI_ID, String status) {
		// ===== GET STATUS COUNT =====
		try (PreparedStatement stmt = con.prepareStatement(queryStatusCount)) {

			stmt.setInt(1, UI_ID);
			stmt.setString(2, status);

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("Total");
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving status count for UI_ID: " + UI_ID);
			e.printStackTrace();
		}

		return -1;
	}
}
package DAOs;

import java.sql.*;
import config.AppConfig;
import models.UserInfo;
import models.Credential;

public class AddUserDAO {
	
	// ===== SQL STRINGS =====
	private String queryUserInfo, queryCredential, queryCheckUser, queryCount;
	
	public AddUserDAO(){
		// ===== INIT SQL =====
		queryUserInfo = """
			INSERT INTO %s(first_name, middle_name, last_name, sex,
				contact_number, email_address, house_number,
				street, purok)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
			""".formatted(AppConfig.TABLE_USER_INFO);
			
		queryCredential = """
			INSERT INTO %s(UI_ID, username, password, role, is_verified)
			VALUES (?, ?, ?, ?, ?);
			""".formatted(AppConfig.TABLE_CREDENTIAL);
			
		queryCheckUser = "SELECT COUNT(*) FROM %s WHERE username = ?".formatted(AppConfig.TABLE_CREDENTIAL);
		
		queryCount = "SELECT COUNT(*) FROM %s".formatted(AppConfig.TABLE_CREDENTIAL);
	}

	public int addUser(Connection con, UserInfo ui) throws SQLException {
		// ===== INSERT USER INFO =====
		try (PreparedStatement stmt = con.prepareStatement(queryUserInfo, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, ui.getFName());
			stmt.setString(2, ui.getMName());
			stmt.setString(3, ui.getLName());
			stmt.setString(4, ui.getSex());
			stmt.setString(5, ui.getContact());
			stmt.setString(6, ui.getEmail());
			stmt.setString(7, ui.getHouseNum());
			stmt.setString(8, ui.getStreet());
			stmt.setString(9, ui.getPurok());

			stmt.executeUpdate();

			// ===== GET GENERATED ID =====
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					return generatedKeys.getInt(1);
				}
			}
		}
		return -1;
	}

	public boolean addCredential(Connection con, int userID, Credential c) throws SQLException {
		// ===== INSERT CREDENTIAL =====
		try (PreparedStatement stmt = con.prepareStatement(queryCredential)) {
			stmt.setInt(1, userID);
			stmt.setString(2, c.getUsername());
			stmt.setString(3, c.getPassword());
			stmt.setString(4, c.getRole());
			stmt.setBoolean(5, c.getIsVerified());

			return stmt.executeUpdate() > 0;
		}
	}

	public boolean isUsernameTaken(Connection con, String username) {
		boolean isTaken = false;
		
		// ===== CHECK USERNAME =====
		try (PreparedStatement stmt = con.prepareStatement(queryCheckUser)) {
			stmt.setString(1, username);
			
			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					if(rs.getInt(1) > 0) isTaken = true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isTaken;
	}

	public int getUserCount(Connection con) {
		int userCount = 0;
		
		// ===== COUNT USERS =====
		try (PreparedStatement stmt = con.prepareStatement(queryCount);
			 ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				userCount = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userCount;
	}
}
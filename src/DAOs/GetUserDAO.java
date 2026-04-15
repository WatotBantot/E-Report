package daos;

import models.Credential;
import models.UserInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) for retrieving users and credentials from the
 * database.
 *
 * Provides methods to fetch:
 * - User information by user ID
 * - Credential details by username and password
 */
public class GetUserDAO {
	
	// ===== SQL STRINGS =====
	private String queryUser, queryCredential;
	
	public GetUserDAO(){
		// ===== INIT SQL =====
		queryUser = """
			SELECT UI_ID, first_name, middle_name, last_name, sex,
				contact_number, email_address, house_number, street, purok
			FROM User_Info
			WHERE UI_ID = ?;
			""";
			
		queryCredential = """
			SELECT Credential.UI_ID, username, password
			FROM Credential
			INNER JOIN User_Info ON Credential.UI_ID = User_Info.UI_ID
			WHERE username = ? AND password = ?;
			""";
	}

	/**
	 * Retrieves a {@link UserInfo} object by user ID.
	 *
	 * Process:
	 * - Executes a SELECT query on User_Info table
	 * - Maps the result set to a UserInfo object
	 *
	 * @param UI_ID the ID of the user to retrieve
	 * @return a populated {@link UserInfo} object if found, null otherwise
	 */
	public UserInfo getUser(Connection con, int UI_ID) {
		// ===== GET USER =====
		try (con;
				PreparedStatement stmt = con.prepareStatement(queryUser)) {

			stmt.setInt(1, UI_ID);

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					// ===== MAP TO USERINFO =====
					UserInfo ui = new UserInfo();
					ui.setUI_ID(rs.getInt("UI_ID"));
					ui.setFName(rs.getString("first_name"));
					ui.setMName(rs.getString("middle_name"));
					ui.setLName(rs.getString("last_name"));
					ui.setSex(rs.getString("sex"));
					ui.setContact(rs.getString("contact_number"));
					ui.setEmail(rs.getString("email_address"));
					ui.setHouseNum(rs.getString("house_number"));
					ui.setStreet(rs.getString("street"));
					ui.setPurok(rs.getString("purok"));
					return ui;
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving user info from User_Info table");
			System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
		}

		return null;
	}

	/**
	 * Retrieves a {@link Credential} object by username and password.
	 *
	 * Process:
	 * - Executes a SELECT query on Credential table joined with User_Info
	 * - Maps the result set to a Credential object
	 *
	 * @param username the username to search for
	 * @param password the password to match
	 * @return a populated {@link Credential} object if found, null otherwise
	 */
	public Credential getCredential(Connection con, String username, String password) {
		// ===== GET CREDENTIAL =====
		try (con;
				PreparedStatement stmt = con.prepareStatement(queryCredential)) {

			stmt.setString(1, username);
			stmt.setString(2, password);

			// ===== EXECUTE QUERY =====
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					// ===== MAP TO CREDENTIAL =====
					Credential c = new Credential();
					c.setUI_ID(rs.getInt("UI_ID"));
					c.setUsername(rs.getString("username"));
					c.setPassword(rs.getString("password"));
					return c;
				}
			}

		} catch (SQLException e) {
			System.err.println("Error retrieving credential from Credential table");
			System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
		}

		return null;
	}
}
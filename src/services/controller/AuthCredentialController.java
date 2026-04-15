package services.controller;

import daos.GetUserDAO;
import config.database.DBConnection;
import java.sql.Connection;
import models.Credential;
import models.UserSession;

/**
 * AuthCredentialController
 * 
 * Handles user authentication by validating credentials against the database.
 */
public class AuthCredentialController {
	
	// ===== DAO INSTANCE =====
	private GetUserDAO userDAO;
	
	public AuthCredentialController(){
		// ===== INIT DAO =====
		userDAO = new GetUserDAO();
	}

	/**
	 * Authenticates a user against the database credentials.
	 * 
	 * @param username The username of the user attempting to log in
	 * @param password The password corresponding to the username
	 * @return UserSession object if authentication succeeds; null otherwise
	 */
	public UserSession authenticateUser(String username, String password) {
		Connection con = null;
		
		try {
			// ===== CREATE CONNECTION =====
			con = DBConnection.connect();
			
			// ===== AUTHENTICATE USER =====
			Credential credential = userDAO.getCredential(con, username, password);

			// ===== CHECK CREDENTIAL =====
			if (credential != null) {
				// ===== CREATE SESSION =====
				return new UserSession(
					credential.getUI_ID(),
					credential.getRole(),
					credential.getIsVerified()
				);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// ===== CLOSE CONNECTION =====
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
package services.controller;

import DAOs.AddUserDAO;
import config.DBConnection;
import models.Credential;
import models.UserInfo;
import java.sql.Connection;
import java.sql.SQLException;

public class UserServiceController {
	
	// ===== DAO INSTANCE =====
	private AddUserDAO addUserDAO;
	
	public UserServiceController(){
		// ===== INIT DAO =====
		addUserDAO = new AddUserDAO();
	}

	/**
	 * Registers a new user with credentials as a single transaction.
	 * 
	 * @param ui UserInfo object
	 * @param c Credential object
	 * @return status message
	 */
	public String registerUser(UserInfo ui, Credential c) {
		Connection con = null;
		
		try {
			// ===== CREATE CONNECTION =====
			con = DBConnection.connect();
			
			// ===== CHECK USERNAME =====
			if (addUserDAO.isUsernameTaken(con, c.getUsername())) {
				return "Username is already taken.";
			}

			// ===== DETERMINE ROLE =====
			int existingUsers = addUserDAO.getUserCount(con);
			if (existingUsers == 0) {
				c.setRole("Secretary");
				c.setIsVerified(true);
			} else {
				c.setRole("Resident");
				c.setIsVerified(false);
			}

			// ===== BEGIN TRANSACTION =====
			con.setAutoCommit(false);

			// ===== INSERT USER =====
			int userId = addUserDAO.addUser(con, ui);
			if (userId == -1) {
				throw new SQLException("Failed to save user profile.");
			}

			// ===== INSERT CREDENTIAL =====
			boolean credSuccess = addUserDAO.addCredential(con, userId, c);

			// ===== COMMIT OR ROLLBACK =====
			if (credSuccess) {
				con.commit();
				return "SUCCESS";
			} else {
				con.rollback();
				return "Failed to create credentials.";
			}

		} catch (SQLException e) {
			// ===== ROLLBACK ON ERROR =====
			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			return "Database error: " + e.getMessage();
		} finally {
			// ===== CLOSE CONNECTION =====
			if (con != null) {
				try {
					con.setAutoCommit(true);
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
package services.controller;

import config.database.DBConnection;
import daos.UpdateUserDao;
import daos.VerifyPasswordDao;
import models.UserInfo;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Controller for profile update operations with password verification.
 */
public class ProfileUpdateController {

    private final VerifyPasswordDao verifyDao;
    private final UpdateUserDao updateDao;

    public ProfileUpdateController() {
        this.verifyDao = new VerifyPasswordDao();
        this.updateDao = new UpdateUserDao();
    }

    public static class VerifyResult {
        public final boolean success;
        public final String message;

        public VerifyResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public VerifyResult verifyPassword(int userId, String password) {
        if (password == null || password.trim().isEmpty()) {
            return new VerifyResult(false, "Please enter your password.");
        }

        try (Connection con = DBConnection.connect()) {
            boolean isValid = verifyDao.verifyPassword(con, userId, password);
            if (isValid) {
                return new VerifyResult(true, "Password verified.");
            } else {
                return new VerifyResult(false, "Incorrect password. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new VerifyResult(false, "Database error during verification.");
        }
    }

    /**
     * Updates profile — email, contact, and username only.
     *
     * @param ui       UserInfo with updated contact and email
     * @param username new username
     * @return true if successful
     */
    public boolean updateProfile(UserInfo ui, String username) {
        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false);

            // Check username conflict — if taken by ANOTHER user, abort
            if (updateDao.isUsernameTakenByOther(con, username, ui.getUI_ID())) {
                con.rollback();
                return false;
            }

            // Update User_Info (contact + email)
            boolean userUpdated = updateDao.updateUserInfo(con, ui);

            // Update Credential (username)
            boolean usernameUpdated = updateDao.updateUsername(con, ui.getUI_ID(), username);

            if (userUpdated && usernameUpdated) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
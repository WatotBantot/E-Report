package daos;

import config.AppConfig;
import models.UserInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for updating user profile information — only email, contact, and
 * username.
 */
public class UpdateUserDao {

    private final String queryUpdateUser;
    private final String queryUpdateCredentialUsername;
    private final String queryCheckUsername;

    public UpdateUserDao() {
        queryUpdateUser = """
                UPDATE %s
                SET contact_number = ?, email_address = ?
                WHERE UI_ID = ?;
                """.formatted(AppConfig.TABLE_USER_INFO);

        queryUpdateCredentialUsername = """
                UPDATE %s
                SET username = ?
                WHERE UI_ID = ?;
                """.formatted(AppConfig.TABLE_CREDENTIAL);

        queryCheckUsername = """
                SELECT COUNT(*) FROM %s
                WHERE username = ? AND UI_ID != ?;
                """.formatted(AppConfig.TABLE_CREDENTIAL);
    }

    /**
     * Updates user contact info (phone and email only).
     */
    public boolean updateUserInfo(Connection con, UserInfo ui) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(queryUpdateUser)) {
            stmt.setString(1, ui.getContact());
            stmt.setString(2, ui.getEmail());
            stmt.setInt(3, ui.getUI_ID());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates username in Credential table.
     */
    public boolean updateUsername(Connection con, int userId, String username) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(queryUpdateCredentialUsername)) {
            stmt.setString(1, username);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if username is already taken by another user.
     */
    public boolean isUsernameTakenByOther(Connection con, String username, int currentUserId) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(queryCheckUsername)) {
            stmt.setString(1, username);
            stmt.setInt(2, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
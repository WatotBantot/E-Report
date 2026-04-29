package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.UserInfo;

public class PasswordResetDao {

    /**
     * Finds a user by their username (via the Credential table).
     * This is used in Step 1 of the forgot-password flow.
     */
    public UserInfo findByUsername(Connection con, String username) throws SQLException {
        String sql = """
                SELECT ui.UI_ID, ui.first_name, ui.middle_name, ui.last_name,
                       ui.sex, ui.contact_number, ui.email_address,
                       ui.house_number, ui.street, ui.purok,
                       c.username
                FROM User_Info ui
                INNER JOIN Credential c ON c.UI_ID = ui.UI_ID
                WHERE c.username = ?
                """;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
        }
        return null;
    }

    /**
     * Finds a user by their registered email address.
     * (Kept for future use; the current flow uses findByUsername.)
     */
    public UserInfo findByEmail(Connection con, String email) throws SQLException {
        String sql = """
                SELECT ui.UI_ID, ui.first_name, ui.middle_name, ui.last_name,
                       ui.sex, ui.contact_number, ui.email_address,
                       ui.house_number, ui.street, ui.purok,
                       c.username
                FROM User_Info ui
                INNER JOIN Credential c ON c.UI_ID = ui.UI_ID
                WHERE ui.email_address = ?
                """;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
        }
        return null;
    }

    /**
     * Updates the password for a user by their UI_ID.
     * 
     * IMPORTANT SECURITY NOTE: Your Credential table stores passwords as
     * VARCHAR(50).
     * If the rest of your app stores passwords in plain text, this will work but is
     * insecure.
     * If you have a hashing utility (e.g., BCrypt), hash 'newPassword' BEFORE
     * calling this method.
     */
    public boolean updatePassword(Connection con, int userId, String newPassword) throws SQLException {
        String sql = """
                UPDATE Credential
                SET password = ?
                WHERE UI_ID = ?
                """;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}
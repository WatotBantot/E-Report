package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for verifying user passwords against the database.
 */
public class VerifyPasswordDao {

    private final String queryVerify;

    public VerifyPasswordDao() {
        queryVerify = """
                SELECT UI_ID FROM Credential
                WHERE UI_ID = ? AND password = ?;
                """;
    }

    /**
     * Verifies if the given password matches the stored password for the user.
     *
     * @param con      active database connection
     * @param userId   the user's UI_ID
     * @param password plain-text password to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(Connection con, int userId, String password) {
        try (PreparedStatement stmt = con.prepareStatement(queryVerify)) {
            stmt.setInt(1, userId);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if row exists = password matches
            }
        } catch (SQLException e) {
            System.err.println("Error verifying password");
            e.printStackTrace();
        }
        return false;
    }
}
package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import config.AppConfig;
import models.UserInfo;
import models.Credential;

/**
 * DAO responsible for inserting users and their credentials.
 * Workflow: addUser() -> addCredential()
 */
public class AddUserDAO {

    /**
     * Inserts a new user into the User_Info table and returns the generated user
     * ID.
     *
     * @param con DB connection
     * @param ui  UserInfo object
     * @return auto-generated user ID, or -1 if insertion failed
     */
    public int addUser(Connection con, UserInfo ui) {
        String query = """
                INSERT INTO %s(first_name, middle_name, last_name, sex,
                                contact_number, email_address, house_number,
                                street, purok)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                """.formatted(AppConfig.TABLE_USER_INFO);

        try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ui.getFName());
            stmt.setString(2, ui.getMName());
            stmt.setString(3, ui.getLName());
            stmt.setString(4, ui.getSex());
            stmt.setString(5, ui.getContact());
            stmt.setString(6, ui.getEmail());
            stmt.setString(7, ui.getHouseNum());
            stmt.setString(8, ui.getStreet());
            stmt.setString(9, ui.getPurok());

            int rows = stmt.executeUpdate();
            System.out.println(rows + " row(s) inserted into " + AppConfig.TABLE_USER_INFO);

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to insert user into " + AppConfig.TABLE_USER_INFO);
            System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
        }

        return -1;
    }

    /**
     * Inserts a credential for a specific user into the Credential table.
     *
     * @param con    DB connection
     * @param userID user ID from User_Info table
     * @param c      Credential object
     * @return true if insertion succeeded, false otherwise
     */
    public boolean addCredential(Connection con, int userID, Credential c) {
        String query = """
                INSERT INTO %s(UI_ID, username, password, role, is_verified)
                VALUES (?, ?, ?, ?, ?);
                """.formatted(AppConfig.TABLE_CREDENTIAL);

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userID);
            stmt.setString(2, c.getUsername());
            stmt.setString(3, c.getPassword());
            stmt.setString(4, c.getRole());
            stmt.setBoolean(5, c.getIsVerified());

            int rows = stmt.executeUpdate();
            System.out.println(rows + " row(s) inserted into " + AppConfig.TABLE_CREDENTIAL);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Failed to insert credential for user ID " + userID);
            System.err.println("SQL State: " + e.getSQLState() + " - " + e.getMessage());
            return false;
        }
    }
}
package services.controller;

import DAOs.GetUserDAO;
import models.Credential;
import models.UserSession;

public class AuthCredential {

    /**
     * Authenticates a user on the database.
     * 
     * @params String username, String password
     * @return UserSession if credentials match, null otherwise
     */

    public static UserSession authenticateUser(String username, String password) {
        GetUserDAO userDAO;
        Credential credential;

        if (username == null || password == null) {
            return null;
        }

        userDAO = new GetUserDAO();
        credential = userDAO.getCredential(username, password);

        if (credential != null) {
            return new UserSession(
                    credential.getUI_ID(),
                    credential.getRole(),
                    credential.getIsVerified());
        }

        return null;
    }
}

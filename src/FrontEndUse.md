Frontend Usage Guide

Before using, make sure you already added sql connector on the following path (VS Code)
"JAVA PROJECTS"
-> "E-Report"
--> "Referenced Libraries"
--> connector here (e.g. mysql-connector-j-x.x.x.jar) x.x.x is version number

Run the following to initialize the software
E_Report.java

Note: Only focus on Services, Controller, and Features classes folder
Only on the Features folder the GUI/UI will be added

=========

Controllers

1.  AuthCredentialController

    Purpose:
    Handles user login authentication. Validates the username and password against the database and returns a UserSession if credentials are valid.

    Responsibilities:
    Verify user credentials.
    Return session information (user ID, role, verification status).

    Usage Process:
    Call authenticateUser(username, password) from the frontend login form.
    If credentials match, a UserSession object is returned.
    If invalid or missing, returns null.

    Parameters:
    String username – The user's login username.
    String password – The user's login password.

    Return Value:
    UserSession – Contains user ID, role, and verification status if successful.
    null – If authentication fails.

    = EXAMPLE USAGE FOR FRONT END =
    import services.controller.AuthCredentialController;
    import models.UserSession;

    public class LoginForm {

         public static void main(String[] args) {
             String inputUsername = "john.doe";
             String inputPassword = "password123";

             UserSession session = AuthCredentialController.authenticateUser(inputUsername, inputPassword);

             if (session != null) {
                 System.out.println("Login successful!");
                 System.out.println("User ID: " + session.getUserID());
                 System.out.println("Role: " + session.getRole());
                 System.out.println("Verified: " + session.getIsVerified());
             } else {
                 System.out.println("Invalid username or password.");
             }
         }

    }

---

2.  ComplaintServiceController

    Purpose:
    Handles submission of user complaints and optional image attachments. Saves complaints to the database.

    Responsibilities:
    Add new complaints for a user.
    Process and attach image files (BLOBs) to ComplaintDetail objects.
    Ensure database connection is safely closed after operation.

    Usage Process:
    Collect complaint details in a ComplaintDetail object.
    Optionally, provide a file to attach (image, photo, document).
    Call addComplaint(userID, complaintDetail, file) to persist the complaint.
    System handles image processing and database insertion automatically.

    Parameters:
    int UI_ID – The ID of the user submitting the complaint.
    ComplaintDetail cd – Object containing complaint information.
    File droppedFile – Optional file to attach (can be null).

    Return Value:
    None (prints success/failure messages).

    = EXAMPLE USAGE FOR FRONT END =
    import services.controller.ComplaintServiceController;
    import models.ComplaintDetail;
    import java.io.File;

    public class ComplaintForm {

         public static void main(String[] args) {
             int userID = 101; // Logged-in user ID
             OR
             int userID = session.getUserID();
             ComplaintDetail complaint = new ComplaintDetail();

             // Fill complaint details
             complaint.setCurrentStatus("Pending");
             complaint.setSubject("Noise Complaint");
             complaint.setType("Community");
             complaint.setStreet("Main St");
             complaint.setPurok("Purok 3");
             complaint.setDetails("Loud music late at night.");

             // Optional image file
             File photo = new File("C:/Users/User/Pictures/noise.jpg");

             // Create service controller and submit complaint
             ComplaintServiceController service = new ComplaintServiceController();
             service.addComplaint(userID, complaint, photo);

             System.out.println("Complaint submission process complete.");
         }

    }

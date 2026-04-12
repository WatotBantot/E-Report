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

=========

Utils

3. ValidationUtil
    Purpose:
    Provides a centralized validation middleware for all frontend forms (Login, Register, Profile Update, etc.).
    It ensures required fields are filled, input formats are correct, and user-friendly error messages are generated consistently across the system.

    Responsibilities:
    Validate required input fields (text and password fields)
    Validate email format
    Validate contact number format
    Validate age constraints
    Automatically detect missing inputs
    Focus the first invalid field for better UX
    Generate readable error messages for UI display

    Usage Process:
    Collect all form inputs from UI components
    Pass fields into validation utility methods
    Build error messages using StringBuilder
    Stop process if validation fails
    Proceed only if all inputs are valid

    Parameters:
    Map<JTextField, String> – Required text fields with labels for error messages
    Map<JPasswordField, String> – Required password fields with labels
    String email – Email input string
    String contact – Contact number string
    StringBuilder error – Container for validation error messages
    Return Value:
    true – If validation fails (errors exist)
    false – If all inputs are valid

    Core Methods:
    1. requireFields()
    Validates if JTextField inputs are empty.

    2. requirePasswordFields()
    Validates if JPasswordField inputs are empty.

    3. validateEmailField()
    Checks if email is:
    not empty
    in correct format

    4. validateContactField()
    Checks if contact number:
    not empty
    follows PH format (09xxxxxxxxx or 08xxxxxxxxx)

    5. isValidEmail()
    Returns boolean result for email regex validation.

    6. isValidContact()
    Returns boolean result for contact number validation.

    7. isValidAge()
    Checks if age is within valid range (0–200).

    EXAMPLE USAGE
    import services.middleware.ValidationUtil;
    import javax.swing.*;
    import java.util.LinkedHashMap;
    import java.util.Map;

    public class RegisterForm {

        public static void main(String[] args) {

            JTextField txtEmail = new JTextField("user@gmail.com");
            JTextField txtContact = new JTextField("09123456789");

            StringBuilder errors = new StringBuilder("Please fix the following:\n");

            Map<JTextField, String> fields = new LinkedHashMap<>();
            fields.put(txtEmail, "Email");
            fields.put(txtContact, "Contact Number");

            boolean hasError = ValidationUtil.requireFields(fields, errors);

            // Format validations
            ValidationUtil.validateEmailField(txtEmail.getText(), errors);
            ValidationUtil.validateContactField(txtContact.getText(), errors);

            if (hasError || errors.length() > "Please fix the following:\n".length()) {
                JOptionPane.showMessageDialog(null,
                        errors.toString(),
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            System.out.println("Register validation passed!");
        }
    }

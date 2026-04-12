# E-Report: Merged Version

## Overview

This is the merged version of the E-Report project, combining:
- **Leader's version (E-Report)**: Application UI framework, login system, database structure
- **User's version (E-Report-main)**: Advanced report submission dashboard, map picker, complaint display

## Key Integration Points

### 1. **Authentication Flow**
- `App.E_Report` (main entry point) initializes the application
- Users see the Homepage with Login/Register buttons
- `LoginUI` handles authentication using `AuthCredentialController` and `AuthCredential`
- After successful login, users are redirected to `SubmitReportView`

### 2. **Report Submission**
- `SubmitReportView` - Main complaint submission interface
  - Categories: Theft, Noise, Physical Injuries, Trespass, Vandalism, Others
  - Location pickin via LeafletJS map (`LeafletMapPickerBridge`)
  - IP-based location detection ("Locate Me" feature)
  - Photo upload with file saving to `images/` directory
  - Document complaints with full details
- `ComplaintService` - Handles complaint submission and image processing
- `AddComplaintDAO` - Persists complaints to database

### 3. **Report Viewing**
- `FullComplaintDisplayView` - Displays individual complaint details with attached photos

### 4. **Map Integration**
- `LeafletMapPickerBridge` - Leaflet-based interactive map picker
  - Opens in browser for location selection
  - Reverse geocoding via Nominatim API
  - Returns coordinates and address to the app
- `SubmitReportMapPanel` - Mini map display in sidebar

## Database Configuration

Located in `src/config/AppConfig.java`:
```java
public static final String DB_URL = "jdbc:mysql://localhost:3306/e_report";
public static final String DB_USERNAME = "root";  // Change this to your MySQL user
public static final String DB_PASSWORD = "";      // Add your MySQL password if needed
```

### Tables Created Automatically:
- `User_Info` - User personal information
- `Credential` - Login credentials (username/password)
- `Complaint_Detail` - Complaint details with photo paths
- `Complaint` - Links complaints to users

## File Structure

```
src/
├── app/
│   └── E_Report.java              # Main entry point with navigation
├── config/
│   ├── AppConfig.java             # Database configuration
│   ├── DBConnection.java          #Database connections
│   ├── DBCreate.java              # Database creation
│   ├── TBCreate.java              # Table creation
│   └── UIConfig.java              # UI styling constants
├── DAOs/
│   ├── AddComplaintDAO.java       # Insert complaints
│   ├── AddUserDAO.java            # Insert users
│   ├── GetComplaintDAO.java       # Retrieve complaints
│   └── GetUserDAO.java            # Retrieve user data
├── features/
│   ├── components/
│   │   ├── UIButton.java
│   │   ├── UICard.java
│   │   ├── UIComboBox.java
│   │   ├── UIInput.java
│   │   ├── UIPasswordInput.java
│   │   └── UIRadioButtonGroup.java
│   ├── submit/                    # Submit feature modules (v2.1+)
│   │   ├── BackgroundImagePanel.java   # Background rendering
│   │   ├── FullComplaintDisplayView.java # Complaint display
│   │   ├── LeafletMapPickerBridge.java   # Map picker HTTP bridge
│   │   ├── SubmitReportConstants.java    # Categories, locations
│   │   ├── SubmitReportMapPanel.java     # Mini map display
│   │   └── SubmitReportView.java         # Main submission UI
│   ├── ui/
│   │   ├── HomepageUI.java
│   │   ├── LoginUI.java
│   │   └── RegisterUI.java
│   └── UI.md                      # UI components documentation
├── models/
│   ├── ComplaintAction.java
│   ├── ComplaintDetail.java       # Updated to use file paths, not bytes
│   ├── ComplaintHistoryDetail.java
│   ├── Credential.java
│   ├── UserInfo.java
│   └── UserSession.java
├── services/
│   ├── controller/
│   │   ├── AuthCredential.java       # Static auth method
│   │   ├── AuthCredentialController.java
│   │   ├── ComplaintService.java      # New - file path based
│   │   ├── ComplaintServiceController.java # Fixed to use file paths
│   │   ├── DatabaseController.java
│   │   ├── UserInputController.java
│   │   └── UserService.java
│   └── middleware/
│       ├── UIValidator.java
│       └── ValidationUtil.java
└── tests/
    ├── integrations/
    │   ├── DAOIntegrationTest.java
    │   └── DBIntegrationTest.java
    └── units/
        ├── AddComplaintDAOTest.java
        ├── AddUserDAOTest.java
        ├── ComplaintServiceTest.java
        ├── DBTest.java
        ├── GetComplaintDAOTest.java
        └── GetUserDAOTest.java
```

## Important Files Modified

### `src/app/E_Report.java`
- Added support for "submitreport" navigation route
- Imported `SubmitReportView`
- When user logs in, they're directed to SubmitReportView

### `src/features/ui/LoginUI.java`
- After successful authentication, navigates to "submitreport" instead of "home"

### `src/models/ComplaintDetail.java`
- **IMPORTANT**: Changed from `byte[] photoAttachmentBytes` to `String photoAttachment`
- Now stores file paths instead of binary data
- Consistent with actual database schema and DAO implementation

### `src/services/controller/ComplaintServiceController.java`
- Updated `processAndAttachImage()` to save files to disk and store path
- Now consistent with `ComplaintService` and database expectations

## Setup Instructions

### 1. **Database Setup**
```bash
# Ensure MySQL is running
mysql -u root -p
```

### 2. **Update Database Credentials** (if needed)
Edit `src/config/AppConfig.java`:
```java
public static final String DB_USERNAME = "your_mysql_user";
public static final String DB_PASSWORD = "your_mysql_password";
```

### 3. **Compile and Run**
```bash
javac -d bin -cp .:lib/* src/**/*.java
java -cp bin:lib/* app.E_Report
```

### 4. **Create Sample User** (for testing)
The registration feature in the UI allows users to create new accounts with their credentials.

## Features Integrated

### From Leader's Version:
✓ Professional UI with CardLayout navigation
✓ Login/Register system with database persistence
✓ Authentication via credentials
✓ Database initialization
✓ User session management

### From User's Version:
✓ Advanced complaint submission form
✓ Interactive Leaflet map picker
✓ IP-based location detection
✓ Photo upload and storage
✓ Categories and Purok selection
✓ Complaint display with images
✓ Comprehensive form validation

## Known Fixes Applied

1. **ComplaintDetail Model**: Changed from byte array storage (BLOB) to file path storage
   - More practical for deployment
   - Consistent with AddComplaintDAO expectations
   - Better performance than storing large binary in database

2. **Navigation**: Integrated SubmitReportView into main app flow
   - Login → SubmitReportView (instead of HomePage)
   - UserSession passed to report dashboard

3. **Service Layer**: Ensured both ComplaintService and ComplaintServiceController use file paths

## Next Steps for Deployment

1. Update MySQL credentials in AppConfig.java
2. Test login with sample users
3. Set up "images/" folder with proper permissions
4. Configure file upload folder path if needed
5. Test complaint submission end-to-end
6. Deploy to GitHub

## Contact & Support

This is a complete, merged version ready for:
- ✓ Git add
- ✓ Git commit  
- ✓ Git push to GitHub

All conflicts have been resolved and both codebases are now fully consolidated.

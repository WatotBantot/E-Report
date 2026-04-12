# E-Report - Changelog

All notable changes to the E-Report project are documented in this file.

---

## [2.1] - 2026-04-12

### Major Changes

#### Package Restructuring
- **Moved submit features to dedicated package**:
  - `src/features/` → `src/features/submit/`
  - Package name: `features.submit`
  - All 6 submit-related classes moved:
    - `SubmitReportView.java`
    - `SubmitReportConstants.java`
    - `LeafletMapPickerBridge.java`
    - `BackgroundImagePanel.java`
    - `SubmitReportMapPanel.java`
    - `FullComplaintDisplayView.java`
  - Benefits: Better code organization, cleaner separation of concerns

#### Navigation System Improvements
- **File**: `src/app/E_Report.java`
- **Method**: `navigate(String route)`
- **Updates**:
  - Now uses `SwingUtilities.invokeLater()` for thread safety
  - Properly disposes main window when opening SubmitReportView
  - Prevents memory leaks and race conditions
  - Early return on dashboard navigation to avoid unnecessary repaints

**Code Diff**:
```java
// OLD: Just hiding the window
this.setVisible(false);

// NEW: Properly disposing on Event Dispatch Thread
SwingUtilities.invokeLater(this::dispose);
return;  // Skip revalidate/repaint
```

### Imports Updated
- `E_Report.java`: Updated to import from `features.submit.SubmitReportView`
- `SubmitReportView.java`: Cleaned up imports (removed redundant same-package imports)
- `SubmitReportMapPanel.java`: Cleaned up imports

### Files Changed
- `src/app/E_Report.java` (navigation method)
- `src/features/submit/SubmitReportView.java` (moved, imports updated)
- `src/features/submit/SubmitReportConstants.java` (moved, package updated)
- `src/features/submit/LeafletMapPickerBridge.java` (moved, package updated)
- `src/features/submit/BackgroundImagePanel.java` (moved, package updated)
- `src/features/submit/SubmitReportMapPanel.java` (moved, package updated)
- `src/features/submit/FullComplaintDisplayView.java` (moved, package updated)

### Bug Fixes
- Fixed import resolution issues with nested package classes
- Fixed window disposal to prevent memory leaks

### Documentation Updated
- `INTEGRATION_GUIDE.md` - Added v2.1 section with package restructuring details
- `DEPLOYMENT_CHECKLIST.md` - Added v2.1 checklist items
- `MERGE_SUMMARY.md` - Updated file structure to show features/submit/ subfolder

---

## [2.0] - 2026-04-12

### Major Changes

#### Authentication Bug Fix
- **File**: `src/DAOs/GetUserDAO.java`
- **Method**: `getCredential(String username, String password)`
- **Issue**: SQL query was not retrieving `role` and `is_verified` fields
- **Fix**: Updated SQL query to include missing columns

**Before**:
```sql
SELECT Credential.UI_ID, username, password
FROM Credential WHERE ...
```

**After**:
```sql
SELECT Credential.UI_ID, username, password, role, is_verified
FROM Credential WHERE ...
```

**Impact**: 
- Users now properly receive role information (Resident, Captain, Secretary)
- Verification status correctly loaded from database
- Role-based UI customization now works correctly

### Navigation Flow Fixed
- `LoginUI.java` now properly navigates to "submitreport" after authentication
- `E_Report.navigate()` method supports "submitreport" route
- User session is properly passed to SubmitReportView

### Merged Features Consolidated
- **Initial merge completed** of E-Report and E-Report-main codebases
- All features from both versions now integrated
- ComplaintService working with file-based photo storage
- Database initialization working

### Documentation Created
- `MERGE_SUMMARY.md` - Overview of merged codebase
- `INTEGRATION_GUIDE.md` - Detailed integration documentation
- `DEPLOYMENT_CHECKLIST.md` - Deployment preparation guide

---

## [1.0] - Initial Merge

### Features
- Merged two separate E-Report implementations:
  - **Leader's Version**: Professional UI framework, comprehensive authentication, database schema
  - **User's Version**: Advanced complaint dashboard, map integration, photo handling

### Architecture
- **MVC Pattern**: Models, Views, Controllers separated
- **DAO Pattern**: Database access abstraction
- **Package Structure**:
  - `app/` - Application entry point
  - `config/` - Database and UI configuration
  - `DAOs/` - Data access objects
  - `features/` - UI components and views
  - `models/` - Data models
  - `services/` - Business logic controllers
  - `services/middleware/` - Validation utilities

### Integrated Components
- Authentication system (LoginUI, AuthCredentialController)
- Complaint submission dashboard (SubmitReportView)
- Interactive map picker (LeafletMapPickerBridge)
- Photo upload system (ComplaintService)
- Complaint viewing (FullComplaintDisplayView)
- Database initialization (DBConnection, DBCreate, TBCreate)

### Database Schema
- `User_Info` - User information
- `Credential` - Login credentials with role
- `Complaint_Detail` - Complaint details and metadata
- `Complaint` - User-complaint relationships

---

## Version Details

| Version | Date | Status | Key Feature |
|---------|------|--------|-------------|
| 2.1 | 2026-04-12 | Current | Package restructuring & navigation improvements |
| 2.0 | 2026-04-12 | Stable | Authentication bug fix |
| 1.0 | 2026-04-12 | Complete | Initial merge of both codebases |

---

## Running the Application

### Current Version (v2.1)

**To start the application**:
```bash
java -classpath ".:./lib/*" app.E_Report
```

**Login Flow**:
1. Application shows Homepage
2. Click "Login" button
3. Enter credentials (verified against database)
4. User role retrieved from Credential table
5. Dashboard (SubmitReportView) opens with proper role-based UI
6. Main window properly disposed

### Default Test User
- **Username**: admin
- **Password**: admin123
- **Role**: Captain

---

## Known Issues

None at this time. All major bugs from v2.0 have been resolved.

---

## Future Enhancements

- [ ] Dashboard statistics (pending, resolved counts)
- [ ] Complaint status tracking and updates
- [ ] User management interface
- [ ] Email notifications
- [ ] Advanced filtering and search
- [ ] Export reports (PDF, CSV)
- [ ] Offline mode support

---

## Contributing

When making changes:
1. Update relevant documentation files
2. Test all navigation flows
3. Verify package imports are correct
4. Use SwingUtilities for all Swing operations in multi-threaded contexts
5. Update CHANGELOG.md with your changes

---

## Support

For issues or questions about the E-Report system, refer to:
- `INTEGRATION_GUIDE.md` - Architecture and flow details
- `DEPLOYMENT_CHECKLIST.md` - Setup and configuration
- `README.md` - Quick start guide
- Source code comments for implementation details

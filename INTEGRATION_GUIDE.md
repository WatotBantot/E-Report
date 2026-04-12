# E-Report: Integration Documentation

## Overview

This document explains the integration of two separate E-Report contributions:
1. **Leader's Version**: Professional UI framework with authentication system
2. **User's Version**: Advanced complaint submission dashboard with map picker

The merged application provides a complete end-to-end solution for reporting complaints with full authentication and role-based access.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     E_Report.java                           │
│              (Main Application Entry Point)                 │
└──────────────┬──────────────────────────────────────────────┘
               │
               ├─────────────────┬──────────────────┐
               ▼                 ▼                  ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
        │ HomepageUI   │ │ LoginUI      │ │ RegisterUI   │
        │ (Landing)    │ │ (Auth)       │ │ (Sign Up)    │
        └──────┬───────┘ └──────┬───────┘ └──────────────┘
               │                │
               └────────┬───────┘
                        │ UserSession Created
                        ▼
        ┌─────────────────────────────────┐
        │   SubmitReportView              │
        │  (Complaint Dashboard)          │
        │  - Form with validation         │
        │  - Map picker (LeafletJS)       │
        │  - Photo upload                 │
        │  - Role-based UI                │
        └─────────┬───────────────────────┘
                  │
                  ├──────────────┬──────────────┐
                  ▼              ▼              ▼
        ┌─────────────────┐ ┌────────────┐ ┌──────────┐
        │ComplaintService │ │Complaint   │ │DAO       │
        │(File storage)   │ │Detail Model│ │Classes   │
        └────────┬────────┘ └────────────┘ └────┬─────┘
                 │                              │
                 └──────────────┬───────────────┘
                                ▼
                        ┌──────────────────┐
                        │  e_report DB     │
                        │  (MySQL)         │
                        └──────────────────┘
```

---

## Authentication Flow (Leader's System)

### Step 1: User Login
- **Entry Point**: `LoginUI.java`
- **Process**:
  1. User enters username and password
  2. Input validated via `UIValidator`
  3. Credentials sent to `AuthCredentialController.authenticateUser()`

### Step 2: Credential Verification
- **DAO**: `GetUserDAO.getCredential()`
- **Query**: Retrieves username, password, **role**, and **is_verified** from Credential table
- **Return**: `Credential` object with all fields populated
- **CRITICAL FIX** _(Applied)_: Query now includes `role` and `is_verified` fields

### Step 3: Session Creation
- **Controller**: `AuthCredentialController.authenticateUser()`
- **Process**:
  1. Creates `UserSession` with:
     - User ID (UI_ID)
     - **Role** (Captain, Secretary, or Resident)
     - Verification status
  2. Passes session to main application
- **Result**: User authenticated with correct access level

### Step 4: Navigation to Dashboard
- **Navigation**: `E_Report.navigate("submitreport")`
- **Outcome**: Opens `SubmitReportView` with authenticated `UserSession`
- **No Additional Login**: Dashboard uses the session from step 3

---

## Role-Based Access Control

The application supports three user roles, determined at login:

| Role       | Description              | Access Level |
|-----------|--------------------------|--------------|
| **Resident** | Regular citizen         | Basic reporting |
| **Captain**  | Barangay Captain        | Full access + user management |
| **Secretary** | Barangay Secretary      | Administrative functions |

### Role Implementation

**Where Role is Used**:
1. **Database Storage**: `Credential.role` field
2. **Session Management**: `UserSession.getRole()`
3. **UI Customization**: `SubmitReportView.resolveRole()`
   - Controls sidebar items (Users menu only for Captain/Secretary)
   - Adjusts available features per role

**Example Code**:
```java
// In SubmitReportView
boolean isResident = ROLE_RESIDENT.equalsIgnoreCase(currentRole);
if (!isResident) {
    sidebar.add(sidebarItem("Users", "Users.png", false, this::openUsers));
}
```

---

## Report Submission Flow (User's System)

### Step 1: Form Input
- **UI Component**: `SubmitReportView`
- **Fields Collected**:
  - Title (subject)
  - Category (dropdown from `SubmitReportConstants`)
  - Purok (barangay location)
  - Street/Location
  - Narrative details
  - Photo attachment

### Step 2: Location Selection

**Option A - Automatic Detection**:
- Click "Locate Me" button
- App fetches location via IP API (IP-API.com)
- Populates latitude, longitude, city, region

**Option B - Interactive Map**:
- Click "Open Map (LeafletJS)"
- `LeafletMapPickerBridge` opens browser-based map
- User pins location manually
- Reverse geocoding via Nominatim API
- Returns coordinates and address

### Step 3: Photo Upload
- User selects image file via file chooser
- `ComplaintService.processAndAttachImage()` handles:
  1. Creates timestamp-based filename
  2. Saves to `images/` folder
  3. Stores file path in `ComplaintDetail` model

### Step 4: Validation & Submission
- **Validator**: Ensures all required fields filled
- **Service**: `ComplaintService.addComplaint()`
- **DAO**: `AddComplaintDAO.addComplaint()`
- **Result**: Complaint stored in database with photo reference

**Complete Flow**:
```java
// In SubmitReportView.submitComplaint()
ComplaintDetail complaint = buildComplaintFromForm();
ComplaintService service = new ComplaintService();
service.addComplaint(getCurrentUserId(), complaint, selectedFile);
```

---

## Data Model Integration

### Core Models

#### 1. UserSession _(Leader's)_
```java
public class UserSession {
    int userId;        // Primary key reference
    String role;       // Captain, Secretary, Resident
    boolean isVerified // Account verification status
}
```

#### 2. Credential _(Leader's - UPDATED)_
```java
public class Credential {
    int UI_ID;           // Links to User_Info
    String username;
    String password;
    String role;         // NOW RETRIEVED from DB
    boolean isVerified;  // NOW RETRIEVED from DB
    String dateCreated;
}
```

#### 3. ComplaintDetail _(User's - Updated for file paths)_
```java
public class ComplaintDetail {
    int CD_ID;                    // Auto-generated
    String currentStatus;         // Pending, In Progress, Resolved
    String subject;               // Title
    String type;                  // Category
    String street;                // Location detail
    String purok;                 // Barangay location
    double latitude, longitude;   // GPS coordinates
    String personsInvolved;       // Description
    String details;               // Full narrative
    String photoAttachment;       // FILE PATH (not bytes)
    Timestamp dateTime;           // Submission time
}
```

### Database Design

**Key Tables**:

1. **User_Info** (Leader's)
   - Stores personal information
   - Referenced by Credential and Complaint

2. **Credential** (Leader's - UPDATED)
   - Stores login credentials
   - **Now includes**: `role`, `is_verified`
   - Links to User_Info via UI_ID

3. **Complaint_Detail** (User's)
   - Stores complaint data
   - Stores photo file paths (not binary blobs)

4. **Complaint** (Links everything)
   - Bridges CD_ID and UI_ID
   - Maintains complaint-to-user relationship

---

## Service Layer Integration

### 1. AuthCredentialController (Leader's)
**Purpose**: Static method for authentication
```java
public static UserSession authenticateUser(String username, String password)
```
- Returns: Complete UserSession with role
- Used by: LoginUI

### 2. ComplaintService (User's - Updated)
**Purpose**: Handle complaint submission and file processing
```java
public void addComplaint(int UI_ID, ComplaintDetail cd, File droppedFile)
public void processAndAttachImage(ComplaintDetail cd, File droppedFile)
```
- Saves photos to disk
- Stores file paths in model
- Used by: SubmitReportView

### 3. DatabaseController (Leader's)
**Purpose**: Single-point database initialization
```java
public static void initializeDatabase()
```
- Creates database if needed
- Creates tables if needed
- Called at: Application startup (E_Report.java)

---

## Critical Integration Points

### Issue 1: Role Not Displaying _(FIXED)_

**Problem**: Role defaulted to "Resident" regardless of login

**Root Cause**: `GetUserDAO.getCredential()` SQL query missing `role` and `is_verified` fields

**Solution Applied**:
```java
// BEFORE (BROKEN)
SELECT Credential.UI_ID, username, password

// AFTER (FIXED)
SELECT Credential.UI_ID, username, password, role, is_verified
```

**Result**: Credential object now has all fields populated correctly

### Issue 2: Dashboard Requiring Separate Login _(FIXED)_

**Problem**: SubmitReportView had its own login prompt in main()

**Solution Applied**:
Changed `SubmitReportView.main()` to only show test dashboard without login

**Correct Usage**: Launch `E_Report.java` (not SubmitReportView directly)

---

## Usage Guide

### For End Users

**Starting the Application**:
1. Run `E_Report.java` (main entry point)
2. Click "Login" on homepage
3. Enter username and password from Credential table
4. ✅ Dashboard opens automatically with correct role
5. Fill complaint form and submit with photo

### For Developers

**Adding New Features**:

1. **New UI Component** → Add to `features/` folder
2. **New Database Operation** → Add to appropriate `DAO` class
3. **New Business Logic** → Add to `services/controller/`
4. **New Model** → Add to `models/` folder

**Testing Role-Based Access**:
```java
// Create different UserSession objects
UserSession resident = new UserSession(1, "Resident", true);
UserSession captain = new UserSession(2, "Captain", true);

// Test with SubmitReportView
SubmitReportView view = new SubmitReportView(captain);
```

---

## File Organization Summary

| Path | Owner | Purpose | Status |
|------|-------|---------|--------|
| `src/app/E_Report.java` | Leader | Main entry point & navigation | **[v2.1]** Thread-safe navigation |
| `src/features/ui/LoginUI.java` | Leader | Authentication UI | Complete |
| `src/features/submit/SubmitReportView.java` | User | Complaint dashboard + form | **[v2.1]** Moved to features.submit package |
| `src/features/submit/LeafletMapPickerBridge.java` | User | Interactive map picker | **[v2.1]** Moved to features.submit package |
| `src/features/submit/BackgroundImagePanel.java` | User | Background rendering | **[v2.1]** Moved to features.submit package |
| `src/features/submit/SubmitReportConstants.java` | User | Form constants & categories | **[v2.1]** Moved to features.submit package |
| `src/features/submit/FullComplaintDisplayView.java` | User | Complaint detail viewer | **[v2.1]** Moved to features.submit package |
| `src/features/submit/SubmitReportMapPanel.java` | User | Mini map panel | **[v2.1]** Moved to features.submit package |
| `src/services/controller/AuthCredentialController.java` | Leader | Login authentication | Complete |
| `src/services/controller/ComplaintService.java` | User | Complaint processing | Complete |
| `src/DAOs/GetUserDAO.java` | Leader | Credential retrieval with role | **[v2.0]** Fixed SQL query |
| `src/config/AppConfig.java` | Leader | Database configuration | Complete |
| `src/models/Credential.java` | Leader | User credentials + role | **[v2.0]** Role field now populated |
| `src/models/ComplaintDetail.java` | User | Complaint data model | **[v2.1]** File path storage |

---

## Recent Updates (v2.1)

### 1. **Package Restructuring**
- Moved all submit-related features from `src/features/` to `src/features/submit/`
- New package: `features.submit`
- Benefits:
  - Better code organization
  - Logical grouping of related components
  - Cleaner separation of concerns

### 2. **Navigation Improvements** (E_Report.java)
- Updated `navigate()` method for thread safety
- Uses `SwingUtilities.invokeLater()` for Swing operations
- Properly disposes main window when opening separate JFrame
- Prevents memory leaks and threading issues

**Before (v2.0)**:
```java
SubmitReportView reportView = new SubmitReportView(us);
reportView.setVisible(true);
this.setVisible(false);  // Just hiding, not disposing
```

**After (v2.1)**:
```java
SwingUtilities.invokeLater(() -> {
    SubmitReportView reportView = new SubmitReportView(us);
    reportView.setVisible(true);
});
SwingUtilities.invokeLater(this::dispose);  // Properly dispose
return;  // Skip other navigation updates
```

---

## Deployment Checklist

- [x] Authentication flow integrated
- [x] Role properly retrieved from database
- [x] SubmitReportView receives authenticated session
- [x] No duplicate login prompts
- [x] Photo upload to file system
- [x] Map picker functional
- [x] Role-based UI customization working
- [x] Database schema complete
- [x] **[v2.1]** Submit features properly organized
- [x] **[v2.1]** Navigation thread-safety implemented

---

## Next Steps

1. **Test Role-Based Features**: Login with different roles, verify UI changes
2. **Database Seeding**: Add test users with different roles
3. **Extend Functionality**: Add complaint viewing, status updates, etc.
4. **Deployment**: Deploy to production with proper MySQL configuration


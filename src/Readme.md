Database Setup & Usage Guide
This guide explains how to initialize and use the database for the application using the provided Java utility classes: DBConnection, DBCreate, and TBCreate.

1. DBConnection
   Purpose:
   DBConnection is responsible for establishing a connection to the database using the credentials and configuration defined in AppConfig.

2. DBCreate
   Purpose:
   DBCreate ensures the main database (e_report) exists. If it does not exist, it will automatically create it.

3. TBCreate
   Purpose:
   TBCreate handles the creation of all required tables in the e_report database.

Recommended Sequence for Setting Up the Database

1. Connection con = DBConnection.connect();
2. DBCreate.createDatabase();
3. TBCreate.createTables(con);

=========

AddUserDAO Usage Guide
This guide explains the process and requirements for adding new users and their corresponding credentials using the AddUserDAO class.

1. addUser
   Purpose:
   The addUser method is responsible for inserting personal details into the User_Info table and retrieving the unique identifier for the new record.

   Process:
   Prepares an INSERT statement for the User_Info table.
   Maps name, contact, and address data from the UserInfo object.
   Retrieves the auto-generated ID (Primary Key) created by the database.

   Parameters:
   Connection con: An active link to the database.
   UserInfo ui: The model object containing the user's personal details.

   Return Value:
   Returns the newly created User ID (int) if successful; returns -1 if the operation fails.

2. addCredential
   Purpose:
   The addCredential method links login information (username, password, and role) to a specific profile using the ID generated in the previous step.

   Process:

   Maps the userID to the UI_ID foreign key column.
   Inserts the account's security details and verification status.

   Parameters:
   Connection con: An active link to the database.
   int userID: The ID returned from the addUser method.
   Credential c: The model object containing login and role data.

   Return Value:
   Returns true if the credentials were saved successfully; false otherwise.

AddComplaintDAO Usage Guide
This guide explains the process and requirements for adding complaints, complaint history, and actions using the AddComplaintDAO class.

1. addComplaint

   Purpose:
   The addComplaint method is responsible for inserting a new complaint into the database and linking it to a user.

   Process:
   Prepares an INSERT statement for the Complaint_Detail table.
   Maps complaint information from the ComplaintDetail object (status, subject, type, location, etc.).
   Executes the insertion and retrieves the auto-generated CD_ID.
   Inserts a record in the Complaint table linking the complaint to the UI_ID of the user.

   Parameters:
   Connection con: An active link to the database.
   int userID: The ID of the user filing the complaint.
   ComplaintDetail cd: The model object containing complaint details.

   Return Value:
   Returns the generated CD_ID if successful; throws SQLException on failure.

2. addComplaintHistory

   Purpose:
   The addComplaintHistory method records updates or progress of a complaint and links it to the corresponding complaint.

   Process:
   Prepares an INSERT statement for the Complaint_History_Detail table.
   Maps status, process, update timestamp, and updatedBy from the ComplaintHistoryDetail object.
   Inserts a record in the Complaint_History table linking CHD_ID to the complaint.

   Parameters:
   Connection con: An active database connection.
   int complaintID: The CD_ID of the complaint to link the history to.
   ComplaintHistoryDetail chd: The model object containing history data.

   Return Value:
   Returns the generated CHD_ID if successful; throws SQLException on failure.

3. addComplaintAction

   Purpose:
   The addComplaintAction method logs the assigned actions, recommendations, or resolutions for a complaint.

   Process:
   Prepares an INSERT statement for the Complaint_Action table.
   Maps action details including actionTaken, recommendation, OIC, and resolutionDateTime from the ComplaintAction object.
   Executes the insertion to link the action to the complaint.

   Parameters:
   Connection con: An active database connection.
   int complaintID: The CD_ID of the complaint.
   ComplaintAction ca: The model object containing action details.

   Return Value:
   Returns true if the action was inserted successfully; throws SQLException on failure.

GetComplaintDAO Usage Guide
This guide explains how to retrieve complaint details, history, and actions using the GetComplaintDAO class.

1. getComplaint

   Purpose:
   Retrieve a single complaint filed by a specific user.

   Process:
   Executes a SELECT query joining Complaint and Complaint_Detail tables.
   Maps the result set to a ComplaintDetail object.

   Parameters:
   int UI_ID: User ID who filed the complaint.
   int CD_ID: Complaint ID to retrieve.

   Return Value:
   Returns a ComplaintDetail object if found; otherwise, returns null.

2. getAllComplaint

   Purpose:
   Retrieve all complaints filed by a specific user.

   Process:
   Executes a SELECT query joining Complaint and Complaint_Detail tables filtered by UI_ID.
   Maps each result row to a ComplaintDetail object.

   Parameters:
   int UI_ID: User ID to filter complaints.

   Return Value:
   Returns a List<ComplaintDetail> containing all complaints for the user.

3. getComplaintHistory

   Purpose:
   Retrieve all history updates for a specific complaint.

   Process:
   Executes a SELECT query joining Complaint_History and Complaint_History_Detail tables.
   Maps each result row to a ComplaintHistoryDetail object.

   Parameters:
   int CD_ID: Complaint ID to retrieve history for.

   Return Value:
   Returns a List<ComplaintHistoryDetail>; empty list if none found.

4. getComplaintAction

   Purpose:
   Retrieve action details linked to a specific complaint.

   Process:

   Executes a SELECT query on Complaint_Action table filtered by CD_ID.
   Maps the result row to a ComplaintAction object.

   Parameters:
   int CD_ID: Complaint ID to retrieve action for.

   Return Value:
   Returns a ComplaintAction object if found; otherwise, returns null.

GetUserDAO Usage Guide
This guide explains how to retrieve user information and credentials using the GetUserDAO class.

1. getUser

   Purpose:
   Retrieve the personal details of a user.

   Process:
   Executes a SELECT query on the User_Info table filtered by UI_ID.
   Maps the result set to a UserInfo object.

   Parameters:
   int UI_ID: The ID of the user to retrieve.

   Return Value:
   Returns a UserInfo object if found; otherwise, returns null.

2. getCredential

   Purpose:
   Retrieve login credentials for a user.

   Process:
   Executes a SELECT query on the Credential table joined with User_Info filtered by username and password.
   Maps the result set to a Credential object.

   Parameters:
   String username: Username to search for.
   String password: Password to match.

   Return Value:
   Returns a Credential object if the username and password match; otherwise, returns null.

User Registration & Credential
┌───────────────┐
│ AddUserDAO │
│ addUser() │
│ addCredential() │
└───────┬───────┘
│
▼
User Retrieval
┌───────────────┐
│ GetUserDAO │
│ getUser() │
│ getCredential() │
└───────┬───────┘
│
▼
Complaint Handling
┌───────────────┐
│ AddComplaintDAO│
│ addComplaint()│
│ addComplaintHistory() │
│ addComplaintAction() │
└───────┬──────────────┘
│
▼
Complaint Retrieval
┌───────────────┐
│ GetComplaintDAO│
│ getComplaint()│
│ getAllComplaint() │
│ getComplaintHistory() │
│ getComplaintAction() │
└────────────────┘

=========

Data Flow
User Input
↓
Middleware (auth, validation, logging)
↓
Controller (handles request/response) WHAT to do
e.g. login(username, password)
↓
Service (business logics) HOW to do it
↓
DAO (database access only) WHERE to get data
e.g.
SELECT user FROM database
↓
Database

=========

Terminologies and functions

assets: Static files (images, audio, videos, etc.) belongs here

models: Represents data structure Represents data structure (matches database tables)

controllers: Handles input and output (such as form or displaying) of data

services: Contains business logic, validation, and rules. Calls DAO.

DAO: Data Access Object acts as bridge from service to database

Database: IKYK this.

=========

Case Types

CASES UNDER KATARUNGANG PAMBARANGAY
X UNLAWFUL USE OF MEANS OF PUBLICATION AND UNLAWFUL
UTTERANCES (ART. 154);
X ALARMS AND SCANDALS (ART. 155);
X USING FALSE CERTIFICATES (ART. 175);
X USING FICTITIOUS NAMES AND CONCEALING TRUE NAMES
(ART. 178);
X ILLEGAL USE OF UNIFORMS AND INSIGNIAS (ART. 179);
X PHYSICAL INJURIES INFLICTED IN A TUMULTUOUS AFFRAY
(ART. 252);
X GIVING ASSISTANCE TO CONSUMMATED SUICIDE (ART. 253);
X RESPONSIBILITY OF PARTICIPANTS IN A DUEL IF ONLY
PHYSICAL INJURIES ARE INFLICTED OR NO PHYSICAL INJURIES
HAVE BEEN INFLICTED (ART. 260);
X LESS SERIOUS PHYSICAL INJURIES (ART. 265);
X SLIGHT PHYSICAL INJURIES AND MALTREATMENT (ART. 266);
X UNLAWFUL ARREST (ART. 269);
X INDUCING A MINOR TO ABANDON HIS/HER HOME (ART.
271);
X ABANDONMENT OF A PERSON IN DANGER AND
ABANDONMENT OF ONE’S OWN VICTIM (ART. 275);
X ABANDONING A MINOR (A CHILD UNDER SEVEN [7] YEARS
OLD) (ART. 276);
X ABANDONMENT OF A MINOR BY PERONS ENTRUSTED WITH
HIS/HER CUSTODY; INDIFFERENCE OF PARENTS (ART. 277);
X QUALIFIED TRESSPASS TO DWELLING (WITHOUT THE USE OF
VIOLENCE AND INTIMIDATION). (ART. 280);
X OTHER FORMS OF TRESSPASS (ART. 281);
X LIGHT THREATS (ART. 283);
X OTHER LIGHT THREATS (ART. 285);
X GRAVE COERCION (ART. 286);
X LIGHT COERCION (ART. 287);
X OTHER SIMILAR COERCIONS (COMPULSORY PURCHASE OF
MERCHANDISE AND PAYMENT OF WAGES BY MEANS OF
TOKENS). (ART. 288); 23. FORMATION, MAINTENANCE AND PROHIBITION OF
COMBINATION OF CAPITAL OR LABOR THROUGH VIOLENCE
OR THREATS (ART. 289);
KATARUNGANG PAMBARANGAY 23
X DISCOVERING SECRETS THROUGH SEIZURE AND
CORRESPONDENCE (ART. 290);
X REVEALING SECRETS WITH ABUSE OF AUTHORITY (ART.
291);
X THEFT (IF THE VALUE OF THE PROPERTY STOLEN DOES NOT
EXCEED P50.00). (ART. 309);
X QUALIFIED THEFT (IF THE AMOUNT DOES NOT EXCEED
P500). (ART. 310);
X OCCUPATION OF REAL PROPERTY OR USURPATION OF REAL
RIGHTS IN PROPERTY (ART 312);
X ALTERING BOUNDARIES OR LANDMARKS (ART. 313);
X SWINDLING OR ESTAFA (IF THE AMOUNT DOES NOT
EXCEED P200.00). (ART. 315);
X OTHER FORMS OF SWINDLING (ART. 316);
X SWINDLING A MINOR (ART. 317);
X OTHER DECEITS (ART. 318);
X REMOVAL, SALE OR PLEDGE OF MORTGAGED PROPERTY
(ART. 319);
X SPECIAL CASES OF MALICIOUS MISCHIEF (IF THE VALUE OF
THE DAMAGED PROPERTY DOES NOT EXCEED P1,000.00).
(ART 328);
X OTHER MISCHIEFS (IF THE VALUE OF THE DAMAGED
PROPERTY DOES NOT EXCEED P1,000.00). (ART. 329);
X SIMPLE SEDUCTION (ART. 338);
X ACTS OF LASCIVIOUSNESS WITH THE CONSENT OF THE
OFFENDED PARTY (ART 339);
X THREATENING TO PUBLISH AND OFFER TO PREVENT SUCH
PUBLICATION FOR COMPENSATION (ART. 356);
X PROHIBITING PUBLICATION OF ACTS REFERRED TO IN THE
COURSE OF OFFICIAL PROCEEDINGS (ART. 357);
X INCRIMINATING INNOCENT PERSONS (ART. 363);
X INTRIGUING AGAINST HONOR (ART. 364);
X ISSUING CHECKS WITHOUT SUFFICIENT FUNDS (BP 22);
X FENCING OF STOLEN PROPERTIES IF THE PROPERTY
INVOLVED IS NOT MORE THAN P50.00 (PD 1612).

Katarungan Pambarangay Reference
https://region5.dilg.gov.ph/attachments/article/182/Related_Laws_Katarungang_Pambarangay_Handbook.pdf#:~:text=Republic%20Act%207160%20or%20the%20Local%20Government,alongside%20its%20mandate%20to%20deliver%20basic%20services.

Other reference (from tarlac)
https://www.scribd.com/document/393046804/Accepted-Cases-Under-Katarungang-Pambarangay

ART (Article of The Revised Penal Code)
PD (Presidential Decree)

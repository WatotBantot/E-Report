package config;

public class AppConfig {
    // Centralized Database Credentials
    public static final String DB_URL = "jdbc:mysql://localhost:3306/e_report";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "";

    // Database Driver
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Connection Timeout (in seconds)
    public static final int DB_TIMEOUT = 5;

    // Database Creation Query
    public static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS e_report;";

    // Table Creation Queries
    public static final String CREATE_USER_INFO_TABLE = """
            CREATE TABLE IF NOT EXISTS User_Info(
                UI_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                first_name VARCHAR(50) NOT NULL,
                middle_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                sex VARCHAR(10) NOT NULL,
                contact_number VARCHAR(11) UNIQUE NOT NULL,
                email_address VARCHAR(50) UNIQUE NOT NULL,
                house_number TINYINT NOT NULL,
                street VARCHAR(50) NOT NULL,
                purok VARCHAR(50) NOT NULL
            );
            """;

    public static final String CREATE_CREDENTIAL_TABLE = """
            CREATE TABLE IF NOT EXISTS Credential(
                Cred_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                UI_ID INT NOT NULL,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(50) NOT NULL,
                role VARCHAR(20) NOT NULL,
                is_verified BOOLEAN NOT NULL,
                date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                FOREIGN KEY (UI_ID) REFERENCES User_Info(UI_ID)
            );
            """;

    public static final String CREATE_COMPLAINT_DETAIL_TABLE = """
            CREATE TABLE IF NOT EXISTS Complaint_Detail(
                CD_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                current_status VARCHAR(20) NOT NULL,
                subject VARCHAR(50) NOT NULL,
                type VARCHAR(50) NOT NULL,
                date_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                street VARCHAR(50) NOT NULL,
                purok VARCHAR(50) NOT NULL,
                longitude DECIMAL(11,8) NOT NULL,
                latitude DECIMAL(10,8) NOT NULL,
                persons_involved TEXT NOT NULL,
                details TEXT NOT NULL,
                photo_attachment TEXT NOT NULL,
                date_time_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
            );
            """;

    public static final String CREATE_COMPLAINT_TABLE = """
            CREATE TABLE IF NOT EXISTS Complaint(
                C_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                CD_ID INT NOT NULL,
                UI_ID INT NOT NULL,
                FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID),
                FOREIGN KEY (UI_ID) REFERENCES User_Info(UI_ID)
            );
            """;

    public static final String CREATE_COMPLAINT_ACTION_TABLE = """
            CREATE TABLE IF NOT EXISTS Complaint_Action(
                CA_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                CD_ID INT NOT NULL,
                action_taken TEXT NOT NULL,
                recommendation TEXT NOT NULL,
                oic VARCHAR(50) NOT NULL,
                date_time_assigned TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                resolution_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID)
            );
            """;

    public static final String CREATE_COMPLAINT_HISTORY_DETAIL_TABLE = """
            CREATE TABLE IF NOT EXISTS Complaint_History_Detail(
                CHD_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                status VARCHAR(50) NOT NULL,
                process TEXT NOT NULL,
                date_time_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated_by VARCHAR(100) NOT NULL
            );
            """;

    public static final String CREATE_COMPLAINT_HISTORY_TABLE = """
            CREATE TABLE IF NOT EXISTS Complaint_History(
                CH_ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                CD_ID INT NOT NULL,
                CHD_ID INT NOT NULL,
                FOREIGN KEY (CD_ID) REFERENCES Complaint_Detail(CD_ID),
                FOREIGN KEY (CHD_ID) REFERENCES Complaint_History_Detail(CHD_ID)
            );
            """;

    // DB table names
    public static final String TABLE_USER_INFO = "User_Info";
    public static final String TABLE_CREDENTIAL = "Credential";
    public static final String TABLE_COMPLAINT_DETAIL = "Complaint_Detail";
    public static final String TABLE_COMPLAINT = "Complaint";
    public static final String TABLE_COMPLAINT_HISTORY_DETAIL = "Complaint_History_Detail";
    public static final String TABLE_COMPLAINT_HISTORY = "Complaint_History";
    public static final String TABLE_COMPLAINT_ACTION = "Complaint_Action";
}
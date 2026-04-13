package services.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import DAOs.AddComplaintDAO;
import config.DBConnection;
import models.ComplaintDetail;

/**
 * ComplaintService
 * 
 * This service class handles operations related to complaints submitted by
 * users.
 * It provides functionality to add a new complaint and process attached images.
 * 
 * Responsibilities:
 * - Add complaints for a user and persist them to the database.
 * - Process and attach image files (as BLOBs) to a ComplaintDetail object.
 * 
 * Usage:
 * - Call addComplaint() to submit a complaint along with an optional image
 * file.
 * - Internally, addComplaint() uses AddComplaintDAO to perform database
 * operations.
 * 
 * Note:
 * - Exceptions during image processing are non-critical; complaint submission
 * will continue.
 * - Database connections are automatically closed in the finally block to
 * prevent resource leaks.
 */
public class ComplaintServiceController {

    /**
     * Adds a new complaint for a given user.
     * 
     * This method is intended for frontend use and allows attaching a file (e.g.,
     * photo)
     * along with the complaint details. If a file is provided, it will be processed
     * and attached to the ComplaintDetail object.
     * 
     * Process:
     * 1. Check if a file is provided; if so, process and attach it.
     * 2. Open a database connection.
     * 3. Call AddComplaintDAO.addComplaint() to persist the complaint to the
     * database.
     * 4. Handle exceptions gracefully and ensure the DB connection is closed.
     * 
     * @param UI_ID       The user ID filing the complaint.
     * @param cd          The ComplaintDetail object containing complaint data.
     * @param droppedFile Optional file to attach (e.g., an image); can be null.
     */
    public void addComplaint(int UI_ID, ComplaintDetail cd, File droppedFile) {
        Connection con = null;

        try {
            // Step 1: Process image file if provided
            if (droppedFile != null) {
                try {
                    processAndAttachImage(cd, droppedFile);
                } catch (Exception e) {
                    // Non-critical error, continue with complaint submission
                    System.err.println(
                            "Non-critical Error: Image failed to save, continuing with complaint submission.");
                    e.printStackTrace();
                }
            }

            // Step 2: Establish DB connection
            con = DBConnection.connect();

            // Step 3: Persist complaint using DAO
            AddComplaintDAO.addComplaint(con, UI_ID, cd);
            System.out.println("Complaint successfully saved!");

        } catch (Exception e) {
            // Catch unexpected exceptions
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Step 4: Ensure DB connection is closed to prevent leaks
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    System.err.println("Warning: Failed to close the database connection.");
                }
            }
        }
    }

    /**
     * Processes and attaches an image file to the ComplaintDetail object.
     * 
     * This method reads the file into a byte array and assigns it to a BLOB field
     * in the ComplaintDetail object. This allows the image to be stored directly
     * in the database.
     * 
     * @param cd          The ComplaintDetail object to attach the image to.
     * @param droppedFile The image file to read and attach.
     */
    public void processAndAttachImage(ComplaintDetail cd, File droppedFile) {
        try {
            // Convert the file into a byte array
            byte[] fileBytes = Files.readAllBytes(droppedFile.toPath());

            // Attach the byte array to the ComplaintDetail model
            cd.setPhotoAttachmentBytes(fileBytes); // Ensure this field exists in the model

            System.out.println("Image successfully read and attached as BLOB: " + droppedFile.getName());

        } catch (IOException e) {
            // File read failure
            System.err.println("Failed to read the image file!");
            e.printStackTrace();
        }
    }
}
package services.controller;

import DAOs.AddComplaintDAO;
import config.DBConnection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
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
     * Reads the file and saves it to the images directory, then stores the file path
     * in the ComplaintDetail object for database storage.
     * 
     * @param cd          The ComplaintDetail object to attach the image to.
     * @param droppedFile The image file to process and attach.
     */
    public void processAndAttachImage(ComplaintDetail cd, File droppedFile) {
        try {
            // Save uploads under the local project images folder.
            String directory = System.getProperty("user.dir") + File.separator + "images" + File.separator;
            String newFileName = System.currentTimeMillis() + "_" + droppedFile.getName();
            File destination = new File(directory + newFileName);

            // Ensure the directory physically exists on the computer
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs(); // Creates the folder if it's missing
            }

            // Copy the file to the images directory
            Files.copy(droppedFile.toPath(), destination.toPath());

            // Save the file path into the model
            cd.setPhotoAttachment(destination.getAbsolutePath());

            System.out.println("Image successfully attached and saved to: " + destination.getAbsolutePath());

        } catch (IOException e) {
            // File read/write failure
            System.err.println("Failed to process the image file!");
            e.printStackTrace();
        }
    }
}
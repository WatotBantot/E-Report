package services.controller;

import DAOs.AddComplaintDAO;
import config.DBConnection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import models.ComplaintDetail;

public class ComplaintService {

    /**
     * Add Complaint for Frontend Use
     * 
     * @params int UI_ID user Id, ComplaintDetail cd complaint, File droppedFile for
     *         files
     * @return none
     */

    public void addComplaint(int UI_ID, ComplaintDetail cd, File droppedFile) {
        Connection con = null;

        try {
            if (droppedFile != null) {
                try {
                    processAndAttachImage(cd, droppedFile);
                } catch (Exception e) {
                    System.err
                            .println("Non-critical Error: Image failed to save, continuing with complaint submission.");
                    e.printStackTrace();
                }
            }

            con = DBConnection.connect();
            AddComplaintDAO.addComplaint(con, UI_ID, cd);
            System.out.println("Complaint successfully saved!");

        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
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
     * Process the image and save it into specified directory
     * 
     * @params ComplaintDetail cd complaint, File droppedFile for
     *         files
     * @return none
     */

    public void processAndAttachImage(ComplaintDetail cd, File droppedFile) {
        try {
            // Save uploads under the local project images folder.
            String directory = System.getProperty("user.dir") + File.separator + "images" + File.separator;
            String newFileName = System.currentTimeMillis() + "_" + droppedFile.getName();
            File destination = new File(directory + newFileName);

            // 2. Ensure the directory physically exists on the computer
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs(); // Creates the folder if it's missing
            }

            // 3. Physically copy the file to your hard drive
            Files.copy(droppedFile.toPath(), destination.toPath());

            // 4. Save the string PATH into your model
            cd.setPhotoAttachment(destination.getAbsolutePath());

            System.out.println("Image successfully attached and saved to: " + destination.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Failed to save the dropped image!");
            e.printStackTrace();
        }
    }
}

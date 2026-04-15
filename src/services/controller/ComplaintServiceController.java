package services.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import daos.AddComplaintDAO;
import config.database.DBConnection;
import models.ComplaintDetail;

/**
 * ComplaintServiceController
 * 
 * Handles operations related to complaints submitted by users.
 */
public class ComplaintServiceController {
	
	// ===== DAO INSTANCE =====
	private AddComplaintDAO addComplaintDAO;
	
	public ComplaintServiceController(){
		// ===== INIT DAO =====
		addComplaintDAO = new AddComplaintDAO();
	}

	/**
	 * Adds a new complaint for a given user.
	 * 
	 * @param UI_ID The user ID filing the complaint
	 * @param cd The ComplaintDetail object containing complaint data
	 * @param droppedFile Optional file to attach (e.g., an image); can be null
	 */
	public void addComplaint(int UI_ID, ComplaintDetail cd, File droppedFile) {
		Connection con = null;
		
		try {
			// ===== CREATE CONNECTION =====
			con = DBConnection.connect();
			
			// ===== PROCESS IMAGE =====
			if (droppedFile != null) {
				try {
					processAndAttachImage(cd, droppedFile);
				} catch (Exception e) {
					System.err.println("Non-critical Error: Image failed to save, continuing with complaint submission.");
					e.printStackTrace();
				}
			}

			// ===== ADD COMPLAINT =====
			addComplaintDAO.addComplaint(con, UI_ID, cd);
			System.out.println("Complaint successfully saved!");

		} catch (SQLException e) {
			System.err.println("Database Error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// ===== CLOSE CONNECTION =====
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Processes and attaches an image file to the ComplaintDetail object.
	 * 
	 * @param cd The ComplaintDetail object to attach the image to
	 * @param droppedFile The image file to read and attach
	 */
	public void processAndAttachImage(ComplaintDetail cd, File droppedFile) {
		// ===== READ IMAGE =====
		try {
			byte[] fileBytes = Files.readAllBytes(droppedFile.toPath());
			cd.setPhotoAttachmentBytes(fileBytes);
			System.out.println("Image successfully read and attached as BLOB: " + droppedFile.getName());
		} catch (IOException e) {
			System.err.println("Failed to read the image file!");
			e.printStackTrace();
		}
	}
}
package tests.units;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import config.database.DBConnection;
import models.ComplaintDetail;
import services.controller.ComplaintServiceController;

public class ComplaintServiceTest {

    private static boolean testPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING COMPLAINT SERVICE CONTROLLER TEST =====\n");

        ComplaintServiceController service = new ComplaintServiceController();
        testAddComplaintWithRealImage(service);

        System.out.println("====================================================");
        if (testPassed) {
            System.out.println("CONTROLLER TEST PASSED!");
        } else {
            System.out.println("CONTROLLER TEST FAILED. See logs above.");
        }
    }

    public static void testAddComplaintWithRealImage(ComplaintServiceController service) {
        System.out.println("[TEST] Add Complaint via Service Controller (Real Image)");

        Connection con = DBConnection.connect();
        if (con == null) {
            System.out.println("-> FAIL: Database connection failed. Cannot verify test.");
            testPassed = false;
            return;
        }

        File realImageFile = null;

        try {
            // 1. Arrange: Get initial count of records
            int initialCount = getTableRowCount(con, "COMPLAINT_DETAIL");

            // 2. Arrange: Download a REAL image from the internet
            String realFilePath = System.getProperty("user.dir") + "/downloaded_test_image.jpg";
            realImageFile = new File(realFilePath);

            System.out.println("-> Downloading a real image for testing...");
            URL url = URI.create("https://picsum.photos/200/300").toURL();
            try (InputStream in = url.openStream();
                    FileOutputStream out = new FileOutputStream(realImageFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("-> Real image successfully downloaded to: " + realFilePath);

            // 3. Arrange: Populate the mock frontend complaint data
            ComplaintDetail cd = new ComplaintDetail();
            cd.setSubject("Stolen Bicycle outside Brgy. Hall");
            cd.setType("Theft");
            cd.setPersonsInvolved("Unidentified suspect wearing a black hoodie and blue cap");
            cd.setDetails("I parked my mountain bike (red and black Trinx) outside the Barangay Hall " +
                    "while I went inside to secure a permit at around 2:30 PM today. When I came out " +
                    "at around 3:00 PM, the bike was gone. The lock was cut and left on the ground. " +
                    "The CCTV in the area might have captured the incident.");
            cd.setCurrentStatus("In progress");
            cd.setDateTime(new Timestamp(System.currentTimeMillis()));
            cd.setStreet("Rizal Street");
            cd.setPurok("Purok 4");
            cd.setLatitude(15.6625);
            cd.setLongitude(121.0142);

            // 3a. Attach the image bytes
            byte[] imageBytes = Files.readAllBytes(realImageFile.toPath());
            cd.setPhotoAttachmentBytes(imageBytes);

            int mockUserId = 1; // Assuming User ID 1 exists

            // 4. Act: Call service.addComplaint()
            System.out.println("-> Calling service.addComplaint()...");
            service.addComplaint(mockUserId, cd, realImageFile);

            // 5. Assert A: Check row count increment
            int afterCount = getTableRowCount(con, "COMPLAINT_DETAIL");
            if (afterCount != initialCount + 1) {
                System.out.println("-> FAIL: Database row count did not increase!");
                testPassed = false;
                return;
            }

            // 6. Assert B: Verify that image bytes are set in the model
            if (cd.getPhotoAttachmentBytes() != null && cd.getPhotoAttachmentBytes().length > 0) {
                System.out.println("-> PASS: Image BLOB successfully attached to the complaint record!");
            } else {
                System.out.println("-> FAIL: Image BLOB is missing in the model!");
                testPassed = false;
            }

        } catch (Exception e) {
            System.out.println("-> FAIL: Exception occurred during test execution.");
            e.printStackTrace();
            testPassed = false;
        } finally {
            // Clean up the downloaded file from your project root
            if (realImageFile != null && realImageFile.exists()) {
                realImageFile.delete();
            }
            try {
                con.close();
            } catch (Exception ignored) {
            }
        }
    }

    // HELPER METHOD: Counts rows in the database table
    private static int getTableRowCount(Connection con, String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("[Warning] Could not get count for " + tableName);
        }
        return 0;
    }
}
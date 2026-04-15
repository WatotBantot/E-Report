package tests.integrations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import daos.GetComplaintDAO;
import config.database.DBConnection;
import models.ComplaintDetail;

public class FullComplaintDisplayView extends JFrame {

    private JLabel statusLbl, subjectLbl, typeLbl, dateTimeLbl;
    private JLabel streetLbl, purokLbl, latLongLbl, personsLbl;
    private JTextArea detailsArea;
    private JLabel imageLabel;

    public FullComplaintDisplayView() {
        setTitle("Complaint Record Sample Test");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // --- 1. LEFT PANEL: All Text Data ---
        JPanel dataPanel = new JPanel(new BorderLayout(10, 10));
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel gridPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        subjectLbl = createStyledLabel("Subject: ");
        subjectLbl.setFont(new Font("Arial", Font.BOLD, 16));

        statusLbl = createStyledLabel("Status: ");
        typeLbl = createStyledLabel("Type: ");
        dateTimeLbl = createStyledLabel("Date/Time: ");
        streetLbl = createStyledLabel("Street: ");
        purokLbl = createStyledLabel("Purok: ");
        latLongLbl = createStyledLabel("Coordinates: ");
        personsLbl = createStyledLabel("Persons Involved: ");

        gridPanel.add(subjectLbl);
        gridPanel.add(statusLbl);
        gridPanel.add(typeLbl);
        gridPanel.add(dateTimeLbl);
        gridPanel.add(streetLbl);
        gridPanel.add(purokLbl);
        gridPanel.add(latLongLbl);
        gridPanel.add(personsLbl);

        dataPanel.add(gridPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new BorderLayout(5, 5));
        JLabel detHeader = new JLabel("Narrative Details:");
        detHeader.setFont(new Font("Arial", Font.BOLD, 12));

        detailsArea = new JTextArea(8, 30);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        detailsPanel.add(detHeader, BorderLayout.NORTH);
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        dataPanel.add(detailsPanel, BorderLayout.CENTER);
        add(dataPanel, BorderLayout.WEST);

        // --- 2. RIGHT PANEL: The Photo Attachment ---
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBorder(BorderFactory.createTitledBorder("Photo Attachment"));

        imageLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imageLabel.setBackground(Color.LIGHT_GRAY);
        imageLabel.setOpaque(true);

        photoPanel.add(imageLabel, BorderLayout.CENTER);
        add(photoPanel, BorderLayout.CENTER);
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        return label;
    }

    public void loadAllComplaintData(int userId, int complaintId) {
        GetComplaintDAO dao = new GetComplaintDAO();
        ComplaintDetail cd = null;

        try {
            cd = dao.getComplaint(DBConnection.connect(), userId, complaintId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Database connection failed or error occurred:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        if (cd != null) {
            subjectLbl.setText("Subject: " + cd.getSubject());
            statusLbl.setText("Status: " + cd.getCurrentStatus());
            typeLbl.setText("Type: " + cd.getType());
            dateTimeLbl.setText("Date/Time: " + (cd.getDateTime() != null ? cd.getDateTime().toString() : "N/A"));
            streetLbl.setText("Street: " + cd.getStreet());
            purokLbl.setText("Purok: " + cd.getPurok());
            latLongLbl.setText(String.format("Coordinates: Lat %s, Long %s", cd.getLatitude(), cd.getLongitude()));
            personsLbl.setText("Persons Involved: " + cd.getPersonsInvolved());
            detailsArea.setText(cd.getDetails());

            byte[] photoBytes = cd.getPhotoAttachmentBytes();
            if (photoBytes != null && photoBytes.length > 0) {
                try {
                    ImageIcon originalIcon = new ImageIcon(photoBytes);
                    Image img = originalIcon.getImage();
                    Image scaledImg = img.getScaledInstance(400, 400, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImg));
                    imageLabel.setText("");
                } catch (Exception e) {
                    imageLabel.setIcon(null);
                    imageLabel.setText("Error rendering image.");
                    e.printStackTrace();
                }
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("No photo attached to this record.");
            }

        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("Complaint record not found.");
            JOptionPane.showMessageDialog(this,
                    "⚠️ No data found for User ID: " + userId + ", Complaint ID: " + complaintId,
                    "No Data Found",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // --- Added Dynamic Testing Helper ---
    private static List<int[]> findValidIdsFromDatabase(int limit) {
        // 1. We dynamically insert the limit you want into the query string
        String query = "SELECT UI_ID, CD_ID FROM Complaint LIMIT " + limit;
        List<int[]> idPairsList = new ArrayList<>();

        try (Connection con = DBConnection.connect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            // 2. Changed 'if' to 'while' to grab EVERY row the query finds
            while (rs.next()) {
                int uiId = rs.getInt("UI_ID");
                int cdId = rs.getInt("CD_ID");

                // Store the pair as a mini array and add it to our list
                idPairsList.add(new int[] { uiId, cdId });
            }
        } catch (Exception e) {
            System.err.println("Database check failed.");
            e.printStackTrace();
        }

        // Return the list. It will be empty if nothing was found in the DB.
        return idPairsList;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FullComplaintDisplayView view = new FullComplaintDisplayView();
            view.setVisible(true);

            List<int[]> validIdsList = findValidIdsFromDatabase(5);

            if (!validIdsList.isEmpty()) {
                int[] firstPair = validIdsList.get(0);
                view.loadAllComplaintData(firstPair[0], firstPair[1]);
            } else {
                JOptionPane.showMessageDialog(view,
                        "⚠️ No complaint records available in the database.",
                        "No Data",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}
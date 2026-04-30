package features.views;

import app.E_Report;
import config.AppConfig;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.submit.SubmitReportConstants;
import features.submit.SubmitReportMapPanel;
import features.ui.DashboardFormUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.sql.Timestamp;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import models.ComplaintDetail;
import models.MapPinOutOfServiceAreaException;
import models.MissingReportFieldException;
import models.ReportSubmissionException;
import services.controller.ComplaintServiceController;

public class SubmitReportView extends JPanel {
    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private final JComboBox<String> categoryCombo;
    private final JComboBox<String> purokCombo;
    private final JTextField locationField;
    private final JTextField latitudeField;
    private final JTextField longitudeField;
    private final JTextArea detailsArea;
    private final JLabel selectedPhotoLabel;
    private final JLabel mapStatusLabel;
    private JButton submitBtn;
    private final SubmitReportMapPanel mapPanel;
    private File selectedFile;
    private boolean pinConfirmed;

    public SubmitReportView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        categoryCombo = DashboardFormUtils.createComboWithPlaceholder(
                AppConfig.COMPLAINT_TYPES,
                AppConfig.REPORT_CATEGORY_PLACEHOLDER);
        purokCombo = DashboardFormUtils.createComboWithPlaceholder(
                AppConfig.REPORT_PUROK_OPTIONS,
                AppConfig.REPORT_PUROK_PLACEHOLDER);
        locationField = new JTextField();
        latitudeField = new JTextField();
        longitudeField = new JTextField();
        detailsArea = new JTextArea(6, 22);
        selectedPhotoLabel = new JLabel("No uploaded photo");
        mapStatusLabel = new JLabel("Click on the map to pin location.");

        latitudeField.setEditable(false);
        longitudeField.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        DashboardFormUtils.installPlaceholder(locationField, AppConfig.REPORT_LOCATION_PLACEHOLDER);

        mapPanel = new SubmitReportMapPanel(new SubmitReportMapPanel.Listener() {
            @Override
            public void onPinned(double latitude, double longitude) {
                latitudeField.setText(String.format("%.6f", latitude));
                longitudeField.setText(String.format("%.6f", longitude));
            }

            @Override
            public void onStatusChanged(String statusText) {
                mapStatusLabel.setText(statusText);
            }

            @Override
            public void onAddressResolved(String addressText) {
                if (addressText != null && !addressText.isBlank()) {
                    locationField.setForeground(Color.BLACK);
                    locationField.setText(addressText);
                }
            }
        });

        add(createMainPanel(), BorderLayout.CENTER);
        initFieldListeners();
        updateSubmitButtonState();
    }

    private JPanel createMainPanel() {
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();
        setNavMenus();

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(createContentPanel(), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(bgPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void setNavMenus() {
        String role = app.getUserSession().getRole();
        if (role.equalsIgnoreCase(AppConfig.ROLE_CAPTAIN)) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (role.equalsIgnoreCase(AppConfig.ROLE_SECRETARY)) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else {
            nav.setResidentMenus(route -> app.navigate(route));
        }
    }

    private JPanel createContentPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.45;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.add(createPageHeader());
        leftColumn.add(Box.createVerticalStrut(18));
        leftColumn.add(DashboardFormUtils.createLabeledField("Category", categoryCombo));
        leftColumn.add(Box.createVerticalStrut(12));
        leftColumn.add(DashboardFormUtils.createLabeledField("Purok", purokCombo));
        leftColumn.add(Box.createVerticalStrut(12));
        leftColumn.add(createLocationField());
        leftColumn.add(Box.createVerticalStrut(12));
        leftColumn.add(createDescriptionField());
        leftColumn.add(Box.createVerticalStrut(12));
        leftColumn.add(createPhotoUploadRow());
        leftColumn.add(Box.createVerticalStrut(18));
        leftColumn.add(createActionButtons());

        card.add(leftColumn, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        JPanel rightColumn = createMapSection();
        card.add(rightColumn, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private Component createPageHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Submit New Report");
        title.setFont(UIConfig.H2);
        title.setForeground(UIConfig.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createLocationField() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(locationField, BorderLayout.CENTER);
        return DashboardFormUtils.createLabeledField("Location", container);
    }

    private JPanel createDescriptionField() {
        JTextArea descArea = detailsArea;
        descArea.setPreferredSize(new Dimension(0, 120));
        JScrollPane scrollPane = new JScrollPane(descArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190), 1, true));
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);
        JLabel label = new JLabel("Description");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPhotoUploadRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        selectedPhotoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        selectedPhotoLabel.setPreferredSize(new Dimension(180, 36));
        selectedPhotoLabel.setMinimumSize(new Dimension(180, 36));
        selectedPhotoLabel.setMaximumSize(new Dimension(180, 36));
        selectedPhotoLabel.setHorizontalAlignment(JLabel.LEFT);

        JButton uploadBtn = createSecondaryButton("Upload");
        uploadBtn.setPreferredSize(new Dimension(150, 36));
        uploadBtn.setMinimumSize(new Dimension(150, 36));
        uploadBtn.setMaximumSize(new Dimension(150, 36));
        uploadBtn.addActionListener(e -> choosePhoto());

        row.add(selectedPhotoLabel);
        row.add(uploadBtn);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return row;
    }

    private JPanel createActionButtons() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JButton cancelBtn = createCompactSecondaryButton("Cancel");
        submitBtn = createCompactPrimaryButton("Submit");
        submitBtn.setEnabled(false);
        cancelBtn.addActionListener(e -> clearForm());
        submitBtn.addActionListener(e -> submitComplaint());
        actions.add(cancelBtn);
        actions.add(submitBtn);
        return actions;
    }

    private JPanel createMapSection() {
        JPanel mapSection = new JPanel(new BorderLayout(0, 12));
        mapSection.setOpaque(false);

        JLabel sectionTitle = new JLabel("Map Location");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mapSection.add(sectionTitle, BorderLayout.NORTH);

        JPanel mapWrapper = new JPanel(new BorderLayout(0, 8));
        mapWrapper.setOpaque(false);
        mapPanel.setPreferredSize(new Dimension(0, 320));
        mapWrapper.add(mapPanel, BorderLayout.CENTER);

        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        controlsRow.setOpaque(false);

        JButton confirmPinBtn = createCompactSecondaryButton("Confirm Pin");
        JButton resetPinBtn = createCompactSecondaryButton("Reset Pin");
        confirmPinBtn.addActionListener(e -> confirmMapPin());
        resetPinBtn.addActionListener(e -> {
            mapPanel.resetView();
            latitudeField.setText("");
            longitudeField.setText("");
            pinConfirmed = false;
            updateSubmitButtonState();
            mapStatusLabel.setText("Map reset. Click again to pin a new location.");
        });
        controlsRow.add(confirmPinBtn);
        controlsRow.add(resetPinBtn);

        mapWrapper.add(controlsRow, BorderLayout.SOUTH);
        mapSection.add(mapWrapper, BorderLayout.CENTER);

        JPanel instructions = new JPanel(new BorderLayout(0, 10));
        instructions.setOpaque(false);
        instructions.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        mapStatusLabel.setForeground(UIConfig.TEXT_SECONDARY);
        instructions.add(mapStatusLabel, BorderLayout.NORTH);

        JTextArea hint = new JTextArea(
                "Click the map to drop your pin. Pins outside the service area are not allowed.");
        hint.setWrapStyleWord(true);
        hint.setLineWrap(true);
        hint.setEditable(false);
        hint.setOpaque(false);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(UIConfig.TEXT_SECONDARY);
        hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        instructions.add(hint, BorderLayout.CENTER);

        mapSection.add(instructions, BorderLayout.SOUTH);
        return mapSection;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(UIConfig.BTN_PRIMARY);
        button.setFont(UIConfig.BTN_PRIMARY_FONT);
        button.setBackground(UIConfig.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 1, true),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(UIConfig.BTN_SECONDARY);
        button.setFont(UIConfig.BTN_SECONDARY_FONT);
        button.setBackground(UIConfig.SECONDARY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        return button;
    }

    private JButton createCompactPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(UIConfig.BTN_SMALL);
        button.setFont(UIConfig.BTN_SMALL_FONT);
        button.setBackground(UIConfig.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        return button;
    }

    private JButton createCompactSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(UIConfig.BTN_SMALL);
        button.setFont(UIConfig.BTN_SMALL_FONT);
        button.setBackground(UIConfig.SECONDARY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        return button;
    }

    private void choosePhoto() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        int choice = chooser.showOpenDialog(this);
        if (choice == javax.swing.JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            selectedPhotoLabel.setText(truncateFileName(selectedFile.getName(), 18));
            updateSubmitButtonState();
        }
    }

    private void submitComplaint() {
        try {
            ComplaintDetail complaint = buildComplaintFromForm();
            ComplaintServiceController service = new ComplaintServiceController();
            service.addComplaint(getCurrentUserId(), complaint, selectedFile);
            JOptionPane.showMessageDialog(this, "Complaint submitted successfully.");
            clearForm();
        } catch (MissingReportFieldException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Missing Information", JOptionPane.WARNING_MESSAGE);
        } catch (ReportSubmissionException ex) {
            String details = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            JOptionPane.showMessageDialog(this, "Submission failed: " + details, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Submission failed: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ComplaintDetail buildComplaintFromForm() {
        String category = getSelectedComboValue(categoryCombo, AppConfig.REPORT_CATEGORY_PLACEHOLDER);
        if (category.isEmpty()) {
            throw new MissingReportFieldException("category", "Category is required.");
        }

        String purok = getSelectedComboValue(purokCombo, AppConfig.REPORT_PUROK_PLACEHOLDER);
        if (purok.isEmpty()) {
            throw new MissingReportFieldException("purok", "Purok is required.");
        }

        String location = locationField.getText().trim();
        if (location.isEmpty() || AppConfig.REPORT_LOCATION_PLACEHOLDER.equals(location)) {
            throw new MissingReportFieldException("location", "Location is required.");
        }

        String details = detailsArea.getText().trim();
        if (details.isEmpty()) {
            throw new MissingReportFieldException("details", "Description is required.");
        }

        if (selectedFile == null) {
            throw new MissingReportFieldException("photo", "A photo attachment is required.");
        }

        Double pinnedLat = mapPanel.getPinnedLatitude();
        Double pinnedLon = mapPanel.getPinnedLongitude();
        if (pinnedLat == null || pinnedLon == null) {
            throw new MissingReportFieldException("mapPin", "You must drop and confirm a pin on the map.");
        }

        double distance = haversineDistanceMeters(
                pinnedLat, pinnedLon,
                SubmitReportConstants.DEFAULT_MAP_LATITUDE,
                SubmitReportConstants.DEFAULT_MAP_LONGITUDE);
        if (distance > SubmitReportConstants.SERVICE_AREA_RADIUS_METERS) {
            throw new IllegalArgumentException("Selected location is outside the service area.");
        }

        String street = parseStreetFromLocation(location);

        ComplaintDetail complaint = new ComplaintDetail();
        // Auto-generate subject from category + current timestamp
        complaint.setSubject(category + " - " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        complaint.setType(category);
        complaint.setPurok(purok);
        complaint.setStreet(street);
        complaint.setPersonsInvolved("");
        complaint.setDetails(details);
        complaint.setCurrentStatus("Pending");
        complaint.setDateTime(new Timestamp(System.currentTimeMillis()));
        complaint.setLatitude(pinnedLat);
        complaint.setLongitude(pinnedLon);
        return complaint;
    }

    private double haversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private String getSelectedComboValue(JComboBox<String> combo, String placeholder) {
        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return "";
        }
        String value = selected.toString().trim();
        return placeholder.equals(value) ? "" : value;
    }

    private int getCurrentUserId() {
        if (app == null || app.getUserSession() == null) {
            throw new IllegalStateException("No active user session found. Please log in before submitting a report.");
        }
        return app.getUserSession().getUserId();
    }

    private String parseStreetFromLocation(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }

        String[] parts = location.split(",");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        String street = parts[0];
        if (parts.length > 1) {
            String candidate = street + ", " + parts[1];
            if (candidate.length() <= 50) {
                street = candidate;
            }
        }

        if (street.length() > 50) {
            street = street.substring(0, 47).trim() + "...";
        }
        return street;
    }

    private void confirmMapPin() {
        Double lat = mapPanel.getPinnedLatitude();
        Double lon = mapPanel.getPinnedLongitude();
        if (lat == null || lon == null) {
            JOptionPane.showMessageDialog(this,
                    "Please pin a location on the map first.",
                    "Map Confirmation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            validatePinWithinServiceArea(lat, lon);
            latitudeField.setText(String.format("%.6f", lat));
            longitudeField.setText(String.format("%.6f", lon));
            mapStatusLabel.setText("Location confirmed: " + String.format("%.6f, %.6f", lat, lon));
            pinConfirmed = true;
            updateSubmitButtonState();
            JOptionPane.showMessageDialog(this,
                    "Map pin confirmed. You can now proceed to submit the report.",
                    "Map Confirmed", JOptionPane.INFORMATION_MESSAGE);
        } catch (MapPinOutOfServiceAreaException ex) {
            mapStatusLabel.setText("Selected pin is outside the service area.");
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Location Unavailable", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        categoryCombo.setSelectedIndex(0);
        purokCombo.setSelectedIndex(0);
        locationField.setText(AppConfig.REPORT_LOCATION_PLACEHOLDER);
        locationField.setForeground(Color.GRAY);
        detailsArea.setText("");
        latitudeField.setText("");
        longitudeField.setText("");
        selectedFile = null;
        selectedPhotoLabel.setText("No uploaded photo");
        mapStatusLabel.setText("Click on the map to pin location.");
        pinConfirmed = false;
        updateSubmitButtonState();
        mapPanel.resetView();
    }

    private String truncateFileName(String fileName, int maxLength) {
        if (fileName == null || fileName.length() <= maxLength) {
            return fileName;
        }
        int partLength = (maxLength - 3) / 2;
        return fileName.substring(0, partLength) + "..." + fileName.substring(fileName.length() - partLength);
    }

    private void validatePinWithinServiceArea(double lat, double lon) {
        double distance = haversineDistanceMeters(lat, lon,
                SubmitReportConstants.DEFAULT_MAP_LATITUDE,
                SubmitReportConstants.DEFAULT_MAP_LONGITUDE);
        if (distance > SubmitReportConstants.SERVICE_AREA_RADIUS_METERS) {
            throw new MapPinOutOfServiceAreaException(
                    "Selected location is outside the service area. Please choose a location inside the highlighted zone.");
        }
    }

    private void initFieldListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }
        };

        locationField.getDocument().addDocumentListener(listener);
        detailsArea.getDocument().addDocumentListener(listener);
        categoryCombo.addActionListener(e -> updateSubmitButtonState());
        purokCombo.addActionListener(e -> updateSubmitButtonState());
    }

    private void updateSubmitButtonState() {
        boolean formReady = isFormReady();
        submitBtn.setEnabled(formReady);
        submitBtn.setBackground(formReady ? UIConfig.PRIMARY : UIConfig.DISABLED_BG);
        submitBtn.setForeground(formReady ? Color.WHITE : UIConfig.DISABLED_TEXT);
    }

    private boolean isFormReady() {
        if (!pinConfirmed) {
            return false;
        }
        if (selectedFile == null) {
            return false;
        }
        if (getSelectedComboValue(categoryCombo, AppConfig.REPORT_CATEGORY_PLACEHOLDER).isEmpty()) {
            return false;
        }
        if (getSelectedComboValue(purokCombo, AppConfig.REPORT_PUROK_PLACEHOLDER).isEmpty()) {
            return false;
        }
        String location = locationField.getText().trim();
        if (location.isEmpty() || AppConfig.REPORT_LOCATION_PLACEHOLDER.equals(location)) {
            return false;
        }
        if (detailsArea.getText().trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
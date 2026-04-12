package features.submit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import models.ComplaintDetail;
import models.UserSession;
import services.controller.ComplaintService;

public class SubmitReportView extends JFrame {

    private static final String CATEGORY_PLACEHOLDER = "Choose category of issue";
    private static final String PUROK_PLACEHOLDER = "Choose a purok";
    private static final String LOCATION_PLACEHOLDER = "Street, Landmark";
    private static final String ROLE_RESIDENT = "Resident";
    private static final String ROLE_CAPTAIN = "Captain";
    private static final String ROLE_SECRETARY = "Secretary";

    private File selectedFile;
    private final UserSession session;
    private final String currentRole;
    private final JTextField subjectField;
    private final JComboBox<String> categoryCombo;
    private final JComboBox<String> purokCombo;
    private final JTextField locationField;
    private final JTextField latitudeField;
    private final JTextField longitudeField;
    private final JTextArea detailsArea;
    private final JLabel selectedPhotoLabel;
    private final JLabel mapStatusLabel;
    private final LeafletMapPickerBridge mapPicker;

    public SubmitReportView() {
        this(new UserSession(1, ROLE_RESIDENT, true));
    }

    public SubmitReportView(UserSession session) {
        this.session = session;
        this.currentRole = resolveRole(session);

        setTitle("E-Reporting System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 690));
        setSize(1080, 690);
        setLocationRelativeTo(null);
        setContentPane(new BackgroundImagePanel(loadBackgroundImage()));
        getContentPane().setLayout(new BorderLayout());

        subjectField = new JTextField();
        categoryCombo = createComboWithPlaceholder(SubmitReportConstants.CATEGORY_OPTIONS, CATEGORY_PLACEHOLDER);
        purokCombo = createComboWithPlaceholder(SubmitReportConstants.PUROK_OPTIONS, PUROK_PLACEHOLDER);
        locationField = new JTextField();
        latitudeField = new JTextField();
        longitudeField = new JTextField();
        detailsArea = new JTextArea(5, 20);
        selectedPhotoLabel = new JLabel("No uploaded Photos");
        mapStatusLabel = new JLabel("Open Leaflet map and pin location");

        mapPicker = new LeafletMapPickerBridge(new LeafletMapPickerBridge.Listener() {
            @Override
            public void onLocationPicked(double latitude, double longitude, String address) {
                latitudeField.setText(String.format("%.6f", latitude));
                longitudeField.setText(String.format("%.6f", longitude));
                if (address != null && !address.isBlank()) {
                    locationField.setForeground(Color.BLACK);
                    locationField.setText(address);
                }
            }

            @Override
            public void onStatusChanged(String statusText) {
                mapStatusLabel.setText(statusText);
            }
        });

        latitudeField.setEditable(false);
        longitudeField.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        installPlaceholder(locationField, LOCATION_PLACEHOLDER);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.add(createSidebarPanel(), BorderLayout.WEST);
        body.add(createFormPanel(), BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        getContentPane().add(root, BorderLayout.CENTER);

        applyPoppinsFont(root);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                mapPicker.shutdown();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(new Color(255, 255, 255, 215));
        header.setBorder(new EmptyBorder(10, 18, 10, 18));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);

        JLabel logo = createLogoLabel();

        JLabel title = new JLabel("E-Reporting System");
        title.setFont(new Font("Poppins", Font.BOLD, 20));

        JLabel user = new JLabel(currentRole);
        user.setFont(new Font("Poppins", Font.BOLD, 14));

        brand.add(logo);
        brand.add(title);
        header.add(brand, BorderLayout.WEST);
        header.add(user, BorderLayout.EAST);
        return header;
    }

    private JLabel createLogoLabel() {
        JLabel logo = new JLabel("E", JLabel.CENTER);
        logo.setPreferredSize(new Dimension(58, 58));
        logo.setOpaque(false);
        logo.setFont(new Font("Poppins", Font.BOLD, 22));

        File logoFile = new File(System.getProperty("user.dir") + File.separator + "images" + File.separator + "Logo.png");
        if (logoFile.exists()) {
            ImageIcon icon = new ImageIcon(logoFile.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(58, 58, Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(scaled));
            logo.setText("");
        }

        return logo;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new GridLayout(0, 1, 0, 8));
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setOpaque(true);
        sidebar.setBackground(new Color(241, 245, 249));
        sidebar.setBorder(new EmptyBorder(12, 10, 12, 10));

        boolean isResident = ROLE_RESIDENT.equalsIgnoreCase(currentRole);

        sidebar.add(sidebarItem("Dashboard", "Dashboard.png", false, this::openDashboard));
        sidebar.add(sidebarItem(isResident ? "My Reports" : "Reports", "ViewReport.png", false, this::openMyReports));
        sidebar.add(sidebarItem("Submit Report", "SubmitReport.png", true, () -> {
        }));

        if (!isResident) {
            sidebar.add(sidebarItem("Users", "Users.png", false, this::openUsers));
        }

        sidebar.add(sidebarItem("Profile", "Profile.png", false, this::openProfile));
        sidebar.add(sidebarItem("Logout", "Logout.png", false, this::logout));
        return sidebar;
    }

    private JButton sidebarItem(String text, String iconFileName, boolean active, Runnable onClickHandler) {
        JButton item = new JButton(text);
        item.setOpaque(true);
        item.setFont(new Font("Poppins", Font.BOLD, 18));
        item.setHorizontalAlignment(JLabel.LEFT);
        item.setIconTextGap(10);
        item.setBorder(new EmptyBorder(8, 10, 8, 10));
        item.setBackground(active ? new Color(124, 167, 234) : Color.WHITE);
        item.setForeground(active ? Color.WHITE : Color.BLACK);
        item.setContentAreaFilled(true);
        item.setBorderPainted(false);
        item.setFocusPainted(false);
        item.setRolloverEnabled(false);
        item.setFocusable(false);
        item.setRequestFocusEnabled(false);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        File iconFile = new File(System.getProperty("user.dir") + File.separator + "images" + File.separator + iconFileName);
        if (iconFile.exists()) {
            ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            item.setIcon(new ImageIcon(scaled));
        }

        item.addActionListener(e -> onClickHandler.run());
        return item;
    }

    private JPanel createFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        JLabel pageTitle = new JLabel("Submit New Report");
        pageTitle.setFont(new Font("Poppins", Font.BOLD, 34));
        pageTitle.setBorder(new EmptyBorder(2, 2, 10, 4));
        outer.add(pageTitle, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 200), 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel topRow = new JPanel(new GridLayout(1, 1));
        topRow.setOpaque(false);
        topRow.add(fieldBlock("Title", subjectField));

        JPanel midGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        midGrid.setOpaque(false);

        JPanel leftColumn = new JPanel(new GridLayout(3, 1, 0, 10));
        leftColumn.setOpaque(false);
        leftColumn.add(fieldBlock("Category", categoryCombo));
        leftColumn.add(fieldBlock("Purok", purokCombo));
        leftColumn.add(buildLocationBlock());

        midGrid.add(leftColumn);
        midGrid.add(createMapPanel());

        JPanel descriptionRow = new JPanel(new BorderLayout(6, 6));
        descriptionRow.setOpaque(false);
        JLabel descLbl = new JLabel("Description");
        descLbl.setFont(new Font("Poppins", Font.BOLD, 14));
        JScrollPane descPane = new JScrollPane(detailsArea);
        descPane.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true));
        descriptionRow.add(descLbl, BorderLayout.NORTH);
        descriptionRow.add(descPane, BorderLayout.CENTER);

        JPanel photoRow = new JPanel(new BorderLayout(10, 10));
        photoRow.setOpaque(false);
        selectedPhotoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JButton uploadBtn = new JButton("Upload Photos");
        uploadBtn.addActionListener(e -> choosePhoto());
        photoRow.add(selectedPhotoLabel, BorderLayout.CENTER);
        photoRow.add(uploadBtn, BorderLayout.EAST);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        JButton cancelBtn = new JButton("Cancel");
        JButton submitBtn = new JButton("Submit");
        cancelBtn.addActionListener(e -> clearForm());
        submitBtn.addActionListener(e -> submitComplaint());
        actionRow.add(cancelBtn);
        actionRow.add(submitBtn);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        midGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        descriptionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        photoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        stack.add(topRow);
        stack.add(Box.createVerticalStrut(10));
        stack.add(midGrid);
        stack.add(Box.createVerticalStrut(10));
        stack.add(descriptionRow);
        stack.add(Box.createVerticalStrut(10));
        stack.add(photoRow);

        card.add(stack, BorderLayout.NORTH);
        card.add(actionRow, BorderLayout.SOUTH);
        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildLocationBlock() {
        JPanel locationBlock = new JPanel(new BorderLayout(6, 6));
        locationBlock.setOpaque(false);
        JLabel locLabel = new JLabel("Location");
        locLabel.setFont(new Font("Poppins", Font.BOLD, 14));
        JPanel locationInputPanel = new JPanel(new BorderLayout(6, 0));
        locationInputPanel.setOpaque(false);
        JButton locateMeBtn = new JButton("Locate Me");
        locateMeBtn.addActionListener(e -> locateMeByIp());
        locationInputPanel.add(locationField, BorderLayout.CENTER);
        locationInputPanel.add(locateMeBtn, BorderLayout.EAST);
        locationBlock.add(locLabel, BorderLayout.NORTH);
        locationBlock.add(locationInputPanel, BorderLayout.CENTER);
        return locationBlock;
    }

    private JPanel createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);

        JLabel mapLbl = new JLabel("Map");
        mapLbl.setFont(new Font("Poppins", Font.BOLD, 14));

        JPanel coordPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        coordPanel.setOpaque(false);
        latitudeField.setToolTipText("Latitude");
        longitudeField.setToolTipText("Longitude");
        coordPanel.add(latitudeField);
        coordPanel.add(longitudeField);

        mapStatusLabel.setFont(new Font("Poppins", Font.PLAIN, 12));

        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 4));
        bottom.setOpaque(false);
        bottom.add(coordPanel);
        bottom.add(mapStatusLabel);

        JTextArea mapHint = new JTextArea(
                "LeafletJS map will open in browser.\n1) Click Open Map\n2) Pin point\n3) Confirm Pin");
        mapHint.setEditable(false);
        mapHint.setLineWrap(true);
        mapHint.setWrapStyleWord(true);
        mapHint.setBackground(new Color(245, 248, 252));
        mapHint.setFont(new Font("Poppins", Font.PLAIN, 12));

        JScrollPane mapHintPane = new JScrollPane(mapHint);
        mapHintPane.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true));

        JButton openMapBtn = new JButton("Open Map (LeafletJS)");
        openMapBtn.addActionListener(e -> openLeafletPicker());

        JPanel mapCenter = new JPanel(new BorderLayout(0, 6));
        mapCenter.setOpaque(false);
        mapCenter.add(mapHintPane, BorderLayout.CENTER);
        mapCenter.add(openMapBtn, BorderLayout.SOUTH);

        panel.add(mapLbl, BorderLayout.NORTH);
        panel.add(mapCenter, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void openLeafletPicker() {
        try {
            double initialLat = parseDoubleOrDefault(latitudeField.getText().trim(),
                    SubmitReportConstants.DEFAULT_MAP_LATITUDE);
            double initialLng = parseDoubleOrDefault(longitudeField.getText().trim(),
                    SubmitReportConstants.DEFAULT_MAP_LONGITUDE);
            mapPicker.openPicker(initialLat, initialLng);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Leaflet map picker: " + ex.getMessage(),
                    "Map Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel fieldBlock(String labelText, JComponent component) {
        JPanel block = new JPanel(new BorderLayout(6, 6));
        block.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Poppins", Font.BOLD, 14));
        component.setPreferredSize(new Dimension(220, 30));
        block.add(label, BorderLayout.NORTH);
        block.add(component, BorderLayout.CENTER);
        return block;
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            selectedPhotoLabel.setText(selectedFile.getName());
        }
    }

    private void locateMeByIp() {
        new Thread(() -> {
            try {
                URL url = URI.create("http://ip-api.com/json/?fields=lat,lon,city,regionName").toURL();
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                String response = new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String lat = extractJsonNumber(response, "lat");
                String lon = extractJsonNumber(response, "lon");
                String city = extractJsonString(response, "city");
                String region = extractJsonString(response, "regionName");

                SwingUtilities.invokeLater(() -> {
                    latitudeField.setText(lat);
                    longitudeField.setText(lon);
                    if (!city.isEmpty() || !region.isEmpty()) {
                        locationField.setForeground(Color.BLACK);
                        locationField.setText((city + " " + region).trim());
                    }
                    mapStatusLabel.setText("Located. You can open map to refine pin.");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Locate Me failed. You can still open map and pin manually.",
                        "Location Error", JOptionPane.WARNING_MESSAGE));
            }
        }).start();
    }

    private void submitComplaint() {
        try {
            ComplaintDetail complaint = buildComplaintFromForm();
            ComplaintService service = new ComplaintService();
            service.addComplaint(getCurrentUserId(), complaint, selectedFile);
            JOptionPane.showMessageDialog(this, "Complaint submitted successfully.");
            clearForm();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Submission failed: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ComplaintDetail buildComplaintFromForm() {
        String title = subjectField.getText().trim();
        String category = getSelectedComboValue(categoryCombo, CATEGORY_PLACEHOLDER);
        String purok = getSelectedComboValue(purokCombo, PUROK_PLACEHOLDER);
        String location = locationField.getText().trim();
        String details = detailsArea.getText().trim();
        String latitude = latitudeField.getText().trim();
        String longitude = longitudeField.getText().trim();

        if (title.isEmpty() || category.isEmpty() || purok.isEmpty() || location.isEmpty() || details.isEmpty()
                || LOCATION_PLACEHOLDER.equals(location)) {
            throw new IllegalArgumentException("Title, Category, Purok, Location, and Description are required.");
        }

        if (selectedFile == null) {
            throw new IllegalArgumentException("Please upload a photo before submitting.");
        }

        ComplaintDetail complaint = new ComplaintDetail();
        complaint.setSubject(title);
        complaint.setType(category);
        complaint.setPurok(purok);
        complaint.setStreet(location);
        complaint.setPersonsInvolved("");
        complaint.setDetails(details);
        complaint.setCurrentStatus("Pending");
        complaint.setDateTime(new Timestamp(System.currentTimeMillis()));
        complaint.setLatitude(parseDoubleOrDefault(latitude, 0.0));
        complaint.setLongitude(parseDoubleOrDefault(longitude, 0.0));
        return complaint;
    }

    private double parseDoubleOrDefault(String value, double fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private void clearForm() {
        subjectField.setText("");
        categoryCombo.setSelectedIndex(0);
        purokCombo.setSelectedIndex(0);
        locationField.setText(LOCATION_PLACEHOLDER);
        locationField.setForeground(Color.GRAY);
        detailsArea.setText("");
        latitudeField.setText("");
        longitudeField.setText("");
        selectedFile = null;
        selectedPhotoLabel.setText("No uploaded Photos");
        mapStatusLabel.setText("Open Leaflet map and pin location");
    }

    private void openDashboard() {
        mapStatusLabel.setText("Dashboard view is not yet wired.");
    }

    private void openMyReports() {
        mapStatusLabel.setText("Reports view is not yet wired.");
    }

    private void openUsers() {
        mapStatusLabel.setText("Users view is not yet wired.");
    }

    private void openProfile() {
        mapStatusLabel.setText("Profile view is not yet wired.");
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private Image loadBackgroundImage() {
        for (String candidate : SubmitReportConstants.BACKGROUND_CANDIDATES) {
            File file = new File(System.getProperty("user.dir"), candidate);
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }
        return null;
    }

    private String extractJsonNumber(String json, String key) {
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx < 0) {
            return "";
        }
        int start = idx + token.length();
        int end = start;
        while (end < json.length()) {
            char ch = json.charAt(end);
            if ((ch >= '0' && ch <= '9') || ch == '-' || ch == '.') {
                end++;
            } else {
                break;
            }
        }
        return json.substring(start, end);
    }

    private String extractJsonString(String json, String key) {
        String token = "\"" + key + "\":\"";
        int idx = json.indexOf(token);
        if (idx < 0) {
            return "";
        }
        int start = idx + token.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            return "";
        }
        return json.substring(start, end);
    }

    private JComboBox<String> createComboWithPlaceholder(String[] options, String placeholder) {
        String[] values = new String[options.length + 1];
        values[0] = placeholder;
        System.arraycopy(options, 0, values, 1, options.length);
        return new JComboBox<>(values);
    }

    private String getSelectedComboValue(JComboBox<String> combo, String placeholder) {
        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return "";
        }
        String value = selected.toString().trim();
        return placeholder.equals(value) ? "" : value;
    }

    private void installPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (placeholder.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void applyPoppinsFont(Component component) {
        if (component == null) {
            return;
        }

        Font current = component.getFont();
        int style = current == null ? Font.PLAIN : current.getStyle();
        int size = current == null ? 13 : current.getSize();
        component.setFont(new Font("Poppins", style, size));

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyPoppinsFont(child);
            }
        }
    }

    private int getCurrentUserId() {
        if (session == null) {
            return 1;
        }
        return session.getUserId();
    }

    private String resolveRole(UserSession session) {
        if (session == null || session.getRole() == null || session.getRole().isBlank()) {
            return ROLE_RESIDENT;
        }

        String role = session.getRole().trim();
        if (role.equalsIgnoreCase(ROLE_CAPTAIN)) {
            return ROLE_CAPTAIN;
        }
        if (role.equalsIgnoreCase(ROLE_SECRETARY)) {
            return ROLE_SECRETARY;
        }
        return ROLE_RESIDENT;
    }

    public static void main(String[] args) {
        // NOTE: For production, launch E_Report.java instead (which handles authentication)
        // This main method is for testing purposes only
        SwingUtilities.invokeLater(() -> {
            // Test with default resident session - replace with E_Report for real authentication
            UserSession testSession = new UserSession(1, ROLE_RESIDENT, true);
            SubmitReportView view = new SubmitReportView(testSession);
            view.setVisible(true);
        });
    }
}

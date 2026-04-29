package features.viewing;

import app.E_Report;
import config.UIConfig;
import config.database.DBConnection;
import daos.GetComplaintDao;
import models.ComplaintDetail;
import models.ComplaintHistoryDetail;
import services.controller.ComplaintStatusController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class FullComplaintDisplayView extends JPanel {

    private final E_Report app;
    private final String currentRole;
    private final boolean canUpdateStatus;

    // Data
    private int currentCdId = -1;
    private ComplaintDetail currentComplaint;

    // UI Components
    private JLabel lblComplaintId, lblSubject, lblCategory, lblPurok, lblLocation, lblStatus, lblDateSubmitted;
    private JTextArea txtDescription;
    private JLabel lblPhoto;
    private ImageIcon fullImageIcon;

    // Update mode
    private boolean isUpdateMode = false;
    private JPanel updatePanel;
    private JComboBox<String> cmbStatus;
    private JTextArea txtProcessNotes;
    private JLabel lblUpdatedBy, lblDateUpdated;

    // Complaint Action (when Resolved)
    private JPanel actionPanel;
    private JTextField txtActionTaken, txtRecommendation, txtOIC, txtDateAssigned, txtResolutionDate;

    // History
    private JTextArea txtHistory;

    // Footer
    private JButton btnBack, btnUpdate, btnSave, btnCancel;

    // Controller
    private final ComplaintStatusController statusController;

    public FullComplaintDisplayView(E_Report app) {
        this.app = app;
        this.currentComplaint = app.getCurrentComplaint();
        this.currentRole = app.getUserSession() != null ? app.getUserSession().getRole() : "Resident";
        this.canUpdateStatus = currentRole.toLowerCase().contains("secretary")
                || currentRole.toLowerCase().contains("captain");
        this.statusController = new ComplaintStatusController();

        setLayout(new BorderLayout());
        setOpaque(false);

        add(createContentPanel(), BorderLayout.CENTER);

        if (currentComplaint != null) {
            loadComplaint(currentComplaint);
        }
    }

    // ==================== MAIN CONTENT ====================

    private JPanel createContentPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        // Card
        JPanel card = new JPanel(new BorderLayout(18, 0));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                new EmptyBorder(20, 24, 20, 24)));

        // Left: Complaint Info
        JPanel left = createLeftPanel();
        card.add(left, BorderLayout.WEST);

        // Right: Map + History + Photo
        JPanel right = createRightPanel();
        card.add(right, BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);

        // Footer buttons (always visible at bottom)
        wrapper.add(createFooterPanel(), BorderLayout.SOUTH);

        return wrapper;
    }

    // ==================== LEFT PANEL (Complaint Info) ====================

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(420, 0));

        // Title
        JLabel title = new JLabel("Complaint Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UIConfig.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        // Info grid
        panel.add(createInfoRow("Complaint ID", lblComplaintId = new JLabel("—")));
        panel.add(createInfoRow("Subject", lblSubject = new JLabel("—")));
        panel.add(createInfoRow("Category", lblCategory = new JLabel("—")));
        panel.add(createInfoRow("Purok", lblPurok = new JLabel("—")));
        panel.add(createInfoRow("Location", lblLocation = new JLabel("—")));
        panel.add(createInfoRow("Current Status", lblStatus = new JLabel("—")));
        panel.add(createInfoRow("Date Submitted", lblDateSubmitted = new JLabel("—")));

        panel.add(Box.createVerticalStrut(16));

        // Description
        JLabel descTitle = new JLabel("Description");
        descTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descTitle);
        panel.add(Box.createVerticalStrut(6));

        txtDescription = new JTextArea(5, 30);
        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setBackground(new Color(248, 249, 250));
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(null);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        panel.add(descScroll);

        panel.add(Box.createVerticalStrut(16));

        // Photo thumbnail
        lblPhoto = new JLabel("No photo attached", SwingConstants.CENTER);
        lblPhoto.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPhoto.setPreferredSize(new Dimension(200, 120));
        lblPhoto.setMaximumSize(new Dimension(280, 160));
        lblPhoto.setOpaque(true);
        lblPhoto.setBackground(new Color(248, 249, 250));
        lblPhoto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        lblPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPhoto.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showFullImage();
            }
        });
        panel.add(lblPhoto);

        // Update section (hidden until Update clicked)
        panel.add(Box.createVerticalStrut(16));
        updatePanel = createUpdatePanel();
        updatePanel.setVisible(false);
        panel.add(updatePanel);

        // Complaint Action (hidden unless Resolved)
        actionPanel = createComplaintActionPanel();
        actionPanel.setVisible(false);
        panel.add(actionPanel);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createInfoRow(String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UIConfig.TEXT_SECONDARY);

        value.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        value.setForeground(UIConfig.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.NORTH);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    // ==================== UPDATE PANEL ====================

    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 232)),
                new EmptyBorder(16, 0, 0, 0)));

        JLabel title = new JLabel("Update Status");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UIConfig.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        // Status
        JPanel statusRow = new JPanel(new BorderLayout(10, 0));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblStatus = new JLabel("New Status");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setPreferredSize(new Dimension(90, 28));

        cmbStatus = new JComboBox<>(new String[] { "Pending", "In Progress", "Resolved", "Rejected" });
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.addActionListener(e -> onStatusChanged());

        statusRow.add(lblStatus, BorderLayout.WEST);
        statusRow.add(cmbStatus, BorderLayout.CENTER);
        panel.add(statusRow);
        panel.add(Box.createVerticalStrut(10));

        // Notes
        JLabel notesLbl = new JLabel("Process / Notes");
        notesLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        notesLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(notesLbl);
        panel.add(Box.createVerticalStrut(4));

        txtProcessNotes = new JTextArea(3, 22);
        txtProcessNotes.setLineWrap(true);
        txtProcessNotes.setWrapStyleWord(true);
        txtProcessNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProcessNotes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1, true),
                new EmptyBorder(8, 8, 8, 8)));

        JScrollPane notesScroll = new JScrollPane(txtProcessNotes);
        notesScroll.setBorder(null);
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        panel.add(notesScroll);
        panel.add(Box.createVerticalStrut(8));

        // Meta
        JPanel meta = new JPanel(new GridLayout(1, 2, 10, 0));
        meta.setOpaque(false);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        lblUpdatedBy = new JLabel("By: —");
        lblUpdatedBy.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUpdatedBy.setForeground(UIConfig.TEXT_SECONDARY);

        lblDateUpdated = new JLabel("Date: —");
        lblDateUpdated.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDateUpdated.setForeground(UIConfig.TEXT_SECONDARY);

        meta.add(lblUpdatedBy);
        meta.add(lblDateUpdated);
        panel.add(meta);

        return panel;
    }

    // ==================== COMPLAINT ACTION PANEL ====================

    private JPanel createComplaintActionPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 232)),
                new EmptyBorder(16, 0, 0, 0)));

        JLabel title = new JLabel("Resolution Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(40, 167, 69));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createActionRow("Action Taken *", txtActionTaken = new JTextField()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createActionRow("Recommendation", txtRecommendation = new JTextField()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createActionRow("Officer in Charge", txtOIC = new JTextField()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createActionRow("Date Assigned", txtDateAssigned = new JTextField()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createActionRow("Resolution Date", txtResolutionDate = new JTextField()));

        return panel;
    }

    private JPanel createActionRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setPreferredSize(new Dimension(140, 26));

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190), 1, true),
                new EmptyBorder(6, 8, 6, 8)));

        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // ==================== RIGHT PANEL (Map + History) ====================

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // Map
        JLabel mapTitle = new JLabel("Report Location");
        mapTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(mapTitle, BorderLayout.NORTH);

        // Use your existing map panel or a placeholder
        JPanel mapPlaceholder = new JPanel(new BorderLayout());
        mapPlaceholder.setOpaque(true);
        mapPlaceholder.setBackground(new Color(232, 234, 237));
        mapPlaceholder.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        mapPlaceholder.setPreferredSize(new Dimension(0, 280));

        JLabel mapLabel = new JLabel("Map View", SwingConstants.CENTER);
        mapLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mapLabel.setForeground(UIConfig.TEXT_SECONDARY);
        mapPlaceholder.add(mapLabel, BorderLayout.CENTER);

        // Coordinates label
        JLabel lblCoords = new JLabel("Lat: —, Long: —", SwingConstants.CENTER);
        lblCoords.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCoords.setForeground(UIConfig.TEXT_SECONDARY);
        lblCoords.setBorder(new EmptyBorder(6, 0, 6, 0));
        mapPlaceholder.add(lblCoords, BorderLayout.SOUTH);

        panel.add(mapPlaceholder, BorderLayout.CENTER);

        // History timeline
        panel.add(Box.createVerticalStrut(14), BorderLayout.SOUTH);
        JPanel historyPanel = createHistoryPanel();
        panel.add(historyPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 200));

        JLabel title = new JLabel("Status History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        txtHistory = new JTextArea();
        txtHistory.setEditable(false);
        txtHistory.setLineWrap(true);
        txtHistory.setWrapStyleWord(true);
        txtHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtHistory.setBackground(new Color(248, 249, 250));
        txtHistory.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232), 1, true),
                new EmptyBorder(10, 10, 10, 10)));

        JScrollPane scroll = new JScrollPane(txtHistory);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ==================== FOOTER ====================

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        btnBack = createFooterButton("← Back to Reports", UIConfig.SECONDARY);
        btnUpdate = createFooterButton("Update Status", UIConfig.PRIMARY);
        btnSave = createFooterButton("Save Update", new Color(40, 167, 69));
        btnCancel = createFooterButton("Cancel", new Color(108, 117, 125));

        btnBack.addActionListener(e -> app.navigate(app.getReturnRoute()));
        btnUpdate.addActionListener(e -> enterUpdateMode());
        btnSave.addActionListener(e -> saveUpdate());
        btnCancel.addActionListener(e -> exitUpdateMode());

        // Default: View mode
        btnSave.setVisible(false);
        btnCancel.setVisible(false);

        panel.add(btnBack);
        if (canUpdateStatus) {
            panel.add(btnUpdate);
        }
        panel.add(btnSave);
        panel.add(btnCancel);

        return panel;
    }

    private JButton createFooterButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1, true),
                new EmptyBorder(10, 20, 10, 20)));
        return btn;
    }

    // ==================== MODE SWITCHING ====================

    private void enterUpdateMode() {
        isUpdateMode = true;
        updatePanel.setVisible(true);

        lblUpdatedBy.setText("By: " + app.getCurrentUserFullName());
        lblDateUpdated.setText("Date: " + new java.sql.Date(System.currentTimeMillis()));

        btnUpdate.setVisible(false);
        btnBack.setVisible(false);
        btnSave.setVisible(true);
        btnCancel.setVisible(true);

        revalidate();
        repaint();
    }

    private void exitUpdateMode() {
        isUpdateMode = false;
        updatePanel.setVisible(false);
        actionPanel.setVisible(false);
        txtProcessNotes.setText("");

        btnUpdate.setVisible(true);
        btnBack.setVisible(true);
        btnSave.setVisible(false);
        btnCancel.setVisible(false);

        revalidate();
        repaint();
    }

    private void onStatusChanged() {
        String status = (String) cmbStatus.getSelectedItem();
        boolean isResolved = "Resolved".equals(status);
        actionPanel.setVisible(isResolved && isUpdateMode);
        if (isResolved) {
            txtOIC.setText(app.getCurrentUserFullName());
            txtResolutionDate.setText(new java.sql.Date(System.currentTimeMillis()).toString());
        }
        revalidate();
        repaint();
    }

    // ==================== SAVE ====================

    private void saveUpdate() {
        String newStatus = (String) cmbStatus.getSelectedItem();
        String note = txtProcessNotes.getText().trim();

        if (newStatus == null || newStatus.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select a status.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean saved = statusController.updateComplaintStatus(
                currentCdId, newStatus, note, app.getUserSession());

        if (!saved) {
            JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("Resolved".equals(newStatus)) {
            var action = buildComplaintAction();
            if (action == null)
                return; // Validation failed
            // TODO: Save ComplaintAction to DB
        }

        JOptionPane.showMessageDialog(this, "Status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        currentComplaint.setCurrentStatus(newStatus);
        lblStatus.setText(newStatus);
        loadHistory(currentCdId);
        exitUpdateMode();
    }

    private models.ComplaintAction buildComplaintAction() {
        String actionTaken = txtActionTaken.getText().trim();
        if (actionTaken.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Action Taken is required for resolved complaints.", "Required",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        var action = new models.ComplaintAction();
        action.setCD_ID(String.valueOf(currentCdId));
        action.setActionTaken(actionTaken);
        action.setRecommendation(txtRecommendation.getText().trim());
        action.setOIC(txtOIC.getText().trim());

        try {
            String da = txtDateAssigned.getText().trim();
            String rd = txtResolutionDate.getText().trim();
            if (!da.isEmpty())
                action.setDateTimeAssigned(Timestamp.valueOf(da + " 00:00:00"));
            if (!rd.isEmpty())
                action.setResolutionDateTime(Timestamp.valueOf(rd + " 00:00:00"));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD format.", "Invalid Date",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return action;
    }

    // ==================== DATA LOADING ====================

    public void loadComplaint(ComplaintDetail cd) {
        if (cd == null)
            return;

        this.currentComplaint = cd;
        this.currentCdId = cd.getComplaintId();

        lblComplaintId.setText(String.valueOf(cd.getComplaintId()));
        lblSubject.setText(safe(cd.getSubject()));
        lblCategory.setText(safe(cd.getType()));
        lblPurok.setText(safe(cd.getPurok()));
        lblLocation.setText(safe(cd.getStreet()));
        lblStatus.setText(safe(cd.getCurrentStatus()));
        lblDateSubmitted.setText(cd.getDateTime() != null ? cd.getDateTime().toString() : "N/A");
        txtDescription.setText(safe(cd.getDetails()));
        cmbStatus.setSelectedItem(safe(cd.getCurrentStatus()));

        // Photo
        byte[] photo = cd.getPhotoAttachmentBytes();
        if (photo != null && photo.length > 0) {
            fullImageIcon = new ImageIcon(photo);
            Image scaled = fullImageIcon.getImage().getScaledInstance(260, 160, Image.SCALE_SMOOTH);
            lblPhoto.setIcon(new ImageIcon(scaled));
            lblPhoto.setText("");
        } else {
            fullImageIcon = null;
            lblPhoto.setIcon(null);
            lblPhoto.setText("No photo attached");
        }

        loadHistory(cd.getComplaintId());
    }

    private void loadHistory(int complaintId) {
        try (Connection con = DBConnection.connect()) {
            List<ComplaintHistoryDetail> history = new GetComplaintDao().getComplaintHistory(con, complaintId);
            if (history == null || history.isEmpty()) {
                txtHistory.setText("No status updates yet.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (ComplaintHistoryDetail h : history) {
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("📅 ").append(h.getDateTimeUpdated()).append("\n");
                sb.append("🔄 Status: ").append(h.getStatus()).append("\n");
                sb.append("👤 By: ").append(h.getUpdatedBy()).append("\n");
                if (h.getProcess() != null && !h.getProcess().isBlank()) {
                    sb.append("📝 ").append(h.getProcess()).append("\n");
                }
                sb.append("\n");
            }
            txtHistory.setText(sb.toString());
        } catch (SQLException e) {
            txtHistory.setText("Unable to load history.");
            e.printStackTrace();
        }
    }

    private void showFullImage() {
        if (fullImageIcon == null) {
            JOptionPane.showMessageDialog(this, "No image available.", "No Image", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Evidence Photo",
                Dialog.ModalityType.APPLICATION_MODAL);
        JLabel imgLabel = new JLabel(fullImageIcon);
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scroll = new JScrollPane(imgLabel);
        scroll.setPreferredSize(new Dimension(900, 700));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String safe(String v) {
        return v != null ? v : "—";
    }
}
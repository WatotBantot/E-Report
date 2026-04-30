package features.viewing;

import app.E_Report;
import config.AppConfig;
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Complaint View & Update Panel
 */
public class ComplaintContentPanel extends JPanel {

    private final E_Report app;
    private final boolean canUpdateStatus;

    // Data
    private int currentCdId = -1;
    private ComplaintDetail currentComplaint;

    // Colors
    private static final Color C_PENDING = new Color(245, 158, 11);
    private static final Color C_IN_PROGRESS = new Color(59, 130, 246);
    private static final Color C_RESOLVED = new Color(16, 185, 129);
    private static final Color C_TRANSFERRED = new Color(139, 92, 246);
    private static final Color C_REJECTED = new Color(239, 68, 68);
    private static final Color C_CARD = new Color(255, 255, 255, 245);
    private static final Color C_BORDER = new Color(226, 232, 240);
    private static final Color C_BG_FIELD = new Color(248, 250, 252);
    private static final Color C_BG_EDITABLE = new Color(255, 255, 255);
    private static final Color C_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color C_TIMELINE_INACTIVE = new Color(203, 213, 225);

    // Main sections
    private JPanel mainContent;
    private JPanel updatePanel;
    private JPanel actionsTakenPanel;

    // Header components
    private JLabel lblStatusBadge;
    private JLabel lblTitle;
    private JButton btnUpdateHeader;
    private JButton btnBackTop;

    // Form fields (view mode)
    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JTextField txtLocation;
    private JTextField txtPurok;
    private JTextField txtCoords;
    private JTextField txtDateSubmitted;
    private JTextField txtLastUpdate;
    private JLabel lblAttachment;

    // Status timeline
    private JLabel[] timelineSteps;
    private JLabel[] timelineLabels;
    private JPanel[] timelineConnectors;

    // Update form
    private JComboBox<String> cmbStatus;
    private JTextArea txtProcessNotes;

    // Pending Review panel (validation mode)
    private JPanel pendingReviewPanel;
    private JTextField txtPendingTitle;
    private JComboBox<String> cmbPendingType;
    private JTextField txtPendingOfficer;
    private JTextArea txtPendingNotes;

    // In Progress panel
    private JPanel inProgressPanel;
    private JTextField txtInProgressOfficer;
    private JTextField txtInProgressAssignedDate;

    // Resolution panel
    private JPanel resolutionPanel;
    private JTextField txtActionTaken, txtRecommendation, txtOIC, txtResolutionDate;

    // Header action buttons
    private JButton btnCancelHeader;
    private JButton btnRejectHeader;
    private JButton btnSaveHeader;

    private final ComplaintStatusController statusController;
    private static final String[] TIMELINE_LABELS = { "Submitted", "In Progress", "Resolved" };

    /** Route to return to when Back is clicked */
    private String returnRoute = "dashboard";

    public ComplaintContentPanel(E_Report app) {
        this.app = app;
        this.currentComplaint = app.getCurrentComplaint();
        String role = app.getUserSession() != null ? app.getUserSession().getRole() : "";
        this.canUpdateStatus = role.toLowerCase().contains("secretary") || role.toLowerCase().contains("captain");
        this.statusController = new ComplaintStatusController();

        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        add(createMainContent(), BorderLayout.CENTER);

        if (currentComplaint != null) {
            loadComplaint(currentComplaint);
        }
    }

    public void setReturnRoute(String route) {
        this.returnRoute = (route != null && !route.isBlank()) ? route : "dashboard";
    }

    // ==================== MAIN CONTENT ====================

    private JPanel createMainContent() {
        mainContent = new JPanel();
        mainContent.setOpaque(false);
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(new EmptyBorder(0, 4, 12, 4));

        // Back button row (top)
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        backRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 32));
        btnBackTop = createGhostButton("← Back");
        btnBackTop.addActionListener(e -> app.navigate(returnRoute));
        backRow.add(btnBackTop);
        mainContent.add(backRow);
        mainContent.add(Box.createVerticalStrut(8));

        // Header row
        JPanel header = createHeaderRow();
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        mainContent.add(header);
        mainContent.add(Box.createVerticalStrut(12));

        // Status timeline
        JPanel timeline = createStatusTimeline();
        timeline.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeline.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));
        mainContent.add(timeline);
        mainContent.add(Box.createVerticalStrut(12));

        // Update panel (hidden by default) - NO maximum size, let it size naturally
        updatePanel = createUpdatePanel();
        updatePanel.setVisible(false);
        updatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(updatePanel);
        mainContent.add(Box.createVerticalStrut(12));

        // Main form card
        JPanel formCard = createFormCard();
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(formCard);
        mainContent.add(Box.createVerticalStrut(12));

        // Actions Taken section
        actionsTakenPanel = createActionsTakenPanel();
        actionsTakenPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(actionsTakenPanel);

        // GLUE: absorbs all extra vertical space, prevents components from stretching
        mainContent.add(Box.createVerticalGlue());

        return mainContent;
    }

    // ==================== HEADER ROW ====================

    private JPanel createHeaderRow() {
        JPanel headerRow = new JPanel(new BorderLayout(16, 0));
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        lblStatusBadge = new JLabel("PENDING", SwingConstants.CENTER);
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(C_PENDING);
        lblStatusBadge.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        lblTitle = new JLabel("Report #000 – Category");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIConfig.TEXT_PRIMARY);

        left.add(lblStatusBadge);
        left.add(lblTitle);

        // RIGHT SIDE: Action buttons (Cancel, Reject, Save)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        btnCancelHeader = createGhostButton("Cancel");
        btnCancelHeader.setVisible(false);
        btnCancelHeader.addActionListener(e -> hideUpdatePanel());

        btnRejectHeader = createDangerButton("Reject");
        btnRejectHeader.setVisible(false);
        btnRejectHeader.addActionListener(e -> rejectComplaint());

        btnSaveHeader = createPrimaryButton("Save Changes", new Color(16, 185, 129));
        btnSaveHeader.setVisible(false);
        btnSaveHeader.addActionListener(e -> saveUpdate());

        btnUpdateHeader = createPrimaryButton("Update Status", UIConfig.PRIMARY);
        btnUpdateHeader.addActionListener(e -> toggleUpdatePanel());

        right.add(btnCancelHeader);
        right.add(btnRejectHeader);
        right.add(btnSaveHeader);
        right.add(btnUpdateHeader);

        headerRow.add(left, BorderLayout.WEST);
        headerRow.add(right, BorderLayout.EAST);

        return headerRow;
    }

    // ==================== STATUS TIMELINE ====================

    private JPanel createStatusTimeline() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(C_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        timelineSteps = new JLabel[TIMELINE_LABELS.length];
        timelineLabels = new JLabel[TIMELINE_LABELS.length];
        timelineConnectors = new JPanel[TIMELINE_LABELS.length - 1];

        for (int i = 0; i < TIMELINE_LABELS.length; i++) {
            JLabel circle = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            circle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            circle.setForeground(Color.WHITE);
            circle.setOpaque(true);
            circle.setBackground(C_TIMELINE_INACTIVE);
            circle.setPreferredSize(new Dimension(28, 28));
            circle.setMinimumSize(new Dimension(28, 28));
            circle.setMaximumSize(new Dimension(28, 28));
            timelineSteps[i] = circle;

            JLabel label = new JLabel(TIMELINE_LABELS[i], SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(C_TEXT_MUTED);
            timelineLabels[i] = label;

            JPanel stepPanel = new JPanel();
            stepPanel.setOpaque(false);
            stepPanel.setLayout(new BoxLayout(stepPanel, BoxLayout.Y_AXIS));
            circle.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            stepPanel.add(circle);
            stepPanel.add(Box.createVerticalStrut(4));
            stepPanel.add(label);

            gbc.gridx = i * 2;
            gbc.weightx = 0;
            panel.add(stepPanel, gbc);

            if (i < TIMELINE_LABELS.length - 1) {
                JPanel line = new JPanel();
                line.setOpaque(true);
                line.setBackground(C_TIMELINE_INACTIVE);
                line.setPreferredSize(new Dimension(60, 3));
                line.setMaximumSize(new Dimension(Short.MAX_VALUE, 3));
                timelineConnectors[i] = line;

                gbc.gridx = i * 2 + 1;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(0, 6, 10, 6);
                panel.add(line, gbc);
                gbc.insets = new Insets(0, 0, 0, 0);
            }
        }

        return panel;
    }

    private void updateTimeline(String currentStatus) {
        int activeIndex = switch (currentStatus) {
            case "Pending" -> 0;
            case "In Progress", "Transferred" -> 1;
            case "Resolved", "Rejected" -> 2;
            default -> -1;
        };

        Color activeColor = getStatusColor(currentStatus);
        if (activeColor.equals(C_TEXT_MUTED))
            activeColor = C_IN_PROGRESS;

        for (int i = 0; i < TIMELINE_LABELS.length; i++) {
            if (i <= activeIndex && activeIndex >= 0) {
                timelineSteps[i].setBackground(activeColor);
                timelineSteps[i].setForeground(Color.WHITE);
                timelineLabels[i].setForeground(activeColor);
                timelineSteps[i].setText(i < activeIndex ? "✓" : String.valueOf(i + 1));
            } else {
                timelineSteps[i].setBackground(C_TIMELINE_INACTIVE);
                timelineSteps[i].setForeground(Color.WHITE);
                timelineLabels[i].setForeground(C_TEXT_MUTED);
                timelineSteps[i].setText(String.valueOf(i + 1));
            }

            if (i < TIMELINE_LABELS.length - 1) {
                timelineConnectors[i]
                        .setBackground(i < activeIndex && activeIndex >= 0 ? activeColor : C_TIMELINE_INACTIVE);
            }
        }
    }

    // ==================== FORM CARD ====================

    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setOpaque(true);
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)));

        card.add(createFormFieldsPanel(), BorderLayout.CENTER);
        return card;
    }

    private JPanel createFormFieldsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 12);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        panel.add(createFieldRow("Title", txtTitle = createReadOnlyField()), gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescription.setBackground(C_BG_FIELD);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(null);
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        panel.add(createFieldRow("Description", descScroll), gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(createFieldRow("Location", txtLocation = createReadOnlyField()), gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.gridx = 0;
        panel.add(createFieldRow("Purok", txtPurok = createReadOnlyField()), gbc);
        gbc.gridx = 2;
        panel.add(createFieldRow("Coordinates", txtCoords = createReadOnlyField()), gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(createFieldRow("Date Submitted", txtDateSubmitted = createReadOnlyField()), gbc);
        gbc.gridx = 2;
        panel.add(createFieldRow("Last Update", txtLastUpdate = createReadOnlyField()), gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        lblAttachment = new JLabel("No attachments");
        lblAttachment.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAttachment.setOpaque(true);
        lblAttachment.setBackground(C_BG_FIELD);
        lblAttachment.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        lblAttachment.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(createFieldRow("Attachments", lblAttachment), gbc);

        return panel;
    }

    private JPanel createFieldRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(C_TEXT_MUTED);

        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(C_BG_FIELD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    // ==================== UPDATE PANEL ====================

    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(new Color(255, 255, 255, 252));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246, 90), 1, true),
                new EmptyBorder(16, 20, 16, 20)));
        // REMOVED: setMaximumSize(Integer.MAX_VALUE, Integer.MAX_VALUE) — this was
        // causing infinite expansion

        // Title only (no close button)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Update Complaint Status");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UIConfig.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        panel.add(header);
        panel.add(Box.createVerticalStrut(12));

        JPanel currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        currentRow.setOpaque(false);
        currentRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));
        JLabel lblCurrentPrefix = new JLabel("Current Status:");
        lblCurrentPrefix.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel lblCurrent = new JLabel("—");
        lblCurrent.setName("lblCurrentStatus");
        lblCurrent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCurrent.setForeground(C_IN_PROGRESS);
        currentRow.add(lblCurrentPrefix);
        currentRow.add(lblCurrent);
        panel.add(currentRow);
        panel.add(Box.createVerticalStrut(12));

        // Form: Status + Notes
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        formGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        formGrid.setMaximumSize(new Dimension(Short.MAX_VALUE, 140));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JPanel statusWrap = new JPanel(new BorderLayout(0, 4));
        statusWrap.setOpaque(false);
        JLabel lblStatus = new JLabel("New Status *");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(C_TEXT_MUTED);
        cmbStatus = new JComboBox<>();
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.setBackground(Color.WHITE);
        cmbStatus.setBorder(BorderFactory.createLineBorder(C_BORDER, 1, true));
        cmbStatus.setMaximumSize(new Dimension(Short.MAX_VALUE, 32));
        cmbStatus.addActionListener(e -> onStatusChanged());
        statusWrap.add(lblStatus, BorderLayout.NORTH);
        statusWrap.add(cmbStatus, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formGrid.add(statusWrap, gbc);

        JPanel notesWrap = new JPanel(new BorderLayout(0, 4));
        notesWrap.setOpaque(false);
        JLabel lblNotes = new JLabel("Process / Notes");
        lblNotes.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNotes.setForeground(C_TEXT_MUTED);
        txtProcessNotes = new JTextArea(3, 20);
        txtProcessNotes.setLineWrap(true);
        txtProcessNotes.setWrapStyleWord(true);
        txtProcessNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProcessNotes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JScrollPane notesScroll = new JScrollPane(txtProcessNotes);
        notesScroll.setBorder(null);
        notesScroll.setOpaque(false);
        notesScroll.getViewport().setOpaque(false);
        notesWrap.add(lblNotes, BorderLayout.NORTH);
        notesWrap.add(notesScroll, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        formGrid.add(notesWrap, gbc);

        panel.add(formGrid);
        panel.add(Box.createVerticalStrut(10));

        // Conditional panels
        pendingReviewPanel = createPendingReviewFields();
        pendingReviewPanel.setVisible(false);
        pendingReviewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(pendingReviewPanel);

        inProgressPanel = createInProgressFields();
        inProgressPanel.setVisible(false);
        inProgressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inProgressPanel);

        resolutionPanel = createResolutionFields();
        resolutionPanel.setVisible(false);
        resolutionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(resolutionPanel);

        panel.add(Box.createVerticalStrut(10));

        // Meta
        JLabel lblMeta = new JLabel("Updated by: " + app.getCurrentUserFullName() + "  •  " +
                new java.sql.Date(System.currentTimeMillis()));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMeta.setForeground(C_TEXT_MUTED);
        lblMeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblMeta);

        return panel;
    }

    // ==================== PENDING REVIEW PANEL ====================

    private JPanel createPendingReviewFields() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, C_PENDING),
                new EmptyBorder(12, 0, 0, 0)));

        JLabel lblHeader = new JLabel("Complaint Validation");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(C_PENDING);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblHeader);
        panel.add(Box.createVerticalStrut(10));

        // Editable Title
        txtPendingTitle = new JTextField();
        txtPendingTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPendingTitle.setBackground(C_BG_EDITABLE);
        txtPendingTitle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JPanel titleWrap = createLabeledFieldPanel("Title (editable) *", txtPendingTitle);
        titleWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleWrap);
        panel.add(Box.createVerticalStrut(8));

        // Type / Category combo box from centralized config
        cmbPendingType = new JComboBox<>(AppConfig.COMPLAINT_TYPES);
        cmbPendingType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbPendingType.setBackground(C_BG_EDITABLE);
        cmbPendingType.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        cmbPendingType.setEditable(true);
        JPanel typeWrap = createLabeledFieldPanel("Type / Category (editable) *", cmbPendingType);
        typeWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(typeWrap);
        panel.add(Box.createVerticalStrut(8));

        // Officer assigned
        txtPendingOfficer = new JTextField();
        txtPendingOfficer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPendingOfficer.setBackground(C_BG_EDITABLE);
        txtPendingOfficer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JPanel officerWrap = createLabeledFieldPanel("Officer / Personnel Assigned", txtPendingOfficer);
        officerWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(officerWrap);
        panel.add(Box.createVerticalStrut(8));

        // Notes
        txtPendingNotes = new JTextArea(3, 20);
        txtPendingNotes.setLineWrap(true);
        txtPendingNotes.setWrapStyleWord(true);
        txtPendingNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPendingNotes.setBackground(C_BG_EDITABLE);
        txtPendingNotes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        JScrollPane notesScroll = new JScrollPane(txtPendingNotes);
        notesScroll.setBorder(null);
        notesScroll.setOpaque(false);
        notesScroll.getViewport().setOpaque(false);
        JPanel notesWrap = createLabeledFieldPanel("Validation Notes", notesScroll);
        notesWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(notesWrap);

        return panel;
    }

    private JPanel createLabeledFieldPanel(String labelText, JComponent field) {
        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(C_TEXT_MUTED);
        wrap.add(lbl, BorderLayout.NORTH);
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    // ==================== IN PROGRESS PANEL ====================

    private JPanel createInProgressFields() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, C_IN_PROGRESS),
                new EmptyBorder(10, 0, 0, 0)));

        JLabel lblHeader = new JLabel("Complaint History Detail");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(C_IN_PROGRESS);
        panel.add(lblHeader);

        txtInProgressOfficer = new JTextField();
        txtInProgressAssignedDate = new JTextField();

        panel.add(createLabeledField("Officer / Personnel Assigned", txtInProgressOfficer));
        panel.add(createLabeledField("Date Assigned (YYYY-MM-DD)", txtInProgressAssignedDate));

        return panel;
    }

    // ==================== RESOLUTION PANEL ====================

    private JPanel createResolutionFields() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 6, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(16, 185, 129)),
                new EmptyBorder(10, 0, 0, 0)));

        JLabel lblHeader = new JLabel("Resolution Actioned");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(new Color(16, 185, 129));
        panel.add(lblHeader);

        txtActionTaken = new JTextField();
        txtRecommendation = new JTextField();
        txtOIC = new JTextField();
        txtResolutionDate = new JTextField();

        panel.add(createLabeledField("Action Taken *", txtActionTaken));
        panel.add(createLabeledField("Recommendation", txtRecommendation));
        panel.add(createLabeledField("Officer in Charge", txtOIC));
        panel.add(createLabeledField("Resolution Date (YYYY-MM-DD)", txtResolutionDate));

        return panel;
    }

    private JPanel createLabeledField(String labelText, JTextField field) {
        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(C_TEXT_MUTED);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        wrap.add(lbl, BorderLayout.NORTH);
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    // ==================== ACTIONS TAKEN PANEL ====================

    private JPanel createActionsTakenPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(C_CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel title = new JLabel("Actions Taken");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));

        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerCols = new JPanel(new GridLayout(1, 3, 0, 0));
        headerCols.setOpaque(false);

        JLabel col1 = new JLabel("Date & Time");
        col1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        col1.setForeground(C_TEXT_MUTED);

        JLabel col2 = new JLabel("Status");
        col2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        col2.setForeground(C_TEXT_MUTED);

        JLabel col3 = new JLabel("Notes / Action");
        col3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        col3.setForeground(C_TEXT_MUTED);

        headerCols.add(col1);
        headerCols.add(col2);
        headerCols.add(col3);
        header.add(headerCols, BorderLayout.CENTER);

        panel.add(header);
        panel.add(Box.createVerticalStrut(6));

        JPanel historyContent = new JPanel();
        historyContent.setName("historyContent");
        historyContent.setOpaque(false);
        historyContent.setLayout(new BoxLayout(historyContent, BoxLayout.Y_AXIS));
        historyContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(historyContent);

        return panel;
    }

    // ==================== BUTTONS ====================

    private JButton createPrimaryButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(C_TEXT_MUTED);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        return btn;
    }

    private JButton createGhostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(C_TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(C_REJECTED);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    // ==================== UPDATE PANEL CONTROL ====================

    private void toggleUpdatePanel() {
        // BLOCK: Rejected and Resolved complaints cannot be updated
        if (currentComplaint != null) {
            String cur = currentComplaint.getCurrentStatus();
            if ("Rejected".equalsIgnoreCase(cur) || "Resolved".equalsIgnoreCase(cur)) {
                JOptionPane.showMessageDialog(this,
                        "This complaint cannot be updated because it is already " + cur + ".",
                        "Update Not Allowed", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        boolean visible = !updatePanel.isVisible();
        updatePanel.setVisible(visible);

        // Toggle header buttons visibility
        btnUpdateHeader.setVisible(!visible);
        btnCancelHeader.setVisible(visible);
        btnSaveHeader.setVisible(visible);

        if (visible) {
            txtProcessNotes.setText("");
            txtOIC.setText(app.getCurrentUserFullName());
            txtInProgressOfficer.setText(app.getCurrentUserFullName());
            txtPendingOfficer.setText(app.getCurrentUserFullName());
            txtResolutionDate.setText(new java.sql.Date(System.currentTimeMillis()).toString());
            txtInProgressAssignedDate.setText(new java.sql.Date(System.currentTimeMillis()).toString());

            // Pre-fill pending fields with RAW values (NOT safe()/placeholder)
            if (currentComplaint != null) {
                String rawSubject = currentComplaint.getSubject();
                txtPendingTitle.setText(rawSubject != null ? rawSubject : "");

                String rawType = currentComplaint.getType();
                if (rawType != null && !rawType.isBlank()) {
                    cmbPendingType.setSelectedItem(rawType);
                    if (cmbPendingType.getSelectedIndex() == -1) {
                        cmbPendingType.insertItemAt(rawType, 0);
                        cmbPendingType.setSelectedIndex(0);
                    }
                } else {
                    cmbPendingType.setSelectedIndex(-1);
                }
            } else {
                txtPendingTitle.setText("");
                cmbPendingType.setSelectedIndex(-1);
            }

            resolutionPanel.setVisible(false);
            inProgressPanel.setVisible(false);
            pendingReviewPanel.setVisible(false);

            populateStatusDropdown();

            // Auto-show pending review panel if current status is Pending
            if (currentComplaint != null && "Pending".equals(currentComplaint.getCurrentStatus())) {
                pendingReviewPanel.setVisible(true);
                btnRejectHeader.setVisible(true);
            } else {
                btnRejectHeader.setVisible(false);
            }

            for (Component c : updatePanel.getComponents()) {
                if (c instanceof JPanel) {
                    for (Component cc : ((JPanel) c).getComponents()) {
                        if ("lblCurrentStatus".equals(cc.getName()) && cc instanceof JLabel) {
                            ((JLabel) cc).setText(currentComplaint != null ? currentComplaint.getCurrentStatus() : "—");
                            break;
                        }
                    }
                }
            }
        } else {
            btnRejectHeader.setVisible(false);
        }

        mainContent.revalidate();
        mainContent.repaint();

        if (visible) {
            SwingUtilities.invokeLater(() -> {
                Container parent = getParent();
                if (parent instanceof JViewport vp) {
                    JScrollPane scroll = (JScrollPane) vp.getParent();
                    if (scroll != null)
                        scroll.getVerticalScrollBar().setValue(0);
                }
            });
        }
    }

    private void populateStatusDropdown() {
        cmbStatus.removeAllItems();
        String current = currentComplaint != null ? currentComplaint.getCurrentStatus() : "Pending";

        switch (current) {
            case "Pending" -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Transferred");
                cmbStatus.addItem("Resolved");
            }
            case "In Progress" -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Resolved");
                cmbStatus.addItem("Transferred");
            }
            case "Transferred" -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Resolved");
            }
            case "Resolved" -> cmbStatus.addItem("Transferred");
            case "Rejected" -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Transferred");
            }
            default -> {
                cmbStatus.addItem("In Progress");
                cmbStatus.addItem("Resolved");
                cmbStatus.addItem("Transferred");
            }
        }
    }

    private void hideUpdatePanel() {
        updatePanel.setVisible(false);
        btnUpdateHeader.setVisible(true);
        btnCancelHeader.setVisible(false);
        btnRejectHeader.setVisible(false);
        btnSaveHeader.setVisible(false);
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void onStatusChanged() {
        String status = (String) cmbStatus.getSelectedItem();
        boolean isResolved = "Resolved".equals(status);
        boolean isInProgress = "In Progress".equals(status);

        pendingReviewPanel.setVisible(false);
        resolutionPanel.setVisible(isResolved);
        inProgressPanel.setVisible(isInProgress);

        mainContent.revalidate();
        mainContent.repaint();
    }

    // ==================== REJECT COMPLAINT ====================

    private void rejectComplaint() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reject this complaint?\nThis action cannot be undone.",
                "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String note = txtPendingNotes.getText().trim();
        if (note.isEmpty()) {
            note = "Complaint rejected during validation";
        }

        boolean saved = statusController.updateComplaintStatus(
                currentCdId, "Rejected", note, app.getUserSession());

        if (!saved) {
            JOptionPane.showMessageDialog(this, "Failed to reject complaint.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Complaint rejected successfully.", "Rejected",
                JOptionPane.INFORMATION_MESSAGE);
        currentComplaint.setCurrentStatus("Rejected");
        loadComplaint(currentComplaint);
        hideUpdatePanel();
    }

    // ==================== SAVE ====================

    private void saveUpdate() {
        String newStatus = (String) cmbStatus.getSelectedItem();
        String note = txtProcessNotes.getText().trim();

        if (newStatus == null || newStatus.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select a status.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pending validation → In Progress: Title and Type/Category are MANDATORY
        if ("In Progress".equals(newStatus) && "Pending".equals(currentComplaint.getCurrentStatus())) {
            String title = txtPendingTitle.getText().trim();

            Object selectedTypeObj = cmbPendingType.getSelectedItem();
            String type = selectedTypeObj != null ? selectedTypeObj.toString().trim() : "";

            // STRICT validation: reject placeholder and blank
            if (title.isBlank() || "—".equals(title)) {
                JOptionPane.showMessageDialog(this, "Title is required.", "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (type.isBlank() || "—".equals(type)) {
                JOptionPane.showMessageDialog(this, "Type / Category is required.", "Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String officer = txtPendingOfficer.getText().trim();
            if (officer.isBlank()) {
                JOptionPane.showMessageDialog(this, "Officer / Personnel Assigned is required.", "Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Build note with validation details
            StringBuilder sb = new StringBuilder();
            if (!note.isEmpty())
                sb.append(note).append("\n");
            sb.append("[Validated by: ").append(app.getCurrentUserFullName()).append("]");
            sb.append(" [Title: ").append(title).append("]");
            sb.append(" [Type: ").append(type).append("]");
            sb.append(" [Assigned to: ").append(officer).append("]");
            String pendingNotes = txtPendingNotes.getText().trim();
            if (!pendingNotes.isEmpty()) {
                sb.append(" [Notes: ").append(pendingNotes).append("]");
            }
            note = sb.toString();

            // Update in-memory object
            currentComplaint.setSubject(title);
            currentComplaint.setType(type);

            // UPDATE DATABASE with new title and type
            if (!updateComplaintDetail(currentCdId, title, type)) {
                JOptionPane.showMessageDialog(this, "Failed to save complaint details.", "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Normal In Progress update
        else if ("In Progress".equals(newStatus)) {
            if (txtInProgressOfficer.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Officer / Personnel Assigned is required.", "Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String officer = txtInProgressOfficer.getText().trim();
            String date = txtInProgressAssignedDate.getText().trim();
            note += "\n[Assigned to: " + officer + (date.isEmpty() ? "" : " | Date: " + date) + "]";
        }

        if ("Resolved".equals(newStatus)) {
            var action = buildComplaintAction();
            if (action == null)
                return;
        }

        boolean saved = statusController.updateComplaintStatus(
                currentCdId, newStatus, note, app.getUserSession());

        if (!saved) {
            JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        currentComplaint.setCurrentStatus(newStatus);
        loadComplaint(currentComplaint);
        hideUpdatePanel();
    }

    private models.ComplaintAction buildComplaintAction() {
        String actionTaken = txtActionTaken.getText().trim();
        if (actionTaken.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Action Taken is required.", "Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        var action = new models.ComplaintAction();
        action.setCD_ID(String.valueOf(currentCdId));
        action.setActionTaken(actionTaken);
        action.setRecommendation(txtRecommendation.getText().trim());
        action.setOIC(txtOIC.getText().trim());

        try {
            String rd = txtResolutionDate.getText().trim();
            if (!rd.isEmpty())
                action.setResolutionDateTime(Timestamp.valueOf(rd + " 00:00:00"));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD.", "Invalid Date",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return action;
    }

    /**
     * Updates the Complaint_Detail table with new subject and type.
     */
    private boolean updateComplaintDetail(int cdId, String subject, String type) {
        String sql = "UPDATE Complaint_Detail SET subject = ?, type = ? WHERE CD_ID = ?";
        try (Connection con = DBConnection.connect();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setString(2, type);
            ps.setInt(3, cdId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== DATA LOADING ====================

    public void loadComplaint(ComplaintDetail cd) {
        if (cd == null)
            return;

        // Reset update panel when switching complaints
        hideUpdatePanel();

        this.currentComplaint = cd;
        this.currentCdId = cd.getComplaintId();

        String status = safe(cd.getCurrentStatus());
        lblStatusBadge.setText(status.toUpperCase());
        lblStatusBadge.setBackground(getStatusColor(status));
        lblTitle.setText("Report #" + String.format("%03d", cd.getComplaintId()) + " – " + safe(cd.getType()));

        txtTitle.setText(safe(cd.getSubject()));
        txtDescription.setText(safe(cd.getDetails()));
        txtLocation.setText(safe(cd.getStreet()));
        txtPurok.setText(safe(cd.getPurok()));
        txtCoords.setText(String.format("%.6f, %.6f", cd.getLatitude(), cd.getLongitude()));
        txtDateSubmitted.setText(cd.getDateTime() != null ? cd.getDateTime().toString() : "N/A");

        updateTimeline(status);

        byte[] photo = cd.getPhotoAttachmentBytes();
        if (photo != null && photo.length > 0) {
            ImageIcon icon = new ImageIcon(photo);
            Image scaled = icon.getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
            lblAttachment.setIcon(new ImageIcon(scaled));
            lblAttachment.setText("  " + (cd.getPhotoName() != null ? cd.getPhotoName() : "Photo"));
        } else {
            lblAttachment.setIcon(null);
            lblAttachment.setText("No attachments");
        }

        // HIDE Update button for final statuses (Rejected / Resolved)
        boolean isFinalStatus = "Rejected".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status);
        btnUpdateHeader.setVisible(canUpdateStatus && !isFinalStatus);

        loadHistory(cd.getComplaintId());
    }

    private void loadHistory(int complaintId) {
        JPanel historyContent = null;
        for (Component c : actionsTakenPanel.getComponents()) {
            if ("historyContent".equals(c.getName())) {
                historyContent = (JPanel) c;
                break;
            }
        }
        if (historyContent == null)
            return;

        historyContent.removeAll();

        try (Connection con = DBConnection.connect()) {
            List<ComplaintHistoryDetail> history = new GetComplaintDao().getComplaintHistory(con, complaintId);

            if (history == null || history.isEmpty()) {
                JLabel empty = new JLabel("No actions recorded yet");
                empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                empty.setForeground(C_TEXT_MUTED);
                historyContent.add(empty);
                txtLastUpdate.setText("—");
            } else {
                Timestamp mostRecent = null;
                for (ComplaintHistoryDetail h : history) {
                    if (h.getDateTimeUpdated() != null) {
                        if (mostRecent == null || h.getDateTimeUpdated().after(mostRecent)) {
                            mostRecent = h.getDateTimeUpdated();
                        }
                    }
                }
                txtLastUpdate.setText(mostRecent != null ? mostRecent.toString() : "—");

                for (ComplaintHistoryDetail h : history) {
                    JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));
                    row.setOpaque(false);
                    row.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
                    row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));

                    JLabel date = new JLabel(h.getDateTimeUpdated() != null ? h.getDateTimeUpdated().toString() : "—");
                    date.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    JLabel st = new JLabel(h.getStatus());
                    st.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    st.setForeground(getStatusColor(h.getStatus()));

                    JLabel action = new JLabel(h.getProcess() != null ? h.getProcess() : "—");
                    action.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                    row.add(date);
                    row.add(st);
                    row.add(action);
                    historyContent.add(row);
                }
            }

        } catch (SQLException e) {
            JLabel err = new JLabel("Unable to load history");
            err.setForeground(Color.RED);
            historyContent.add(err);
            e.printStackTrace();
            txtLastUpdate.setText("—");
        }

        historyContent.revalidate();
        historyContent.repaint();
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case "Pending" -> C_PENDING;
            case "In Progress" -> C_IN_PROGRESS;
            case "Resolved" -> C_RESOLVED;
            case "Transferred" -> C_TRANSFERRED;
            case "Rejected" -> C_REJECTED;
            default -> C_TEXT_MUTED;
        };
    }

    private String safe(String v) {
        return v != null && !v.isBlank() ? v : "—";
    }
}
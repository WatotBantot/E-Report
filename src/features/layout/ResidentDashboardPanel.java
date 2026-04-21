package features.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// SWING IMPORTS
// ============================================================
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// ============================================================
// APPLICATION IMPORTS
// ============================================================
import app.E_Report;
import config.UIConfig;
import features.core.RecentReportsPanel;
import features.core.dashboardpanel.DashboardInfoCardsPanel;
import features.core.dashboardpanel.secretary.InfoPanel;

/**
 * SecretaryDashboardPanel - Main dashboard view for Secretary role
 * Displays statistics cards, recent reports table, activities, and tasks
 * All data is externally configurable through public API methods
 * No default data - panel starts empty until populated by controller
 */
public class ResidentDashboardPanel extends JPanel {

    // ============================================================
    // CONSTANTS - Table Configuration
    // ============================================================

    /** Column headers for the recent reports table */
    private static final String[] REPORT_TABLE_COLUMNS = {
            "Report ID", "Category", "Purok", "Date Submitted",
            "Last Update", "Status", "Action"
    };

    /** Column index for the action button in reports table */
    private static final int ACTION_COLUMN_INDEX = 6;

    /** Action button text for report rows */
    private static final String ACTION_BUTTON_TEXT = "View";

    /** Action button color for report rows */
    private static final Color ACTION_BUTTON_COLOR = new Color(120, 100, 200);

    // ============================================================
    // CONSTANTS - Layout Spacing
    // ============================================================

    /** Vertical gap between major sections in pixels */
    private static final int SECTION_GAP = 20;

    /** Maximum height for bottom panel in pixels */
    private static final int BOTTOM_PANEL_MAX_HEIGHT = 200;

    /** Border padding around entire dashboard in pixels */
    private static final int DASHBOARD_PADDING = 20;

    /** Bottom panel left column weight (activities) */
    private static final double BOTTOM_LEFT_WEIGHT = 0.6;

    /** Bottom panel right column weight (tasks) */
    private static final double BOTTOM_RIGHT_WEIGHT = 0.4;

    // ============================================================
    // INSTANCE VARIABLES - Application Reference
    // ============================================================

    /** Reference to main application for user info and navigation */
    protected E_Report app;

    // ============================================================
    // INSTANCE VARIABLES - UI Components
    // ============================================================

    /** Statistics cards panel (Total, Pending, In Progress, Resolved) */
    private DashboardInfoCardsPanel statsCards;

    /** Recent reports table with glassmorphism styling */
    private RecentReportsPanel reportsPanel;

    /** Recent activities info panel (60% width in bottom section) */
    private InfoPanel activitiesPanel;

    /** Tasks info panel (40% width in bottom section) */
    private InfoPanel tasksPanel;

    // ============================================================
    // INSTANCE VARIABLES - Configuration Data
    // ============================================================

    /** Icon paths for statistics cards loaded from UIConfig */
    private String[] statIconPaths;

    /** Current values for 4 statistics cards (index 0-3) */
    private int[] statValues;

    // ============================================================
    // INSTANCE VARIABLES - Mutable Data Storage
    // ============================================================

    /** Current report data entries stored for controller management */
    private List<Object[]> reportDataList;

    /** Current activity entries stored for controller management */
    private List<String> activityList;

    /** Current task entries stored for controller management */
    private List<String> taskList;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructs the Secretary dashboard with empty sections
     * Initializes data storage lists but does not populate content
     * Data must be injected via controller methods after construction
     * 
     * @param app Main application reference for context
     */
    public ResidentDashboardPanel(E_Report app) {
        this.app = app;
        this.statIconPaths = UIConfig.STAT_ICON_PATHS;
        this.statValues = new int[4];

        // Initialize data storage lists (empty)
        this.reportDataList = new ArrayList<>();

        initializeUI();
    }

    // ============================================================
    // PRIVATE UI INITIALIZATION METHODS
    // ============================================================

    /**
     * Initializes all UI components and layout
     * No data is populated - panel starts completely empty
     * 
     * @return void
     */
    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(
                DASHBOARD_PADDING, DASHBOARD_PADDING, DASHBOARD_PADDING, DASHBOARD_PADDING));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        // 1. STATS CARDS — full width, unchanged
        JPanel statsRow = new JPanel(new BorderLayout());
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsCards = new DashboardInfoCardsPanel(
                statValues[0], statValues[1], statValues[2], statValues[3], statIconPaths);
        statsRow.add(statsCards, BorderLayout.CENTER);
        wrapper.add(statsRow);
        wrapper.add(Box.createRigidArea(new Dimension(0, SECTION_GAP)));

        // 2. MAIN CONTENT — reports on left, activities+tasks stacked on right
        JPanel contentRow = new JPanel(new GridBagLayout());
        contentRow.setOpaque(false);
        contentRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // LEFT — Recent Reports (60% width, full height)
        reportsPanel = new RecentReportsPanel("My Recent Reports", REPORT_TABLE_COLUMNS);
        reportsPanel.setButtonColumn(ACTION_COLUMN_INDEX, ACTION_BUTTON_TEXT, ACTION_BUTTON_COLOR);
        reportsPanel.setOnViewClicked(row -> handleReportAction(row));

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, SECTION_GAP);
        contentRow.add(reportsPanel, gbc);

        wrapper.add(contentRow);
        wrapper.add(Box.createVerticalGlue());
        add(wrapper, BorderLayout.CENTER);

        // Seed data
        addReport(new Object[] { "R-001", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
        addReport(new Object[] { "R-002", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
        addReport(new Object[] { "R-003", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
        addReport(new Object[] { "R-004", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
        addReport(new Object[] { "R-005", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
        addReport(new Object[] { "R-006", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
        addReport(new Object[] { "R-007", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
        addReport(new Object[] { "R-008", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
        addReport(new Object[] { "R-009", "Noise", "Purok 1", "2026-04-20", "2026-04-21", "Pending", "View" });
    }

    // ============================================================
    // PRIVATE EVENT HANDLERS
    // ============================================================

    /**
     * Handles report action button clicks
     * 
     * @param row Index of the clicked row in the reports table
     * @return void
     */
    private void handleReportAction(int row) {
        if (row >= 0 && row < reportDataList.size()) {
            String reportId = (String) reportDataList.get(row)[0];
            JOptionPane.showMessageDialog(this, "Viewing Report: " + reportId);
        }
    }

    // ============================================================
    // PUBLIC API - Statistics Updates
    // ============================================================

    /**
     * Updates a specific statistics card value
     * 
     * @param cardIndex Card index (0=Total, 1=Pending, 2=In Progress, 3=Resolved)
     * @param value     New value to display
     * @return void
     */
    public void updateStatValue(int cardIndex, int value) {
        statValues[cardIndex] = value;
        statsCards.updateCardValue(cardIndex, value);
    }

    /**
     * Updates all statistics card values at once
     * 
     * @param total      Total Reports value
     * @param pending    Pending value
     * @param inProgress In Progress value
     * @param resolved   Resolved value
     * @return void
     */
    public void updateAllStatValues(int total, int pending, int inProgress, int resolved) {
        statValues[0] = total;
        statValues[1] = pending;
        statValues[2] = inProgress;
        statValues[3] = resolved;
        statsCards.updateValues(total, pending, inProgress, resolved);
    }

    // ============================================================
    // PUBLIC API - Report Management
    // ============================================================

    /**
     * Adds a new report row to the Recent Reports table
     * Stores in internal list for controller management
     * 
     * @param reportData Object array matching column structure:
     *                   {Report ID, Category, Purok, Date Submitted, Last Update,
     *                   Status, Action}
     * @return void
     */
    public void addReport(Object[] reportData) {
        if (reportData == null)
            return;

        Object[] finalData;

        // If DB gives only 6 columns, append "View"
        if (reportData.length == ACTION_COLUMN_INDEX) {
            finalData = new Object[ACTION_COLUMN_INDEX + 1];
            System.arraycopy(reportData, 0, finalData, 0, reportData.length);
            finalData[ACTION_COLUMN_INDEX] = ACTION_BUTTON_TEXT; // "View"
        }
        // If already 7 columns, force last column to "View"
        else if (reportData.length == ACTION_COLUMN_INDEX + 1) {
            finalData = reportData.clone();
            finalData[ACTION_COLUMN_INDEX] = ACTION_BUTTON_TEXT;
        } else {
            throw new IllegalArgumentException(
                    "Invalid report data length. Expected 6 or 7 columns.");
        }

        reportDataList.add(finalData);
        reportsPanel.addReport(finalData);
    }

    /**
     * Removes a report by index from the table and internal storage
     * 
     * @param index Row index to remove
     * @return void
     */
    public void removeReport(int index) {
        if (index >= 0 && index < reportDataList.size()) {
            reportDataList.remove(index);
            refreshReportsTable();
        }
    }

    /**
     * Clears all reports and repopulates with new data
     * 
     * @param reports List of report data arrays
     * @return void
     */
    public void setReports(List<Object[]> reports) {
        reportDataList.clear();
        reportDataList.addAll(reports);
        refreshReportsTable();
    }

    /**
     * Clears and refreshes the reports table from internal list
     * 
     * @return void
     */
    public void refreshReportsTable() {
        reportsPanel.clearReports();
        for (Object[] report : reportDataList) {
            reportsPanel.addReport(report);
        }
    }

    /**
     * Gets the current report data list for external manipulation
     * 
     * @return List of report data arrays
     */
    public List<Object[]> getReportDataList() {
        return new ArrayList<>(reportDataList);
    }

    // ============================================================
    // PUBLIC API - Bulk Operations
    // ============================================================

    /**
     * Clears all dashboard data (reports, activities, tasks)
     * 
     * @return void
     */
    public void clearAllData() {
        reportDataList.clear();
        activityList.clear();
        taskList.clear();
        refreshReportsTable();
    }

    /**
     * Checks if dashboard has any data loaded
     * 
     * @return true if all data lists are empty, false otherwise
     */
    public boolean isEmpty() {
        return reportDataList.isEmpty() &&
                activityList.isEmpty() &&
                taskList.isEmpty();
    }
}
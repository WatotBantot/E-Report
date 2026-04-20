package features.core.dashboardpanel.secretary;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * DashboardTable
 *
 * A customized JTable implementation used for dashboard displays with
 * simplified styling and utility methods for row management.
 *
 * This table removes grid lines, applies consistent row height and fonts,
 * and provides helper methods for adding and clearing rows through its
 * underlying DefaultTableModel.
 */
public class DashboardTable extends JTable {

    // ============================================================
    // INSTANCE VARIABLES (CONFIGURATION)
    // ============================================================

    /** Fixed row height used for all table rows. */
    private int rowHeight = 40;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructs a DashboardTable with predefined column names.
     *
     * Initializes the table using a DefaultTableModel and applies custom
     * styling for dashboard UI consistency.
     *
     * @param columnNames Array of column headers for the table.
     */
    public DashboardTable(String[] columnNames) {
        super(new DefaultTableModel(columnNames, 0));
        initializeStyling();
    }

    // ============================================================
    // INITIALIZATION METHODS
    // ============================================================

    /**
     * Applies visual styling to the table and its header.
     *
     * Configures font styles, row height, grid visibility, spacing, and
     * header appearance to match the dashboard UI design.
     */
    private void initializeStyling() {

        setRowHeight(rowHeight);
        setFont(new Font("Arial", Font.PLAIN, 12));

        setOpaque(false);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));

        getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        getTableHeader().setBackground(new Color(240, 240, 240, 200));
        getTableHeader().setOpaque(false);
        getTableHeader().setPreferredSize(new Dimension(0, 35));
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Adds a new row to the table.
     *
     * Inserts data into the underlying DefaultTableModel.
     *
     * @param rowData Array of objects representing row values.
     */
    public void addRow(Object[] rowData) {
        ((DefaultTableModel) getModel()).addRow(rowData);
    }

    /**
     * Clears all rows from the table.
     *
     * Resets the underlying DefaultTableModel row count to zero.
     */
    public void clearRows() {
        ((DefaultTableModel) getModel()).setRowCount(0);
    }
}
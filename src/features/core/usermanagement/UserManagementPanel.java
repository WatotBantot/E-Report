package features.core.usermanagement;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import features.components.UIComboBox;
import features.components.UIButton;
import config.UIConfig;

/**
 * User Management Panel - Displays user list with filters and pagination
 * Columns: Name, Role, Purok, Phone, Action (Edit, Ban/Unban)
 */
public class UserManagementPanel extends JPanel {

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField nameFilterField;
    private UIComboBox<String> roleFilterCombo;
    private UIComboBox<String> purokFilterCombo;
    private UIComboBox<String> streetFilterCombo;
    private UIComboBox<String> statusFilterCombo;
    private JLabel paginationLabel;
    private UIButton prevButton;
    private UIButton nextButton;

    private int currentPage = 1;
    private int totalPages = 5;
    private int itemsPerPage = 10;
    private int totalItems = 50;

    private UserActionListener actionListener;
    private FilterListener filterListener;

    public interface UserActionListener {
        void onEdit(int rowIndex, UserData user);

        void onBanToggle(int rowIndex, UserData user, boolean currentlyBanned);
    }

    public interface FilterListener {
        void onFilterChanged(String name, String role, String purok, String street, String status);
    }

    public UserManagementPanel(UserActionListener actionListener, FilterListener filterListener) {
        this.actionListener = actionListener;
        this.filterListener = filterListener;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Manage Users");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);

        // Filter Panel
        JPanel filterPanel = createFilterPanel();

        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setOpaque(false);
        northPanel.add(titlePanel, BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Pagination Panel
        JPanel paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        panel.setOpaque(false);

        // Name filter
        nameFilterField = new JTextField(15);
        nameFilterField.setPreferredSize(new Dimension(150, 32));
        nameFilterField.setFont(UIConfig.BODY);
        nameFilterField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        panel.add(createLabeledComponent("Name:", nameFilterField));

        // Role filter
        String[] roles = { "All Roles", "Resident", "Secretary", "Barangay Captain", "Admin" };
        roleFilterCombo = new UIComboBox<>(roles);
        roleFilterCombo.setPreferredSize(new Dimension(140, 32));
        panel.add(createLabeledComponent("Role:", roleFilterCombo));

        // Purok filter
        String[] puroks = { "All Puroks", "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5" };
        purokFilterCombo = new UIComboBox<>(puroks);
        purokFilterCombo.setPreferredSize(new Dimension(130, 32));
        panel.add(createLabeledComponent("Purok:", purokFilterCombo));

        // Street filter
        String[] streets = { "All Streets", "Main Street", "2nd Street", "3rd Street", "Oak Street" };
        streetFilterCombo = new UIComboBox<>(streets);
        streetFilterCombo.setPreferredSize(new Dimension(140, 32));
        panel.add(createLabeledComponent("Street:", streetFilterCombo));

        // Status filter
        String[] statuses = { "All Status", "Active", "Banned" };
        statusFilterCombo = new UIComboBox<>(statuses);
        statusFilterCombo.setPreferredSize(new Dimension(120, 32));
        panel.add(createLabeledComponent("Status:", statusFilterCombo));

        // Apply filter button
        UIButton applyFilterBtn = new UIButton(
                "Filter",
                new Color(25, 118, 210),
                new Dimension(80, 32),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.PRIMARY);
        applyFilterBtn.addActionListener(e -> notifyFilterChanged());
        panel.add(Box.createHorizontalStrut(10));
        panel.add(applyFilterBtn);

        return panel;
    }

    private JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(UIConfig.BODY);
        label.setForeground(new Color(100, 100, 100));

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "Name", "Role", "Purok", "Phone", "Action" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(45);
        userTable.setFont(UIConfig.BODY);
        userTable.setShowGrid(false);
        userTable.setIntercellSpacing(new Dimension(0, 0));
        userTable.setOpaque(false);

        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(new Color(50, 50, 50));
        header.setBackground(new Color(240, 245, 250));
        header.setPreferredSize(new Dimension(0, 40));

        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(200);
        columnModel.getColumn(1).setPreferredWidth(120);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(150);
        columnModel.getColumn(4).setPreferredWidth(180);

        columnModel.getColumn(4).setCellRenderer(new ActionCellRenderer());
        columnModel.getColumn(4).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        loadSampleData();

        return panel;
    }

    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        paginationLabel = new JLabel("Showing 1 to 10 of 50 Users");
        paginationLabel.setFont(UIConfig.BODY);
        paginationLabel.setForeground(new Color(80, 80, 80));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        prevButton = new UIButton(
                "Previous",
                Color.WHITE,
                new Dimension(80, 32),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.OUTLINED);
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> goToPage(currentPage - 1));

        nextButton = new UIButton(
                "Next",
                new Color(25, 118, 210),
                new Dimension(70, 32),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.PRIMARY);
        nextButton.addActionListener(e -> goToPage(currentPage + 1));

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        panel.add(paginationLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void loadSampleData() {
        addUserRow(new UserData("Juan C. DelaCruz", "Resident", "Purok 1", "0912-456-7891", false));
        addUserRow(new UserData("Maria Santos", "Secretary", "Purok 2", "0918-123-4567", false));
        addUserRow(new UserData("Pedro Reyes", "Barangay Captain", "Purok 1", "0917-987-6543", false));
        addUserRow(new UserData("Ana Garcia", "Resident", "Purok 3", "0915-555-8888", true));
        addUserRow(new UserData("Jose Martinez", "Resident", "Purok 2", "0919-777-3333", false));
    }

    public void addUserRow(UserData user) {
        Object[] row = {
                user.getName(),
                user.getRole(),
                user.getPurok(),
                user.getPhone(),
                user
        };
        tableModel.addRow(row);
    }

    private void notifyFilterChanged() {
        if (filterListener != null) {
            filterListener.onFilterChanged(
                    nameFilterField.getText(),
                    (String) roleFilterCombo.getSelectedItem(),
                    (String) purokFilterCombo.getSelectedItem(),
                    (String) streetFilterCombo.getSelectedItem(),
                    (String) statusFilterCombo.getSelectedItem());
        }
    }

    private void goToPage(int page) {
        if (page < 1 || page > totalPages)
            return;
        currentPage = page;

        int start = (currentPage - 1) * itemsPerPage + 1;
        int end = Math.min(currentPage * itemsPerPage, totalItems);
        paginationLabel.setText("Showing " + start + " to " + end + " of " + totalItems + " Users");

        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    // Cell renderer and editor classes...
    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        public ActionCellRenderer() {
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(false);

            if (value instanceof UserData) {
                UserData user = (UserData) value;

                JButton editBtn = new JButton("Edit");
                editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                editBtn.setPreferredSize(new Dimension(60, 28));

                JButton banBtn = new JButton(user.isBanned() ? "Unban" : "Ban");
                banBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                banBtn.setPreferredSize(new Dimension(70, 28));
                banBtn.setForeground(user.isBanned() ? new Color(0, 150, 0) : new Color(200, 50, 50));

                panel.add(editBtn);
                panel.add(banBtn);
            }

            return panel;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private UserData currentUser;
        private int currentRow;

        public ActionCellEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(false);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            panel.removeAll();
            currentRow = row;

            if (value instanceof UserData) {
                currentUser = (UserData) value;

                UIButton editBtn = new UIButton(
                        "Edit",
                        new Color(25, 118, 210),
                        new Dimension(60, 28),
                        new Font("Segoe UI", Font.PLAIN, 11),
                        6,
                        UIButton.ButtonType.PRIMARY);
                editBtn.addActionListener(e -> {
                    fireEditingStopped();
                    if (actionListener != null) {
                        actionListener.onEdit(currentRow, currentUser);
                    }
                });

                UIButton banBtn = new UIButton(
                        currentUser.isBanned() ? "Unban" : "Ban",
                        currentUser.isBanned() ? new Color(0, 150, 0) : new Color(200, 50, 50),
                        new Dimension(70, 28),
                        new Font("Segoe UI", Font.PLAIN, 11),
                        6,
                        UIButton.ButtonType.PRIMARY);
                banBtn.addActionListener(e -> {
                    fireEditingStopped();
                    if (actionListener != null) {
                        actionListener.onBanToggle(currentRow, currentUser, currentUser.isBanned());
                    }
                });

                panel.add(editBtn);
                panel.add(banBtn);
            }

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentUser;
        }
    }

    public void clearFilters() {
        nameFilterField.setText("");
        roleFilterCombo.setSelectedIndex(0);
        purokFilterCombo.setSelectedIndex(0);
        streetFilterCombo.setSelectedIndex(0);
        statusFilterCombo.setSelectedIndex(0);
    }
}
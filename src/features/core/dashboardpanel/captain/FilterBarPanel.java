package features.core.dashboardpanel.captain;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Date;
import config.UIConfig;
import features.components.UIComboBox;
import features.components.UIButton;
import features.components.UIInput;
import features.components.filter.TimeFilter;
import features.components.filter.TimeFilterPanel;

/**
 * FilterBarPanel (Pill Design - Fixed)
 */
public class FilterBarPanel extends JPanel {

    // -------------------------------------------------------------------------
    // COMPONENTS
    // -------------------------------------------------------------------------
    private TimeFilterPanel timeFilterPanel;
    private UIComboBox<String> categoryCombo;
    private UIComboBox<String> purokCombo;
    private UIComboBox<String> statusCombo;
    private UIInput searchField;
    private UIButton applyButton;
    private JButton moreFiltersButton;
    private JPanel advancedFiltersRow;
    private JPanel activeFiltersChips;

    // -------------------------------------------------------------------------
    // STATE
    // -------------------------------------------------------------------------
    private final Mode currentMode;
    private boolean advancedFiltersVisible = false;
    private FilterListener filterListener;
    private SearchListener searchListener;

    // -------------------------------------------------------------------------
    // STYLE CONSTANTS
    // -------------------------------------------------------------------------
    private static final Color BAR_BG = new Color(245, 247, 250);
    private static final Color TIME_COLOR = new Color(25, 118, 210);
    private static final Color CATEGORY_COLOR = new Color(156, 39, 176);
    private static final Color PUROK_COLOR = new Color(46, 125, 50);
    private static final Color STATUS_COLOR = new Color(230, 81, 0);
    private static final Color SEARCH_COLOR = new Color(0, 150, 136);
    private static final Color FIELD_BG = Color.WHITE;
    private static final Color FIELD_BORDER = new Color(220, 224, 230);
    private static final Color MORE_BTN_COLOR = new Color(100, 110, 130);

    private static final int PILL_RADIUS = 20;
    private static final int FIELD_HEIGHT = 34;
    private static final int H_GAP = 8;

    // ===================================================================
    // NESTED TYPES
    // ===================================================================
    public enum Mode {
        DASHBOARD, SEARCH
    }

    public interface FilterListener {
        void onApply(Date fromDate, Date toDate, String category, String purok, String status);

        void onReset();
    }

    public interface SearchListener {
        void onSearch(String searchText, String category, Date fromDate, Date toDate);

        void onClearSearch();
    }

    // ===================================================================
    // CONSTRUCTORS
    // ===================================================================
    public FilterBarPanel(String[] categories, String[] puroks, String[] statuses, FilterListener listener) {
        this.currentMode = Mode.DASHBOARD;
        this.filterListener = listener;
        initializeDashboard(categories, puroks, statuses);
    }

    public FilterBarPanel(FilterListener listener) {
        this(new String[] { "All Categories", "Theft", "Vandalism", "Scam", "Others" },
                new String[] { "All Puroks", "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5" },
                new String[] { "All Statuses", "Submitted", "Pending", "In Progress", "Resolved", "Invalid" },
                listener);
    }

    public FilterBarPanel(String[] categories, SearchListener listener) {
        this.currentMode = Mode.SEARCH;
        this.searchListener = listener;
        initializeSearch(categories);
    }

    public FilterBarPanel(SearchListener listener) {
        this(new String[] { "All Categories", "Theft", "Vandalism", "Scam", "Others" }, listener);
    }

    // ===================================================================
    // INITIALIZATION — DASHBOARD MODE
    // ===================================================================
    private void initializeDashboard(String[] categories, String[] puroks, String[] statuses) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel mainBar = new JPanel();
        mainBar.setLayout(new BoxLayout(mainBar, BoxLayout.Y_AXIS));
        mainBar.setOpaque(true);
        mainBar.setBackground(BAR_BG);
        mainBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 215, 225), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        // === TOP ROW: Use BorderLayout for left/right alignment ===
        JPanel topWrapper = new JPanel(new BorderLayout(0, 0));
        topWrapper.setOpaque(false);
        topWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT + 10));

        JPanel leftRow = new JPanel(new FlowLayout(FlowLayout.LEFT, H_GAP, 0));
        leftRow.setOpaque(false);

        timeFilterPanel = new TimeFilterPanel();
        timeFilterPanel.setFilterType(TimeFilter.FilterType.SINGLE_YEAR);
        addColorIndicator(leftRow, timeFilterPanel, TIME_COLOR, "Time Period");

        categoryCombo = new UIComboBox<>(categories);
        categoryCombo.setPreferredSize(new Dimension(150, FIELD_HEIGHT));
        styleComboAsPill(categoryCombo, CATEGORY_COLOR);
        addColorIndicator(leftRow, categoryCombo, CATEGORY_COLOR, "Category");

        moreFiltersButton = createMoreFiltersButton();
        moreFiltersButton.addActionListener(e -> toggleAdvancedFilters());
        leftRow.add(Box.createHorizontalStrut(8));
        leftRow.add(moreFiltersButton);

        topWrapper.add(leftRow, BorderLayout.WEST);

        JPanel rightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, H_GAP, 0));
        rightRow.setOpaque(false);

        applyButton = createSolidPillButton("Apply", TIME_COLOR);
        applyButton.addActionListener(e -> {
            notifyApply();
            updateActiveChips();
        });
        rightRow.add(applyButton);

        JButton resetBtn = createTextButton("Reset");
        resetBtn.addActionListener(e -> notifyReset());
        rightRow.add(resetBtn);

        topWrapper.add(rightRow, BorderLayout.EAST);
        mainBar.add(topWrapper);

        // === ADVANCED ROW ===
        advancedFiltersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, H_GAP, 0));
        advancedFiltersRow.setOpaque(true);
        advancedFiltersRow.setBackground(new Color(245, 247, 250));
        advancedFiltersRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)));
        advancedFiltersRow.setVisible(false);
        advancedFiltersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT + 20));

        purokCombo = new UIComboBox<>(puroks);
        purokCombo.setPreferredSize(new Dimension(140, FIELD_HEIGHT));
        styleComboAsPill(purokCombo, PUROK_COLOR);
        addColorIndicator(advancedFiltersRow, purokCombo, PUROK_COLOR, "Purok");

        statusCombo = new UIComboBox<>(statuses);
        statusCombo.setPreferredSize(new Dimension(150, FIELD_HEIGHT));
        styleComboAsPill(statusCombo, STATUS_COLOR);
        addColorIndicator(advancedFiltersRow, statusCombo, STATUS_COLOR, "Status");

        mainBar.add(advancedFiltersRow);

        // === ACTIVE FILTERS CHIPS ===
        activeFiltersChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        activeFiltersChips.setOpaque(false);
        activeFiltersChips.setVisible(false);
        activeFiltersChips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        mainBar.add(activeFiltersChips);

        add(mainBar, BorderLayout.CENTER);
    }

    // ===================================================================
    // INITIALIZATION — SEARCH MODE
    // ===================================================================
    private void initializeSearch(String[] categories) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel mainBar = new JPanel(new BorderLayout(0, 0));
        mainBar.setOpaque(true);
        mainBar.setBackground(BAR_BG);
        mainBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 215, 225), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, H_GAP, 0));
        row.setOpaque(false);

        searchField = new UIInput(25);
        searchField.setPreferredSize(new Dimension(220, FIELD_HEIGHT));
        searchField.setPlaceholder("Search reports...");
        styleInputAsPill(searchField, SEARCH_COLOR);
        addColorIndicator(row, searchField, SEARCH_COLOR, "Search");

        timeFilterPanel = new TimeFilterPanel();
        timeFilterPanel.setFilterType(TimeFilter.FilterType.SINGLE_YEAR);
        addColorIndicator(row, timeFilterPanel, TIME_COLOR, "Period");

        categoryCombo = new UIComboBox<>(categories);
        categoryCombo.setPreferredSize(new Dimension(150, FIELD_HEIGHT));
        styleComboAsPill(categoryCombo, CATEGORY_COLOR);
        addColorIndicator(row, categoryCombo, CATEGORY_COLOR, "Category");

        mainBar.add(row, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, H_GAP, 0));
        rightPanel.setOpaque(false);

        applyButton = createSolidPillButton("Search", SEARCH_COLOR);
        applyButton.addActionListener(e -> notifySearch());
        rightPanel.add(applyButton);

        JButton clearBtn = createTextButton("Clear");
        clearBtn.addActionListener(e -> notifyClearSearch());
        rightPanel.add(clearBtn);

        mainBar.add(rightPanel, BorderLayout.EAST);
        add(mainBar, BorderLayout.CENTER);
    }

    // ===================================================================
    // COLOR-CODED PILL STYLING
    // ===================================================================
    private void addColorIndicator(JPanel container, JComponent component, Color color, String tooltip) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, PILL_RADIUS, PILL_RADIUS);
                g2.setColor(FIELD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, PILL_RADIUS, PILL_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);

        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(4, FIELD_HEIGHT - 4));
        indicator.setBackground(color);
        indicator.setOpaque(true);
        indicator.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        wrapper.add(indicator, BorderLayout.WEST);
        wrapper.add(component, BorderLayout.CENTER);
        wrapper.setToolTipText(tooltip);
        container.add(wrapper);
    }

    private void styleComboAsPill(UIComboBox<?> combo, Color accentColor) {
        combo.setOpaque(true);
        combo.setFont(UIConfig.BODY.deriveFont(Font.PLAIN, 12f));
        combo.setForeground(new Color(60, 60, 60));
        combo.setForcePlainBackground(true);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setFont(UIConfig.BODY.deriveFont(Font.PLAIN, 12f));
                label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 4));

                if (value != null && value.toString().startsWith("All ")) {
                    label.setForeground(new Color(150, 150, 150));
                    label.setFont(UIConfig.BODY.deriveFont(Font.ITALIC, 12f));
                } else {
                    label.setForeground(new Color(60, 60, 60));
                }

                return label;
            }
        });
        combo.setSelectedIndex(0);
    }

    private void styleInputAsPill(UIInput input, Color accentColor) {
        input.setOpaque(true);
        input.setFont(UIConfig.BODY.deriveFont(Font.PLAIN, 12f));
    }

    // ===================================================================
    // BUTTON FACTORIES
    // ===================================================================
    private JButton createMoreFiltersButton() {
        JButton btn = new JButton("More ▼") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(200, 205, 215));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(220, 225, 235));
                } else {
                    g2.setColor(new Color(235, 238, 242));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), PILL_RADIUS, PILL_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(UIConfig.BODY.deriveFont(Font.PLAIN, 12f));
        btn.setForeground(MORE_BTN_COLOR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setPreferredSize(new Dimension(80, FIELD_HEIGHT));
        return btn;
    }

    private JButton createTextButton(String text) {
        UIButton btn = new UIButton(text, null, new Dimension(84, FIELD_HEIGHT),
                new Font("Arial", Font.PLAIN, 12), PILL_RADIUS, UIButton.ButtonType.OUTLINED);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setForeground(MORE_BTN_COLOR);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(new Color(80, 90, 110));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(MORE_BTN_COLOR);
            }
        });
        return btn;
    }

    private UIButton createSolidPillButton(String text, Color bgColor) {
        UIButton btn = new UIButton(text, bgColor, new Dimension(85, FIELD_HEIGHT),
                new Font("Arial", Font.PLAIN, 12), PILL_RADIUS, UIButton.ButtonType.PRIMARY);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    // ===================================================================
    // ACTIVE FILTER CHIPS
    // ===================================================================
    private void updateActiveChips() {
        activeFiltersChips.removeAll();

        String timeDesc = timeFilterPanel.getFilterDescription();
        if (!timeDesc.equals(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))) {
            activeFiltersChips.add(createFilterChip(timeDesc, TIME_COLOR, () -> {
                timeFilterPanel.reset();
                updateActiveChips();
            }));
        }

        String cat = (String) categoryCombo.getSelectedItem();
        if (cat != null && !cat.startsWith("All ")) {
            activeFiltersChips.add(createFilterChip(cat, CATEGORY_COLOR, () -> {
                categoryCombo.setSelectedIndex(0);
                updateActiveChips();
            }));
        }

        if (advancedFiltersVisible) {
            String purok = (String) purokCombo.getSelectedItem();
            if (purok != null && !purok.startsWith("All ")) {
                activeFiltersChips.add(createFilterChip(purok, PUROK_COLOR, () -> {
                    purokCombo.setSelectedIndex(0);
                    updateActiveChips();
                }));
            }

            String status = (String) statusCombo.getSelectedItem();
            if (status != null && !status.startsWith("All ")) {
                activeFiltersChips.add(createFilterChip(status, STATUS_COLOR, () -> {
                    statusCombo.setSelectedIndex(0);
                    updateActiveChips();
                }));
            }
        }

        activeFiltersChips.setVisible(activeFiltersChips.getComponentCount() > 0);
        activeFiltersChips.revalidate();
        activeFiltersChips.repaint();
    }

    private JPanel createFilterChip(String text, Color color, Runnable onRemove) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chip.setOpaque(false);

        JPanel rounded = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rounded.setOpaque(false);
        rounded.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 8));

        JLabel label = new JLabel(text);
        label.setFont(UIConfig.BODY.deriveFont(Font.PLAIN, 11f));
        label.setForeground(Color.WHITE);
        rounded.add(label);

        JLabel remove = new JLabel("×");
        remove.setFont(UIConfig.BODY.deriveFont(Font.BOLD, 13f));
        remove.setForeground(new Color(255, 255, 255, 180));
        remove.setCursor(new Cursor(Cursor.HAND_CURSOR));
        remove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onRemove.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                remove.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                remove.setForeground(new Color(255, 255, 255, 180));
            }
        });
        rounded.add(remove);

        chip.add(rounded);
        return chip;
    }

    // ===================================================================
    // INTERACTION
    // ===================================================================
    private void toggleAdvancedFilters() {
        advancedFiltersVisible = !advancedFiltersVisible;
        moreFiltersButton.setText(advancedFiltersVisible ? "Less ▲" : "More ▼");
        advancedFiltersRow.setVisible(advancedFiltersVisible);

        advancedFiltersRow.revalidate();
        Container mainBar = advancedFiltersRow.getParent();
        if (mainBar != null) {
            mainBar.revalidate();
            mainBar.repaint();
        }
        this.revalidate();
        this.repaint();
        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    // ===================================================================
    // NOTIFICATION
    // ===================================================================
    private void notifyApply() {
        if (filterListener != null) {
            filterListener.onApply(
                    timeFilterPanel.getStartDate(),
                    timeFilterPanel.getEndDate(),
                    (String) categoryCombo.getSelectedItem(),
                    (String) purokCombo.getSelectedItem(),
                    (String) statusCombo.getSelectedItem());
        }
    }

    private void notifyReset() {
        timeFilterPanel.reset();
        categoryCombo.setSelectedIndex(0);
        if (purokCombo != null)
            purokCombo.setSelectedIndex(0);
        if (statusCombo != null)
            statusCombo.setSelectedIndex(0);
        if (advancedFiltersVisible)
            toggleAdvancedFilters();
        updateActiveChips();
        if (filterListener != null)
            filterListener.onReset();
    }

    private void notifySearch() {
        if (searchListener != null) {
            searchListener.onSearch(
                    searchField.getValue(),
                    (String) categoryCombo.getSelectedItem(),
                    timeFilterPanel.getStartDate(),
                    timeFilterPanel.getEndDate());
        }
    }

    private void notifyClearSearch() {
        searchField.setText("");
        timeFilterPanel.reset();
        categoryCombo.setSelectedIndex(0);
        if (searchListener != null)
            searchListener.onClearSearch();
    }

    // ===================================================================
    // GETTERS
    // ===================================================================
    public TimeFilterPanel getDateFromPicker() {
        return timeFilterPanel;
    }

    public TimeFilterPanel getDateToPicker() {
        return timeFilterPanel;
    }

    public UIComboBox<String> getCategoryCombo() {
        return categoryCombo;
    }

    public UIComboBox<String> getPurokCombo() {
        return purokCombo;
    }

    public UIComboBox<String> getStatusCombo() {
        return statusCombo;
    }

    public UIInput getSearchField() {
        return searchField;
    }

    public String getFromDateString() {
        return new java.text.SimpleDateFormat("MM/dd/yyyy").format(timeFilterPanel.getStartDate());
    }

    public String getToDateString() {
        return new java.text.SimpleDateFormat("MM/dd/yyyy").format(timeFilterPanel.getEndDate());
    }

    public String getSearchText() {
        return searchField != null ? searchField.getValue() : "";
    }

    public TimeFilter getTimeFilter() {
        return timeFilterPanel.getTimeFilter();
    }

    public String getTimeFilterDescription() {
        return timeFilterPanel.getFilterDescription();
    }

    public Mode getCurrentMode() {
        return currentMode;
    }
}
package features.components.filter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Date;

import features.components.UIComboBox;

/**
 * TimeFilterPanel
 * 
 * A replacement for ModernDatePicker that provides structured time-based
 * filtering
 * instead of free-form date selection. Supports five filter modes:
 * 
 * 1. Span of Years: Select start and end year
 * 2. Single Year: Select one year
 * 3. Span of Months: Select year, start month, and end month
 * 4. Single Month: Select year and month
 * 5. Single Week: Select year, month, and week number
 * 
 * All modes default to the current system year. The panel dynamically
 * shows/hides
 * relevant dropdowns based on the selected filter type.
 * 
 * This component is designed to be backward-compatible with code expecting
 * date-based filtering by providing computed Date objects via getStartDate()
 * and getEndDate().
 */
public class TimeFilterPanel extends JPanel {

    // ========================================================================
    // UI COMPONENTS
    // ========================================================================

    private UIComboBox<String> filterTypeCombo;
    private UIComboBox<Integer> yearCombo;
    private UIComboBox<Integer> startYearCombo; // For year span
    private UIComboBox<Integer> endYearCombo; // For year span
    private UIComboBox<String> monthCombo;
    private UIComboBox<String> startMonthCombo; // For month span
    private UIComboBox<String> endMonthCombo; // For month span
    private UIComboBox<Integer> weekCombo;

    // ========================================================================
    // STATE
    // ========================================================================

    private int currentYear;
    private TimeFilter currentFilter;
    private java.util.List<FilterChangeListener> listeners = new java.util.ArrayList<>();

    // ========================================================================
    // CONSTANTS
    // ========================================================================

    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private static final String[] FILTER_TYPES = {
            "Span of Years",
            "Single Year",
            "Span of Months",
            "Single Month",
            "Single Week"
    };

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     * Constructs a TimeFilterPanel with default settings (current year, single year
     * mode).
     */
    public TimeFilterPanel() {
        this.currentYear = Calendar.getInstance().get(Calendar.YEAR);
        initialize();
        setFilterType(TimeFilter.FilterType.SINGLE_YEAR);
    }

    /**
     * Constructs a TimeFilterPanel initialized to a specific filter.
     * 
     * @param initialFilter The initial filter configuration
     */
    public TimeFilterPanel(TimeFilter initialFilter) {
        this.currentYear = Calendar.getInstance().get(Calendar.YEAR);
        this.currentFilter = initialFilter;
        initialize();
        syncUIFromFilter(initialFilter);
    }

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    private void initialize() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        setOpaque(false);

        // Filter type selector
        filterTypeCombo = new UIComboBox<>(FILTER_TYPES);
        filterTypeCombo.setPreferredSize(new Dimension(140, 32));
        filterTypeCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        filterTypeCombo.setOpaque(true);
        filterTypeCombo.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                        BorderFactory.createEmptyBorder(2, 8, 2, 4)));
        filterTypeCombo.addActionListener(e -> onFilterTypeChanged());
        add(wrapWithLabel("Filter By:", filterTypeCombo));

        // Year range combos (for span of years)
        startYearCombo = createYearCombo(currentYear - 5, currentYear + 5);
        endYearCombo = createYearCombo(currentYear - 5, currentYear + 5);
        add(wrapWithLabel("Start Year:", startYearCombo));
        add(wrapWithLabel("End Year:", endYearCombo));

        // Single year combo
        yearCombo = createYearCombo(currentYear - 5, currentYear + 5);
        yearCombo.setSelectedItem(currentYear);
        add(wrapWithLabel("Year:", yearCombo));

        // Month combos
        startMonthCombo = new UIComboBox<>(MONTH_NAMES);
        startMonthCombo.setPreferredSize(new Dimension(100, 32));
        endMonthCombo = new UIComboBox<>(MONTH_NAMES);
        endMonthCombo.setPreferredSize(new Dimension(100, 32));
        monthCombo = new UIComboBox<>(MONTH_NAMES);
        monthCombo.setPreferredSize(new Dimension(100, 32));

        add(wrapWithLabel("Start Month:", startMonthCombo));
        add(wrapWithLabel("End Month:", endMonthCombo));
        add(wrapWithLabel("Month:", monthCombo));

        // Week combo (1-6, since max weeks in month is 6)
        Integer[] weeks = { 1, 2, 3, 4, 5, 6 };
        weekCombo = new UIComboBox<>(weeks);
        weekCombo.setPreferredSize(new Dimension(70, 32));
        add(wrapWithLabel("Week:", weekCombo));

        // Add change listeners to all combos
        addChangeListeners();

        // Initial visibility update
        updateVisibility();
    }

    private UIComboBox<Integer> createYearCombo(int startYear, int endYear) {
        java.util.List<Integer> years = new java.util.ArrayList<>();
        for (int y = startYear; y <= endYear; y++)
            years.add(y);

        UIComboBox<Integer> combo = new UIComboBox<>(years.toArray(new Integer[0]));
        combo.setPreferredSize(new Dimension(90, 32));
        combo.setFont(new Font("Arial", Font.PLAIN, 12));
        combo.setOpaque(true);

        // Rounded border matching pill theme
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 4)));

        return combo;
    }

    private JPanel wrapWithLabel(String text, JComponent component) {
        // NO labels — components are self-explanatory or use tooltips
        component.setToolTipText(text);

        // Just return the component wrapped in minimal padding
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private void addChangeListeners() {
        ActionListener updateListener = e -> updateCurrentFilter();

        filterTypeCombo.addActionListener(updateListener);
        yearCombo.addActionListener(updateListener);
        startYearCombo.addActionListener(updateListener);
        endYearCombo.addActionListener(updateListener);
        monthCombo.addActionListener(updateListener);
        startMonthCombo.addActionListener(updateListener);
        endMonthCombo.addActionListener(updateListener);
        weekCombo.addActionListener(updateListener);
    }

    // ========================================================================
    // FILTER TYPE HANDLING
    // ========================================================================

    private void onFilterTypeChanged() {
        updateVisibility();
        updateCurrentFilter();
    }

    private void updateVisibility() {
        int typeIndex = filterTypeCombo.getSelectedIndex();

        // Hide all first
        startYearCombo.getParent().setVisible(false);
        endYearCombo.getParent().setVisible(false);
        yearCombo.getParent().setVisible(false);
        startMonthCombo.getParent().setVisible(false);
        endMonthCombo.getParent().setVisible(false);
        monthCombo.getParent().setVisible(false);
        weekCombo.getParent().setVisible(false);

        // Show relevant based on type
        switch (typeIndex) {
            case 0: // Span of Years
                startYearCombo.getParent().setVisible(true);
                endYearCombo.getParent().setVisible(true);
                break;
            case 1: // Single Year
                yearCombo.getParent().setVisible(true);
                break;
            case 2: // Span of Months
                yearCombo.getParent().setVisible(true);
                startMonthCombo.getParent().setVisible(true);
                endMonthCombo.getParent().setVisible(true);
                break;
            case 3: // Single Month
                yearCombo.getParent().setVisible(true);
                monthCombo.getParent().setVisible(true);
                break;
            case 4: // Single Week
                yearCombo.getParent().setVisible(true);
                monthCombo.getParent().setVisible(true);
                weekCombo.getParent().setVisible(true);
                break;
        }

        revalidate();
        repaint();
    }

    // ========================================================================
    // FILTER COMPUTATION
    // ========================================================================

    private void updateCurrentFilter() {
        int typeIndex = filterTypeCombo.getSelectedIndex();
        TimeFilter newFilter = null;

        try {
            switch (typeIndex) {
                case 0: // Span of Years
                    int sYear = (Integer) startYearCombo.getSelectedItem();
                    int eYear = (Integer) endYearCombo.getSelectedItem();
                    if (sYear > eYear) {
                        int temp = sYear;
                        sYear = eYear;
                        eYear = temp;
                    }
                    newFilter = TimeFilter.forYearSpan(sYear, eYear);
                    break;

                case 1: // Single Year
                    newFilter = TimeFilter.forSingleYear((Integer) yearCombo.getSelectedItem());
                    break;

                case 2: // Span of Months
                    int year = (Integer) yearCombo.getSelectedItem();
                    int sMonth = startMonthCombo.getSelectedIndex();
                    int eMonth = endMonthCombo.getSelectedIndex();
                    if (sMonth > eMonth) {
                        int temp = sMonth;
                        sMonth = eMonth;
                        eMonth = temp;
                    }
                    newFilter = TimeFilter.forMonthSpan(year, sMonth, eMonth);
                    break;

                case 3: // Single Month
                    newFilter = TimeFilter.forSingleMonth(
                            (Integer) yearCombo.getSelectedItem(),
                            monthCombo.getSelectedIndex());
                    break;

                case 4: // Single Week
                    newFilter = TimeFilter.forSingleWeek(
                            (Integer) yearCombo.getSelectedItem(),
                            monthCombo.getSelectedIndex(),
                            (Integer) weekCombo.getSelectedItem());
                    break;
            }
        } catch (Exception ex) {
            newFilter = TimeFilter.forSingleYear(currentYear);
        }

        if (newFilter != null && !newFilter.equals(currentFilter)) {
            currentFilter = newFilter;
            notifyListeners();
        }
    }

    private void syncUIFromFilter(TimeFilter filter) {
        if (filter == null)
            return;

        filterTypeCombo.setSelectedIndex(filter.getFilterType().ordinal());

        switch (filter.getFilterType()) {
            case SPAN_OF_YEARS:
                startYearCombo.setSelectedItem(filter.getStartYear());
                endYearCombo.setSelectedItem(filter.getEndYear());
                break;
            case SINGLE_YEAR:
                yearCombo.setSelectedItem(filter.getYear());
                break;
            case SPAN_OF_MONTHS:
                yearCombo.setSelectedItem(filter.getYear());
                startMonthCombo.setSelectedIndex(filter.getStartMonth());
                endMonthCombo.setSelectedIndex(filter.getEndMonth());
                break;
            case SINGLE_MONTH:
                yearCombo.setSelectedItem(filter.getYear());
                monthCombo.setSelectedIndex(filter.getMonth());
                break;
            case SINGLE_WEEK:
                yearCombo.setSelectedItem(filter.getYear());
                monthCombo.setSelectedIndex(filter.getMonth());
                weekCombo.setSelectedItem(filter.getWeekOfMonth());
                break;
        }

        updateVisibility();
    }

    // ========================================================================
    // LISTENER INTERFACE
    // ========================================================================

    /**
     * Listener interface for filter change events.
     */
    public interface FilterChangeListener {
        void onFilterChanged(TimeFilter newFilter);
    }

    public void addFilterChangeListener(FilterChangeListener listener) {
        listeners.add(listener);
    }

    public void removeFilterChangeListener(FilterChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (FilterChangeListener listener : listeners) {
            listener.onFilterChanged(currentFilter);
        }
    }

    // ========================================================================
    // PUBLIC API (Backward Compatible + New)
    // ========================================================================

    /**
     * Gets the current TimeFilter object.
     * This is the primary method for backend access.
     * 
     * @return Current TimeFilter configuration
     */
    public TimeFilter getTimeFilter() {
        return currentFilter;
    }

    /**
     * Sets the filter type programmatically.
     * 
     * @param type The filter type to set
     */
    public void setFilterType(TimeFilter.FilterType type) {
        filterTypeCombo.setSelectedIndex(type.ordinal());
    }

    /**
     * Gets the computed start date for the current filter.
     * Backward-compatible with date picker expectations.
     * 
     * @return Start Date (00:00:00 of first day)
     */
    public Date getStartDate() {
        if (currentFilter == null) {
            updateCurrentFilter();
        }
        return currentFilter != null ? currentFilter.getStartDate()
                : TimeFilter.forSingleYear(currentYear).getStartDate();
    }

    /**
     * Gets the computed end date for the current filter.
     * Backward-compatible with date picker expectations.
     * 
     * @return End Date (23:59:59 of last day)
     */
    public Date getEndDate() {
        if (currentFilter == null) {
            updateCurrentFilter();
        }
        return currentFilter != null ? currentFilter.getEndDate() : TimeFilter.forSingleYear(currentYear).getEndDate();
    }

    /**
     * Returns a formatted string describing the current filter.
     * Useful for display or debugging.
     * 
     * @return Human-readable filter description
     */
    public String getFilterDescription() {
        return currentFilter != null ? currentFilter.getDescription() : String.valueOf(currentYear);
    }

    /**
     * Resets to default (current year, single year mode).
     */
    public void reset() {
        filterTypeCombo.setSelectedIndex(1); // Single Year
        yearCombo.setSelectedItem(currentYear);
        updateCurrentFilter();
    }
}
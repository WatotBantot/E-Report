package features.components.filter;

import java.util.Calendar;
import java.util.Date;

/**
 * TimeFilter
 * 
 * A backend-accessible data model representing structured time-based filtering.
 * This class encapsulates all time filter parameters and provides utility
 * methods
 * to generate concrete Date ranges for database queries.
 * 
 * Supported filter types:
 * - SPAN_OF_YEARS: Multiple consecutive years (e.g., 2020-2023)
 * - SINGLE_YEAR: One specific year (e.g., 2024)
 * - SPAN_OF_MONTHS: Multiple consecutive months within a year
 * - SINGLE_MONTH: One specific month within a year
 * - SINGLE_WEEK: One specific week within a month
 * 
 * All filter types default to the current system year when not explicitly set.
 * 
 * Backend Usage:
 * 1. Receive TimeFilter from frontend via FilterListener or SearchListener
 * 2. Call getStartDate() and getEndDate() to get Date objects for SQL BETWEEN
 * queries
 * 3. Or call getSqlStartDateString() / getSqlEndDateString() for JDBC prepared
 * statements
 * 
 * Example:
 * TimeFilter filter = TimeFilter.forSingleYear(2024);
 * Date start = filter.getStartDate(); // Jan 1, 2024 00:00:00
 * Date end = filter.getEndDate(); // Dec 31, 2024 23:59:59
 */
public class TimeFilter {

    // ========================================================================
    // ENUMS
    // ========================================================================

    /**
     * Defines the granularity/type of time filtering available.
     */
    public enum FilterType {
        SPAN_OF_YEARS, // e.g., 2020 to 2023
        SINGLE_YEAR, // e.g., 2024
        SPAN_OF_MONTHS, // e.g., Jan to Jun within a year
        SINGLE_MONTH, // e.g., March 2024
        SINGLE_WEEK // e.g., Week 2 of March 2024
    }

    // ========================================================================
    // FIELDS
    // ========================================================================

    private final FilterType filterType;
    private final int startYear;
    private final int endYear; // Used for SPAN_OF_YEARS
    private final int year; // Used for SINGLE_YEAR, MONTH, WEEK
    private final int startMonth; // 0-based: 0=Jan, 11=Dec. Used for SPAN_OF_MONTHS
    private final int endMonth; // 0-based. Used for SPAN_OF_MONTHS
    private final int month; // 0-based. Used for SINGLE_MONTH, SINGLE_WEEK
    private final int weekOfMonth; // 1-based. Used for SINGLE_WEEK

    // ========================================================================
    // CONSTRUCTORS (Private - use factory methods)
    // ========================================================================

    private TimeFilter(FilterType filterType, int startYear, int endYear,
            int year, int startMonth, int endMonth, int month, int weekOfMonth) {
        this.filterType = filterType;
        this.startYear = startYear;
        this.endYear = endYear;
        this.year = year;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.month = month;
        this.weekOfMonth = weekOfMonth;
    }

    // ========================================================================
    // FACTORY METHODS
    // ========================================================================

    /**
     * Creates a filter for a span of years.
     * 
     * @param startYear First year in the span (inclusive)
     * @param endYear   Last year in the span (inclusive)
     * @return TimeFilter configured for year span
     */
    public static TimeFilter forYearSpan(int startYear, int endYear) {
        return new TimeFilter(FilterType.SPAN_OF_YEARS, startYear, endYear,
                startYear, 0, 0, 0, 0);
    }

    /**
     * Creates a filter for a single year.
     * Defaults to current year if 0 or negative.
     * 
     * @param year The target year
     * @return TimeFilter configured for single year
     */
    public static TimeFilter forSingleYear(int year) {
        int effectiveYear = (year <= 0) ? getCurrentYear() : year;
        return new TimeFilter(FilterType.SINGLE_YEAR, 0, 0,
                effectiveYear, 0, 0, 0, 0);
    }

    /**
     * Creates a filter for a span of months within a year.
     * Defaults to current year if year is 0 or negative.
     * 
     * @param year       The target year
     * @param startMonth Start month (0-based: 0=Jan, 11=Dec)
     * @param endMonth   End month (0-based, inclusive)
     * @return TimeFilter configured for month span
     */
    public static TimeFilter forMonthSpan(int year, int startMonth, int endMonth) {
        int effectiveYear = (year <= 0) ? getCurrentYear() : year;
        return new TimeFilter(FilterType.SPAN_OF_MONTHS, 0, 0,
                effectiveYear, startMonth, endMonth, 0, 0);
    }

    /**
     * Creates a filter for a single month within a year.
     * Defaults to current year if year is 0 or negative.
     * 
     * @param year  The target year
     * @param month Month (0-based: 0=Jan, 11=Dec)
     * @return TimeFilter configured for single month
     */
    public static TimeFilter forSingleMonth(int year, int month) {
        int effectiveYear = (year <= 0) ? getCurrentYear() : year;
        return new TimeFilter(FilterType.SINGLE_MONTH, 0, 0,
                effectiveYear, 0, 0, month, 0);
    }

    /**
     * Creates a filter for a single week within a month.
     * Defaults to current year if year is 0 or negative.
     * 
     * @param year        The target year
     * @param month       Month (0-based: 0=Jan, 11=Dec)
     * @param weekOfMonth Week number (1-based, as per Calendar.WEEK_OF_MONTH)
     * @return TimeFilter configured for single week
     */
    public static TimeFilter forSingleWeek(int year, int month, int weekOfMonth) {
        int effectiveYear = (year <= 0) ? getCurrentYear() : year;
        return new TimeFilter(FilterType.SINGLE_WEEK, 0, 0,
                effectiveYear, 0, 0, month, weekOfMonth);
    }

    // ========================================================================
    // DATE COMPUTATION METHODS (Backend API)
    // ========================================================================

    /**
     * Computes the start date for this filter.
     * Time is set to 00:00:00.000 of the start day.
     * 
     * @return Date representing the inclusive start boundary
     */
    public Date getStartDate() {
        Calendar cal = Calendar.getInstance();
        cal.clear();

        switch (filterType) {
            case SPAN_OF_YEARS:
                cal.set(startYear, Calendar.JANUARY, 1, 0, 0, 0);
                break;

            case SINGLE_YEAR:
                cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
                break;

            case SPAN_OF_MONTHS:
                cal.set(year, startMonth, 1, 0, 0, 0);
                break;

            case SINGLE_MONTH:
                cal.set(year, month, 1, 0, 0, 0);
                break;

            case SINGLE_WEEK:
                cal.set(year, month, 1, 0, 0, 0);
                cal.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                break;
        }

        return cal.getTime();
    }

    /**
     * Computes the end date for this filter.
     * Time is set to 23:59:59.999 of the last day.
     * 
     * @return Date representing the inclusive end boundary
     */
    public Date getEndDate() {
        Calendar cal = Calendar.getInstance();
        cal.clear();

        switch (filterType) {
            case SPAN_OF_YEARS:
                cal.set(endYear, Calendar.DECEMBER, 31, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                break;

            case SINGLE_YEAR:
                cal.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                break;

            case SPAN_OF_MONTHS:
                cal.set(year, endMonth, 1, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                cal.add(Calendar.MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case SINGLE_MONTH:
                cal.set(year, month, 1, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                cal.add(Calendar.MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case SINGLE_WEEK:
                cal.set(year, month, 1, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                cal.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                int firstDay = cal.getFirstDayOfWeek();
                cal.set(Calendar.DAY_OF_WEEK, firstDay);
                cal.add(Calendar.DAY_OF_WEEK, 6);
                if (cal.get(Calendar.MONTH) != month) {
                    cal.set(year, month, 1);
                    cal.add(Calendar.MONTH, 1);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                }
                break;
        }

        return cal.getTime();
    }

    /**
     * Returns a SQL-friendly formatted start date string (yyyy-MM-dd HH:mm:ss).
     * Useful for JDBC prepared statements or raw SQL construction.
     * 
     * @return Formatted start date string
     */
    public String getSqlStartDateString() {
        return formatForSql(getStartDate());
    }

    /**
     * Returns a SQL-friendly formatted end date string (yyyy-MM-dd HH:mm:ss).
     * 
     * @return Formatted end date string
     */
    public String getSqlEndDateString() {
        return formatForSql(getEndDate());
    }

    /**
     * Useful for UI labels or report headers.
     * 
     * @return Description string (e.g., "March 2024", "2020-2023", "Week 2 of March
     *         2024")
     */
    public String getDescription() {
        String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

        switch (filterType) {
            case SPAN_OF_YEARS:
                return startYear + " - " + endYear;
            case SINGLE_YEAR:
                return String.valueOf(year);
            case SPAN_OF_MONTHS:
                return months[startMonth] + " - " + months[endMonth] + " " + year;
            case SINGLE_MONTH:
                return months[month] + " " + year;
            case SINGLE_WEEK:
                return "Week " + weekOfMonth + " of " + months[month] + " " + year;
            default:
                return "Unknown";
        }
    }

    // ========================================================================
    // GETTERS
    // ========================================================================

    public FilterType getFilterType() {
        return filterType;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public int getYear() {
        return year;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public int getMonth() {
        return month;
    }

    public int getWeekOfMonth() {
        return weekOfMonth;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private String formatForSql(Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public String toString() {
        return "TimeFilter{" +
                "type=" + filterType +
                ", description='" + getDescription() + "'" +
                ", start=" + getSqlStartDateString() +
                ", end=" + getSqlEndDateString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TimeFilter that = (TimeFilter) o;
        return filterType == that.filterType &&
                startYear == that.startYear &&
                endYear == that.endYear &&
                year == that.year &&
                startMonth == that.startMonth &&
                endMonth == that.endMonth &&
                month == that.month &&
                weekOfMonth == that.weekOfMonth;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(filterType, startYear, endYear, year,
                startMonth, endMonth, month, weekOfMonth);
    }
}
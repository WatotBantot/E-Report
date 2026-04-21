package features.layout;

import javax.swing.*;

import app.E_Report;
import features.core.dashboardpanel.DashboardInfoCardsPanel;
import features.core.dashboardpanel.captain.*;

import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.Date;

public class CaptainDashboardPanel extends JPanel {
    private E_Report app;
    private DashboardInfoCardsPanel infoCardsPanel;
    private FilterBarPanel filterBarPanel;
    private static final int MIN_CONTENT_WIDTH = 1000;

    public CaptainDashboardPanel(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());
        setOpaque(false);

        JScrollPane scrollPane = createScrollableDashboard();
        add(scrollPane, BorderLayout.CENTER);
    }

    private JScrollPane createScrollableDashboard() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // 1. Info Cards Panel
        String[] iconPaths = {
                "src/assets/icons/reports_icon.png",
                "src/assets/icons/report_pending_icon.png",
                "src/assets/icons/report_in_progress_icon.png",
                "src/assets/icons/resolved_icon.png"
        };
        infoCardsPanel = new DashboardInfoCardsPanel(12, 12, 12, 12, iconPaths);
        infoCardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(infoCardsPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // 2. Filter Bar Panel
        filterBarPanel = new FilterBarPanel(new FilterBarPanel.FilterListener() {
            @Override
            public void onApply(Date fromDate, Date toDate, String category, String purok, String status) {
                refreshDashboardData(category, purok, status);
            }

            @Override
            public void onReset() {
                refreshDashboardData(null, null, null);
            }
        });
        filterBarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(filterBarPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // 3. Charts Row
        JPanel chartsWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chartsWrapper.setOpaque(false);
        chartsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartsWrapper.setMinimumSize(new Dimension(MIN_CONTENT_WIDTH, 280));
        chartsWrapper.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 280));

        JPanel chartsRow = createChartsRow();
        chartsRow.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 280));
        chartsWrapper.add(chartsRow);
        contentPanel.add(chartsWrapper);
        contentPanel.add(Box.createVerticalStrut(20));

        // 4. Bottom Row
        JPanel bottomWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomWrapper.setOpaque(false);
        bottomWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomWrapper.setMinimumSize(new Dimension(MIN_CONTENT_WIDTH, 250));
        bottomWrapper.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 250));

        JPanel bottomRow = createBottomRow();
        bottomRow.setPreferredSize(new Dimension(MIN_CONTENT_WIDTH, 250));
        bottomWrapper.add(bottomRow);
        contentPanel.add(bottomWrapper);

        contentPanel.add(Box.createVerticalGlue());

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setMinimumSize(new Dimension(800, 600));

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setUnitIncrement(16);
        verticalBar.setBlockIncrement(100);

        return scrollPane;
    }

    private JPanel createChartsRow() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setOpaque(false);

        double[] lineData = { 10, 6, 15, 40 };
        String[] months = { "January", "February", "March", "April" };

        String[] trendLabels = { "Theft", "Garbage Waste", "Vandalism", "Scam", "Others" };
        int[] trendValues = { 28, 23, 8, 13, 30 };
        Color[] trendColors = {
                new Color(186, 85, 211),
                new Color(255, 193, 7),
                new Color(66, 133, 244),
                new Color(52, 168, 83),
                new Color(255, 152, 0)
        };

        String[] statusLabels = { "Submitted", "Pending", "In Progress", "Resolved", "Invalid" };
        int[] statusValues = { 200, 200, 200, 200, 200 };
        int[] statusFilled = { 45, 65, 20, 100, 10 };
        Color[] statusColors = {
                new Color(66, 133, 244),
                new Color(255, 193, 7),
                new Color(186, 85, 211),
                new Color(52, 168, 83),
                new Color(189, 189, 189)
        };

        panel.add(new LineGraphPanel("Monthly Case Graph", lineData, months));
        panel.add(new DonutChartPanel("Case Trends Category", trendLabels, trendValues, trendColors));
        panel.add(new StackedBarChartPanel("Case Status", statusLabels, statusValues, statusFilled, statusColors));

        return panel;
    }

    private JPanel createBottomRow() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);

        List<ActivityItem> activities = Arrays.asList(
                new ActivityItem("Report Status Updated", "You updated Report #180 (ID: C180) to In Progress",
                        "11:30 AM", "11-28-2026"),
                new ActivityItem("Report Status Updated", "You updated Report #179 (ID: C179) to Resolved", "4:24 PM",
                        "11-27-2026"),
                new ActivityItem("Report Status Updated", "You updated Report #178 (ID: C178) to Invalid", "9:28 AM",
                        "11-27-2026"));
        panel.add(new RecentActivitiesPanel("Recent Activities", activities));

        String[] sourceLabels = { "Resident", "Captain", "Secretary" };
        int[] sourceValues = { 70, 89, 20 };
        Color[] sourceColors = {
                new Color(255, 193, 7),
                new Color(186, 85, 211),
                new Color(66, 133, 244)
        };
        panel.add(new BarChartPanel("Report Source", sourceLabels, sourceValues, sourceColors));

        return panel;
    }

    private void refreshDashboardData(String category, String purok, String status) {
        // Implement refresh logic
    }

    public DashboardInfoCardsPanel getInfoCardsPanel() {
        return infoCardsPanel;
    }

    public FilterBarPanel getFilterBarPanel() {
        return filterBarPanel;
    }

    public void updateInfoCards(int total, int pending, int inProgress, int resolved) {
        if (infoCardsPanel != null) {
            infoCardsPanel.updateValues(total, pending, inProgress, resolved);
        }
    }
}
package features.views;

import app.E_Report;
import config.UIConfig;
import features.core.BackgroundPanel;
import features.layout.DashboardPanel;
import features.layout.HeaderPanel;
import features.layout.NavPanel;

import javax.swing.*;
import java.awt.*;

public class DashboardView extends JPanel {
    private E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private DashboardPanel content;

    public DashboardView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // Use shared BackgroundPanel
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Initialize components
        header = new HeaderPanel();
        nav = new NavPanel();
        content = new DashboardPanel();

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(content, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    public HeaderPanel getHeader() { return header; }
    public NavPanel getNav() { return nav; }
    public DashboardPanel getContent() { return content; }
}
package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.viewing.ComplaintContentPanel;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Full-page complaint detail view with persistent Header and NavPanel.
 * The content panel handles both View and Update modes internally.
 */
public class ComplaintDetailView extends JPanel {

    private final E_Report app;
    private HeaderPanel header;
    private NavPanel nav;
    private ComplaintContentPanel contentPanel;

    public ComplaintDetailView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header (always visible)
        header = new HeaderPanel(app);
        bgPanel.add(header, BorderLayout.NORTH);

        // Navigation sidebar (always visible)
        nav = new NavPanel();
        setupNavigation();
        bgPanel.add(nav, BorderLayout.WEST);

        // Main content - handles both view and update modes
        contentPanel = new ComplaintContentPanel(app);
        bgPanel.add(contentPanel, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    private void setupNavigation() {
        UserSession us = app.getUserSession();
        if (us == null)
            return;

        String role = us.getRole().toLowerCase();
        if (role.contains("captain")) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (role.contains("secretary")) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else {
            nav.setResidentMenus(route -> app.navigate(route));
        }
    }

    public ComplaintContentPanel getContentPanel() {
        return contentPanel;
    }

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNav() {
        return nav;
    }
}
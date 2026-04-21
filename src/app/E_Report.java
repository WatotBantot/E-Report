package app;

import java.awt.CardLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.List;

import models.*;
import config.UIConfig;
import features.views.DashboardView;
import features.views.HomepageView;
import features.views.LoginView;
import features.views.RegisterView;
import services.controller.DatabaseController;

public class E_Report extends JFrame {
    private CardLayout cardLayout;
    private JPanel container;

    protected UserSession us;
    protected UserInfo ui;
    protected Credential cred;
    private ComplaintDetail cd;
    private List<ComplaintDetail> cdList;

    public E_Report() {
        // Initialize Database
        DatabaseController.initializeDatabase();

        setTitle("Barangay Malacañang E-Reporting System");
        setIconImage(new ImageIcon(getClass().getResource("/assets/images/barangay_logo.png")).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UIConfig.WIDTH, UIConfig.HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        container.add(new HomepageView(this), "home");

        add(container);

        cardLayout.show(container, "home");
        setVisible(true);
    }

    public void navigate(String route) {
        // 1. Remove whatever is currently showing
        getContentPane().removeAll();

        // 2. Decide which "Canvas" to load
        if (route.equalsIgnoreCase("login")) {
            add(new LoginView(this));
        } else if (route.equalsIgnoreCase("home")) {
            add(new HomepageView(this));
        } else if (route.equalsIgnoreCase("register")) {
            add(new RegisterView(this));
        } else if (route.equalsIgnoreCase("dashboard")) {
            add(new DashboardView(this));
        }

        // 3. Tell Swing to redraw the window
        revalidate();
        repaint();
    }

    public void logout() {
        this.ui = null;
        this.cred = null;
        this.us = null;
        navigate("home");
    }

    public void setUserSession(UserSession us) {
        this.us = us;
    }

    public UserSession getUserSession() {
        return us;
    }

    public void setUserInfo(UserInfo ui) {
        this.ui = ui;
    }

    public UserInfo getUserInfo() {
        return ui;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(E_Report::new);
    }

}

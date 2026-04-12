package app;

import java.awt.CardLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

import models.*;
import config.UIConfig;
import features.ui.HomepageUI;
import features.ui.LoginUI;
import features.ui.RegisterUI;
import services.controller.DatabaseController;

public class E_Report extends JFrame {
    private CardLayout cardLayout;
    private JPanel container;

    private UserSession us;
    private UserInfo ui;
    private Credential cred;
    private ComplaintDetail cd;
    private List<ComplaintDetail> cdList;

    public E_Report() {
        // Initialize Database
        DatabaseController.initializeDatabase();

        setTitle("Barangay Malacañang E-Reporting System");
        setIconImage(new ImageIcon(getClass().getResource("/assets/barangay_logo.png")).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UIConfig.WIDTH, UIConfig.HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        container.add(new HomepageUI(this), "home");
        container.add(new LoginUI(this), "login");
        container.add(new RegisterUI(this), "register");

        add(container);

        cardLayout.show(container, "home");
        setVisible(true);
    }

    public void navigate(String route) {
        // 1. Remove whatever is currently showing
        getContentPane().removeAll();

        // 2. Decide which "Canvas" to load
        if (route.equalsIgnoreCase("login")) {
            add(new LoginUI(this));
        } else if (route.equalsIgnoreCase("home")) {
            add(new HomepageUI(this));
        } else if (route.equalsIgnoreCase("register")) {
            add(new RegisterUI(this));
        }

        // 3. Tell Swing to redraw the window
        revalidate();
        repaint();
    }

    public void setUserSession(UserSession us) {
        this.us = us;
    }

    public UserSession getUserSession() {
        return us;
    }

    public void setUserInfo(UserInfo ui){
        this.ui = ui;
    }

    public UserInfo getUserInfo(){
        return ui;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(E_Report::new);
    }

}

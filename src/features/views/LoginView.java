package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.*;
import features.core.BackgroundPanel;
import features.core.FormLayoutUtils;
import models.UserSession;
import services.controller.AuthCredentialController;
import services.validation.UIValidator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginView extends JPanel {
    private E_Report app;

    public LoginView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());
        
        // Background
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        headerPanel.setOpaque(false);
        
        ImageIcon logo = new ImageIcon(new ImageIcon(UIConfig.LOGO_PATH).getImage()
                .getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        headerPanel.add(new JLabel(logo));
        
        JLabel lblHeaderText = new JLabel("E-Reporting System");
        lblHeaderText.setFont(UIConfig.H2);
        headerPanel.add(lblHeaderText);
        
        bgPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center wrapper
        JPanel centerWrapper = FormLayoutUtils.createCenterWrapper();
        UICard loginCard = new UICard(30, Color.WHITE);
        loginCard.setPreferredSize(new Dimension(420, 500));
        loginCard.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = FormLayoutUtils.createFormConstraints();
        gbc.insets = new Insets(5, 40, 5, 40);
        
        // Title
        JLabel lblLogin = new JLabel("Login", SwingConstants.CENTER);
        lblLogin.setFont(UIConfig.H2);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginCard.add(lblLogin, gbc);
        
        // Username
        UIInput txtUser = new UIInput(20, UIConfig.USER_ICON_PATH);
        txtUser.setPlaceholder("Enter your username");
        gbc.gridy = 1;
        FormLayoutUtils.addInputGroup(loginCard, "Username", txtUser, gbc, 0, 1);
        
        // Password
        UIPasswordInput txtPass = new UIPasswordInput(20);
        txtPass.setPlaceholder("Enter your password");
        gbc.gridy = 3;
        FormLayoutUtils.addInputGroup(loginCard, "Password", txtPass, gbc, 0, 3);
        
        // Forgot password
        JLabel lblForgot = new JLabel("Forgot password?");
        lblForgot.setFont(UIConfig.CAPTION);
        lblForgot.setForeground(Color.GRAY);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 40, 25, 40);
        loginCard.add(lblForgot, gbc);
        
        // Login button
        UIButton btnLogin = new UIButton("Login", UIConfig.PRIMARY,
            new Dimension(340, 55), UIConfig.BTN_SECONDARY_FONT, 25,
            UIButton.ButtonType.PRIMARY);
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 40, 0, 40);
        loginCard.add(btnLogin, gbc);
        
        // Footer
        JPanel footer = FormLayoutUtils.createFooterLink(
            "Don't have an account? ", 
            "Register here",
            UIConfig.SUCCESS,
            () -> app.navigate("register")
        );
        gbc.gridy = 7;
        loginCard.add(footer, gbc);
        
        centerWrapper.add(loginCard);
        bgPanel.add(centerWrapper, BorderLayout.CENTER);
        add(bgPanel, BorderLayout.CENTER);
        
        // Action listener
        btnLogin.addActionListener(e -> {
            txtUser.clearError();
            txtPass.clearError();
            
            boolean hasError = UIValidator.validatePasswords(List.of(txtPass)) 
                | UIValidator.validateInputs(List.of(txtUser));
            if (hasError) return;
            
            UserSession session = new AuthCredentialController()
                .authenticateUser(txtUser.getValue(), txtPass.getValue());
            
            if (session != null) {
                app.setUserSession(session);
                app.navigate("home");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Credential has no match. Ensure that the username and password are correct",
                    "Login Failed", JOptionPane.WARNING_MESSAGE);
                txtPass.setText("");
                txtUser.requestFocus();
            }
        });
    }
}
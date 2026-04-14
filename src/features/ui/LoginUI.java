package features.ui;

import javax.swing.*;
import java.awt.*;
import config.UIConfig;
import features.components.*;
import models.UserSession;
import services.controller.AuthCredentialController;
import app.E_Report;
import services.middleware.UIValidator;
import java.util.List;

public class LoginUI extends JPanel {
    private E_Report app;
    private String username, password;

    public LoginUI(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // 1. Background Setup
        HomepageUI temp = new HomepageUI(app);
        JPanel bgPanel = temp.new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout()); // Use BorderLayout for the background to place header

        // 2. Top-Left Header (Logo + System Name)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        headerPanel.setOpaque(false);

        // Assuming your HomepageUI has a way to get the logo or using the path
        ImageIcon logo = new ImageIcon(
                new ImageIcon(UIConfig.LOGO_PATH).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        JLabel lblHeaderLogo = new JLabel(logo);
        JLabel lblHeaderText = new JLabel("E-Reporting System");
        lblHeaderText.setFont(UIConfig.H2);
        lblHeaderText.setForeground(Color.BLACK);

        headerPanel.add(lblHeaderLogo);
        headerPanel.add(lblHeaderText);
        bgPanel.add(headerPanel, BorderLayout.NORTH);

        // 3. Center Container for the Card
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        UICard loginCard = new UICard(30, Color.WHITE);
        loginCard.setPreferredSize(new Dimension(420, 500));
        loginCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 40, 5, 40); // Horizontal padding for elements

        // --- TITLE ---
        JLabel lblLogin = new JLabel("Login", SwingConstants.CENTER);
        lblLogin.setFont(UIConfig.H2);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginCard.add(lblLogin, gbc);

        // --- USERNAME ---
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UIConfig.INPUT_TITLE);
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 40, 5, 40);
        loginCard.add(lblUser, gbc);

        UIInput txtUser = new UIInput(20, UIConfig.USER_ICON_PATH);
        txtUser.setPlaceholder("Enter your username");
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 40, 18, 40);
        loginCard.add(txtUser, gbc);

        // --- PASSWORD ---
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UIConfig.INPUT_TITLE);
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 40, 5, 40);
        loginCard.add(lblPass, gbc);

        // For icons like the eye, we would need a custom panel,
        // but let's fix the basic layout first
        UIPasswordInput txtPass = new UIPasswordInput(20);
        txtPass.setPlaceholder("Enter your password");
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 40, 5, 40);
        loginCard.add(txtPass, gbc);

        // --- FORGOT PASSWORD ---
        JLabel lblForgot = new JLabel("Forgot password?");
        lblForgot.setFont(UIConfig.CAPTION);
        lblForgot.setForeground(Color.GRAY);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 40, 25, 40);
        loginCard.add(lblForgot, gbc);

        // --- LOGIN BUTTON (Full Width) ---
        UIButton btnLogin = new UIButton(
                "Login",
                UIConfig.PRIMARY,
                new Dimension(340, 55),
                UIConfig.BTN_SECONDARY_FONT,
                25,
                UIButton.ButtonType.PRIMARY);

        gbc.gridy = 6;
        gbc.insets = new Insets(10, 40, 0, 40);
        loginCard.add(btnLogin, gbc);

        // --- FOOTER ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        JLabel lblNoAccount = new JLabel("Don't have an account? ");
        JLabel lblRegister = new JLabel("Register here");
        lblNoAccount.setFont(UIConfig.CAPTION);
        lblNoAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.setForeground(UIConfig.SUCCESS);
        lblRegister.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                app.navigate("register");
            }
        });

        footer.add(lblNoAccount);
        footer.add(lblRegister);
        gbc.gridy = 7;
        loginCard.add(footer, gbc);

        centerWrapper.add(loginCard);
        bgPanel.add(centerWrapper, BorderLayout.CENTER);
        add(bgPanel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> {

            // 1. Clear previous errors automatically
            txtUser.clearError();
            txtPass.clearError();

            // 2. Validate using UI system
            boolean hasError = UIValidator.validatePasswords(List.of(txtPass)) | UIValidator.validateInputs(List.of(txtUser));

            if (hasError)
                return;

            // 3. Get values AFTER validation
            username = txtUser.getValue();
            password = txtPass.getValue();

            // 4. Authenticate
            AuthCredentialController acc = new AuthCredentialController();
            UserSession session = acc.authenticateUser(username, password);

            if (session != null) {
                app.setUserSession(session);
                app.navigate("home");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Credential has no match. Ensure that the username and password are correct",
                        "Login Failed",
                        JOptionPane.WARNING_MESSAGE);

                txtPass.setText("");
                txtUser.requestFocus();
            }
        });
    }
}
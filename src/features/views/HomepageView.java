package features.views;

import config.UIConfig;
import features.components.UIButton;
import app.E_Report;

import javax.swing.*;
import java.awt.*;
import features.core.BackgroundPanel;

public class HomepageView extends JPanel {
    private E_Report app;

    public HomepageView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // Background panel (inlined)
        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // 1. Logo - SIMPLE SCALING
        ImageIcon logo = new ImageIcon(new ImageIcon(UIConfig.LOGO_PATH).getImage()
                .getScaledInstance(280, 280, Image.SCALE_SMOOTH));
        JLabel lblLogo = new JLabel(logo);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        bgPanel.add(lblLogo, gbc);

        // 2. Title
        JLabel lblTitle = new JLabel("E-Reporting System");
        lblTitle.setFont(UIConfig.H1);
        lblTitle.setForeground(new Color(25, 25, 25));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        bgPanel.add(lblTitle, gbc);

        // 3. Subtitle
        JLabel lblSubtitle = new JLabel(
                "<html><center>No more reports lost in silence—track every step<br>from submission to solution</center></html>");
        lblSubtitle.setFont(UIConfig.BODY_LARGE);
        lblSubtitle.setForeground(new Color(60, 60, 60));
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 50, 0);
        bgPanel.add(lblSubtitle, gbc);

        // 4. Buttons
        UIButton btnLogin = new UIButton(
                "Login",
                UIConfig.PRIMARY,
                UIConfig.BTN_PRIMARY,
                UIConfig.BTN_PRIMARY_FONT,
                UIConfig.RADIUS_PRIMARY,
                UIButton.ButtonType.PRIMARY);
        UIButton btnRegister = new UIButton(
                "Register",
                UIConfig.SUCCESS,
                UIConfig.BTN_PRIMARY,
                UIConfig.BTN_PRIMARY_FONT,
                UIConfig.RADIUS_PRIMARY,
                UIButton.ButtonType.PRIMARY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);

        btnLogin.addActionListener(e -> app.navigate("login"));
        btnRegister.addActionListener(e -> app.navigate("register"));

        gbc.gridy = 3;
        bgPanel.add(buttonPanel, gbc);

        add(bgPanel, BorderLayout.CENTER);
    }
}
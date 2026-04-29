package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.UIButton;
import features.core.BackgroundPanel;
import features.core.FormLayoutUtils;
import services.controller.PasswordResetController;
import services.controller.PasswordResetController.OtpResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Forgot Password View — OTP-based 3-step flow:
 * 1. Enter username → system sends OTP to registered email
 * 2. Enter OTP → verify
 * 3. Set new password
 */
public class ForgotPasswordView extends JPanel {

    private final E_Report app;
    private final PasswordResetController controller;

    // Current username being processed
    private String currentUsername;

    // Card layout for steps
    private CardLayout cardLayout;
    private JPanel cardPanel;

    // Step 1: Username
    private JTextField txtUsername;
    private JButton btnRequestOtp;
    private JLabel lblStep1Status;

    // Step 2: OTP
    private JLabel lblEmailMask;
    private JTextField txtOtp;
    private JButton btnVerifyOtp;
    private JButton btnResendOtp;
    private JLabel lblStep2Status;
    private Timer countdownTimer;
    private int resendCooldown = 60;

    // Step 3: New Password
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnResetPassword;
    private JLabel lblStep3Status;

    public ForgotPasswordView(E_Report app) {
        this.app = app;
        this.controller = new PasswordResetController();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

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

        // Center
        JPanel centerWrapper = FormLayoutUtils.createCenterWrapper();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        cardPanel.add(createStep1Panel(), "step1");
        cardPanel.add(createStep2Panel(), "step2");
        cardPanel.add(createStep3Panel(), "step3");

        centerWrapper.add(cardPanel);
        bgPanel.add(centerWrapper, BorderLayout.CENTER);
        add(bgPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "step1");
    }

    // ==================== STEP 1: USERNAME ====================

    private JPanel createStep1Panel() {
        JPanel panel = createCardPanel("Forgot Password");

        GridBagConstraints gbc = FormLayoutUtils.createFormConstraints();
        gbc.insets = new Insets(8, 40, 8, 40);

        JLabel lblTitle = new JLabel("Reset Your Password", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H3);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitle, gbc);

        JLabel lblInstr = new JLabel(
                "<html><center>Enter your username. We'll send a One-Time Password (OTP) to your registered email.</center></html>",
                SwingConstants.CENTER);
        lblInstr.setFont(UIConfig.CAPTION);
        lblInstr.setForeground(UIConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 20, 40);
        panel.add(lblInstr, gbc);

        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(340, 45));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 15, 10, 15)));
        gbc.gridy = 2;
        FormLayoutUtils.addInputGroup(panel, "Username", txtUsername, gbc, 0, 2);

        lblStep1Status = new JLabel(" ");
        lblStep1Status.setFont(UIConfig.CAPTION);
        lblStep1Status.setForeground(UIConfig.TEXT_SECONDARY);
        lblStep1Status.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(lblStep1Status, gbc);

        btnRequestOtp = new UIButton("Send OTP", UIConfig.PRIMARY,
                new Dimension(340, 50), UIConfig.BTN_SECONDARY_FONT, 25,
                UIButton.ButtonType.PRIMARY);
        gbc.gridy = 5;
        gbc.insets = new Insets(15, 40, 10, 40);
        panel.add(btnRequestOtp, gbc);

        btnRequestOtp.addActionListener(e -> onRequestOtp());

        // Enter key support
        txtUsername.addActionListener(e -> onRequestOtp());

        // Back to login
        JPanel footer = FormLayoutUtils.createFooterLink(
                "Remember your password? ",
                "Login here",
                UIConfig.PRIMARY,
                () -> app.navigate("login"));
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 20, 0);
        panel.add(footer, gbc);

        return panel;
    }

    // ==================== STEP 2: OTP ====================

    private JPanel createStep2Panel() {
        JPanel panel = createCardPanel("Verify OTP");

        GridBagConstraints gbc = FormLayoutUtils.createFormConstraints();
        gbc.insets = new Insets(8, 40, 8, 40);

        JLabel lblTitle = new JLabel("Enter OTP Code", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H3);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitle, gbc);

        lblEmailMask = new JLabel("OTP sent to: ***", SwingConstants.CENTER);
        lblEmailMask.setFont(UIConfig.CAPTION);
        lblEmailMask.setForeground(UIConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 15, 40);
        panel.add(lblEmailMask, gbc);

        txtOtp = new JTextField(20);
        txtOtp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtOtp.setPreferredSize(new Dimension(340, 50));
        txtOtp.setHorizontalAlignment(JTextField.CENTER);
        txtOtp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 15, 10, 15)));
        gbc.gridy = 2;
        FormLayoutUtils.addInputGroup(panel, "6-Digit Code", txtOtp, gbc, 0, 2);

        lblStep2Status = new JLabel(" ");
        lblStep2Status.setFont(UIConfig.CAPTION);
        lblStep2Status.setForeground(UIConfig.TEXT_SECONDARY);
        lblStep2Status.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(lblStep2Status, gbc);

        btnVerifyOtp = new UIButton("Verify OTP", UIConfig.SUCCESS,
                new Dimension(340, 50), UIConfig.BTN_SECONDARY_FONT, 25,
                UIButton.ButtonType.PRIMARY);
        gbc.gridy = 5;
        gbc.insets = new Insets(15, 40, 5, 40);
        panel.add(btnVerifyOtp, gbc);

        btnVerifyOtp.addActionListener(e -> onVerifyOtp());

        btnResendOtp = new JButton("Resend OTP (60s)");
        btnResendOtp.setFont(UIConfig.SMALL);
        btnResendOtp.setContentAreaFilled(false);
        btnResendOtp.setBorderPainted(false);
        btnResendOtp.setForeground(UIConfig.TEXT_MUTED);
        btnResendOtp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnResendOtp.setEnabled(false);
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(btnResendOtp, gbc);

        btnResendOtp.addActionListener(e -> onResendOtp());

        // Back button
        JButton btnBack = new JButton("← Use different username");
        btnBack.setFont(UIConfig.SMALL);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setForeground(UIConfig.TEXT_MUTED);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            controller.clearSession(currentUsername);
            currentUsername = null;
            txtUsername.setText("");
            lblStep1Status.setText(" ");
            cardLayout.show(cardPanel, "step1");
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 20, 0);
        panel.add(btnBack, gbc);

        return panel;
    }

    // ==================== STEP 3: NEW PASSWORD ====================

    private JPanel createStep3Panel() {
        JPanel panel = createCardPanel("Set New Password");

        GridBagConstraints gbc = FormLayoutUtils.createFormConstraints();
        gbc.insets = new Insets(8, 40, 8, 40);

        JLabel lblTitle = new JLabel("Create New Password", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H3);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitle, gbc);

        JLabel lblInstr = new JLabel(
                "<html><center>Your identity is verified. Enter a strong new password.</center></html>",
                SwingConstants.CENTER);
        lblInstr.setFont(UIConfig.CAPTION);
        lblInstr.setForeground(UIConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 20, 40);
        panel.add(lblInstr, gbc);

        txtNewPassword = new JPasswordField(20);
        txtNewPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNewPassword.setPreferredSize(new Dimension(340, 45));
        txtNewPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 15, 10, 15)));
        gbc.gridy = 2;
        FormLayoutUtils.addInputGroup(panel, "New Password", txtNewPassword, gbc, 0, 2);

        txtConfirmPassword = new JPasswordField(20);
        txtConfirmPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtConfirmPassword.setPreferredSize(new Dimension(340, 45));
        txtConfirmPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 15, 10, 15)));
        gbc.gridy = 4;
        FormLayoutUtils.addInputGroup(panel, "Confirm Password", txtConfirmPassword, gbc, 0, 4);

        lblStep3Status = new JLabel(" ");
        lblStep3Status.setFont(UIConfig.CAPTION);
        lblStep3Status.setForeground(UIConfig.TEXT_SECONDARY);
        lblStep3Status.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(lblStep3Status, gbc);

        btnResetPassword = new UIButton("Reset Password", UIConfig.SUCCESS,
                new Dimension(340, 50), UIConfig.BTN_SECONDARY_FONT, 25,
                UIButton.ButtonType.PRIMARY);
        gbc.gridy = 7;
        gbc.insets = new Insets(15, 40, 10, 40);
        panel.add(btnResetPassword, gbc);

        btnResetPassword.addActionListener(e -> onResetPassword());

        return panel;
    }

    // ==================== UI HELPERS ====================

    private JPanel createCardPanel(String title) {
        JPanel card = new features.components.UICard(30, Color.WHITE);
        card.setPreferredSize(new Dimension(440, 520));
        card.setLayout(new GridBagLayout());
        return card;
    }

    // ==================== EVENT HANDLERS ====================

    private void onRequestOtp() {
        String username = txtUsername.getText().trim();

        if (username.isEmpty()) {
            lblStep1Status.setText("Please enter your username.");
            lblStep1Status.setForeground(Color.RED);
            return;
        }

        setStep1Loading(true);

        SwingWorker<OtpResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OtpResult doInBackground() {
                return controller.sendOtp(username);
            }

            @Override
            protected void done() {
                try {
                    OtpResult result = get();
                    if (result.success) {
                        currentUsername = username;
                        lblEmailMask.setText(result.message);
                        lblStep2Status.setText(" ");
                        txtOtp.setText("");
                        cardLayout.show(cardPanel, "step2");
                        startResendCooldown();
                    } else {
                        lblStep1Status.setText(result.message);
                        lblStep1Status.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    lblStep1Status.setText("An error occurred. Please try again.");
                    lblStep1Status.setForeground(Color.RED);
                    e.printStackTrace();
                } finally {
                    setStep1Loading(false);
                }
            }
        };

        worker.execute();
    }

    private void onVerifyOtp() {
        String otp = txtOtp.getText().trim();

        if (otp.isEmpty()) {
            lblStep2Status.setText("Please enter the OTP code.");
            lblStep2Status.setForeground(Color.RED);
            return;
        }

        if (otp.length() != 6 || !otp.matches("\\d{6}")) {
            lblStep2Status.setText("Please enter a valid 6-digit code.");
            lblStep2Status.setForeground(Color.RED);
            return;
        }

        setStep2Loading(true);

        SwingWorker<OtpResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OtpResult doInBackground() {
                return controller.verifyOtp(currentUsername, otp);
            }

            @Override
            protected void done() {
                try {
                    OtpResult result = get();
                    if (result.success) {
                        lblStep3Status.setText(" ");
                        txtNewPassword.setText("");
                        txtConfirmPassword.setText("");
                        cardLayout.show(cardPanel, "step3");
                    } else {
                        lblStep2Status.setText(result.message);
                        lblStep2Status.setForeground(Color.RED);
                        txtOtp.selectAll();
                        txtOtp.requestFocus();
                    }
                } catch (Exception e) {
                    lblStep2Status.setText("Verification failed. Please try again.");
                    lblStep2Status.setForeground(Color.RED);
                    e.printStackTrace();
                } finally {
                    setStep2Loading(false);
                }
            }
        };

        worker.execute();
    }

    private void onResendOtp() {
        if (currentUsername == null)
            return;

        btnResendOtp.setEnabled(false);
        lblStep2Status.setText("Resending...");
        lblStep2Status.setForeground(UIConfig.TEXT_SECONDARY);

        SwingWorker<OtpResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OtpResult doInBackground() {
                return controller.sendOtp(currentUsername);
            }

            @Override
            protected void done() {
                try {
                    OtpResult result = get();
                    if (result.success) {
                        lblStep2Status.setText("New OTP sent!");
                        lblStep2Status.setForeground(new Color(52, 168, 83));
                        txtOtp.setText("");
                        txtOtp.requestFocus();
                        startResendCooldown();
                    } else {
                        lblStep2Status.setText(result.message);
                        lblStep2Status.setForeground(Color.RED);
                        btnResendOtp.setEnabled(true);
                    }
                } catch (Exception e) {
                    lblStep2Status.setText("Failed to resend. Please try again.");
                    lblStep2Status.setForeground(Color.RED);
                    btnResendOtp.setEnabled(true);
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private void onResetPassword() {
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        // Validation
        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            lblStep3Status.setText("Please fill in both password fields.");
            lblStep3Status.setForeground(Color.RED);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            lblStep3Status.setText("Passwords do not match.");
            lblStep3Status.setForeground(Color.RED);
            txtConfirmPassword.setText("");
            txtConfirmPassword.requestFocus();
            return;
        }

        if (newPass.length() < 6) {
            lblStep3Status.setText("Password must be at least 6 characters.");
            lblStep3Status.setForeground(Color.RED);
            return;
        }

        setStep3Loading(true);

        SwingWorker<OtpResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OtpResult doInBackground() {
                return controller.resetPassword(currentUsername, newPass);
            }

            @Override
            protected void done() {
                try {
                    OtpResult result = get();
                    if (result.success) {
                        JOptionPane.showMessageDialog(ForgotPasswordView.this,
                                "Password reset successfully! Please log in with your new password.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        app.navigate("login");
                    } else {
                        lblStep3Status.setText(result.message);
                        lblStep3Status.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    lblStep3Status.setText("Error: " + e.getMessage());
                    lblStep3Status.setForeground(Color.RED);
                    e.printStackTrace();
                } finally {
                    setStep3Loading(false);
                }
            }
        };

        worker.execute();
    }

    // ==================== UI STATE HELPERS ====================

    private void setStep1Loading(boolean loading) {
        btnRequestOtp.setEnabled(!loading);
        btnRequestOtp.setText(loading ? "Sending..." : "Send OTP");
        txtUsername.setEnabled(!loading);
    }

    private void setStep2Loading(boolean loading) {
        btnVerifyOtp.setEnabled(!loading);
        btnVerifyOtp.setText(loading ? "Verifying..." : "Verify OTP");
        txtOtp.setEnabled(!loading);
    }

    private void setStep3Loading(boolean loading) {
        btnResetPassword.setEnabled(!loading);
        btnResetPassword.setText(loading ? "Updating..." : "Reset Password");
        txtNewPassword.setEnabled(!loading);
        txtConfirmPassword.setEnabled(!loading);
    }

    private void startResendCooldown() {
        resendCooldown = 60;
        btnResendOtp.setEnabled(false);
        btnResendOtp.setText("Resend OTP (" + resendCooldown + "s)");

        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        countdownTimer = new Timer(1000, e -> {
            resendCooldown--;
            if (resendCooldown > 0) {
                btnResendOtp.setText("Resend OTP (" + resendCooldown + "s)");
            } else {
                btnResendOtp.setText("Resend OTP");
                btnResendOtp.setEnabled(true);
                countdownTimer.stop();
            }
        });
        countdownTimer.start();
    }
}
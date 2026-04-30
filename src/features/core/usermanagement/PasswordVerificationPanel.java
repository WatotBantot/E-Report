package features.core.usermanagement;

import config.UIConfig;
import features.components.UIButton;
import features.components.UIPasswordInput;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Generic password verification panel — reusable for any password check flow.
 */
public class PasswordVerificationPanel extends JPanel {

    public interface Listener {
        void onVerified();

        void onCancelled();
    }

    private final Listener listener;
    private final UIPasswordInput passwordField;
    private final JLabel errorLabel;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private String expectedPassword = null;

    public PasswordVerificationPanel(Listener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Title with lock icon
        titleLabel = new JLabel("Verification Required", loadIcon(UIConfig.LOCK_ICON_PATH, 24),
                SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setIconTextGap(10);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        add(titleLabel, gbc);

        // Subtitle — wrapped, centered
        subtitleLabel = new JLabel("<html><div style='text-align: center; width: 260px;'>"
                + "Please enter your password to continue"
                + "</div></html>", SwingConstants.CENTER);
        subtitleLabel.setFont(UIConfig.BODY);
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 20, 0);
        add(subtitleLabel, gbc);

        // Password label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passwordLabel.setForeground(TEXT_PRIMARY);
        passwordLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 0, 6, 0);
        add(passwordLabel, gbc);

        // Password input — full width, proper padding
        passwordField = new UIPasswordInput(24);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(280, 42));
        passwordField.setMinimumSize(new Dimension(280, 42));
        passwordField.setPlaceholder("Enter your password");
        passwordField.setIdleBorderColor(new Color(200, 200, 200));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        add(passwordField, gbc);

        // Error label
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 60, 60));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 16, 0);
        add(errorLabel, gbc);

        // Button panel — centered, using UIButton
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        UIButton cancelButton = new UIButton(
                "Cancel",
                Color.WHITE,
                new Dimension(100, 38),
                new Font("Segoe UI", Font.BOLD, 13),
                8,
                UIButton.ButtonType.OUTLINED);
        cancelButton.setHoverBg(new Color(248, 250, 252));
        cancelButton.setPressedBg(new Color(241, 245, 249));
        cancelButton.addActionListener(e -> {
            if (listener != null)
                listener.onCancelled();
        });

        UIButton verifyButton = new UIButton(
                "Verify",
                new Color(37, 99, 235),
                new Dimension(100, 38),
                new Font("Segoe UI", Font.BOLD, 13),
                8,
                UIButton.ButtonType.PRIMARY);
        verifyButton.setHoverBg(new Color(29, 78, 216));
        verifyButton.setPressedBg(new Color(30, 64, 175));
        verifyButton.addActionListener(e -> verify());

        buttonPanel.add(cancelButton);
        buttonPanel.add(verifyButton);

        gbc.gridy = 5;
        gbc.insets = new Insets(8, 0, 0, 0);
        add(buttonPanel, gbc);

        // Enter key support
        passwordField.addActionListener(e -> verify());
    }

    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);

    /**
     * Sets custom title and subtitle text.
     */
    public void setPromptText(String title, String subtitle) {
        titleLabel.setText(title);
        subtitleLabel.setText("<html><div style='text-align: center; width: 260px;'>"
                + subtitle + "</div></html>");
    }

    /**
     * Backward-compatible: sets an expected password for client-side verification.
     */
    public void setExpectedPassword(String password) {
        this.expectedPassword = password;
    }

    private void verify() {
        String entered = passwordField.getValue();

        if (entered.isEmpty()) {
            showError("Please enter a password");
            return;
        }

        if (expectedPassword != null) {
            if (!entered.equals(expectedPassword)) {
                showError("Incorrect password. Please try again.");
                return;
            }
            errorLabel.setText(" ");
            passwordField.setText("");
            if (listener != null) {
                listener.onVerified();
            }
            return;
        }

        if (listener != null) {
            listener.onVerified();
        }
    }

    public String getPassword() {
        return passwordField.getValue();
    }

    public void showError(String message) {
        errorLabel.setText(message);
        passwordField.setText("");
        passwordField.requestFocus();
    }

    public void clear() {
        passwordField.setText("");
        errorLabel.setText(" ");
    }

    private static ImageIcon loadIcon(String path, int size) {
        try {
            Image src = new ImageIcon(path).getImage();
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(src, 0, 0, size, size, null);
            g2d.dispose();
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }
}
package features.core.usermanagement;

import javax.swing.*;
import java.awt.*;
import features.components.UIComboBox;
import features.components.UIButton;
import config.UIConfig;

public class EditUserPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Password verification
    private JPasswordField passwordField;
    private JLabel passwordErrorLabel;
    private UIButton verifyButton;
    private UIButton cancelVerifyButton;

    // Edit form
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField houseNumberField;
    private UIComboBox<String> streetCombo;
    private UIComboBox<String> purokCombo;
    private UIComboBox<String> roleCombo;
    private JLabel editErrorLabel;
    private UIButton saveButton;
    private UIButton cancelEditButton;

    private UserData currentUser;
    private String secretaryPassword = "admin123";

    private EditListener editListener;

    public interface EditListener {
        void onPasswordVerified();

        void onUserSaved(UserData updatedUser);

        void onCancelled();
    }

    public EditUserPanel(EditListener listener) {
        this.editListener = listener;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setOpaque(false);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        JPanel passwordPanel = createPasswordPanel();
        JPanel editPanel = createEditPanel();

        contentPanel.add(passwordPanel, "PASSWORD");
        contentPanel.add(editPanel, "EDIT");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "PASSWORD");
    }

    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Secretary Verification Required", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Please enter your password to edit user information", SwingConstants.CENTER);
        subtitleLabel.setFont(UIConfig.BODY);
        subtitleLabel.setForeground(new Color(100, 100, 100));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 20, 10);
        panel.add(subtitleLabel, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(UIConfig.BODY);
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 5, 10);
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(250, 35));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        passwordField.setEchoChar('●');
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 5, 10);
        panel.add(passwordField, gbc);

        passwordErrorLabel = new JLabel("", SwingConstants.CENTER);
        passwordErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordErrorLabel.setForeground(new Color(220, 60, 60));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 10, 15, 10);
        panel.add(passwordErrorLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        verifyButton = new UIButton(
                "Verify",
                new Color(25, 118, 210),
                new Dimension(100, 35),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.PRIMARY);
        verifyButton.addActionListener(e -> verifyPassword());

        cancelVerifyButton = new UIButton(
                "Cancel",
                Color.WHITE,
                new Dimension(100, 35),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.OUTLINED);
        cancelVerifyButton.addActionListener(e -> {
            if (editListener != null)
                editListener.onCancelled();
        });

        buttonPanel.add(verifyButton);
        buttonPanel.add(cancelVerifyButton);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(buttonPanel, gbc);

        passwordField.addActionListener(e -> verifyPassword());

        return panel;
    }

    private JPanel createEditPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Edit User Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 10, 20, 10);
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 10, 2, 10);

        addFormRow(panel, gbc, 1, "Name:", nameField = createReadOnlyTextField());
        addFormRow(panel, gbc, 2, "Phone Number:", phoneField = createTextField());
        addFormRow(panel, gbc, 3, "House Number:", houseNumberField = createTextField());

        String[] streets = { "Main Street", "2nd Street", "3rd Street", "Oak Street", "Maple Avenue" };
        streetCombo = new UIComboBox<>(streets);
        streetCombo.setPreferredSize(new Dimension(250, 32));
        addFormRow(panel, gbc, 4, "Street:", streetCombo);

        String[] puroks = { "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5" };
        purokCombo = new UIComboBox<>(puroks);
        purokCombo.setPreferredSize(new Dimension(250, 32));
        addFormRow(panel, gbc, 5, "Purok:", purokCombo);

        String[] roles = { "Resident", "Secretary", "Barangay Captain", "Admin" };
        roleCombo = new UIComboBox<>(roles);
        roleCombo.setPreferredSize(new Dimension(250, 32));
        addFormRow(panel, gbc, 6, "Role:", roleCombo);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        editErrorLabel = new JLabel("", SwingConstants.CENTER);
        editErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editErrorLabel.setForeground(new Color(220, 60, 60));
        panel.add(editErrorLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        saveButton = new UIButton(
                "Save Changes",
                new Color(25, 118, 210),
                new Dimension(130, 38),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.PRIMARY);
        saveButton.addActionListener(e -> saveUser());

        cancelEditButton = new UIButton(
                "Cancel",
                Color.WHITE,
                new Dimension(100, 38),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.OUTLINED);
        cancelEditButton.addActionListener(e -> {
            if (editListener != null)
                editListener.onCancelled();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelEditButton);

        gbc.gridy = 8;
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;

        JLabel label = new JLabel(labelText);
        label.setFont(UIConfig.BODY);
        label.setForeground(new Color(80, 80, 80));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(component, gbc);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setPreferredSize(new Dimension(250, 32));
        field.setFont(UIConfig.BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return field;
    }

    private JTextField createReadOnlyTextField() {
        JTextField field = createTextField();
        field.setEditable(false);
        field.setBackground(new Color(245, 245, 245));
        field.setForeground(new Color(100, 100, 100));
        return field;
    }

    private void verifyPassword() {
        String enteredPassword = new String(passwordField.getPassword());

        if (enteredPassword.isEmpty()) {
            passwordErrorLabel.setText("Please enter a password");
            return;
        }

        if (enteredPassword.equals(secretaryPassword)) {
            passwordErrorLabel.setText("");
            passwordField.setText("");

            if (editListener != null)
                editListener.onPasswordVerified();
            cardLayout.show(contentPanel, "EDIT");
        } else {
            passwordErrorLabel.setText("Incorrect password. Please try again.");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void saveUser() {
        if (phoneField.getText().trim().isEmpty()) {
            editErrorLabel.setText("Phone number is required");
            return;
        }

        currentUser.setPhone(phoneField.getText().trim());
        currentUser.setHouseNumber(houseNumberField.getText().trim());
        currentUser.setStreet((String) streetCombo.getSelectedItem());
        currentUser.setPurok((String) purokCombo.getSelectedItem());
        currentUser.setRole((String) roleCombo.getSelectedItem());

        if (editListener != null) {
            editListener.onUserSaved(currentUser);
        }
    }

    public void setUserData(UserData user) {
        this.currentUser = user;

        nameField.setText(user.getName());
        phoneField.setText(user.getPhone());
        houseNumberField.setText(user.getHouseNumber() != null ? user.getHouseNumber() : "");
        streetCombo.setSelectedItem(user.getStreet() != null ? user.getStreet() : "Main Street");
        purokCombo.setSelectedItem(user.getPurok());
        roleCombo.setSelectedItem(user.getRole());

        passwordErrorLabel.setText("");
        editErrorLabel.setText("");
        passwordField.setText("");

        cardLayout.show(contentPanel, "PASSWORD");
    }

    public void resetToPasswordPanel() {
        passwordField.setText("");
        passwordErrorLabel.setText("");
        cardLayout.show(contentPanel, "PASSWORD");
    }

    public void setSecretaryPassword(String password) {
        this.secretaryPassword = password;
    }
}
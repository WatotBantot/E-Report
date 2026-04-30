package features.layout.common;

import features.components.RoundedLineBorder;
import features.components.UIButton;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Account information card — only email, phone, and username are editable.
 */
public class AccountInfoCard extends JPanel {

    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BG_READONLY = new Color(245, 245, 245);
    private static final int SPACING_MD = 16;

    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField emailField;
    private final JTextField addressField;
    private final JTextField purokField;
    private final JTextField usernameField;

    private final UIButton editButton;
    private final UIButton cancelButton;

    public AccountInfoCard() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel card = new features.components.UICard(12, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(226, 232, 240), 8),
                new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD)));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Account Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        cancelButton = new UIButton("Cancel", Color.WHITE,
                new Dimension(80, 32), new Font("Segoe UI", Font.BOLD, 12), 6,
                UIButton.ButtonType.OUTLINED);
        cancelButton.setVisible(false);

        editButton = new UIButton("Edit", new Color(37, 99, 235),
                new Dimension(80, 32), new Font("Segoe UI", Font.BOLD, 12), 6,
                UIButton.ButtonType.PRIMARY);
        editButton.setHoverBg(new Color(29, 78, 216));
        editButton.setPressedBg(new Color(30, 64, 175));

        btnPanel.add(cancelButton);
        btnPanel.add(editButton);
        header.add(btnPanel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // Form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;

        // Name — READ-ONLY
        nameField = createField("Name", true);
        gbc.gridy = 0;
        formPanel.add(createFieldRow("Name", nameField, true), gbc);

        // Phone — EDITABLE
        phoneField = createField("Phone", false);
        gbc.gridy = 1;
        formPanel.add(createFieldRow("Phone", phoneField, false), gbc);

        // Email — EDITABLE
        emailField = createField("Email", false);
        gbc.gridy = 2;
        formPanel.add(createFieldRow("Email", emailField, false), gbc);

        // Address — READ-ONLY
        addressField = createField("Address", true);
        gbc.gridy = 3;
        formPanel.add(createFieldRow("Address", addressField, true), gbc);

        // Purok — READ-ONLY
        purokField = createField("Purok", true);
        gbc.gridy = 4;
        formPanel.add(createFieldRow("Purok", purokField, true), gbc);

        // Username — EDITABLE
        usernameField = createField("Username", false);
        gbc.gridy = 5;
        formPanel.add(createFieldRow("Username", usernameField, false), gbc);

        card.add(formPanel, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private JTextField createField(String placeholder, boolean readOnly) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(210, 215, 225), 6),
                new EmptyBorder(8, 12, 8, 12)));
        if (readOnly) {
            field.setEditable(false);
            field.setFocusable(false);
            field.setBackground(BG_READONLY);
            field.setForeground(TEXT_SECONDARY);
        }
        return field;
    }

    private JPanel createFieldRow(String labelText, JTextField field, boolean readOnly) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(readOnly ? TEXT_SECONDARY : TEXT_PRIMARY);
        label.setPreferredSize(new Dimension(80, 28));

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    public void setFieldsEditable(boolean editable) {
        phoneField.setEditable(editable);
        phoneField.setFocusable(editable);
        phoneField.setBackground(editable ? Color.WHITE : BG_READONLY);

        emailField.setEditable(editable);
        emailField.setFocusable(editable);
        emailField.setBackground(editable ? Color.WHITE : BG_READONLY);

        usernameField.setEditable(editable);
        usernameField.setFocusable(editable);
        usernameField.setBackground(editable ? Color.WHITE : BG_READONLY);

        nameField.setEditable(false);
        nameField.setFocusable(false);
        nameField.setBackground(BG_READONLY);

        addressField.setEditable(false);
        addressField.setFocusable(false);
        addressField.setBackground(BG_READONLY);

        purokField.setEditable(false);
        purokField.setFocusable(false);
        purokField.setBackground(BG_READONLY);
    }

    public void updateFieldBackgrounds(boolean editing) {
        Color bg = editing ? Color.WHITE : BG_READONLY;
        phoneField.setBackground(bg);
        emailField.setBackground(bg);
        usernameField.setBackground(bg);
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JTextField getPhoneField() {
        return phoneField;
    }

    public JTextField getEmailField() {
        return emailField;
    }

    public JTextField getAddressField() {
        return addressField;
    }

    public JTextField getPurokField() {
        return purokField;
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public UIButton getEditButton() {
        return editButton;
    }

    public UIButton getCancelButton() {
        return cancelButton;
    }
}
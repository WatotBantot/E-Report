package features.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class DashboardFormUtils {
    private DashboardFormUtils() {
    }

    public static JComboBox<String> createComboWithPlaceholder(String[] options, String placeholder) {
        String[] values = new String[options.length + 1];
        values[0] = placeholder;
        System.arraycopy(options, 0, values, 1, options.length);
        return new JComboBox<>(values);
    }

    public static JPanel createLabeledField(String labelText, JComponent component) {
        JPanel block = new JPanel(new BorderLayout(6, 6));
        block.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Poppins", Font.BOLD, 14));
        component.setPreferredSize(new Dimension(220, 30));
        block.add(label, BorderLayout.NORTH);
        block.add(component, BorderLayout.CENTER);
        return block;
    }

    public static void installPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (placeholder.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    public static void applyPoppinsFontRecursively(Component component) {
        if (component == null) {
            return;
        }

        Font current = component.getFont();
        int style = current == null ? Font.PLAIN : current.getStyle();
        int size = current == null ? 13 : current.getSize();
        component.setFont(new Font("Poppins", style, size));

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyPoppinsFontRecursively(child);
            }
        }
    }
}
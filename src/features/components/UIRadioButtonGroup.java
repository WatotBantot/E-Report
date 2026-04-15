package features.components;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import config.UIConfig;

public class UIRadioButtonGroup extends JPanel {
    private ButtonGroup group;

    public UIRadioButtonGroup(String[] options) {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        group = new ButtonGroup();

        for (String option : options) {
            JRadioButton rb = new JRadioButton(option);
            rb.setFont(UIConfig.BODY);
            rb.setOpaque(false);
            rb.setFocusPainted(false);
            rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            group.add(rb);
            add(rb);
        }
    }

    public String getSelectedValue() {
        for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }
}
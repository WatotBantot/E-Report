package features.components;

import javax.swing.JTextField;
import javax.swing.BorderFactory;
import config.UIConfig;

class UIInput extends JTextField {
    public UIInput(int columns) {
        super(columns);
        setFont(UIConfig.BODY);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}

package features.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class DashboardSidebarPanel extends JPanel {

    public static class NavItem {
        private final String label;
        private final javax.swing.Icon icon;
        private final boolean active;
        private final Runnable onClick;

        public NavItem(String label, javax.swing.Icon icon, boolean active, Runnable onClick) {
            this.label = label;
            this.icon = icon;
            this.active = active;
            this.onClick = onClick;
        }
    }

    public DashboardSidebarPanel(List<NavItem> items) {
        super(new GridLayout(0, 1, 0, 8));
        setPreferredSize(new Dimension(205, 0));
        setOpaque(true);
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(12, 10, 12, 10));

        for (NavItem item : items) {
            add(createSidebarButton(item));
        }
    }

    private JButton createSidebarButton(NavItem item) {
        JButton button = new JButton(item.label);
        button.setOpaque(true);
        button.setFont(new Font("Poppins", Font.BOLD, 18));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
        button.setBorder(new EmptyBorder(8, 10, 8, 10));
        button.setBackground(item.active ? new Color(124, 167, 234) : Color.WHITE);
        button.setForeground(item.active ? Color.WHITE : Color.BLACK);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);
        button.setFocusable(false);
        button.setRequestFocusEnabled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (item.icon != null) {
            button.setIcon(item.icon);
        }

        button.addActionListener(e -> item.onClick.run());
        return button;
    }
}
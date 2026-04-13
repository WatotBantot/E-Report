package features.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class DashboardHeaderPanel extends JPanel {

    public DashboardHeaderPanel(String titleText, String roleText, ImageIcon logoIcon) {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(255, 255, 255, 215));
        setBorder(new EmptyBorder(10, 18, 10, 18));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);

        JLabel logo = createLogoLabel(logoIcon);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Poppins", Font.BOLD, 20));

        JLabel role = new JLabel(roleText);
        role.setFont(new Font("Poppins", Font.BOLD, 14));

        brand.add(logo);
        brand.add(title);
        add(brand, BorderLayout.WEST);
        add(role, BorderLayout.EAST);
    }

    private JLabel createLogoLabel(ImageIcon logoIcon) {
        JLabel logo = new JLabel("E", JLabel.CENTER);
        logo.setPreferredSize(new Dimension(58, 58));
        logo.setOpaque(false);
        logo.setFont(new Font("Poppins", Font.BOLD, 22));

        if (logoIcon != null) {
            Image scaled = logoIcon.getImage().getScaledInstance(58, 58, Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(scaled));
            logo.setText("");
        }

        return logo;
    }
}
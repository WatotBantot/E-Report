package features.core;

import config.UIConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Utility class for creating consistent form layouts and components.
 * Standardizes input groups, cards, and footer links across forms.
 */
public class FormLayoutUtils {
    
    /**
     * Creates a center wrapper panel with GridBagLayout.
     * Standard container for centered cards.
     */
    public static JPanel createCenterWrapper() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        return wrapper;
    }
    
    /**
     * Adds a label-input pair to a form panel with consistent spacing.
     * Refactored from RegisterUI.addInputGroup()
     * 
     * @param panel Target panel
     * @param title Label text
     * @param input Input component
     * @param gbc GridBagConstraints instance
     * @param x Grid x position
     * @param y Grid y position
     * @param horizontalInsets Left/right padding
     */
    public static void addInputGroup(JPanel panel, String title, JComponent input,
                                     GridBagConstraints gbc, int x, int y, 
                                     int horizontalInsets) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(0, horizontalInsets, 2, horizontalInsets);
        gbc.gridwidth = 1;
        
        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConfig.INPUT_TITLE);
        panel.add(lbl, gbc);
        
        gbc.gridy = y + 1;
        gbc.insets = new Insets(5, horizontalInsets, 5, horizontalInsets);
        panel.add(input, gbc);
    }
    
    /**
     * Overload with default insets matching your existing code (40px)
     */
    public static void addInputGroup(JPanel panel, String title, JComponent input,
                                     GridBagConstraints gbc, int x, int y) {
        addInputGroup(panel, title, input, gbc, x, y, 40);
    }
    
    /**
     * Creates a footer with a text link for navigation.
     * Refactored from RegisterUI.addFooter() and LoginUI footer.
     * 
     * @param normalText Text before link (e.g., "Already have an account?")
     * @param linkText Clickable link text (e.g., "Login here")
     * @param linkColor Color for the link
     * @param onClick Action to perform when link is clicked
     * @return Configured footer panel
     */
    public static JPanel createFooterLink(String normalText, String linkText, 
                                          Color linkColor, Runnable onClick) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        footer.setOpaque(false);
        
        JLabel lblNormal = new JLabel(normalText);
        lblNormal.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JLabel lblLink = new JLabel(linkText);
        lblLink.setForeground(linkColor);
        lblLink.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                onClick.run();
            }
            
            @Override
            public void mouseEntered(MouseEvent evt) {
                lblLink.setText("<html><u>" + linkText + "</u></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                lblLink.setText(linkText);
            }
        });
        
        footer.add(lblNormal);
        footer.add(lblLink);
        return footer;
    }
    
    public static JButton createBackButton(String text, Runnable onClick) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.GRAY);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> onClick.run());
        return btn;
    }
    
    public static GridBagConstraints createFormConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }
}
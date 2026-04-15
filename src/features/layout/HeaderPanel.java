package features.layout;

// ============================================
// HEADER PANEL - Top bar with logo and user info
// ============================================
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import config.UIConfig;

public class HeaderPanel extends JPanel {
    private JLabel userNameLabel;
    private JLabel userIconLabel;
    private JLabel logoLabel;
    private JLabel titleLabel;
    
    // MODIFY THIS: Change the user role/name dynamically
    private String currentUserRole = "Secretary"; // <-- CHANGE THIS VALUE
    
    // Glass effect opacity (0.0 = fully transparent, 1.0 = fully opaque)
    private float glassOpacity = 0.9f;
    private Color glassBorderColor = new Color(255, 255, 255, 180);
    private Color shadowColor = new Color(0, 0, 0, 30);
    
    public HeaderPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setPreferredSize(new Dimension(720, 70));
        
        // Left side: Logo and Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        logoLabel = createResizableIconLabel(UIConfig.LOGO_PATH, 50, 50);
        leftPanel.add(logoLabel);
        
        titleLabel = new JLabel("E-Reporting System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        leftPanel.add(titleLabel);
        
        add(leftPanel, BorderLayout.WEST);
                
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 10); // Gap between icon and text
        
        // User Icon
        userIconLabel = createResizableIconLabel(UIConfig.USER_ICON_PATH, 35, 35);
        gbc.gridx = 0;
        rightPanel.add(userIconLabel, gbc);
        
        // User Name - Vertically centered with icon
        userNameLabel = new JLabel(currentUserRole);
        userNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userNameLabel.setForeground(new Color(50, 50, 50));
        userNameLabel.setVerticalAlignment(SwingConstants.CENTER);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0); // Reset insets for last item
        rightPanel.add(userNameLabel, gbc);
        
        add(rightPanel, BorderLayout.EAST);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int shadowOffset = 1;
        
        
        // 1. Shadow
        g2.setColor(shadowColor);
        g2.fillRoundRect(shadowOffset, shadowOffset, width - shadowOffset, height - shadowOffset, 20, 20);
        
        // 3. Border
        g2.setColor(glassBorderColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, width - shadowOffset - 1, height - shadowOffset - 1, 20, 20);

        // 2. Glass background
        Color glassWhite = new Color(255, 255, 255, (int)(255 * glassOpacity));
        g2.setColor(glassWhite);
        g2.fillRoundRect(0, 0, width - shadowOffset, height - shadowOffset, 20, 20);
        
        
        // 4. Gradient overlay
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(255, 255, 255, 50),
            0, height / 2, new Color(255, 255, 255, 0)
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width - shadowOffset, height / 2, 20, 20);
        
        g2.dispose();
    }
    
    public void setUserRole(String role) {
        this.currentUserRole = role;
        userNameLabel.setText(role);
        revalidate();
        repaint();
    }
    
    public void setGlassOpacity(float opacity) {
        this.glassOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
        repaint();
    }
    
    public void setGlassBorderColor(Color color) {
        this.glassBorderColor = color;
        repaint();
    }
    
    private JLabel createResizableIconLabel(String imagePath, int targetWidth, int targetHeight) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(targetWidth, targetHeight));
        
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            if (originalImage != null) {
                Image scaledImage = originalImage.getScaledInstance(
                    targetWidth, targetHeight, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
            } else {
                label.setOpaque(true);
                label.setBackground(new Color(200, 200, 200, 150));
                label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        } catch (IOException e) {
            label.setOpaque(true);
            label.setBackground(new Color(200, 200, 200, 150));
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
        
        return label;
    }
    
    public void updateLogoIcon(String newPath) {
        updateIcon(logoLabel, newPath, 50, 50);
    }
    
    public void updateUserIcon(String newPath) {
        updateIcon(userIconLabel, newPath, 35, 35);
    }
    
    private void updateIcon(JLabel label, String imagePath, int w, int h) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            if (originalImage != null) {
                Image scaledImage = originalImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
                label.setOpaque(false);
                label.setBackground(null);
                label.setBorder(null);
            }
        } catch (IOException e) {
            System.err.println("Could not load image: " + imagePath);
        }
        revalidate();
        repaint();
    }
}
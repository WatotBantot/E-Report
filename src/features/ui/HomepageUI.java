package features.ui;

import javax.swing.*;

import config.UIConfig;
import features.components.UIButton;
import app.E_Report;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class HomepageUI extends JPanel {
    private BackgroundPanel bgPanel;
    private GridBagConstraints gbc;
    private ImageIcon logoIcon;
    private JLabel lblLogo, lblTitle, lblSubtitle;
    private JPanel buttonPanel;
    private UIButton btnLogin, btnRegister;
    private E_Report app;

    public HomepageUI(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());        

        // Main Container        
        bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // 1. Logo - Increased size and quality
        // We use a larger target size to ensure clarity on high-DPI screens
        logoIcon = loadHighFidelityImage(UIConfig.LOGO_PATH, 280, 280);
        lblLogo = (logoIcon != null) ? new JLabel(logoIcon) : new JLabel("Logo Not Found");
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        bgPanel.add(lblLogo, gbc);

        // 2. Text Content
        lblTitle = new JLabel("E-Reporting System");
        lblTitle.setFont(UIConfig.H1);
        lblTitle.setForeground(new Color(25, 25, 25));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        bgPanel.add(lblTitle, gbc);

        lblSubtitle = new JLabel(
                "<html><center>No more reports lost in silence—track every step<br>from submission to solution</center></html>");
        lblSubtitle.setFont(UIConfig.BODY_LARGE);
        lblSubtitle.setForeground(new Color(60, 60, 60));
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 50, 0);
        bgPanel.add(lblSubtitle, gbc);

        // 3. Buttons
        btnLogin = new UIButton(
                "Login",
                UIConfig.PRIMARY,
                UIConfig.BTN_PRIMARY,
                UIConfig.BTN_PRIMARY_FONT,
                UIConfig.RADIUS_PRIMARY,
                UIButton.ButtonType.PRIMARY);
        btnRegister = new UIButton(
                "Register",
                UIConfig.SUCCESS,
                UIConfig.BTN_PRIMARY,
                UIConfig.BTN_PRIMARY_FONT,
                UIConfig.RADIUS_PRIMARY,
                UIButton.ButtonType.PRIMARY);
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);

        btnLogin.addActionListener(e -> app.navigate("login"));
        btnRegister.addActionListener(e -> app.navigate("register"));

        gbc.gridy = 3;
        bgPanel.add(buttonPanel, gbc);

        add(bgPanel, BorderLayout.CENTER);   
    }

    /**
     * Advanced Scaling: Uses Multi-step Bilinear rendering for crisp edges
     */
    private ImageIcon loadHighFidelityImage(String path, int targetW, int targetH) {
        BufferedImage original;
        Image scaled;
        BufferedImage resized;
        Graphics2D g2;

        try {
            original = ImageIO.read(new File(path));

            scaled = original.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);

            resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            g2 = resized.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();

            return new ImageIcon(resized);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    class BackgroundPanel extends JPanel {
        private BufferedImage bg;

        public BackgroundPanel(String path) {
            try {
                bg = ImageIO.read(new File(path));
            } catch (Exception e) {
                System.out.println("Background image not found at: " + path);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            if (bg != null) {
                // Ensure background covers the full HD width/height
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
            } else {
                // Fallback Sky Blue
                g2d.setColor(new Color(215, 238, 255));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public static void main(String[] args) {        
    }   

}
package features.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class HomepageUI extends JFrame {

    // --- FILE DIRECTORIES ---
    private static final String LOGO_PATH = "src/assets/barangay_logo.png";
    private static final String BACKGROUND_PATH = "src/assets/background1.png";

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    public HomepageUI() {
        setTitle("Barangay Malacañang E-Reporting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);

        // Main Container
        BackgroundPanel mainPanel = new BackgroundPanel(BACKGROUND_PATH);
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // 1. Logo - Increased size and quality
        // We use a larger target size to ensure clarity on high-DPI screens
        ImageIcon logoIcon = loadHighFidelityImage(LOGO_PATH, 280, 280);
        JLabel logoLabel = (logoIcon != null) ? new JLabel(logoIcon) : new JLabel("Logo Not Found");
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(logoLabel, gbc);

        // 2. Text Content
        JLabel titleLabel = new JLabel("E-Reporting System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 56));
        titleLabel.setForeground(new Color(25, 25, 25));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(titleLabel, gbc);

        JLabel subTitleLabel = new JLabel(
                "<html><center>No more reports lost in silence—track every step<br>from submission to solution</center></html>");
        subTitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
        subTitleLabel.setForeground(new Color(60, 60, 60));
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 50, 0);
        mainPanel.add(subTitleLabel, gbc);

        // 3. Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(new RoundedButton("Login", new Color(25, 87, 191)));
        buttonPanel.add(new RoundedButton("Register", new Color(67, 181, 67)));

        gbc.gridy = 3;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
        setVisible(true);
    }

    /**
     * Advanced Scaling: Uses Multi-step Bilinear rendering for crisp edges
     */
    private ImageIcon loadHighFidelityImage(String path, int targetW, int targetH) {
        try {
            BufferedImage original = ImageIO.read(new File(path));

            Image scaled = original.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);

            BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();

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

    class RoundedButton extends JButton {
        private Color bgColor;

        public RoundedButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setPreferredSize(new Dimension(220, 65));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 22));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 45, 45));
            super.paintComponent(g); // Call super last to draw text over the shape
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HomepageUI::new);
    }
}
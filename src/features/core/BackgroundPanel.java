package features.core;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/*
    Note to others and to future coder, always use this for good high quality image
 */

public class BackgroundPanel extends JPanel {
    
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
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
            } else {
                g2d.setColor(new Color(215, 238, 255));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
}
package features.submit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class BackgroundImagePanel extends JPanel {

    private final Image backgroundImage;

    public BackgroundImagePanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setPaint(new Color(181, 219, 255));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setPaint(new Color(255, 255, 255, 45));
                g2.fillRoundRect(24, 24, getWidth() - 48, getHeight() - 48, 40, 40);
            }
        } finally {
            g2.dispose();
        }
    }
}

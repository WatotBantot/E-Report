package features.submit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class SubmitReportMapPanel extends JPanel {

    public interface Listener {
        void onPinned(double latitude, double longitude);

        void onStatusChanged(String statusText);

        void onAddressResolved(String addressText);
    }

    private final int zoom = 16;
    private double centerLat = SubmitReportConstants.DEFAULT_MAP_LATITUDE;
    private double centerLon = SubmitReportConstants.DEFAULT_MAP_LONGITUDE;
    private Double pinLat;
    private Double pinLon;
    private final Map<String, BufferedImage> tileCache = new ConcurrentHashMap<>();
    private final Listener listener;

    public SubmitReportMapPanel(Listener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(320, 170));
        setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true));
        setOpaque(true);
        setBackground(new Color(242, 246, 250));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double worldX = lonToWorldX(centerLon) - (getWidth() / 2.0) + e.getX();
                double worldY = latToWorldY(centerLat) - (getHeight() / 2.0) + e.getY();

                double lon = worldXToLon(worldX);
                double lat = worldYToLat(worldY);
                setPin(lat, lon);
            }
        });
    }

    public void setCenterAndPin(double lat, double lon) {
        centerLat = lat;
        centerLon = lon;
        setPin(lat, lon);
    }

    public void clearPin() {
        pinLat = null;
        pinLon = null;
        repaint();
    }

    private void setPin(double lat, double lon) {
        pinLat = lat;
        pinLon = lon;
        centerLat = lat;
        centerLon = lon;

        listener.onPinned(lat, lon);
        listener.onStatusChanged("Pinned: " + String.format("%.6f, %.6f", lat, lon));
        repaint();

        new Thread(() -> reverseGeocode(lat, lon)).start();
    }

    private void reverseGeocode(double lat, double lon) {
        try {
            String api = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + lat + "&lon=" + lon;
            HttpURLConnection connection = (HttpURLConnection) URI.create(api).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "E-Report/1.0");
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);
            String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String address = extractJsonString(response, "display_name");
            if (!address.isBlank()) {
                SwingUtilities.invokeLater(() -> listener.onAddressResolved(address));
            }
        } catch (IOException ignored) {
            // Keep UX responsive even when reverse geocoding is unavailable.
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            drawTiles(g2);
            drawPin(g2);
        } finally {
            g2.dispose();
        }
    }

    private void drawTiles(Graphics2D g2) {
        int tileSize = 256;
        double worldCenterX = lonToWorldX(centerLon);
        double worldCenterY = latToWorldY(centerLat);

        int originX = (int) Math.round(worldCenterX - (getWidth() / 2.0));
        int originY = (int) Math.round(worldCenterY - (getHeight() / 2.0));

        int startTileX = (int) Math.floor(originX / (double) tileSize);
        int endTileX = (int) Math.floor((originX + getWidth()) / (double) tileSize);
        int startTileY = (int) Math.floor(originY / (double) tileSize);
        int endTileY = (int) Math.floor((originY + getHeight()) / (double) tileSize);

        int maxIndex = 1 << zoom;

        for (int ty = startTileY; ty <= endTileY; ty++) {
            if (ty < 0 || ty >= maxIndex) {
                continue;
            }
            for (int tx = startTileX; tx <= endTileX; tx++) {
                int wrappedX = Math.floorMod(tx, maxIndex);
                int px = tx * tileSize - originX;
                int py = ty * tileSize - originY;

                BufferedImage img = getTile(wrappedX, ty);
                if (img != null) {
                    g2.drawImage(img, px, py, tileSize, tileSize, null);
                } else {
                    g2.setColor(new Color(230, 235, 240));
                    g2.fillRect(px, py, tileSize, tileSize);
                    g2.setColor(new Color(180, 185, 190));
                    g2.drawRect(px, py, tileSize, tileSize);
                }
            }
        }
    }

    private BufferedImage getTile(int tileX, int tileY) {
        String key = zoom + "/" + tileX + "/" + tileY;
        BufferedImage cached = tileCache.get(key);
        if (cached != null) {
            return cached;
        }

        new Thread(() -> {
            try {
                String tileUrl = "https://tile.openstreetmap.org/" + zoom + "/" + tileX + "/" + tileY + ".png";
                BufferedImage img = ImageIO.read(URI.create(tileUrl).toURL());
                if (img != null) {
                    tileCache.put(key, img);
                    SwingUtilities.invokeLater(this::repaint);
                }
            } catch (IOException ignored) {
                // Ignore tile failures and keep rendering placeholders.
            }
        }).start();

        return null;
    }

    private void drawPin(Graphics2D g2) {
        if (pinLat == null || pinLon == null) {
            return;
        }

        double pinWorldX = lonToWorldX(pinLon);
        double pinWorldY = latToWorldY(pinLat);
        double centerWorldX = lonToWorldX(centerLon);
        double centerWorldY = latToWorldY(centerLat);

        double x = (getWidth() / 2.0) + (pinWorldX - centerWorldX);
        double y = (getHeight() / 2.0) + (pinWorldY - centerWorldY);

        g2.setColor(new Color(230, 60, 60));
        g2.fill(new Ellipse2D.Double(x - 6, y - 6, 12, 12));
        g2.setColor(Color.WHITE);
        g2.draw(new Ellipse2D.Double(x - 6, y - 6, 12, 12));
    }

    private double lonToWorldX(double lon) {
        int tileSize = 256;
        int scale = 1 << zoom;
        return ((lon + 180.0) / 360.0) * scale * tileSize;
    }

    private double latToWorldY(double lat) {
        int tileSize = 256;
        int scale = 1 << zoom;
        double sin = Math.sin(Math.toRadians(lat));
        double y = 0.5 - Math.log((1 + sin) / (1 - sin)) / (4 * Math.PI);
        return y * scale * tileSize;
    }

    private double worldXToLon(double worldX) {
        int tileSize = 256;
        int scale = 1 << zoom;
        return (worldX / (scale * tileSize)) * 360.0 - 180.0;
    }

    private double worldYToLat(double worldY) {
        int tileSize = 256;
        int scale = 1 << zoom;
        double y = 0.5 - (worldY / (scale * tileSize));
        return 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
    }

    private String extractJsonString(String json, String key) {
        String token = "\"" + key + "\":\"";
        int idx = json.indexOf(token);
        if (idx < 0) {
            return "";
        }
        int start = idx + token.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            return "";
        }
        return json.substring(start, end);
    }
}

package features.submit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
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

    private int zoom = SubmitReportConstants.DEFAULT_MAP_ZOOM;
    private static final int MIN_ZOOM = 3;
    private static final int MAX_ZOOM = 19;
    private double centerLat = SubmitReportConstants.DEFAULT_MAP_LATITUDE;
    private double centerLon = SubmitReportConstants.DEFAULT_MAP_LONGITUDE;
    private Double pinLat;
    private Double pinLon;
    private boolean panning;
    private int dragStartX;
    private int dragStartY;
    private double dragStartCenterWorldX;
    private double dragStartCenterWorldY;
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
                if (!panning) {
                    double worldX = lonToWorldX(centerLon) - (getWidth() / 2.0) + e.getX();
                    double worldY = latToWorldY(centerLat) - (getHeight() / 2.0) + e.getY();

                    double lon = worldXToLon(worldX);
                    double lat = worldYToLat(worldY);
                    setPin(lat, lon);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                panning = true;
                dragStartX = e.getX();
                dragStartY = e.getY();
                dragStartCenterWorldX = lonToWorldX(centerLon);
                dragStartCenterWorldY = latToWorldY(centerLat);
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                panning = false;
                setCursor(Cursor.getDefaultCursor());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!panning) {
                    return;
                }

                double deltaX = e.getX() - dragStartX;
                double deltaY = e.getY() - dragStartY;
                double worldX = dragStartCenterWorldX - deltaX;
                double worldY = dragStartCenterWorldY - deltaY;
                centerLon = worldXToLon(worldX);
                centerLat = worldYToLat(worldY);
                repaint();
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        });
    }

    public void setPinLocation(double latitude, double longitude) {
        this.pinLat = latitude;
        this.pinLon = longitude;

        // Repaint to show the pin
        repaint();

        // If you have a map image, you might want to center it on this location
        // Or draw a marker at the calculated pixel position
    }

    public void setCenterAndPin(double lat, double lon) {
        centerLat = lat;
        centerLon = lon;
        setPin(lat, lon);
    }

    public void setCenter(double lat, double lon) {
        centerLat = lat;
        centerLon = lon;
        repaint();
    }

    public void zoomIn() {
        setZoom(zoom + 1);
    }

    public void zoomOut() {
        setZoom(zoom - 1);
    }

    public void setZoom(int newZoom) {
        if (newZoom < MIN_ZOOM || newZoom > MAX_ZOOM) {
            return;
        }
        if (newZoom == zoom) {
            return;
        }
        zoom = newZoom;
        tileCache.clear();
        repaint();
    }

    public void resetView() {
        centerLat = SubmitReportConstants.DEFAULT_MAP_LATITUDE;
        centerLon = SubmitReportConstants.DEFAULT_MAP_LONGITUDE;
        zoom = SubmitReportConstants.DEFAULT_MAP_ZOOM;
        tileCache.clear();
        clearPin();
        repaint();
        listener.onStatusChanged("Map reset to service area center.");
    }

    public int getZoom() {
        return zoom;
    }

    public Double getPinnedLatitude() {
        return pinLat;
    }

    public Double getPinnedLongitude() {
        return pinLon;
    }

    public void searchLocation(String query) {
        if (query == null || query.trim().isEmpty()) {
            listener.onStatusChanged("Enter a search term first.");
            return;
        }

        String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        listener.onStatusChanged("Searching for: " + query.trim());

        new Thread(() -> {
            try {
                String api = "https://nominatim.openstreetmap.org/search?format=jsonv2&q=" + encoded + "&limit=1";
                HttpURLConnection connection = (HttpURLConnection) URI.create(api).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) E-Report/1.0");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String latString = extractJsonNumber(response, "lat");
                String lonString = extractJsonNumber(response, "lon");
                String displayName = extractJsonString(response, "display_name");

                if (latString.isBlank() || lonString.isBlank()) {
                    SwingUtilities.invokeLater(() -> listener.onStatusChanged("Search returned no results."));
                    return;
                }

                double lat = Double.parseDouble(latString);
                double lon = Double.parseDouble(lonString);
                SwingUtilities.invokeLater(() -> {
                    setCenter(lat, lon);
                    listener.onStatusChanged("Search centered to: "
                            + (displayName.isBlank() ? latString + ", " + lonString : displayName));
                    if (!displayName.isBlank()) {
                        listener.onAddressResolved(displayName);
                    }
                });
            } catch (IOException ignored) {
                SwingUtilities.invokeLater(() -> listener.onStatusChanged("Search failed. Try another query."));
            }
        }).start();
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
            drawServiceArea(g2);
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
                HttpURLConnection connection = (HttpURLConnection) URI.create(tileUrl).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) E-Report/1.0");
                connection.setRequestProperty("Accept", "image/png,image/*;q=0.8,*/*;q=0.5");
                connection.setRequestProperty("Referer", "https://www.openstreetmap.org/");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                try (InputStream input = connection.getInputStream()) {
                    BufferedImage img = ImageIO.read(input);
                    if (img != null) {
                        tileCache.put(key, img);
                        SwingUtilities.invokeLater(this::repaint);
                    }
                }
            } catch (IOException ignored) {
                // Ignore tile failures and keep rendering placeholders.
            }
        }).start();

        return null;
    }

    private void drawServiceArea(Graphics2D g2) {
        double worldCenterX = lonToWorldX(centerLon);
        double worldCenterY = latToWorldY(centerLat);
        double defaultWorldX = lonToWorldX(SubmitReportConstants.DEFAULT_MAP_LONGITUDE);
        double defaultWorldY = latToWorldY(SubmitReportConstants.DEFAULT_MAP_LATITUDE);

        double dx = defaultWorldX - worldCenterX;
        double dy = defaultWorldY - worldCenterY;
        double screenX = (getWidth() / 2.0) + dx;
        double screenY = (getHeight() / 2.0) + dy;

        double metersPerPixel = 156543.03392 * Math.cos(Math.toRadians(SubmitReportConstants.DEFAULT_MAP_LATITUDE))
                / (1 << zoom);
        double radius = SubmitReportConstants.SERVICE_AREA_RADIUS_METERS / metersPerPixel;

        if (radius > 10 && radius < Math.max(getWidth(), getHeight()) * 2) {
            g2.setColor(new Color(33, 150, 243, 60));
            g2.fillOval((int) Math.round(screenX - radius), (int) Math.round(screenY - radius),
                    (int) Math.round(radius * 2), (int) Math.round(radius * 2));
            g2.setColor(new Color(33, 150, 243, 180));
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawOval((int) Math.round(screenX - radius), (int) Math.round(screenY - radius),
                    (int) Math.round(radius * 2), (int) Math.round(radius * 2));
        }
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

    private String extractJsonNumber(String json, String key) {
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx < 0) {
            return "";
        }
        int start = idx + token.length();
        int end = start;
        while (end < json.length()) {
            char ch = json.charAt(end);
            if ((ch >= '0' && ch <= '9') || ch == '-' || ch == '.' || ch == '+') {
                end++;
            } else {
                break;
            }
        }
        return json.substring(start, end);
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
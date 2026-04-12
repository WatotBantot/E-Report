package features.submit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class LeafletMapPickerBridge {

    public interface Listener {
        void onLocationPicked(double latitude, double longitude, String address);

        void onStatusChanged(String statusText);
    }

    private static final int CALLBACK_PORT = 18765;
    private final Listener listener;
    private HttpServer callbackServer;

    public LeafletMapPickerBridge(Listener listener) {
        this.listener = listener;
    }

    public void openPicker(double initialLat, double initialLng) throws IOException {
        ensureServer();
        Path pickerFile = createPickerHtml(initialLat, initialLng);

        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop browsing is not supported on this system.");
        }

        Desktop.getDesktop().browse(pickerFile.toUri());
        listener.onStatusChanged("Leaflet map opened. Pin location then click Confirm Pin.");
    }

    public void shutdown() {
        if (callbackServer != null) {
            callbackServer.stop(0);
            callbackServer = null;
        }
    }

    private void ensureServer() throws IOException {
        if (callbackServer != null) {
            return;
        }

        callbackServer = HttpServer.create(new InetSocketAddress("127.0.0.1", CALLBACK_PORT), 0);
        callbackServer.createContext("/selected", this::handleSelection);
        callbackServer.setExecutor(null);
        callbackServer.start();
    }

    private void handleSelection(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        double latitude = parseDouble(query.getOrDefault("lat", "0"), 0.0);
        double longitude = parseDouble(query.getOrDefault("lng", "0"), 0.0);
        String address = query.getOrDefault("address", "");

        listener.onLocationPicked(latitude, longitude, address);
        listener.onStatusChanged("Pinned: " + String.format("%.6f, %.6f", latitude, longitude));

        byte[] body = "Location received. You can close this tab.".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private Path createPickerHtml(double initialLat, double initialLng) throws IOException {
        String callbackUrl = "http://127.0.0.1:" + CALLBACK_PORT + "/selected";
        Path file = Path.of(System.getProperty("java.io.tmpdir"), "e-report-leaflet-picker.html");

        String html = "<!doctype html>\n"
                + "<html><head><meta charset='utf-8'>\n"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>\n"
                + "<title>Leaflet Map Picker</title>\n"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'>\n"
                + "<style>html,body{margin:0;height:100%;font-family:Poppins,Arial,sans-serif;}"
                + ".bar{padding:10px;background:#f6fbff;border-bottom:1px solid #cbdff7;}"
                + "#map{height:calc(100% - 56px);} button{padding:8px 12px;margin-right:8px;}"
                + "#status{font-size:13px;color:#2f3a4a;}</style></head><body>\n"
                + "<div class='bar'><button id='confirm'>Confirm Pin</button><span id='status'>Click map to pin.</span></div>\n"
                + "<div id='map'></div>\n"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n"
                + "<script>const initial=[" + initialLat + "," + initialLng + "];"
                + "const map=L.map('map').setView(initial,16);"
                + "L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png',"
                + "{subdomains:'abcd',maxZoom:20,attribution:'&copy;OpenStreetMap &copy;CARTO'}).addTo(map);"
                + "let marker=L.marker(initial).addTo(map); let selected={lat:initial[0],lng:initial[1]};"
                + "const status=document.getElementById('status');"
                + "map.on('click',e=>{selected=e.latlng; marker.setLatLng(selected); status.textContent='Pinned '+selected.lat.toFixed(6)+', '+selected.lng.toFixed(6);});"
                + "document.getElementById('confirm').addEventListener('click', async ()=>{"
                + "let address=''; try{const r=await fetch('https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat='+selected.lat+'&lon='+selected.lng);"
                + "const d=await r.json(); address=d.display_name||'';}catch(e){}"
                + "const q='?lat='+encodeURIComponent(selected.lat)+'&lng='+encodeURIComponent(selected.lng)+'&address='+encodeURIComponent(address);"
                + "fetch('"
                + callbackUrl
                + "'+q).then(()=>alert('Location sent to app')).catch(()=>alert('Failed to send location'));"
                + "});</script></body></html>";

        Files.writeString(file, html, StandardCharsets.UTF_8);
        return file;
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> values = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return values;
        }

        for (String pair : rawQuery.split("&")) {
            String[] keyValue = pair.split("=", 2);
            String key = decode(keyValue[0]);
            String value = keyValue.length > 1 ? decode(keyValue[1]) : "";
            values.put(key, value);
        }
        return values;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}

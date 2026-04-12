package features.submit;

public final class SubmitReportConstants {

    private SubmitReportConstants() {
    }

    public static final String[] BACKGROUND_CANDIDATES = {
            "desktop.png",
            "images/desktop.png",
            "desktop.jpg",
            "images/desktop.jpg",
            "images/desktop2.png",
            "images/desktop2.jpg",
            "images/desktop2.jpeg",
            "images/desktop2.PNG",
            "images/desktop2.JPG",
            "images/desktop2.JPEG"
    };

    public static final String[] CATEGORY_OPTIONS = {
            "Theft",
            "Noise Complaints / Alarms and Scandals",
            "Physical Injuries / Fights",
            "Tresspass",
            "Malicious Mischief / Vandalism Curfew",
            "Others"
    };

    public static final String[] PUROK_OPTIONS = {
            "Purok 1",
            "Purok 2",
            "Purok 3",
            "Purok 4",
            "Purok 5"
    };

    // Default area center for the e-report service coverage map.
    public static final double DEFAULT_MAP_LATITUDE = 15.6118;
    public static final double DEFAULT_MAP_LONGITUDE = 121.1659;
}

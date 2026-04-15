package config;

import java.awt.*;

public class UIConfig {

    // =========================
    // PATHS
    // =========================
    public static final String LOGO_PATH = "src/assets/images/barangay_logo.png";
    public static final String BACKGROUND_PATH = "src/assets/images/background1.png";

    // =========================
    // WINDOW
    // =========================
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    // =========================
    // LOGO
    // =========================
    public static final int LOGO_SIZE = 280;

    // =========================
    // ICONS
    // =========================
    public static final String USER_ICON_PATH = "src/assets/icons/circle_user_icon.png";
    public static final String LOCK_ICON_PATH = "src/assets/icons/lock_icon.png";
    public static final String EYE_ICON_PATH = "src/assets/icons/eye_icon.png";
    public static final String EYE_OFF_ICON_PATH = "src/assets/icons/eye_off_icon.png";
    public static final String[] STAT_ICON_PATHS = {
        "src/assets/icons/total_report_icon.png",
        "src/assets/icons/pending_icon.png",
        "src/assets/icons/in_progress_icon.png",
        "src/assets/icons/resolved_icon.png"
    };
    public static final String[] STAT_LABEL_PATHS = {
        "Total Reports", "Pending", "In Progress", "Resolved"
    };

    public static String[] NAV_ICON_PATHS = {
        "src/assets/icons/dashboard_icon.png",
        "src/assets/icons/reports_icon.png", 
        "src/assets/icons/submit_icon.png",
        "src/assets/icons/users_icon.png",
        "src/assets/icons/circle_user_icon.png",
        "src/assets/icons/logout_icon.png"
    };

    public static String[] NAV_ICON_LABELS = {
        "Dashboard", "My Reports", "Submit Report",
        "Users", "Profile", "Logout"
    };

    // =========================
    // FONTS
    // =========================
    public static final Font H1 = new Font("Helvetica", Font.BOLD, 56);
    public static final Font H2 = new Font("Helvetica", Font.BOLD, 32);
    public static final Font H3 = new Font("Helvetica", Font.BOLD, 24);

    public static final Font BODY_LARGE = new Font("Helvetica", Font.PLAIN, 22);
    public static final Font BODY = new Font("Helvetica", Font.PLAIN, 16);
    public static final Font CAPTION = new Font("Helvetica", Font.PLAIN, 13);

    public static final Font INPUT_TITLE = new Font("Helvetica", Font.BOLD, 16);

    // =========================
    // BUTTON SIZES
    // =========================
    public static final Dimension BTN_PRIMARY = new Dimension(220, 65);
    public static final Dimension BTN_SECONDARY = new Dimension(180, 55);
    public static final Dimension BTN_SMALL = new Dimension(140, 45);

    public static final Font BTN_PRIMARY_FONT = new Font("Helvetica", Font.BOLD, 22);
    public static final Font BTN_SECONDARY_FONT = new Font("Helvetica", Font.BOLD, 18);
    public static final Font BTN_SMALL_FONT = new Font("Helvetica", Font.PLAIN, 16);

    public static final int RADIUS_PRIMARY = 45;
    public static final int RADIUS_SECONDARY = 35;
    public static final int RADIUS_SMALL = 25;

    // =========================
    // SPACING
    // =========================
    public static final int XS = 5;
    public static final int SM = 10;
    public static final int MD = 20;
    public static final int LG = 40;
    public static final int XL = 55;

    // =========================
    // COLORS
    // =========================
    public static final Color PRIMARY = new Color(25, 87, 191);
    public static final Color SECONDARY = new Color(53, 131, 234);
    public static final Color SUCCESS = new Color(60, 191, 42);

    public static final Color TEXT_PRIMARY = new Color(25, 25, 25);
    public static final Color TEXT_SECONDARY = new Color(60, 60, 60);

    public static final Color BG_LIGHT = new Color(245, 247, 250);

    public static final Color[] STAT_COLORS = {
        new Color(100, 150, 255, 220),   // Blue with transparency
        new Color(255, 200, 100, 220),   // Gold with transparency
        new Color(200, 100, 255, 220),   // Purple with transparency
        new Color(100, 200, 100, 220)    // Green with transparency
    };

    // =========================
    // OUTLINED BUTTON CONFIG
    // =========================
    public static final Color OUTLINE_PRIMARY = PRIMARY;
    public static final Color OUTLINE_TEXT = PRIMARY;
    public static final Color OUTLINE_BG = new Color(0, 0, 0, 0); // transparent
    public static final int OUTLINE_THICKNESS = 2;

    // =========================
    // DISABLED STATE
    // =========================
    public static final Color DISABLED_BG = new Color(200, 200, 200);
    public static final Color DISABLED_TEXT = new Color(150, 150, 150);

    // =========================
    // ELEVATION (SHADOW)
    // =========================
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 60);
    public static final int SHADOW_OFFSET_X = 3;
    public static final int SHADOW_OFFSET_Y = 4;
    public static final int SHADOW_BLUR = 8; // conceptual (used in rendering style)

}
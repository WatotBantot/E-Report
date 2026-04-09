package config;

import java.awt.*;

public class UIConfig {

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
    // FONTS
    // =========================
    public static final Font H1 = new Font("SansSerif", Font.BOLD, 56);
    public static final Font H2 = new Font("SansSerif", Font.BOLD, 32);
    public static final Font H3 = new Font("SansSerif", Font.BOLD, 24);

    public static final Font BODY_LARGE = new Font("SansSerif", Font.PLAIN, 22);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font CAPTION = new Font("SansSerif", Font.PLAIN, 13);

    // =========================
    // BUTTON SIZES
    // =========================
    public static final Dimension BTN_PRIMARY = new Dimension(220, 65);
    public static final Dimension BTN_SECONDARY = new Dimension(180, 55);
    public static final Dimension BTN_SMALL = new Dimension(140, 45);

    public static final Font BTN_PRIMARY_FONT = new Font("SansSerif", Font.BOLD, 22);
    public static final Font BTN_SECONDARY_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font BTN_SMALL_FONT = new Font("SansSerif", Font.PLAIN, 16);

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
    public static final Color SUCCESS = new Color(67, 181, 67);

    public static final Color TEXT_PRIMARY = new Color(25, 25, 25);
    public static final Color TEXT_SECONDARY = new Color(60, 60, 60);

    public static final Color BG_LIGHT = new Color(245, 247, 250);
}
package com.pos.ui.components;

import java.awt.*;

public class UIConstants {

    // Colors
    public static final Color PRIMARY      = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color SUCCESS      = new Color(22, 163, 74);
    public static final Color DANGER       = new Color(220, 38, 38);
    public static final Color WARNING      = new Color(202, 138, 4);
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    public static final Color TEXT_MUTED   = new Color(107, 114, 128);
    public static final Color BG_LIGHT     = new Color(249, 250, 251);
    public static final Color BORDER       = new Color(229, 231, 235);
    public static final Color SIDEBAR_BG   = new Color(15, 23, 42);
    public static final Color SIDEBAR_TEXT = new Color(226, 232, 240);
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);

    // Fonts
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 13);

    // Sizes
    public static final int SIDEBAR_WIDTH = 200;
    public static final int PADDING       = 16;
    public static final int BTN_HEIGHT    = 36;
    public static final int ROW_HEIGHT    = 32;

    private UIConstants() {}
}

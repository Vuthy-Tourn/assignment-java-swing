package com.pos.ui.components;

import java.awt.*;

/**
 * Design tokens — white sidebar + clean light content area.
 */
public class UIConstants {

    private UIConstants() {}

    // ── Sidebar ────────────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG           = new Color(0xFFFFFF); // white
    public static final Color SIDEBAR_DIVIDER      = new Color(0xF3F4F6); // gray-100
    public static final Color SIDEBAR_HOVER        = new Color(0xF9FAFB); // gray-50
    public static final Color SIDEBAR_ACTIVE       = new Color(0xEFF6FF); // blue-50
    public static final Color SIDEBAR_ACTIVE_HOVER = new Color(0xDBEAFE); // blue-100
    public static final Color SIDEBAR_TEXT         = new Color(0x6B7280); // gray-500
    public static final Color SIDEBAR_TEXT_ACTIVE  = new Color(0x2563EB); // blue-600
    public static final int   SIDEBAR_WIDTH        = 240;

    // ── Avatar / user area ───────────────────────────────────────────────────
    public static final Color AVATAR_BG  = new Color(0xEFF6FF); // blue-50
    public static final Color AVATAR_FG  = new Color(0x2563EB); // blue-600
    public static final Color ROLE_TEXT  = new Color(0x9CA3AF); // gray-400

    // ── Content area ────────────────────────────────────────────────────────
    public static final Color CONTENT_BG   = new Color(0xF9FAFB); // gray-50
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(0xE5E7EB); // gray-200
    public static final Color SHADOW_COLOR = new Color(0x00000010);

    // ── Text ────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(0x111827); // gray-900
    public static final Color TEXT_SECONDARY = new Color(0x6B7280); // gray-500
    public static final Color TEXT_MUTED     = new Color(0x9CA3AF); // gray-400

    // ── Semantic ─────────────────────────────────────────────────────────────
    public static final Color SUCCESS    = new Color(0x059669);
    public static final Color SUCCESS_BG = new Color(0xECFDF5);
    public static final Color SUCCESS_FG = new Color(0x065F46);
    public static final Color WARNING    = new Color(0xD97706);
    public static final Color WARNING_BG = new Color(0xFFFBEB);
    public static final Color WARNING_FG = new Color(0x92400E);
    public static final Color DANGER     = new Color(0xDC2626);
    public static final Color DANGER_BG  = new Color(0xFEF2F2);
    public static final Color DANGER_FG  = new Color(0x991B1B);
    public static final Color INFO       = new Color(0x2563EB);
    public static final Color INFO_BG    = new Color(0xEFF6FF);
    public static final Color INFO_FG    = new Color(0x1E40AF);

    // ── Accent ───────────────────────────────────────────────────────────────
    public static final Color ACCENT       = new Color(0x2563EB); // blue-600
    public static final Color ACCENT_HOVER = new Color(0x1D4ED8); // blue-700
    public static final Color ACCENT_LIGHT = new Color(0xDBEAFE); // blue-100
    public static final Color ACCENT_TEXT  = Color.WHITE;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADING    = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_CAPTION    = new Font("Segoe UI", Font.PLAIN, 11);
}
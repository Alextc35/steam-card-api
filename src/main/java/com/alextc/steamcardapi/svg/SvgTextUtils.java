package com.alextc.steamcardapi.svg;

public final class SvgTextUtils {

    private SvgTextUtils() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static String truncate(String value, int maxCodePoints) {
        if (value == null) {
            return "";
        }
        if (value.codePointCount(0, value.length()) <= maxCodePoints) {
            return value;
        }
        int end = value.offsetByCodePoints(0, Math.max(0, maxCodePoints - 3));
        return value.substring(0, end) + "...";
    }

    public static String text(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}

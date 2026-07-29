package com.alextc.steamcardapi.svg;

public final class SvgImageUtils {

    private SvgImageUtils() {
    }

    public static String image(String href, int x, int y, int width, int height, String clipPathId) {
        if (href == null || href.isBlank()) {
            return "";
        }
        String clip = clipPathId == null || clipPathId.isBlank() ? "" : " clip-path=\"url(#%s)\"".formatted(clipPathId);
        return "<image href=\"%s\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"%s preserveAspectRatio=\"xMidYMid slice\"/>"
                .formatted(SvgTextUtils.escape(href), x, y, width, height, clip);
    }
}

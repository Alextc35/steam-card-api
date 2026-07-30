package com.alextc.steamcardapi.svg;

public final class SvgImageUtils {

    private SvgImageUtils() {
    }

    public static String image(String href, int x, int y, int width, int height, String clipPathId) {
        return image(href, x, y, width, height, clipPathId, "xMidYMid slice");
    }

    public static String fittedImage(String href, int x, int y, int width, int height, String clipPathId) {
        return image(href, x, y, width, height, clipPathId, "xMidYMid meet");
    }

    private static String image(
            String href,
            int x,
            int y,
            int width,
            int height,
            String clipPathId,
            String preserveAspectRatio
    ) {
        if (href == null || href.isBlank()) {
            return "";
        }
        String clip = clipPathId == null || clipPathId.isBlank() ? "" : " clip-path=\"url(#%s)\"".formatted(clipPathId);
        return "<image href=\"%s\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"%s preserveAspectRatio=\"%s\"/>"
                .formatted(SvgTextUtils.escape(href), x, y, width, height, clip, preserveAspectRatio);
    }
}

package com.alextc.steamcardapi.svg;

import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import java.util.Arrays;

public enum SvgLayout {
    COMPACT("compact", 500, 170),
    NORMAL("normal", 600, 230),
    SHOWCASE("showcase", 700, 300),
    HERO("hero", 700, 270),
    MINIMAL("minimal", 500, 150),
    LIBRARY("library", 700, 330);

    private final String value;
    private final int width;
    private final int height;

    SvgLayout(String value, int width, int height) {
        this.value = value;
        this.width = width;
        this.height = height;
    }

    public String value() {
        return value;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public static SvgLayout parse(String value) {
        String normalized = value == null || value.isBlank() ? "showcase" : value;
        return Arrays.stream(values())
                .filter(layout -> layout.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new InvalidCardParameterException(
                        "layout must be one of compact, normal, showcase, hero, minimal, library"));
    }
}

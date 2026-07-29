package com.alextc.steamcardapi.model;

import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import java.util.Arrays;

public enum BorderStyle {
    ROUNDED("rounded", 20),
    SQUARE("square", 0),
    NONE("none", 0);

    private final String value;
    private final int radius;

    BorderStyle(String value, int radius) {
        this.value = value;
        this.radius = radius;
    }

    public String value() {
        return value;
    }

    public int radius() {
        return radius;
    }

    public boolean visible() {
        return this != NONE;
    }

    public static BorderStyle parse(String value) {
        String normalized = value == null || value.isBlank() ? "rounded" : value;
        return Arrays.stream(values())
                .filter(style -> style.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new InvalidCardParameterException("border must be one of rounded, square, none"));
    }
}

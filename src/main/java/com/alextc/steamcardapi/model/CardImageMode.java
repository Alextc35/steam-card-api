package com.alextc.steamcardapi.model;

import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import java.util.Arrays;

public enum CardImageMode {
    EMBEDDED("embedded"),
    EXTERNAL("external");

    private final String value;

    CardImageMode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CardImageMode parse(String value) {
        String normalized = value == null || value.isBlank() ? "embedded" : value;
        return Arrays.stream(values())
                .filter(mode -> mode.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new InvalidCardParameterException("imageMode must be either embedded or external"));
    }
}

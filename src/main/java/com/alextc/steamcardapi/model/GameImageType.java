package com.alextc.steamcardapi.model;

import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import java.util.Arrays;

public enum GameImageType {
    NONE("none"),
    ICON("icon"),
    HEADER("header"),
    PORTRAIT("portrait"),
    HERO("hero"),
    LOGO("logo"),
    SMALL("small"),
    AUTO("auto");

    private final String value;

    GameImageType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static GameImageType parse(String value) {
        String normalized = value == null || value.isBlank() ? "auto" : value;
        return Arrays.stream(values())
                .filter(type -> type.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new InvalidCardParameterException(
                        "gameImage must be one of none, icon, header, portrait, hero, logo, small, auto"));
    }
}

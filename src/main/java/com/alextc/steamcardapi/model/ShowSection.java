package com.alextc.steamcardapi.model;

import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public enum ShowSection {
    AVATAR("avatar"),
    STATUS("status"),
    LEVEL("level"),
    FRIENDS("friends"),
    LIBRARY("library"),
    ACCOUNT_AGE("accountAge"),
    COUNTRY("country"),
    CURRENT_GAME("currentGame"),
    RECENT_GAMES("recentGames"),
    FAVORITE_GAME("favoriteGame"),
    ACHIEVEMENTS("achievements"),
    HOURS("hours"),
    COVER("cover"),
    GAME_ICON("gameIcon"),
    GENRES("genres"),
    RELEASE_DATE("releaseDate");

    private final String value;

    ShowSection(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Set<ShowSection> parse(String value) {
        if (value == null || value.isBlank()) {
            return Set.of(values());
        }
        if (value.length() > 240) {
            throw new InvalidCardParameterException("show is too long");
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .map(ShowSection::parseOne)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static ShowSection parseOne(String value) {
        return Arrays.stream(values())
                .filter(section -> section.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new InvalidCardParameterException(
                        "show contains an unsupported section: " + value.toLowerCase(Locale.ROOT)));
    }
}

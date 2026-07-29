package com.alextc.steamcardapi.model;

public record SteamLibrarySummary(
        Integer gameCount,
        Integer totalPlaytimeMinutes,
        SteamGame mostPlayedGame
) {
}

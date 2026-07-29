package com.alextc.steamcardapi.model;

public record SteamStatistics(
        Integer steamLevel,
        Integer friendCount,
        Integer gameCount,
        Integer totalPlaytimeMinutes,
        Integer recentPlaytimeMinutes
) {
}

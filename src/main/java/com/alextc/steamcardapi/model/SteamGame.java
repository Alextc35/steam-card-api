package com.alextc.steamcardapi.model;

import java.time.Instant;
import java.util.List;

public record SteamGame(
        Integer appId,
        String name,
        Integer playtimeLastTwoWeeksMinutes,
        Integer playtimeForeverMinutes,
        Instant lastPlayedAt,
        boolean currentlyPlaying,
        String iconHash,
        SteamGameImages images,
        List<String> genres,
        List<String> developers,
        List<String> publishers,
        List<String> categories,
        String releaseDate,
        String shortDescription,
        String type,
        String price,
        boolean freeToPlay,
        Integer metacriticScore,
        Integer recommendations,
        StorePlatforms platforms
) {

    public double hoursForever() {
        return minutesToHours(playtimeForeverMinutes);
    }

    public double hoursLastTwoWeeks() {
        return minutesToHours(playtimeLastTwoWeeksMinutes);
    }

    private double minutesToHours(Integer minutes) {
        if (minutes == null) {
            return 0.0;
        }
        return Math.round((minutes / 60.0) * 10.0) / 10.0;
    }
}

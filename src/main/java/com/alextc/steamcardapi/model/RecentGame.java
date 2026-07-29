package com.alextc.steamcardapi.model;

import java.time.Instant;

public record RecentGame(
        Integer appId,
        String name,
        Double hoursLastTwoWeeks,
        Double hoursTotal,
        Instant lastPlayedAt,
        String iconUrl
) {
}

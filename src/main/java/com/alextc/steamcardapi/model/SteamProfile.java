package com.alextc.steamcardapi.model;

import java.time.Instant;

public record SteamProfile(
        String steamId,
        String nickname,
        String profileUrl,
        String avatarUrl,
        String status,
        boolean currentlyPlaying,
        Instant accountCreatedAt,
        Instant lastOnlineAt,
        String countryCode,
        Integer steamLevel,
        Integer friendCount,
        Integer gameCount
) {
}

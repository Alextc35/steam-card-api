package com.alextc.steamcardapi.model;

public record SteamGameImages(
        String iconUrl,
        String headerUrl,
        String portraitCoverUrl,
        String heroUrl,
        String logoUrl,
        String smallCapsuleUrl,
        String backgroundUrl,
        String primaryImageUrl
) {
}

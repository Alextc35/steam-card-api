package com.alextc.steamcardapi.model;

import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTheme;
import java.util.List;

public record SteamCardData(
        SteamProfile profile,
        SteamGame selectedGame,
        List<SteamGame> recentGames,
        List<SteamGame> favoriteGames,
        SteamStatistics statistics,
        SteamLibrarySummary librarySummary,
        SvgTheme theme,
        SvgLayout layout,
        String locale,
        String accent,
        java.util.Set<ShowSection> show,
        BorderStyle border,
        SteamGameImages resolvedImages,
        String renderedAvatarUrl,
        String renderedPrimaryImageUrl
) {
}

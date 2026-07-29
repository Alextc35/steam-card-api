package com.alextc.steamcardapi;

import com.alextc.steamcardapi.config.SteamProperties;
import com.alextc.steamcardapi.model.BorderStyle;
import com.alextc.steamcardapi.model.CardImageMode;
import com.alextc.steamcardapi.model.GameImageType;
import com.alextc.steamcardapi.model.SteamCardData;
import com.alextc.steamcardapi.model.SteamCardRequest;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.model.SteamGameImages;
import com.alextc.steamcardapi.model.SteamLibrarySummary;
import com.alextc.steamcardapi.model.SteamProfile;
import com.alextc.steamcardapi.model.SteamStatistics;
import com.alextc.steamcardapi.model.SteamSubject;
import com.alextc.steamcardapi.model.StorePlatforms;
import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTheme;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class TestFixtures {

    public static final String STEAM_ID = "76561198000000000";

    private TestFixtures() {
    }

    public static SteamProperties properties() {
        return new SteamProperties(
                "test-key",
                "alextc",
                "",
                Duration.ofMinutes(5),
                Duration.ofSeconds(15),
                Duration.ofHours(1),
                Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                1024 * 1024);
    }

    public static SteamCardRequest request() {
        return request(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK, GameImageType.AUTO);
    }

    public static SteamCardRequest request(SvgLayout layout, SvgTheme theme, GameImageType imageType) {
        return new SteamCardRequest(
                new SteamSubject(STEAM_ID, null),
                theme,
                layout,
                "en",
                null,
                CardImageMode.EMBEDDED,
                imageType,
                BorderStyle.ROUNDED);
    }

    public static SteamProfile profile(String nickname) {
        return new SteamProfile(
                STEAM_ID,
                nickname,
                "https://steamcommunity.com/id/alextc",
                "https://avatars.steamstatic.com/avatar.jpg",
                "Online",
                false,
                Instant.ofEpochSecond(1_600_000_000L),
                Instant.ofEpochSecond(1_700_000_000L),
                "ES",
                42,
                12,
                120);
    }

    public static SteamGameImages images() {
        return new SteamGameImages(
                "https://media.steampowered.com/steamcommunity/public/images/apps/730/iconhash.jpg",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/library_600x900.jpg",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/library_hero.jpg",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/logo.png",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/capsule_184x69.jpg",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/background.jpg",
                "data:image/png;base64,AAAA");
    }

    public static SteamGame game(int appId, String name, int playtimeForeverMinutes, boolean currentlyPlaying) {
        return new SteamGame(
                appId,
                name,
                90,
                playtimeForeverMinutes,
                Instant.ofEpochSecond(1_710_000_000L),
                currentlyPlaying,
                "iconhash",
                images(),
                List.of("Action", "Multiplayer"),
                List.of("Valve"),
                List.of("Valve"),
                List.of("Online PvP"),
                "Aug 21, 2012",
                "A tactical shooter with sharp corners & teamwork.",
                "game",
                null,
                true,
                83,
                1_000_000,
                new StorePlatforms(true, false, true));
    }

    public static SteamCardData cardData(SvgLayout layout, SvgTheme theme) {
        SteamGame game = game(730, "Counter <Strike> 2", 600, false);
        return cardData(layout, theme, game);
    }

    public static SteamCardData cardData(SvgLayout layout, SvgTheme theme, SteamGame game) {
        SteamProfile profile = profile("Alex <Dev> & Co");
        SteamStatistics statistics = new SteamStatistics(42, 12, 120, 12_000, 90);
        SteamLibrarySummary library = new SteamLibrarySummary(120, 12_000, game);
        return new SteamCardData(
                profile,
                game,
                List.of(game, game(570, "Dota 2", 300, false), game(440, "Team Fortress 2", 120, false)),
                List.of(game),
                statistics,
                library,
                theme,
                layout,
                "en",
                null,
                BorderStyle.ROUNDED,
                images(),
                "data:image/png;base64,AVATAR",
                "data:image/png;base64,COVER");
    }
}

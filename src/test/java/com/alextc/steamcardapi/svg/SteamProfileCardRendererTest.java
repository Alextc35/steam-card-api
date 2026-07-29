package com.alextc.steamcardapi.svg;

import static org.assertj.core.api.Assertions.assertThat;

import com.alextc.steamcardapi.TestFixtures;
import com.alextc.steamcardapi.model.SteamCardData;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.model.SteamProfile;
import org.junit.jupiter.api.Test;

class SteamProfileCardRendererTest {

    private final SteamProfileCardRenderer renderer = new SteamProfileCardRenderer();

    @Test
    void generatesValidAccessibleSvgWithEscapedProfileAndGameData() {
        String svg = renderer.render(TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK));

        assertThat(svg).startsWith("<svg");
        assertThat(svg).contains("role=\"img\"");
        assertThat(svg).contains("<title");
        assertThat(svg).contains("<desc");
        assertThat(svg).contains("Alex &lt;Dev&gt; &amp; Co");
        assertThat(svg).contains("Counter &lt;Strike&gt; 2");
        assertThat(svg).doesNotContain("Alex <Dev>");
        assertThat(svg).doesNotContain("Counter <Strike>");
    }

    @Test
    void rendersEveryLayoutWithoutLeavingTheViewBox() {
        for (SvgLayout layout : SvgLayout.values()) {
            SteamCardData data = TestFixtures.cardData(layout, SvgTheme.DARK);

            String svg = renderer.render(data);

            assertThat(svg).contains("width=\"%d\"".formatted(layout.width()));
            assertThat(svg).contains("height=\"%d\"".formatted(layout.height()));
            assertThat(svg).contains("viewBox=\"0 0 %d %d\"".formatted(layout.width(), layout.height()));
        }
    }

    @Test
    void supportsDarkAndLightThemesAndUsesClipPathsForImages() {
        String dark = renderer.render(TestFixtures.cardData(SvgLayout.HERO, SvgTheme.DARK));
        String light = renderer.render(TestFixtures.cardData(SvgLayout.NORMAL, SvgTheme.LIGHT));

        assertThat(dark).contains("#171a21");
        assertThat(light).contains("#f5f7fa");
        assertThat(dark).contains("<clipPath");
        assertThat(light).contains("<clipPath");
        assertThat(dark).contains("preserveAspectRatio=\"xMidYMid slice\"");
    }

    @Test
    void truncatesLongNamesAndCanRenderFallbackSvg() {
        SteamCardData data = TestFixtures.cardData(SvgLayout.COMPACT, SvgTheme.STEAM,
                TestFixtures.game(730, "Counter-Strike 2 Competitive Premier Legacy Edition", 600, false));
        String svg = renderer.render(data);
        String fallback = renderer.renderFallback(SvgTheme.STEAM, SvgLayout.COMPACT);

        assertThat(svg).contains("...");
        assertThat(fallback).contains("Steam profile temporarily unavailable");
        assertThat(fallback).contains("role=\"img\"");
    }

    @Test
    void rendersPlayingOnlineAndOfflineBadges() {
        SteamCardData playing = TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK,
                TestFixtures.game(730, "Counter <Strike> 2", 600, true));
        SteamCardData online = TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK);
        SteamCardData offline = withStatus(online, "Offline", false);

        assertThat(renderer.render(playing))
                .contains(">In-game<")
                .contains(">Currently playing<")
                .contains("#90ba3c");
        assertThat(renderer.render(online))
                .contains(">ONLINE<")
                .contains("#66c0f4");
        assertThat(renderer.render(offline))
                .contains(">OFFLINE<")
                .contains("#f85149");
    }

    @Test
    void showcaseAvoidsDuplicateGameHoursAndHidesLastSessionWhenOnline() {
        SteamCardData online = TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK);
        SteamCardData offline = withStatus(online, "Offline", false);

        String onlineSvg = renderer.render(online);
        String offlineSvg = renderer.render(offline);

        assertThat(onlineSvg).containsOnlyOnce("Total 10.0 h · 2 weeks 1.5 h");
        assertThat(onlineSvg).contains("Total playtime in the last 2 weeks 1.5 h");
        assertThat(onlineSvg).contains("Aug 21, 2012");
        assertThat(onlineSvg).doesNotContain("Last session");
        assertThat(offlineSvg)
                .contains("Last session")
                .contains("<text x=\"292\" y=\"102\" font-size=\"10\"")
                .doesNotContain("<text x=\"216\" y=\"286\" font-size=\"11\"");
    }

    @Test
    void offlineShowcaseFallsBackToProfileLastOnlineWhenGameLastPlayedIsMissing() {
        SteamCardData online = TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK);
        SteamCardData offline = withoutGameLastPlayed(withStatus(online, "Offline", false));

        String svg = renderer.render(offline);

        assertThat(svg)
                .contains("Last session Nov 14, 2023")
                .contains("<text x=\"292\" y=\"102\" font-size=\"10\"");
    }

    @Test
    void showcaseRendersSpainFlagBesideNameAndFooterLink() {
        String svg = renderer.render(TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK));

        assertThat(svg)
                .contains("y=\"36\" width=\"26\" height=\"18\"")
                .contains("<title>ES</title>")
                .contains("#aa151b")
                .contains("#f1bf00")
                .contains("<a href=\"https://alextc.es\"")
                .doesNotContain("y=\"66\" width=\"26\" height=\"18\"");
    }

    @Test
    void showcaseRendersSupportedCountryFlagsBeyondSpain() {
        SteamCardData data = withCountryCode(TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK), "AR");

        String svg = renderer.render(data);

        assertThat(svg)
                .contains("aria-label=\"AR\"")
                .contains("<title>AR</title>")
                .contains("#74acdf")
                .doesNotContain(">AR</text>");
    }

    @Test
    void rendersHelpfulPlaceholderWhenGameAndCoverAreUnavailable() {
        SteamCardData data = withoutSelectedGameAndCover(TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK));

        String svg = renderer.render(data);

        assertThat(svg)
                .contains("No game found")
                .contains("No public recent game data")
                .contains("No game available")
                .doesNotContain("<image href=\"\"")
                .doesNotContain("data:image/png;base64,COVER")
                .doesNotContain("https://store.steampowered.com/app/");
    }

    @Test
    void profileAvatarAndNameLinkToSteamProfile() {
        String svg = renderer.render(TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK));

        assertThat(svg)
                .containsPattern("(?s)<a href=\"https://steamcommunity\\.com/id/alextc\"[^>]*>\\s*<image href=\"data:image/png;base64,AVATAR\"")
                .containsPattern("(?s)<a href=\"https://steamcommunity\\.com/id/alextc\"[^>]*>\\s*<text x=\"292\" y=\"55\"");
    }

    @Test
    void gameCoverLinksToSteamStorePage() {
        String svg = renderer.render(TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK));

        assertThat(svg)
                .containsPattern("(?s)<a href=\"https://store\\.steampowered\\.com/app/730\"[^>]*>\\s*<image href=\"data:image/png;base64,COVER\"");
    }

    private SteamCardData withStatus(SteamCardData data, String status, boolean currentlyPlaying) {
        SteamProfile profile = data.profile();
        SteamProfile updatedProfile = new SteamProfile(
                profile.steamId(),
                profile.nickname(),
                profile.profileUrl(),
                profile.avatarUrl(),
                status,
                currentlyPlaying,
                profile.accountCreatedAt(),
                profile.lastOnlineAt(),
                profile.countryCode(),
                profile.steamLevel(),
                profile.friendCount(),
                profile.gameCount());
        return new SteamCardData(
                updatedProfile,
                data.selectedGame(),
                data.recentGames(),
                data.favoriteGames(),
                data.statistics(),
                data.librarySummary(),
                data.theme(),
                data.layout(),
                data.locale(),
                data.accent(),
                data.show(),
                data.border(),
                data.resolvedImages(),
                data.renderedAvatarUrl(),
                data.renderedPrimaryImageUrl());
    }

    private SteamCardData withCountryCode(SteamCardData data, String countryCode) {
        SteamProfile profile = data.profile();
        SteamProfile updatedProfile = new SteamProfile(
                profile.steamId(),
                profile.nickname(),
                profile.profileUrl(),
                profile.avatarUrl(),
                profile.status(),
                profile.currentlyPlaying(),
                profile.accountCreatedAt(),
                profile.lastOnlineAt(),
                countryCode,
                profile.steamLevel(),
                profile.friendCount(),
                profile.gameCount());
        return new SteamCardData(
                updatedProfile,
                data.selectedGame(),
                data.recentGames(),
                data.favoriteGames(),
                data.statistics(),
                data.librarySummary(),
                data.theme(),
                data.layout(),
                data.locale(),
                data.accent(),
                data.show(),
                data.border(),
                data.resolvedImages(),
                data.renderedAvatarUrl(),
                data.renderedPrimaryImageUrl());
    }

    private SteamCardData withoutSelectedGameAndCover(SteamCardData data) {
        return new SteamCardData(
                data.profile(),
                null,
                data.recentGames(),
                data.favoriteGames(),
                data.statistics(),
                data.librarySummary(),
                data.theme(),
                data.layout(),
                data.locale(),
                data.accent(),
                data.show(),
                data.border(),
                data.resolvedImages(),
                data.renderedAvatarUrl(),
                "");
    }

    private SteamCardData withoutGameLastPlayed(SteamCardData data) {
        SteamGame game = data.selectedGame();
        SteamGame updatedGame = new SteamGame(
                game.appId(),
                game.name(),
                game.playtimeLastTwoWeeksMinutes(),
                game.playtimeForeverMinutes(),
                null,
                game.currentlyPlaying(),
                game.iconHash(),
                game.images(),
                game.genres(),
                game.developers(),
                game.publishers(),
                game.categories(),
                game.releaseDate(),
                game.shortDescription(),
                game.type(),
                game.price(),
                game.freeToPlay(),
                game.metacriticScore(),
                game.recommendations(),
                game.platforms());
        return new SteamCardData(
                data.profile(),
                updatedGame,
                data.recentGames(),
                data.favoriteGames(),
                data.statistics(),
                data.librarySummary(),
                data.theme(),
                data.layout(),
                data.locale(),
                data.accent(),
                data.show(),
                data.border(),
                data.resolvedImages(),
                data.renderedAvatarUrl(),
                data.renderedPrimaryImageUrl());
    }
}

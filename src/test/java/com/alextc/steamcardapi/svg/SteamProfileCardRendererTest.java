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
    void compactLayoutUsesWideFittedGameThumbnail() {
        String svg = renderer.render(TestFixtures.cardData(SvgLayout.COMPACT, SvgTheme.GITHUB_DARK));

        assertThat(svg)
                .contains("<rect x=\"344\" y=\"28\" width=\"130\" height=\"74\" rx=\"12\"")
                .contains("<clipPath id=\"")
                .contains("\"><rect x=\"344\" y=\"28\" width=\"130\" height=\"74\" rx=\"12\"/></clipPath>")
                .contains("preserveAspectRatio=\"xMidYMid meet\"")
                .doesNotContain("x=\"376\" y=\"24\" width=\"88\" height=\"88\"");
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
                .contains(">IN-GAME<")
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
    void profileLayoutsRenderCountryFlagBesideName() {
        assertThat(renderer.render(TestFixtures.cardData(SvgLayout.COMPACT, SvgTheme.GITHUB_DARK)))
                .contains("y=\"28\" width=\"26\" height=\"18\"")
                .doesNotContain("y=\"63\" width=\"26\" height=\"18\"");
        assertThat(renderer.render(TestFixtures.cardData(SvgLayout.NORMAL, SvgTheme.GITHUB_DARK)))
                .contains("y=\"32\" width=\"26\" height=\"18\"")
                .doesNotContain("y=\"66\" width=\"26\" height=\"18\"");
        assertThat(renderer.render(TestFixtures.cardData(SvgLayout.HERO, SvgTheme.GITHUB_DARK)))
                .contains("y=\"39\" width=\"26\" height=\"18\"")
                .doesNotContain("y=\"71\" width=\"26\" height=\"18\"");
        assertThat(renderer.render(TestFixtures.cardData(SvgLayout.MINIMAL, SvgTheme.GITHUB_DARK)))
                .contains("y=\"31\" width=\"26\" height=\"18\"")
                .doesNotContain("y=\"62\" width=\"26\" height=\"18\"");
    }

    @Test
    void normalLayoutPlacesReleaseDateUnderCoverAndUsesCountryFlag() {
        String svg = renderer.render(TestFixtures.cardData(SvgLayout.NORMAL, SvgTheme.GITHUB_DARK));

        assertThat(svg)
                .contains("<text x=\"448\" y=\"158\" text-anchor=\"middle\" font-size=\"11\"")
                .contains(">Aug 21, 2012</text>")
                .contains("<text x=\"34\" y=\"144\" font-size=\"11\"")
                .contains("Level 42 · Library 120 · Friends 12")
                .contains("aria-label=\"ES\"")
                .contains("#aa151b")
                .contains("#f1bf00")
                .contains("<a href=\"https://alextc.es\"")
                .doesNotContain(">ES</text>")
                .doesNotContain("Steam Card API")
                .doesNotContain("Last session")
                .doesNotContain("Level 42 · 12 friends · 120 games")
                .doesNotContain("<text x=\"146\" y=\"112\" font-size=\"13\"")
                .doesNotContain(" · Aug 21, 2012");
    }

    @Test
    void profileLayoutsUseFlagBadgesAndDoNotRenderApiFooterBranding() {
        for (SvgLayout layout : SvgLayout.values()) {
            String svg = renderer.render(TestFixtures.cardData(layout, SvgTheme.GITHUB_DARK));

            assertThat(svg)
                    .describedAs(layout.value())
                    .contains("aria-label=\"ES\"")
                    .contains("#aa151b")
                    .contains("#f1bf00")
                    .doesNotContain(">ES</text>")
                    .doesNotContain("Steam Card API");
        }
    }

    @Test
    void normalLayoutShowsLastSessionUnderOfflineBadgeOnly() {
        SteamCardData online = TestFixtures.cardData(SvgLayout.NORMAL, SvgTheme.GITHUB_DARK);
        SteamCardData offline = withStatus(online, "Offline", false);

        String onlineSvg = renderer.render(online);
        String offlineSvg = renderer.render(offline);

        assertThat(onlineSvg).doesNotContain("Last session");
        assertThat(offlineSvg)
                .contains("Last session Mar 9, 2024")
                .contains("<text x=\"146\" y=\"100\" font-size=\"10\"")
                .doesNotContain(" · Last session");
    }

    @Test
    void compactLayoutShowsOnlyLastSessionUnderOfflineBadge() {
        SteamCardData online = TestFixtures.cardData(SvgLayout.COMPACT, SvgTheme.GITHUB_DARK);
        SteamCardData offline = withStatus(online, "Offline", false);

        String onlineSvg = renderer.render(online);
        String offlineSvg = renderer.render(offline);

        assertThat(onlineSvg).doesNotContain("Last session");
        assertThat(offlineSvg)
                .contains("Last session Mar 9, 2024")
                .contains("<text x=\"140\" y=\"95\" font-size=\"10\"")
                .doesNotContain("Level 42")
                .doesNotContain("Library 120")
                .doesNotContain("Friends 12")
                .doesNotContain(" · Last session");
    }

    @Test
    void heroLayoutPlacesReleaseDateInFooterAndOfflineLastSessionUnderBadge() {
        SteamCardData online = TestFixtures.cardData(SvgLayout.HERO, SvgTheme.GITHUB_DARK);
        SteamCardData offline = withStatus(online, "Offline", false);
        SteamCardData playing = TestFixtures.cardData(SvgLayout.HERO, SvgTheme.GITHUB_DARK,
                TestFixtures.game(730, "Counter <Strike> 2", 600, true));

        String onlineSvg = renderer.render(online);
        String offlineSvg = renderer.render(offline);
        String playingSvg = renderer.render(playing);

        assertThat(onlineSvg)
                .containsPattern("(?s)<a href=\"https://store\\.steampowered\\.com/app/730\"[^>]*>\\s*<text x=\"42\" y=\"187\"")
                .contains("<text x=\"42\" y=\"256\" font-size=\"11\"")
                .contains(">Aug 21, 2012</text>")
                .contains("<text x=\"674\" y=\"256\" text-anchor=\"end\" font-size=\"11\"")
                .contains("<text x=\"42\" y=\"219\" font-size=\"15\"")
                .contains(">Total 10.0 h · 2 weeks 1.5 h</text>")
                .doesNotContain("Last session")
                .doesNotContain("Live on Steam")
                .doesNotContain("y=\"179\" font-size=\"11\"")
                .doesNotContain(">- Aug 21, 2012</text>")
                .doesNotContain(" · Aug 21, 2012")
                .doesNotContain(" · Last session")
                .doesNotContain("<text x=\"42\" y=\"211\" font-size=\"11\"");
        assertThat(offlineSvg)
                .contains("Last session Mar 9, 2024")
                .contains("<text x=\"126\" y=\"105\" font-size=\"10\"");
        assertThat(playingSvg)
                .contains(">Currently playing<")
                .doesNotContain("Live on Steam");
    }

    @Test
    void minimalLayoutOnlyShowsGameTitleWhenInGame() {
        SteamCardData online = TestFixtures.cardData(SvgLayout.MINIMAL, SvgTheme.GITHUB_DARK);
        SteamCardData offline = withStatus(online, "Offline", false);
        SteamCardData playing = TestFixtures.cardData(SvgLayout.MINIMAL, SvgTheme.GITHUB_DARK,
                TestFixtures.game(730, "Counter <Strike> 2", 600, true));

        String onlineSvg = renderer.render(online);
        String offlineSvg = renderer.render(offline);
        String playingSvg = renderer.render(playing);

        assertThat(onlineSvg)
                .contains("<a href=\"https://alextc.es\"")
                .doesNotContain("<text x=\"126\" y=\"126\"")
                .doesNotContain("https://store.steampowered.com/app/730")
                .doesNotContain("Last session");
        assertThat(offlineSvg)
                .contains("<a href=\"https://alextc.es\"")
                .contains("Last session Mar 9, 2024")
                .contains("<text x=\"126\" y=\"104\" font-size=\"11\"")
                .doesNotContain("<text x=\"126\" y=\"126\"")
                .doesNotContain("https://store.steampowered.com/app/730");
        assertThat(playingSvg)
                .contains("<a href=\"https://alextc.es\"")
                .contains(">IN-GAME<")
                .containsPattern("(?s)<a href=\"https://store\\.steampowered\\.com/app/730\"[^>]*>\\s*<text x=\"126\" y=\"126\"")
                .contains("Counter &lt;Strike&gt; 2")
                .doesNotContain("Last session");
    }

    @Test
    void localizesProfileLabelsWithRequestedLanguage() {
        SteamCardData showcase = withLocale(TestFixtures.cardData(SvgLayout.SHOWCASE, SvgTheme.GITHUB_DARK), "es");
        SteamCardData online = withLocale(TestFixtures.cardData(SvgLayout.MINIMAL, SvgTheme.GITHUB_DARK), "es");
        SteamCardData playing = withLocale(TestFixtures.cardData(SvgLayout.HERO, SvgTheme.GITHUB_DARK,
                TestFixtures.game(730, "Counter <Strike> 2", 600, true)), "es");
        SteamCardData offline = withLocale(withStatus(TestFixtures.cardData(SvgLayout.MINIMAL, SvgTheme.GITHUB_DARK),
                "Offline", false), "es");

        String showcaseSvg = renderer.render(showcase);
        String onlineSvg = renderer.render(online);
        String playingSvg = renderer.render(playing);
        String offlineSvg = renderer.render(offline);

        assertThat(showcaseSvg)
                .contains(">Último juego<")
                .contains("Nivel 42 · Biblioteca 120 · Amigos 12")
                .contains("Total 10.0 h · 2 semanas 1.5 h")
                .contains("Tiempo total jugado en las últimas 2 semanas 1.5 h")
                .doesNotContain(">Last played<")
                .doesNotContain("Level 42 · Library 120 · Friends 12")
                .doesNotContain("2 weeks");
        assertThat(onlineSvg)
                .contains(">EN LÍNEA<")
                .doesNotContain(">EN LINEA<")
                .doesNotContain(">ONLINE<");
        assertThat(playingSvg)
                .contains(">Jugando ahora<")
                .contains(">EN JUEGO<")
                .doesNotContain(">Currently playing<")
                .doesNotContain(">IN-GAME<");
        assertThat(offlineSvg)
                .contains("Última sesión")
                .contains("2024")
                .contains(">DESCONECTADO<")
                .contains("<rect x=\"126\" y=\"62\" width=\"128\" height=\"24\"")
                .doesNotContain("Last session")
                .doesNotContain(">OFFLINE<");
    }

    @Test
    void nonPlayingGameEyebrowSaysLastPlayed() {
        for (SvgLayout layout : new SvgLayout[] {SvgLayout.COMPACT, SvgLayout.NORMAL, SvgLayout.SHOWCASE, SvgLayout.HERO}) {
            String svg = renderer.render(TestFixtures.cardData(layout, SvgTheme.GITHUB_DARK));

            assertThat(svg)
                    .describedAs(layout.value())
                    .contains(">Last played<")
                    .doesNotContain("Featured game")
                    .doesNotContain("Featured Steam game");
        }
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
                .containsPattern("(?s)<a href=\"https://store\\.steampowered\\.com/app/730\"[^>]*>\\s*<image href=\"data:image/png;base64,COVER\"")
                .containsPattern("(?s)<a href=\"https://store\\.steampowered\\.com/app/730\"[^>]*>\\s*<text x=\"216\" y=\"164\"");
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
                data.border(),
                data.resolvedImages(),
                data.renderedAvatarUrl(),
                data.renderedPrimaryImageUrl());
    }

    private SteamCardData withLocale(SteamCardData data, String locale) {
        return new SteamCardData(
                data.profile(),
                data.selectedGame(),
                data.recentGames(),
                data.favoriteGames(),
                data.statistics(),
                data.librarySummary(),
                data.theme(),
                data.layout(),
                locale,
                data.accent(),
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
                data.border(),
                data.resolvedImages(),
                data.renderedAvatarUrl(),
                data.renderedPrimaryImageUrl());
    }
}

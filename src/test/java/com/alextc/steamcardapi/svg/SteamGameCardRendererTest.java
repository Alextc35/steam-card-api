package com.alextc.steamcardapi.svg;

import static org.assertj.core.api.Assertions.assertThat;

import com.alextc.steamcardapi.TestFixtures;
import com.alextc.steamcardapi.model.BorderStyle;
import com.alextc.steamcardapi.model.SteamGame;
import org.junit.jupiter.api.Test;

class SteamGameCardRendererTest {

    private final SteamGameCardRenderer renderer = new SteamGameCardRenderer();

    @Test
    void generatesGameCardWithCoverAppIdHoursAndMetadata() {
        SteamGame game = TestFixtures.game(730, "Counter-Strike 2", 600, false);

        String svg = renderer.render(game, SvgTheme.GITHUB_DARK, SvgLayout.SHOWCASE,
                BorderStyle.ROUNDED, game.images().portraitCoverUrl(), "en");

        assertThat(svg).startsWith("<svg");
        assertThat(svg).contains("Steam game · AppID 730");
        assertThat(svg).contains("Counter-Strike 2");
        assertThat(svg).contains("10.0 h played");
        assertThat(svg).contains("Free to play");
        assertThat(svg).contains(game.images().portraitCoverUrl());
        assertThat(svg).contains("<clipPath");
        assertThat(svg).doesNotContain("Steam Card API");
    }

    @Test
    void rendersHeroLayoutWithBackgroundOverlay() {
        SteamGame game = TestFixtures.game(730, "Counter-Strike 2", 600, false);

        String svg = renderer.render(game, SvgTheme.STEAM, SvgLayout.HERO,
                BorderStyle.SQUARE, game.images().heroUrl(), "en");

        assertThat(svg).contains("width=\"700\"");
        assertThat(svg).contains("height=\"270\"");
        assertThat(svg).contains("opacity=\"0.78\"");
        assertThat(svg).contains("preserveAspectRatio=\"xMidYMid slice\"");
    }

    @Test
    void localizesGameCardLabels() {
        SteamGame game = TestFixtures.game(730, "Counter-Strike 2", 600, false);

        String svg = renderer.render(game, SvgTheme.GITHUB_DARK, SvgLayout.SHOWCASE,
                BorderStyle.ROUNDED, game.images().portraitCoverUrl(), "es");

        assertThat(svg)
                .contains("Juego de Steam · AppID 730")
                .contains("10.0 h jugadas")
                .contains("Gratis")
                .doesNotContain("Steam game · AppID 730")
                .doesNotContain("10.0 h played")
                .doesNotContain("Free to play");
    }
}

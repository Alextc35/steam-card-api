package com.alextc.steamcardapi.svg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CountryFlagRendererTest {

    @Test
    void rendersEverySupportedCountryFlagAsInlineSvg() {
        assertThat(CountryFlagRenderer.SUPPORTED_COUNTRY_CODES).hasSize(100);

        for (String code : CountryFlagRenderer.SUPPORTED_COUNTRY_CODES) {
            String svg = CountryFlagRenderer.render(code, SvgTheme.GITHUB_DARK.palette(null), 10, 20);

            assertThat(svg)
                    .as(code)
                    .contains("aria-label=\"%s\"".formatted(code))
                    .contains("<title>%s</title>".formatted(code))
                    .contains("width=\"26\" height=\"18\"")
                    .contains("<svg x=\"12\" y=\"22\" width=\"22\" height=\"14\"");
        }
    }

    @Test
    void returnsBlankForUnsupportedOrEmptyCountryCode() {
        assertThat(CountryFlagRenderer.render("XX", SvgTheme.GITHUB_DARK.palette(null), 10, 20)).isBlank();
        assertThat(CountryFlagRenderer.render("", SvgTheme.GITHUB_DARK.palette(null), 10, 20)).isBlank();
        assertThat(CountryFlagRenderer.render(null, SvgTheme.GITHUB_DARK.palette(null), 10, 20)).isBlank();
    }
}

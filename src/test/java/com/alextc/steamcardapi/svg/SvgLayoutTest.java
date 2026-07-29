package com.alextc.steamcardapi.svg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import org.junit.jupiter.api.Test;

class SvgLayoutTest {

    @Test
    void parsesSupportedLayoutsAndDefaultsToShowcase() {
        assertThat(SvgLayout.parse(null)).isEqualTo(SvgLayout.SHOWCASE);
        assertThat(SvgLayout.parse("compact")).isEqualTo(SvgLayout.COMPACT);
        assertThat(SvgLayout.parse("normal")).isEqualTo(SvgLayout.NORMAL);
        assertThat(SvgLayout.parse("showcase")).isEqualTo(SvgLayout.SHOWCASE);
        assertThat(SvgLayout.parse("hero")).isEqualTo(SvgLayout.HERO);
        assertThat(SvgLayout.parse("minimal")).isEqualTo(SvgLayout.MINIMAL);
    }

    @Test
    void rejectsRemovedLibraryLayout() {
        assertThatThrownBy(() -> SvgLayout.parse("library"))
                .isInstanceOf(InvalidCardParameterException.class)
                .hasMessage("layout must be one of compact, normal, showcase, hero, minimal");
    }
}

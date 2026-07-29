package com.alextc.steamcardapi.model;

import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTheme;
import java.util.Set;

public record SteamCardRequest(
        SteamSubject subject,
        SvgTheme theme,
        SvgLayout layout,
        String locale,
        String accent,
        Set<ShowSection> show,
        CardImageMode imageMode,
        GameImageType gameImage,
        boolean animation,
        BorderStyle border
) {

    public String cacheKey() {
        return "%s|%s|%s|%s|%s|%s|%s|%s|%s|%s".formatted(
                subject.cacheKey(),
                theme.value(),
                layout.value(),
                locale,
                accent == null ? "" : accent,
                show.stream().map(ShowSection::value).sorted().toList(),
                imageMode.value(),
                gameImage.value(),
                animation,
                border.value());
    }
}

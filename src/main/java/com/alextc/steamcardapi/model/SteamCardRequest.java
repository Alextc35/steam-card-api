package com.alextc.steamcardapi.model;

import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTheme;

public record SteamCardRequest(
        SteamSubject subject,
        SvgTheme theme,
        SvgLayout layout,
        String locale,
        String accent,
        CardImageMode imageMode,
        GameImageType gameImage,
        BorderStyle border
) {

    public String cacheKey() {
        return "%s|%s|%s|%s|%s|%s|%s|%s".formatted(
                subject.cacheKey(),
                theme.value(),
                layout.value(),
                locale,
                accent == null ? "" : accent,
                imageMode.value(),
                gameImage.value(),
                border.value());
    }
}

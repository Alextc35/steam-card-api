package com.alextc.steamcardapi.svg;

import com.alextc.steamcardapi.model.BorderStyle;
import com.alextc.steamcardapi.model.SteamGame;
import org.springframework.stereotype.Component;

@Component
public class SteamGameCardRenderer {

    public String render(SteamGame game, SvgTheme theme, SvgLayout layout, BorderStyle border, String renderedImageUrl) {
        SvgTheme.Palette palette = theme.palette(null);
        int width = layout == SvgLayout.COMPACT || layout == SvgLayout.MINIMAL ? 500 : 700;
        int height = layout == SvgLayout.HERO ? 270 : layout == SvgLayout.SHOWCASE ? 300 : 230;
        String id = "steam-game-" + game.appId() + "-" + layout.value();
        int radius = border.radius();
        String image = layout == SvgLayout.HERO
                ? SvgImageUtils.image(renderedImageUrl, 0, 0, width, height, id + "-bg")
                : SvgImageUtils.image(renderedImageUrl, 28, 28, 156, Math.min(234, height - 56), id + "-cover");
        String overlay = layout == SvgLayout.HERO
                ? "<rect width=\"%d\" height=\"%d\" rx=\"%d\" fill=\"%s\" opacity=\"0.78\"/>".formatted(width, height, radius, palette.background())
                : "";
        int textX = layout == SvgLayout.HERO ? 42 : 220;
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d" role="img" aria-labelledby="%s-title %s-desc">
                  <title id="%s-title">Steam game card for %s</title>
                  <desc id="%s-desc">AppID %d</desc>
                  <defs>
                    <clipPath id="%s-cover"><rect x="28" y="28" width="156" height="%d" rx="18"/></clipPath>
                    <clipPath id="%s-bg"><rect width="%d" height="%d" rx="%d"/></clipPath>
                  </defs>
                  <rect width="%d" height="%d" rx="%d" fill="%s"/>
                  %s
                  %s
                  <text x="%d" y="72" font-size="14" font-weight="700" fill="%s">Steam game · AppID %d</text>
                  <text x="%d" y="112" font-size="32" font-weight="700" fill="%s">%s</text>
                  <text x="%d" y="145" font-size="15" fill="%s">%s</text>
                  <text x="%d" y="178" font-size="14" fill="%s">%s</text>
                  <text x="%d" y="%d" font-size="12" fill="%s">Steam Card API</text>
                </svg>
                """.formatted(
                width, height, width, height, id, id, id, SvgTextUtils.escape(game.name()), id, game.appId(),
                id, Math.min(234, height - 56), id, width, height, radius,
                width, height, radius, palette.background(),
                image,
                overlay,
                textX, palette.accent(), game.appId(),
                textX, palette.primaryText(), SvgTextUtils.escape(SvgTextUtils.truncate(game.name(), 32)),
                textX, palette.secondaryText(), SvgTextUtils.escape(SvgTextUtils.truncate(description(game), 62)),
                textX, palette.mutedText(), meta(game),
                textX, height - 22, palette.mutedText());
    }

    private String description(SteamGame game) {
        if (game.shortDescription() != null && !game.shortDescription().isBlank()) {
            return game.shortDescription();
        }
        if (!game.genres().isEmpty()) {
            return String.join(", ", game.genres());
        }
        return "Steam game metadata";
    }

    private String meta(SteamGame game) {
        String price = game.freeToPlay() ? "Free to play" : game.price() == null ? "Price unavailable" : game.price();
        String release = game.releaseDate() == null ? "Release unavailable" : game.releaseDate();
        return "%s · %s · %.1f h played".formatted(price, release, game.hoursForever());
    }
}

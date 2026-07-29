package com.alextc.steamcardapi.svg;

import com.alextc.steamcardapi.model.BorderStyle;
import com.alextc.steamcardapi.model.ShowSection;
import com.alextc.steamcardapi.model.SteamCardData;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.model.SteamStatistics;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class SteamProfileCardRenderer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("MMM d, yyyy", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);
    private static final String STEAM_ONLINE_BLUE = "#66c0f4";
    private static final String STEAM_IN_GAME_GREEN = "#90ba3c";

    public String render(SteamCardData data) {
        return switch (data.layout()) {
            case COMPACT -> renderCompact(data);
            case NORMAL -> renderNormal(data);
            case SHOWCASE -> renderShowcase(data);
            case HERO -> renderHero(data);
            case MINIMAL -> renderMinimal(data);
            case LIBRARY -> renderLibrary(data);
        };
    }

    public String renderFallback(SvgTheme theme, SvgLayout layout) {
        SvgTheme.Palette palette = theme.palette(null);
        int width = layout.width();
        int height = layout.height();
        String id = id("fallback", theme.value(), layout.value());
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d" role="img" aria-labelledby="%s-title %s-desc">
                  <title id="%s-title">Steam profile temporarily unavailable</title>
                  <desc id="%s-desc">Steam profile temporarily unavailable</desc>
                  <rect width="%d" height="%d" rx="20" fill="%s"/>
                  <rect x="1" y="1" width="%d" height="%d" rx="19" fill="none" stroke="%s" stroke-width="2"/>
                  <circle cx="82" cy="%d" r="46" fill="%s"/>
                  <text x="82" y="%d" text-anchor="middle" font-size="30" font-weight="700" fill="%s">!</text>
                  <text x="154" y="%d" font-size="22" font-weight="700" fill="%s">Steam profile temporarily unavailable</text>
                  <text x="154" y="%d" font-size="14" fill="%s">Try again in a few minutes.</text>
                </svg>
                """.formatted(width, height, width, height, id, id, id, id,
                width, height, palette.background(), width - 2, height - 2, palette.border(),
                height / 2, palette.border(), height / 2 + 9, palette.mutedText(),
                height / 2 - 7, palette.primaryText(), height / 2 + 22, palette.secondaryText());
    }

    private String renderCompact(SteamCardData data) {
        SvgTheme.Palette palette = data.theme().palette(data.accent());
        String id = id(data);
        SteamGame game = data.selectedGame();
        String gameName = game == null ? "No game available" : game.name();
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="500" height="170" viewBox="0 0 500 170" role="img" aria-labelledby="%s-title %s-desc">
                  %s
                  %s
                  <defs>
                    <clipPath id="%s-avatar"><circle cx="72" cy="72" r="46"/></clipPath>
                    <clipPath id="%s-game"><rect x="376" y="24" width="88" height="88" rx="12"/></clipPath>
                  </defs>
                  %s
                  %s
                  %s
                  %s
                  %s
                  <text x="238" y="76" font-size="13" fill="%s">%s</text>
                  <text x="140" y="105" font-size="12" font-weight="700" fill="%s">%s</text>
                  <text x="140" y="129" font-size="18" font-weight="700" fill="%s">%s</text>
                  <text x="140" y="151" font-size="13" fill="%s">%s</text>
                  %s
                </svg>
                """.formatted(
                id, id,
                title(data, id), desc(data, id), id, id,
                frame(500, 170, data.border(), palette),
                avatar(data, id, 26, 26, 92, 92),
                primaryImage(data, palette, 376, 24, 88, 88, id + "-game"),
                profileName(data, 140, 46, 24, 23, palette.primaryText()),
                statusBadge(data, palette, 140, 61, 88), palette.secondaryText(), countryText(data),
                palette.accent(), game == null || !game.currentlyPlaying() ? "Last played" : "Currently playing",
                palette.primaryText(), text(gameName, 30),
                palette.secondaryText(), hours(game),
                footer(500, 158, palette));
    }

    private String renderNormal(SteamCardData data) {
        SvgTheme.Palette palette = data.theme().palette(data.accent());
        String id = id(data);
        SteamGame game = data.selectedGame();
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="600" height="230" viewBox="0 0 600 230" role="img" aria-labelledby="%s-title %s-desc">
                  %s
                  %s
                  <defs>
                    <clipPath id="%s-avatar"><circle cx="78" cy="78" r="46"/></clipPath>
                    <clipPath id="%s-header"><rect x="330" y="24" width="236" height="110" rx="14"/></clipPath>
                  </defs>
                  %s
                  %s
                  <rect x="318" y="16" width="260" height="128" rx="18" fill="%s"/>
                  %s
                  %s
                  %s
                  <text x="246" y="79" font-size="13" fill="%s">%s</text>
                  <text x="146" y="112" font-size="13" fill="%s">Level %s · %s friends · %s games</text>
                  <text x="34" y="160" font-size="12" font-weight="700" fill="%s">%s</text>
                  <text x="34" y="186" font-size="21" font-weight="700" fill="%s">%s</text>
                  <text x="34" y="208" font-size="13" fill="%s">%s</text>
                  %s
                </svg>
                """.formatted(
                id, id,
                title(data, id), desc(data, id), id, id,
                frame(600, 230, data.border(), palette),
                avatar(data, id, 32, 32, 92, 92),
                palette.panel(),
                primaryImage(data, palette, 330, 24, 236, 110, id + "-header"),
                profileName(data, 146, 51, 25, 26, palette.primaryText()),
                statusBadge(data, palette, 146, 64, 88), palette.secondaryText(), countryText(data),
                palette.mutedText(), number(data.profile().steamLevel()), number(data.profile().friendCount()), number(data.profile().gameCount()),
                palette.accent(), game == null || !game.currentlyPlaying() ? "Featured game" : "Currently playing",
                palette.primaryText(), text(game == null ? "No game available" : game.name(), 42),
                palette.secondaryText(), gameMeta(game),
                footer(600, 218, palette));
    }

    private String renderShowcase(SteamCardData data) {
        SvgTheme.Palette palette = data.theme().palette(data.accent());
        String id = id(data);
        SteamGame game = data.selectedGame();
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="700" height="300" viewBox="0 0 700 300" role="img" aria-labelledby="%s-title %s-desc">
                  %s
                  %s
                  <defs>
                    <clipPath id="%s-cover"><rect x="26" y="24" width="156" height="234" rx="18"/></clipPath>
                    <clipPath id="%s-avatar"><circle cx="240" cy="70" r="34"/></clipPath>
                  </defs>
                  %s
                  <rect x="18" y="16" width="172" height="250" rx="22" fill="%s"/>
                  %s
                  <text x="104" y="278" text-anchor="middle" font-size="11" fill="%s">%s</text>
                  %s
                  %s
                  %s
                  %s
                  %s
                  <text x="216" y="134" font-size="13" font-weight="700" fill="%s">%s</text>
                  <text x="216" y="164" font-size="28" font-weight="700" fill="%s">%s</text>
                  <text x="216" y="193" font-size="14" fill="%s">%s</text>
                  <text x="216" y="228" font-size="14" fill="%s">Level %s · Library %s · Friends %s</text>
                  <text x="216" y="253" font-size="13" fill="%s">%s</text>
                  %s
                </svg>
                """.formatted(
                id, id,
                title(data, id), desc(data, id), id, id,
                frame(700, 300, data.border(), palette),
                palette.panel(),
                primaryImage(data, palette, 26, 24, 156, 234, id + "-cover"),
                palette.mutedText(), releaseDate(game),
                avatar(data, id, 206, 36, 68, 68),
                profileName(data, 292, 55, 27, 26, palette.primaryText()),
                countryBadgeAfterName(data, palette, 292, 36, 27, 26),
                statusBadge(data, 292, 64, 88),
                lastSessionUnderStatus(data, palette, 292, 102),
                palette.accent(), game == null || !game.currentlyPlaying() ? "Last played" : "Currently playing",
                palette.primaryText(), text(game == null ? "No game available" : game.name(), 33),
                palette.secondaryText(), game == null ? "No playtime data" : hours(game),
                palette.secondaryText(), number(data.profile().steamLevel()), number(data.profile().gameCount()), number(data.profile().friendCount()),
                palette.mutedText(), showcaseMeta(data),
                showcaseFooter(700, 286, palette));
    }

    private String renderHero(SteamCardData data) {
        SvgTheme.Palette palette = data.theme().palette(data.accent());
        String id = id(data);
        SteamGame game = data.selectedGame();
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="700" height="270" viewBox="0 0 700 270" role="img" aria-labelledby="%s-title %s-desc">
                  %s
                  %s
                  <defs>
                    <clipPath id="%s-card"><rect width="700" height="270" rx="%d"/></clipPath>
                    <clipPath id="%s-avatar"><circle cx="70" cy="72" r="38"/></clipPath>
                    <linearGradient id="%s-overlay" x1="0" y1="0" x2="1" y2="0">
                      <stop offset="0" stop-color="%s" stop-opacity="0.92"/>
                      <stop offset="0.62" stop-color="%s" stop-opacity="0.74"/>
                      <stop offset="1" stop-color="%s" stop-opacity="0.46"/>
                    </linearGradient>
                  </defs>
                  <rect width="700" height="270" rx="%d" fill="%s"/>
                  %s
                  <rect width="700" height="270" rx="%d" fill="url(#%s-overlay)"/>
                  %s
                  %s
                  %s
                  <text x="226" y="85" font-size="13" fill="%s">%s</text>
                  <text x="42" y="152" font-size="13" font-weight="700" fill="%s">%s</text>
                  <text x="42" y="187" font-size="31" font-weight="700" fill="%s">%s</text>
                  <text x="42" y="219" font-size="15" fill="%s">%s</text>
                  %s
                </svg>
                """.formatted(
                id, id,
                title(data, id), desc(data, id), id, data.border().radius(), id, id,
                palette.background(), palette.background(), palette.background(),
                data.border().radius(), palette.background(),
                primaryImage(data, palette, 0, 0, 700, 270, id + "-card"),
                data.border().radius(), id,
                avatar(data, id, 32, 34, 76, 76),
                profileName(data, 126, 58, 27, 28, palette.primaryText()),
                statusBadge(data, palette, 126, 69, 88), palette.secondaryText(), countryText(data),
                palette.accent(), game == null || !game.currentlyPlaying() ? "Featured Steam game" : "Live on Steam",
                palette.primaryText(), text(game == null ? "No game available" : game.name(), 34),
                palette.secondaryText(), gameMeta(game),
                footer(700, 256, palette));
    }

    private String renderMinimal(SteamCardData data) {
        SvgTheme.Palette palette = data.theme().palette(data.accent());
        String id = id(data);
        SteamGame game = data.selectedGame();
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="500" height="150" viewBox="0 0 500 150" role="img" aria-labelledby="%s-title %s-desc">
                  %s
                  %s
                  <defs><clipPath id="%s-avatar"><circle cx="61" cy="61" r="34"/></clipPath></defs>
                  %s
                  %s
                  %s
                  %s
                  <text x="216" y="76" font-size="13" fill="%s">%s</text>
                  <text x="116" y="112" font-size="18" font-weight="700" fill="%s">%s</text>
                </svg>
                """.formatted(id, id, title(data, id), desc(data, id), id,
                frame(500, 150, data.border(), palette), avatar(data, id, 27, 27, 68, 68),
                profileName(data, 116, 48, 23, 25, palette.primaryText()),
                statusBadge(data, palette, 116, 60, 88), palette.secondaryText(), countryText(data),
                palette.primaryText(), text(game == null ? "No game available" : game.name(), 34));
    }

    private String renderLibrary(SteamCardData data) {
        SvgTheme.Palette palette = data.theme().palette(data.accent());
        String id = id(data);
        StringBuilder rows = new StringBuilder();
        int y = 156;
        int max = data.recentGames().stream()
                .mapToInt(game -> game.playtimeForeverMinutes() == null ? 0 : game.playtimeForeverMinutes())
                .max()
                .orElse(1);
        for (SteamGame game : data.recentGames()) {
            int barWidth = Math.max(24, (int) Math.round((game.playtimeForeverMinutes() == null ? 0 : game.playtimeForeverMinutes()) / (double) max * 220));
            rows.append("""
                      <text x="52" y="%d" font-size="14" fill="%s">%s</text>
                      <rect x="330" y="%d" width="%d" height="10" rx="5" fill="%s"/>
                      <text x="570" y="%d" text-anchor="end" font-size="12" fill="%s">%.1f h</text>
                    """.formatted(y, palette.primaryText(), text(game.name(), 32), y - 9, barWidth,
                    palette.accent(), y, palette.secondaryText(), game.hoursForever()));
            y += 36;
        }
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="700" height="330" viewBox="0 0 700 330" role="img" aria-labelledby="%s-title %s-desc">
                  %s
                  %s
                  <defs><clipPath id="%s-avatar"><circle cx="68" cy="68" r="38"/></clipPath></defs>
                  %s
                  %s
                  %s
                  %s
                  <text x="226" y="82" font-size="13" fill="%s">%s</text>
                  <text x="52" y="126" font-size="13" font-weight="700" fill="%s">Recent library activity</text>
                  %s
                  <text x="52" y="296" font-size="13" fill="%s">Most played: %s</text>
                  %s
                </svg>
                """.formatted(id, id, title(data, id), desc(data, id), id,
                frame(700, 330, data.border(), palette), avatar(data, id, 30, 30, 76, 76),
                profileName(data, 126, 56, 27, 26, palette.primaryText()),
                statusBadge(data, palette, 126, 66, 88), palette.secondaryText(), countryText(data),
                palette.accent(), rows,
                palette.secondaryText(), text(data.librarySummary() == null || data.librarySummary().mostPlayedGame() == null
                        ? "Unavailable"
                        : data.librarySummary().mostPlayedGame().name(), 36),
                footer(700, 318, palette));
    }

    private String title(SteamCardData data, String id) {
        return "<title id=\"%s-title\">Steam profile card for %s</title>"
                .formatted(id, SvgTextUtils.escape(data.profile().nickname()));
    }

    private String desc(SteamCardData data, String id) {
        String gameName = data.selectedGame() == null ? "no game selected" : data.selectedGame().name();
        return "<desc id=\"%s-desc\">%s, %s</desc>"
                .formatted(id, SvgTextUtils.escape(data.profile().status()), SvgTextUtils.escape(gameName));
    }

    private String frame(int width, int height, BorderStyle border, SvgTheme.Palette palette) {
        int radius = border.radius();
        String stroke = border.visible()
                ? "<rect x=\"1\" y=\"1\" width=\"%d\" height=\"%d\" rx=\"%d\" fill=\"none\" stroke=\"%s\" stroke-width=\"2\"/>"
                        .formatted(width - 2, height - 2, Math.max(0, radius - 1), palette.border())
                : "";
        return """
                <rect width="%d" height="%d" rx="%d" fill="%s"/>
                %s
                <rect x="0" y="0" width="8" height="%d" rx="%d" fill="%s"/>
                """.formatted(width, height, radius, palette.background(), stroke, height, Math.min(4, radius), palette.accent());
    }

    private String avatar(SteamCardData data, String id, int x, int y, int size, int size2) {
        if (!data.show().contains(ShowSection.AVATAR)) {
            return "";
        }
        return profileLink(data, SvgImageUtils.image(data.renderedAvatarUrl(), x, y, size, size2, id + "-avatar"));
    }

    private String primaryImage(SteamCardData data, SvgTheme.Palette palette, int x, int y, int width, int height, String clipPathId) {
        String image = SvgImageUtils.image(data.renderedPrimaryImageUrl(), x, y, width, height, clipPathId);
        if (image.isBlank()) {
            image = gameImagePlaceholder(data, palette, x, y, width, height, clipPathId);
        }
        return gameStoreLink(data, image);
    }

    private String gameImagePlaceholder(SteamCardData data, SvgTheme.Palette palette, int x, int y, int width, int height, String clipPathId) {
        boolean gameMissing = data.selectedGame() == null;
        String title = gameMissing ? "No game found" : "Cover not found";
        String detail = gameMissing ? "No public recent game data" : text(data.selectedGame().name(), 28);
        String clip = clipPathId == null || clipPathId.isBlank() ? "" : " clip-path=\"url(#%s)\"".formatted(clipPathId);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int iconSize = Math.max(26, Math.min(width, height) / 4);
        int iconX = centerX - iconSize / 2;
        int iconY = centerY - iconSize / 2 - (height >= 150 ? 26 : 14);
        int titleSize = height >= 150 ? 13 : 10;
        int detailSize = height >= 150 ? 10 : 8;
        int titleY = iconY + iconSize + (height >= 150 ? 28 : 20);
        int detailY = titleY + (height >= 150 ? 18 : 13);
        return """
                <g%s>
                  <rect x="%d" y="%d" width="%d" height="%d" fill="%s"/>
                  <path d="M%d %d C%d %d %d %d %d %d" fill="none" stroke="%s" stroke-width="2" opacity="0.28"/>
                  <rect x="%d" y="%d" width="%d" height="%d" rx="%d" fill="%s" stroke="%s" stroke-width="1.5" opacity="0.92"/>
                  <circle cx="%d" cy="%d" r="%d" fill="%s" opacity="0.18"/>
                  <text x="%d" y="%d" text-anchor="middle" font-size="%d" font-weight="700" fill="%s">?</text>
                  <text x="%d" y="%d" text-anchor="middle" font-size="%d" font-weight="700" fill="%s">%s</text>
                  <text x="%d" y="%d" text-anchor="middle" font-size="%d" fill="%s">%s</text>
                </g>
                """.formatted(clip,
                x, y, width, height, palette.panel(),
                x + 12, y + height - 36, x + width / 3, y + height - 60, x + width * 2 / 3, y + height - 18, x + width - 12, y + height - 44,
                palette.accent(),
                iconX, iconY, iconSize, iconSize, Math.max(6, iconSize / 5), palette.background(), palette.border(),
                centerX, iconY + iconSize / 2, Math.max(8, iconSize / 3), palette.accent(),
                centerX, iconY + iconSize / 2 + Math.max(5, iconSize / 8), Math.max(18, iconSize / 2), palette.accent(),
                centerX, titleY, titleSize, palette.primaryText(), SvgTextUtils.escape(title),
                centerX, detailY, detailSize, palette.mutedText(), SvgTextUtils.escape(detail));
    }

    private String profileName(SteamCardData data, int x, int y, int fontSize, int maxCodePoints, String fill) {
        String name = """
                <text x="%d" y="%d" font-size="%d" font-weight="700" fill="%s">%s</text>
                """.formatted(x, y, fontSize, fill, text(data.profile().nickname(), maxCodePoints));
        return profileLink(data, name);
    }

    private String profileLink(SteamCardData data, String content) {
        String profileUrl = data.profile().profileUrl();
        if (profileUrl == null || profileUrl.isBlank() || content.isBlank()) {
            return content;
        }
        return """
                <a href="%s" target="_blank" rel="noopener noreferrer">
                  %s
                </a>
                """.formatted(SvgTextUtils.escape(profileUrl), content);
    }

    private String gameStoreLink(SteamCardData data, String content) {
        SteamGame game = data.selectedGame();
        if (game == null || game.appId() == null || content.isBlank()) {
            return content;
        }
        return """
                <a href="https://store.steampowered.com/app/%d" target="_blank" rel="noopener noreferrer">
                  %s
                </a>
                """.formatted(game.appId(), content);
    }

    private String statusBadge(SteamCardData data, SvgTheme.Palette palette, int x, int y, int width) {
        return statusBadge(data, x, y, width);
    }

    private String statusBadge(SteamCardData data, int x, int y, int width) {
        boolean playing = isPlaying(data);
        boolean online = isOnline(data);
        String fill = playing ? STEAM_IN_GAME_GREEN : online ? STEAM_ONLINE_BLUE : offlineColor();
        String label = playing ? "In-game" : online ? "ONLINE" : "OFFLINE";
        return """
                <g>
                  <rect x="%d" y="%d" width="%d" height="22" rx="11" fill="%s"/>
                  <circle cx="%d" cy="%d" r="4" fill="#ffffff" opacity="0.9"/>
                  <text x="%d" y="%d" text-anchor="middle" font-size="10" font-weight="700" fill="#ffffff">%s</text>
                </g>
                """.formatted(x, y, width, fill, x + 14, y + 11, x + width / 2 + 7, y + 15, label);
    }

    private String countryText(SteamCardData data) {
        return data.profile().countryCode() == null ? "" : text(data.profile().countryCode(), 8);
    }

    private String countryBadge(SteamCardData data, SvgTheme.Palette palette, int x, int y) {
        String countryCode = data.profile().countryCode();
        if (countryCode == null || countryCode.isBlank()) {
            return "";
        }
        String flag = CountryFlagRenderer.render(countryCode, palette, x, y);
        if (!flag.isBlank()) {
            return flag;
        }
        return "<text x=\"%d\" y=\"%d\" font-size=\"13\" fill=\"%s\">%s</text>"
                .formatted(x, y + 14, palette.secondaryText(), text(countryCode, 8));
    }

    private String countryBadgeAfterName(SteamCardData data, SvgTheme.Palette palette, int textX, int y, int fontSize, int maxCodePoints) {
        String displayName = SvgTextUtils.truncate(SvgTextUtils.text(data.profile().nickname(), "Unknown"), maxCodePoints);
        int x = Math.min(646, textX + estimatedTextWidth(displayName, fontSize) + 14);
        return countryBadge(data, palette, x, y);
    }

    private String gameMeta(SteamGame game) {
        if (game == null) {
            return "No Steam game data available";
        }
        String release = game.releaseDate() == null ? "" : " · " + game.releaseDate();
        String last = game.lastPlayedAt() == null ? "" : " · Last session " + date(game.lastPlayedAt());
        return "%s%s%s".formatted(hours(game), release, last);
    }

    private String showcaseMeta(SteamCardData data) {
        return libraryRecentHours(data.statistics());
    }

    private String libraryRecentHours(SteamStatistics statistics) {
        if (statistics == null || statistics.recentPlaytimeMinutes() == null) {
            return "";
        }
        return "Total playtime in the last 2 weeks %.1f h".formatted(minutesToHours(statistics.recentPlaytimeMinutes()));
    }

    private String lastSession(SteamCardData data) {
        SteamGame game = data.selectedGame();
        if (isOnline(data)) {
            return "";
        }
        Instant lastSessionAt = game != null && game.lastPlayedAt() != null
                ? game.lastPlayedAt()
                : data.profile().lastOnlineAt();
        return lastSessionAt == null ? "" : "Last session " + date(lastSessionAt);
    }

    private String lastSessionUnderStatus(SteamCardData data, SvgTheme.Palette palette, int x, int y) {
        String lastSession = lastSession(data);
        if (lastSession.isBlank()) {
            return "";
        }
        return "<text x=\"%d\" y=\"%d\" font-size=\"10\" fill=\"%s\">%s</text>"
                .formatted(x, y, palette.mutedText(), SvgTextUtils.escape(lastSession));
    }

    private String releaseDate(SteamGame game) {
        return game == null || game.releaseDate() == null ? "" : text(game.releaseDate(), 24);
    }

    private String hours(SteamGame game) {
        if (game == null) {
            return "No playtime data";
        }
        return "Total %.1f h · 2 weeks %.1f h".formatted(game.hoursForever(), game.hoursLastTwoWeeks());
    }

    private String footer(int width, int y, SvgTheme.Palette palette) {
        return """
                <text x="26" y="%d" font-size="11" fill="%s">Steam Card API</text>
                %s
                """.formatted(y, palette.mutedText(), footerLink(width, y, palette));
    }

    private String showcaseFooter(int width, int y, SvgTheme.Palette palette) {
        return footerLink(width, y, palette);
    }

    private String footerLink(int width, int y, SvgTheme.Palette palette) {
        return """
                <a href="https://alextc.es" target="_blank" rel="noopener noreferrer">
                  <text x="%d" y="%d" text-anchor="end" font-size="11" fill="%s">alextc.es</text>
                </a>
                """.formatted(width - 26, y, palette.mutedText());
    }

    private boolean isOnline(SteamCardData data) {
        return isPlaying(data)
                || "Online".equals(data.profile().status());
    }

    private boolean isPlaying(SteamCardData data) {
        return data.profile().currentlyPlaying()
                || (data.selectedGame() != null && data.selectedGame().currentlyPlaying());
    }

    private String offlineColor() {
        return "#f85149";
    }

    private String text(String value, int maxCodePoints) {
        return SvgTextUtils.escape(SvgTextUtils.truncate(SvgTextUtils.text(value, "Unknown"), maxCodePoints));
    }

    private String number(Integer value) {
        return value == null ? "?" : String.valueOf(value);
    }

    private String date(Instant instant) {
        return DATE_FORMATTER.format(instant);
    }

    private double minutesToHours(Integer minutes) {
        if (minutes == null) {
            return 0.0;
        }
        return Math.round((minutes / 60.0) * 10.0) / 10.0;
    }

    private int estimatedTextWidth(String value, int fontSize) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        double units = value.codePoints()
                .mapToDouble(this::characterWidth)
                .sum();
        return (int) Math.round(units * fontSize);
    }

    private double characterWidth(int codePoint) {
        if (Character.isWhitespace(codePoint)) {
            return 0.32;
        }
        if (".,;:!'|iIl1".indexOf(codePoint) >= 0) {
            return 0.28;
        }
        if ("mwMW@#%&".indexOf(codePoint) >= 0) {
            return 0.82;
        }
        return 0.56;
    }

    private String id(SteamCardData data) {
        return id(data.profile().steamId(), data.layout().value(), data.theme().value(), data.selectedGame() == null ? "" : data.selectedGame().appId());
    }

    private String id(Object... values) {
        return "steam-card-" + Integer.toHexString(Objects.hash(values)).replace("-", "x");
    }
}

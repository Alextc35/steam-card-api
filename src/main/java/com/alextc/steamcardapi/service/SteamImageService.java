package com.alextc.steamcardapi.service;

import com.alextc.steamcardapi.client.SteamCdnClient;
import com.alextc.steamcardapi.dto.store.SteamStoreAppDetailsResponse;
import com.alextc.steamcardapi.exception.SteamImageUnavailableException;
import com.alextc.steamcardapi.model.CardImageMode;
import com.alextc.steamcardapi.model.DownloadedImage;
import com.alextc.steamcardapi.model.GameImageType;
import com.alextc.steamcardapi.model.RenderedHttpResource;
import com.alextc.steamcardapi.model.SteamGameImages;
import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTextUtils;
import com.alextc.steamcardapi.svg.SvgTheme;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.alextc.steamcardapi.config.CacheConfig.STEAM_EMBEDDED_IMAGE_CACHE;
import static com.alextc.steamcardapi.config.CacheConfig.STEAM_IMAGE_RESOLUTION_CACHE;

@Service
public class SteamImageService {

    private static final String ASSET_BASE = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/%d/%s";
    private static final String ICON_BASE = "https://media.steampowered.com/steamcommunity/public/images/apps/%d/%s.jpg";

    private final SteamCdnClient steamCdnClient;
    private final CacheManager cacheManager;
    private final SteamCardService steamCardService;

    public SteamImageService(SteamCdnClient steamCdnClient, CacheManager cacheManager, SteamCardService steamCardService) {
        this.steamCdnClient = steamCdnClient;
        this.cacheManager = cacheManager;
        this.steamCardService = steamCardService;
    }

    public SteamGameImages resolveImages(
            Integer appId,
            String iconHash,
            Optional<SteamStoreAppDetailsResponse.AppData> storeData,
            SvgLayout layout,
            GameImageType requestedImage
    ) {
        if (appId == null) {
            return new SteamGameImages(null, null, null, null, null, null, null, null);
        }
        String cacheKey = "%d:%s:%s:%s".formatted(appId, iconHash == null ? "" : iconHash,
                layout.value(), requestedImage.value());
        Cache cache = cacheManager.getCache(STEAM_IMAGE_RESOLUTION_CACHE);
        SteamGameImages cached = cache == null ? null : cache.get(cacheKey, SteamGameImages.class);
        if (cached != null) {
            return cached;
        }

        SteamStoreAppDetailsResponse.AppData data = storeData.orElse(null);
        String iconUrl = StringUtils.hasText(iconHash) ? ICON_BASE.formatted(appId, iconHash) : null;
        String headerUrl = firstNonBlank(data == null ? null : data.headerImage(), asset(appId, "header.jpg"));
        String portraitUrl = asset(appId, "library_600x900.jpg");
        String heroUrl = asset(appId, "library_hero.jpg");
        String logoUrl = asset(appId, "logo.png");
        String smallUrl = firstNonBlank(data == null ? null : data.capsuleImage(), asset(appId, "capsule_184x69.jpg"));
        String backgroundUrl = firstNonBlank(
                data == null ? null : data.backgroundRaw(),
                data == null ? null : data.background(),
                firstScreenshot(data));
        String primary = primaryImage(layout, requestedImage, iconUrl, headerUrl, portraitUrl, heroUrl, logoUrl, smallUrl);

        SteamGameImages resolved = new SteamGameImages(iconUrl, headerUrl, portraitUrl, heroUrl, logoUrl,
                smallUrl, backgroundUrl, primary);
        if (cache != null) {
            cache.put(cacheKey, resolved);
        }
        return resolved;
    }

    public String renderableImageUrl(String url, String label, Integer appId, SvgTheme theme, CardImageMode imageMode) {
        if (imageMode == CardImageMode.EXTERNAL) {
            return StringUtils.hasText(url) ? url : placeholderDataUri(label, appId, theme);
        }
        if (!StringUtils.hasText(url)) {
            return placeholderDataUri(label, appId, theme);
        }
        try {
            return embeddedDataUri(url);
        } catch (SteamImageUnavailableException exception) {
            return placeholderDataUri(label, appId, theme);
        }
    }

    public String renderableGameImageUrl(
            SteamGameImages images,
            GameImageType imageType,
            String label,
            Integer appId,
            SvgTheme theme,
            CardImageMode imageMode
    ) {
        List<String> candidates = gameImageCandidates(images, imageType);
        if (imageMode == CardImageMode.EXTERNAL) {
            return candidates.stream()
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElseGet(() -> placeholderDataUri(label, appId, theme));
        }
        for (String candidate : candidates) {
            try {
                return embeddedDataUri(candidate);
            } catch (SteamImageUnavailableException ignored) {
                // Try the next deterministic fallback before using a local placeholder.
            }
        }
        return placeholderDataUri(label, appId, theme);
    }

    private String embeddedDataUri(String url) {
        Cache cache = cacheManager.getCache(STEAM_EMBEDDED_IMAGE_CACHE);
        String key = url;
        String cached = cache == null ? null : cache.get(key, String.class);
        if (cached != null) {
            return cached;
        }
        DownloadedImage image = steamCdnClient.download(url);
        String dataUri = "data:%s;base64,%s".formatted(
                image.contentType(),
                Base64.getEncoder().encodeToString(image.bytes()));
        if (cache != null) {
            cache.put(key, dataUri);
        }
        return dataUri;
    }

    public RenderedHttpResource coverResource(
            int appId,
            GameImageType type,
            Optional<SteamStoreAppDetailsResponse.AppData> storeData,
            SvgTheme theme
    ) {
        SteamGameImages images = resolveImages(appId, null, storeData, SvgLayout.SHOWCASE, type);
        List<String> candidates = coverCandidates(type, images);
        for (String candidate : candidates) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            try {
                DownloadedImage downloaded = steamCdnClient.download(candidate);
                if (downloaded == null) {
                    continue;
                }
                return steamCardService.resource(downloaded.bytes(), downloaded.contentType());
            } catch (SteamImageUnavailableException ignored) {
                // Try the next deterministic fallback.
            }
        }
        byte[] placeholder = placeholderSvg("Steam App " + appId, appId, theme).getBytes(StandardCharsets.UTF_8);
        return steamCardService.resource(placeholder, "image/svg+xml;charset=UTF-8");
    }

    private List<String> coverCandidates(GameImageType type, SteamGameImages images) {
        return switch (type) {
            case PORTRAIT -> List.of(
                    images.portraitCoverUrl(),
                    images.headerUrl(),
                    images.heroUrl(),
                    nullToBlank(images.backgroundUrl()));
            case HERO -> List.of(images.heroUrl(), nullToBlank(images.backgroundUrl()), images.headerUrl());
            case HEADER -> List.of(images.headerUrl(), images.smallCapsuleUrl());
            case ICON -> List.of(nullToBlank(images.iconUrl()), images.logoUrl(), images.smallCapsuleUrl());
            case LOGO -> List.of(images.logoUrl(), images.smallCapsuleUrl(), nullToBlank(images.iconUrl()));
            case SMALL -> List.of(images.smallCapsuleUrl(), images.headerUrl(), nullToBlank(images.iconUrl()));
            default -> List.of();
        };
    }

    private List<String> gameImageCandidates(SteamGameImages images, GameImageType type) {
        if (images == null || type == GameImageType.NONE) {
            return List.of();
        }
        if (type == GameImageType.AUTO) {
            return candidates(
                    images.primaryImageUrl(),
                    images.portraitCoverUrl(),
                    images.heroUrl(),
                    images.headerUrl(),
                    images.smallCapsuleUrl(),
                    images.iconUrl(),
                    images.logoUrl(),
                    images.backgroundUrl());
        }
        return candidates(images.primaryImageUrl(), coverCandidates(type, images).toArray(String[]::new));
    }

    private List<String> candidates(String first, String... rest) {
        Set<String> unique = new LinkedHashSet<>();
        if (StringUtils.hasText(first)) {
            unique.add(first);
        }
        for (String value : rest) {
            if (StringUtils.hasText(value)) {
                unique.add(value);
            }
        }
        return new ArrayList<>(unique);
    }

    private String primaryImage(
            SvgLayout layout,
            GameImageType requested,
            String icon,
            String header,
            String portrait,
            String hero,
            String logo,
            String small
    ) {
        GameImageType effective = requested == GameImageType.AUTO ? switch (layout) {
            case COMPACT -> GameImageType.SMALL;
            case NORMAL -> GameImageType.HEADER;
            case SHOWCASE -> GameImageType.PORTRAIT;
            case HERO -> GameImageType.HERO;
            case MINIMAL -> GameImageType.NONE;
        } : requested;
        return switch (effective) {
            case NONE -> null;
            case ICON -> firstNonBlank(icon, logo, small);
            case HEADER -> firstNonBlank(header, small, icon);
            case PORTRAIT -> firstNonBlank(portrait, header, small, icon);
            case HERO -> firstNonBlank(hero, header, small);
            case LOGO -> firstNonBlank(logo, small, icon);
            case SMALL -> firstNonBlank(small, icon, header);
            case AUTO -> header;
        };
    }

    public String placeholderDataUri(String label, Integer appId, SvgTheme theme) {
        String svg = placeholderSvg(label, appId, theme);
        return "data:image/svg+xml;base64," + Base64.getEncoder()
                .encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    private String placeholderSvg(String label, Integer appId, SvgTheme theme) {
        SvgTheme.Palette palette = theme.palette(null);
        String safeLabel = SvgTextUtils.escape(SvgTextUtils.truncate(label == null ? "Steam game" : label, 28));
        String safeAppId = appId == null ? "unknown" : String.valueOf(appId);
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="600" height="900" viewBox="0 0 600 900" role="img">
                  <rect width="600" height="900" fill="%s"/>
                  <rect x="36" y="36" width="528" height="828" rx="36" fill="%s" stroke="%s" stroke-width="6"/>
                  <circle cx="300" cy="330" r="94" fill="%s"/>
                  <rect x="230" y="305" width="140" height="56" rx="28" fill="%s"/>
                  <circle cx="260" cy="333" r="13" fill="%s"/>
                  <circle cx="340" cy="333" r="13" fill="%s"/>
                  <text x="300" y="540" text-anchor="middle" font-family="Arial, sans-serif" font-size="42" font-weight="700" fill="%s">%s</text>
                  <text x="300" y="598" text-anchor="middle" font-family="Arial, sans-serif" font-size="25" fill="%s">AppID %s</text>
                  <text x="300" y="690" text-anchor="middle" font-family="Arial, sans-serif" font-size="28" fill="%s">Artwork unavailable</text>
                </svg>
                """.formatted(
                palette.background(),
                palette.panel(),
                palette.border(),
                palette.border(),
                palette.accent(),
                palette.background(),
                palette.background(),
                palette.primaryText(),
                safeLabel,
                palette.secondaryText(),
                safeAppId,
                palette.mutedText());
    }

    private String asset(int appId, String fileName) {
        return ASSET_BASE.formatted(appId, fileName);
    }

    private String firstScreenshot(SteamStoreAppDetailsResponse.AppData data) {
        if (data == null || data.screenshots() == null) {
            return null;
        }
        return data.screenshots().stream()
                .filter(screenshot -> screenshot != null && StringUtils.hasText(screenshot.pathFull()))
                .map(SteamStoreAppDetailsResponse.Screenshot::pathFull)
                .findFirst()
                .orElse(null);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

}

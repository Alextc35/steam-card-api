package com.alextc.steamcardapi.validation;

import com.alextc.steamcardapi.config.SteamProperties;
import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import com.alextc.steamcardapi.model.BorderStyle;
import com.alextc.steamcardapi.model.CardImageMode;
import com.alextc.steamcardapi.model.GameImageType;
import com.alextc.steamcardapi.model.ShowSection;
import com.alextc.steamcardapi.model.SteamCardRequest;
import com.alextc.steamcardapi.model.SteamSubject;
import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTheme;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SteamRequestValidator {

    private static final Pattern STEAM_ID = Pattern.compile("\\d{15,20}");
    private static final Pattern VANITY = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("es", "en", "fr", "de");

    private final SteamProperties steamProperties;

    public SteamRequestValidator(SteamProperties steamProperties) {
        this.steamProperties = steamProperties;
    }

    public SteamSubject resolveSubject(String steamId, String vanity) {
        if (StringUtils.hasText(steamId) && StringUtils.hasText(vanity)) {
            throw new InvalidCardParameterException("Use exactly one of steamId or vanity");
        }

        String resolvedSteamId = blankToNull(steamId);
        String resolvedVanity = blankToNull(vanity);

        if (resolvedSteamId == null && resolvedVanity == null) {
            resolvedSteamId = blankToNull(steamProperties.defaultId());
            resolvedVanity = blankToNull(steamProperties.defaultVanity());
        }

        if (resolvedSteamId != null && resolvedVanity != null) {
            throw new InvalidCardParameterException("Default configuration must use either STEAM_DEFAULT_ID or STEAM_DEFAULT_VANITY");
        }
        if (resolvedSteamId == null && resolvedVanity == null) {
            throw new InvalidCardParameterException("steamId or vanity is required when no default Steam profile is configured");
        }
        if (resolvedSteamId != null && !STEAM_ID.matcher(resolvedSteamId).matches()) {
            throw new InvalidCardParameterException("steamId must contain 15 to 20 digits");
        }
        if (resolvedVanity != null && !VANITY.matcher(resolvedVanity).matches()) {
            throw new InvalidCardParameterException("vanity may only contain letters, digits, underscores and hyphens");
        }
        return new SteamSubject(resolvedSteamId, resolvedVanity);
    }

    public SteamCardRequest cardRequest(
            String steamId,
            String vanity,
            String theme,
            String layout,
            String lang,
            String accent,
            String show,
            String imageMode,
            String gameImage,
            Boolean animation,
            String border
    ) {
        return new SteamCardRequest(
                resolveSubject(steamId, vanity),
                SvgTheme.parse(limit(theme, "theme", 24)),
                SvgLayout.parse(limit(layout, "layout", 24)),
                language(lang),
                HexColorValidator.validate(limit(accent, "accent", 6)),
                ShowSection.parse(limit(show, "show", 240)),
                CardImageMode.parse(limit(imageMode, "imageMode", 16)),
                GameImageType.parse(limit(gameImage, "gameImage", 16)),
                animation != null && animation,
                BorderStyle.parse(limit(border, "border", 16)));
    }

    public String language(String lang) {
        String normalized = lang == null || lang.isBlank() ? "en" : lang.toLowerCase(Locale.ROOT);
        if (normalized.length() > 2 || !SUPPORTED_LANGUAGES.contains(normalized)) {
            throw new InvalidCardParameterException("lang must be one of es, en, fr, de");
        }
        return normalized;
    }

    public int appId(String appId) {
        try {
            int parsed = Integer.parseInt(appId);
            if (parsed <= 0) {
                throw new InvalidCardParameterException("appId must be a positive integer");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new InvalidCardParameterException("appId must be a positive integer");
        }
    }

    public GameImageType coverType(String type) {
        GameImageType parsed = GameImageType.parse(type);
        if (parsed == GameImageType.AUTO || parsed == GameImageType.NONE) {
            throw new InvalidCardParameterException("type must be one of portrait, header, hero, icon, logo, small");
        }
        return parsed;
    }

    private String limit(String value, String name, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new InvalidCardParameterException(name + " is too long");
        }
        return value;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}

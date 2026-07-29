package com.alextc.steamcardapi.svg;

import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import java.util.Arrays;

public enum SvgTheme {
    DARK("dark", "#171a21", "#24384a", "#f2f6fb", "#c7d5e0", "#8f98a0", "#66c0f4", "#8bc34a", "#737b83", "#f2b84b", "#10141b"),
    LIGHT("light", "#f5f7fa", "#b8c4d4", "#17202a", "#34495e", "#607487", "#2477a8", "#2fa866", "#8b97a3", "#d99028", "#e9eef5"),
    STEAM("steam", "#0e141b", "#1b405c", "#ffffff", "#c7d5e0", "#8f98a0", "#66c0f4", "#90ba3c", "#66707a", "#f5b342", "#15212e"),
    DRACULA("dracula", "#282a36", "#44475a", "#f8f8f2", "#d6d6e7", "#bd93f9", "#8be9fd", "#50fa7b", "#6272a4", "#ffb86c", "#1f2029"),
    NORD("nord", "#2e3440", "#4c566a", "#eceff4", "#d8dee9", "#a3acbd", "#88c0d0", "#a3be8c", "#718096", "#ebcb8b", "#242933"),
    GITHUB_DARK("github-dark", "#0d1117", "#30363d", "#f0f6fc", "#c9d1d9", "#8b949e", "#58a6ff", "#3fb950", "#6e7681", "#d29922", "#161b22"),
    GITHUB_LIGHT("github-light", "#ffffff", "#d0d7de", "#24292f", "#57606a", "#6e7781", "#0969da", "#1a7f37", "#8c959f", "#bf8700", "#f6f8fa");

    private final String value;
    private final Palette palette;

    SvgTheme(
            String value,
            String background,
            String border,
            String primaryText,
            String secondaryText,
            String mutedText,
            String accent,
            String online,
            String offline,
            String away,
            String panel
    ) {
        this.value = value;
        this.palette = new Palette(background, border, primaryText, secondaryText, mutedText, accent,
                online, offline, away, panel);
    }

    public String value() {
        return value;
    }

    public Palette palette(String accentOverride) {
        if (accentOverride == null || accentOverride.isBlank()) {
            return palette;
        }
        return new Palette(palette.background(), palette.border(), palette.primaryText(), palette.secondaryText(),
                palette.mutedText(), "#" + accentOverride, palette.online(), palette.offline(), palette.away(),
                palette.panel());
    }

    public static SvgTheme parse(String value) {
        String normalized = value == null || value.isBlank() ? "github-dark" : value;
        return Arrays.stream(values())
                .filter(theme -> theme.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new InvalidCardParameterException(
                        "theme must be one of dark, light, steam, dracula, nord, github-dark, github-light"));
    }

    public record Palette(
            String background,
            String border,
            String primaryText,
            String secondaryText,
            String mutedText,
            String accent,
            String online,
            String offline,
            String away,
            String panel
    ) {
    }
}

package com.alextc.steamcardapi.svg;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record SvgLabels(
        String datePattern,
        Locale dateLocale,
        String profileCardTitle,
        String noGameSelected,
        String lastPlayed,
        String currentlyPlaying,
        String level,
        String library,
        String friends,
        String noPlaytimeData,
        String total,
        String twoWeeks,
        String totalPlaytimeLastTwoWeeks,
        String lastSession,
        String noGameFound,
        String coverNotFound,
        String noPublicRecentGameData,
        String noGameAvailable,
        String statusInGame,
        String statusOnline,
        String statusOffline,
        String steamGameCardTitle,
        String steamGame,
        String steamGameMetadata,
        String freeToPlay,
        String priceUnavailable,
        String releaseUnavailable,
        String hoursPlayed
) {

    public static SvgLabels forLocale(String locale) {
        return switch (normalize(locale)) {
            case "es" -> new SvgLabels(
                    "d MMM, yyyy",
                    Locale.forLanguageTag("es"),
                    "Tarjeta de perfil de Steam de %s",
                    "sin juego seleccionado",
                    "Último juego",
                    "Jugando ahora",
                    "Nivel",
                    "Biblioteca",
                    "Amigos",
                    "Sin datos de juego",
                    "Total",
                    "2 semanas",
                    "Tiempo total jugado en las últimas 2 semanas %.1f h",
                    "Última sesión",
                    "Sin juego",
                    "Sin portada",
                    "Sin datos públicos recientes",
                    "No hay juego disponible",
                    "EN JUEGO",
                    "EN LÍNEA",
                    "DESCONECTADO",
                    "Tarjeta de juego de Steam de %s",
                    "Juego de Steam",
                    "Metadatos del juego",
                    "Gratis",
                    "Precio no disponible",
                    "Fecha no disponible",
                    "%.1f h jugadas");
            case "fr" -> new SvgLabels(
                    "d MMM, yyyy",
                    Locale.forLanguageTag("fr"),
                    "Carte de profil Steam de %s",
                    "aucun jeu sélectionné",
                    "Dernier jeu",
                    "En train de jouer",
                    "Niveau",
                    "Bibliothèque",
                    "Amis",
                    "Aucune donnée de jeu",
                    "Total",
                    "2 semaines",
                    "Temps total joué sur les 2 dernières semaines %.1f h",
                    "Dernière session",
                    "Aucun jeu",
                    "Image introuvable",
                    "Aucune donnée récente publique",
                    "Aucun jeu disponible",
                    "EN JEU",
                    "EN LIGNE",
                    "HORS LIGNE",
                    "Carte de jeu Steam pour %s",
                    "Jeu Steam",
                    "Métadonnées du jeu",
                    "Gratuit",
                    "Prix indisponible",
                    "Date indisponible",
                    "%.1f h jouées");
            case "de" -> new SvgLabels(
                    "d MMM, yyyy",
                    Locale.forLanguageTag("de"),
                    "Steam-Profilkarte für %s",
                    "kein Spiel ausgewählt",
                    "Zuletzt gespielt",
                    "Wird gespielt",
                    "Level",
                    "Bibliothek",
                    "Freunde",
                    "Keine Spielzeitdaten",
                    "Gesamt",
                    "2 Wochen",
                    "Gesamte Spielzeit in den letzten 2 Wochen %.1f h",
                    "Letzte Sitzung",
                    "Kein Spiel",
                    "Cover nicht gefunden",
                    "Keine öffentlichen aktuellen Spieldaten",
                    "Kein Spiel verfügbar",
                    "IM SPIEL",
                    "ONLINE",
                    "OFFLINE",
                    "Steam-Spielkarte für %s",
                    "Steam-Spiel",
                    "Spielmetadaten",
                    "Kostenlos",
                    "Preis nicht verfügbar",
                    "Datum nicht verfügbar",
                    "%.1f h gespielt");
            default -> new SvgLabels(
                    "MMM d, yyyy",
                    Locale.ENGLISH,
                    "Steam profile card for %s",
                    "no game selected",
                    "Last played",
                    "Currently playing",
                    "Level",
                    "Library",
                    "Friends",
                    "No playtime data",
                    "Total",
                    "2 weeks",
                    "Total playtime in the last 2 weeks %.1f h",
                    "Last session",
                    "No game found",
                    "Cover not found",
                    "No public recent game data",
                    "No game available",
                    "IN-GAME",
                    "ONLINE",
                    "OFFLINE",
                    "Steam game card for %s",
                    "Steam game",
                    "Steam game metadata",
                    "Free to play",
                    "Price unavailable",
                    "Release unavailable",
                    "%.1f h played");
        };
    }

    public String profileCardTitle(String nickname) {
        return profileCardTitle.formatted(nickname);
    }

    public String steamGameCardTitle(String gameName) {
        return steamGameCardTitle.formatted(gameName);
    }

    public String status(boolean playing, boolean online) {
        if (playing) {
            return statusInGame;
        }
        return online ? statusOnline : statusOffline;
    }

    public String gameHours(double totalHours, double recentHours) {
        return "%s %.1f h · %s %.1f h".formatted(total, totalHours, twoWeeks, recentHours);
    }

    public String totalPlaytimeLastTwoWeeks(double hours) {
        return totalPlaytimeLastTwoWeeks.formatted(hours);
    }

    public String lastSession(String date) {
        return "%s %s".formatted(lastSession, date);
    }

    public String hoursPlayed(double hours) {
        return hoursPlayed.formatted(hours);
    }

    public String date(Instant instant) {
        return DateTimeFormatter.ofPattern(datePattern, dateLocale)
                .withZone(ZoneOffset.UTC)
                .format(instant);
    }

    private static String normalize(String locale) {
        if (locale == null || locale.isBlank()) {
            return "en";
        }
        return locale.toLowerCase(Locale.ROOT);
    }
}

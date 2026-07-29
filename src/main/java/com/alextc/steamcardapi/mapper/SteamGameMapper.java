package com.alextc.steamcardapi.mapper;

import com.alextc.steamcardapi.dto.steam.OwnedGamesResponse;
import com.alextc.steamcardapi.dto.steam.PlayerSummariesResponse;
import com.alextc.steamcardapi.dto.steam.RecentlyPlayedGamesResponse;
import com.alextc.steamcardapi.dto.store.SteamStoreAppDetailsResponse;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.model.SteamGameImages;
import com.alextc.steamcardapi.model.StorePlatforms;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SteamGameMapper {

    public SteamGame fromActivePlayer(
            PlayerSummariesResponse.Player player,
            Optional<SteamStoreAppDetailsResponse.AppData> storeData,
            SteamGameImages images
    ) {
        return fromActivePlayer(player, null, null, storeData, images);
    }

    public SteamGame fromActivePlayer(
            PlayerSummariesResponse.Player player,
            OwnedGamesResponse.Game ownedGame,
            RecentlyPlayedGamesResponse.Game recentGame,
            Optional<SteamStoreAppDetailsResponse.AppData> storeData,
            SteamGameImages images
    ) {
        Integer appId = parseInteger(player.gameId());
        return withStoreData(
                appId,
                player.gameExtraInfo(),
                firstNonNull(
                        ownedGame == null ? null : ownedGame.playtimeTwoWeeks(),
                        recentGame == null ? null : recentGame.playtimeTwoWeeks()),
                firstNonNull(
                        ownedGame == null ? null : ownedGame.playtimeForever(),
                        recentGame == null ? null : recentGame.playtimeForever()),
                toInstant(firstNonNull(
                        ownedGame == null ? null : ownedGame.lastPlayed(),
                        recentGame == null ? null : recentGame.lastPlayed())),
                true,
                firstNonBlank(
                        ownedGame == null ? null : ownedGame.imageIconUrl(),
                        recentGame == null ? null : recentGame.imageIconUrl()),
                storeData,
                images);
    }

    public SteamGame fromRecentGame(
            RecentlyPlayedGamesResponse.Game game,
            boolean currentlyPlaying,
            Optional<SteamStoreAppDetailsResponse.AppData> storeData,
            SteamGameImages images
    ) {
        return withStoreData(game.appId(), game.name(), game.playtimeTwoWeeks(), game.playtimeForever(),
                toInstant(game.lastPlayed()), currentlyPlaying, game.imageIconUrl(), storeData, images);
    }

    public SteamGame fromOwnedGame(
            OwnedGamesResponse.Game game,
            Optional<SteamStoreAppDetailsResponse.AppData> storeData,
            SteamGameImages images
    ) {
        return withStoreData(game.appId(), game.name(), game.playtimeTwoWeeks(), game.playtimeForever(),
                toInstant(game.lastPlayed()), false, game.imageIconUrl(), storeData, images);
    }

    public SteamGame fromAppDetails(
            int appId,
            Optional<SteamStoreAppDetailsResponse.AppData> storeData,
            SteamGameImages images
    ) {
        String fallbackName = "Steam App " + appId;
        return withStoreData(appId, fallbackName, null, null, null, false, null, storeData, images);
    }

    private SteamGame withStoreData(
            Integer appId,
            String fallbackName,
            Integer playtimeTwoWeeks,
            Integer playtimeForever,
            Instant lastPlayedAt,
            boolean currentlyPlaying,
            String iconHash,
            Optional<SteamStoreAppDetailsResponse.AppData> storeData,
            SteamGameImages images
    ) {
        SteamStoreAppDetailsResponse.AppData data = storeData.orElse(null);
        String name = data != null && data.name() != null && !data.name().isBlank() ? data.name() : fallbackName;
        return new SteamGame(
                appId,
                name,
                playtimeTwoWeeks,
                playtimeForever,
                lastPlayedAt,
                currentlyPlaying,
                iconHash,
                images,
                data == null ? List.of() : data.genreDescriptions(),
                safeList(data == null ? null : data.developers()),
                safeList(data == null ? null : data.publishers()),
                data == null ? List.of() : data.categoryDescriptions(),
                data == null || data.releaseDate() == null ? null : data.releaseDate().date(),
                data == null ? null : data.shortDescription(),
                data == null ? null : data.type(),
                data == null || data.priceOverview() == null ? null : data.priceOverview().finalFormatted(),
                data != null && Boolean.TRUE.equals(data.freeToPlay()),
                data == null || data.metacritic() == null ? null : data.metacritic().score(),
                data == null || data.recommendations() == null ? null : data.recommendations().total(),
                platforms(data == null ? null : data.platforms()));
    }

    private StorePlatforms platforms(SteamStoreAppDetailsResponse.Platforms platforms) {
        if (platforms == null) {
            return new StorePlatforms(false, false, false);
        }
        return new StorePlatforms(Boolean.TRUE.equals(platforms.windows()),
                Boolean.TRUE.equals(platforms.mac()),
                Boolean.TRUE.equals(platforms.linux()));
    }

    private List<String> safeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private Instant toInstant(Long epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return null;
        }
        return Instant.ofEpochSecond(epochSeconds);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.valueOf(value);
    }

    private <T> T firstNonNull(T first, T second) {
        return first == null ? second : first;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}

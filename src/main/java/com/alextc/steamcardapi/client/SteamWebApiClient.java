package com.alextc.steamcardapi.client;

import com.alextc.steamcardapi.dto.steam.FriendListResponse;
import com.alextc.steamcardapi.dto.steam.OwnedGamesResponse;
import com.alextc.steamcardapi.dto.steam.PlayerAchievementsResponse;
import com.alextc.steamcardapi.dto.steam.PlayerSummariesResponse;
import com.alextc.steamcardapi.dto.steam.RecentlyPlayedGamesResponse;
import com.alextc.steamcardapi.dto.steam.ResolveVanityResponse;
import com.alextc.steamcardapi.dto.steam.SteamLevelResponse;
import com.alextc.steamcardapi.exception.SteamApiException;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static com.alextc.steamcardapi.config.CacheConfig.STEAM_FRIENDS_CACHE;
import static com.alextc.steamcardapi.config.CacheConfig.STEAM_LEVEL_CACHE;
import static com.alextc.steamcardapi.config.CacheConfig.STEAM_LIBRARY_CACHE;
import static com.alextc.steamcardapi.config.CacheConfig.STEAM_PROFILE_CACHE;
import static com.alextc.steamcardapi.config.CacheConfig.STEAM_RECENT_GAMES_CACHE;

@Component
public class SteamWebApiClient {

    private final RestClient restClient;

    public SteamWebApiClient(@Qualifier("steamWebRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ResolveVanityResponse resolveVanityUrl(String apiKey, String vanityUrl) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ISteamUser/ResolveVanityURL/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("vanityurl", vanityUrl)
                        .build())
                .retrieve()
                .body(ResolveVanityResponse.class));
    }

    @Cacheable(cacheNames = STEAM_PROFILE_CACHE, key = "#steamId", unless = "#result == null")
    public PlayerSummariesResponse getPlayerSummaries(String apiKey, String steamId) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ISteamUser/GetPlayerSummaries/v2/")
                        .queryParam("key", apiKey)
                        .queryParam("steamids", steamId)
                        .build())
                .retrieve()
                .body(PlayerSummariesResponse.class));
    }

    @Cacheable(cacheNames = STEAM_RECENT_GAMES_CACHE, key = "#steamId", unless = "#result == null")
    public RecentlyPlayedGamesResponse getRecentlyPlayedGames(String apiKey, String steamId, int count) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/IPlayerService/GetRecentlyPlayedGames/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steamId)
                        .queryParam("count", count)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(RecentlyPlayedGamesResponse.class));
    }

    @Cacheable(cacheNames = STEAM_LIBRARY_CACHE, key = "#steamId", unless = "#result == null")
    public OwnedGamesResponse getOwnedGames(String apiKey, String steamId) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/IPlayerService/GetOwnedGames/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steamId)
                        .queryParam("include_appinfo", true)
                        .queryParam("include_played_free_games", true)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(OwnedGamesResponse.class));
    }

    @Cacheable(cacheNames = STEAM_LEVEL_CACHE, key = "#steamId", unless = "#result == null")
    public SteamLevelResponse getSteamLevel(String apiKey, String steamId) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/IPlayerService/GetSteamLevel/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steamId)
                        .build())
                .retrieve()
                .body(SteamLevelResponse.class));
    }

    @Cacheable(cacheNames = STEAM_FRIENDS_CACHE, key = "#steamId", unless = "#result == null")
    public FriendListResponse getFriendList(String apiKey, String steamId) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ISteamUser/GetFriendList/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steamId)
                        .queryParam("relationship", "friend")
                        .build())
                .retrieve()
                .body(FriendListResponse.class));
    }

    public PlayerAchievementsResponse getPlayerAchievements(String apiKey, String steamId, int appId) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ISteamUserStats/GetPlayerAchievements/v1/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steamId)
                        .queryParam("appid", appId)
                        .build())
                .retrieve()
                .body(PlayerAchievementsResponse.class));
    }

    private <T> T execute(Supplier<T> request) {
        try {
            T body = request.get();
            if (body == null) {
                throw new SteamApiException("Steam API returned an empty response");
            }
            return body;
        } catch (SteamApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new SteamApiException("Unable to communicate with Steam API", exception);
        }
    }
}

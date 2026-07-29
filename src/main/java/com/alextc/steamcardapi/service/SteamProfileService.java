package com.alextc.steamcardapi.service;

import com.alextc.steamcardapi.client.SteamWebApiClient;
import com.alextc.steamcardapi.config.SteamProperties;
import com.alextc.steamcardapi.dto.steam.FriendListResponse;
import com.alextc.steamcardapi.dto.steam.OwnedGamesResponse;
import com.alextc.steamcardapi.dto.steam.PlayerSummariesResponse;
import com.alextc.steamcardapi.dto.steam.RecentlyPlayedGamesResponse;
import com.alextc.steamcardapi.dto.steam.ResolveVanityResponse;
import com.alextc.steamcardapi.dto.steam.SteamLevelResponse;
import com.alextc.steamcardapi.exception.SteamApiException;
import com.alextc.steamcardapi.exception.SteamProfileNotFoundException;
import com.alextc.steamcardapi.mapper.SteamCardMapper;
import com.alextc.steamcardapi.mapper.SteamProfileMapper;
import com.alextc.steamcardapi.model.GameImageType;
import com.alextc.steamcardapi.model.SteamCardData;
import com.alextc.steamcardapi.model.SteamCardRequest;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.model.SteamGameImages;
import com.alextc.steamcardapi.model.SteamLibrarySummary;
import com.alextc.steamcardapi.model.SteamProfile;
import com.alextc.steamcardapi.model.SteamStatistics;
import com.alextc.steamcardapi.model.SteamSubject;
import java.util.Comparator;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.alextc.steamcardapi.config.CacheConfig.STEAM_SVG_CACHE;

@Service
public class SteamProfileService {

    private final SteamWebApiClient steamWebApiClient;
    private final SteamProperties steamProperties;
    private final SteamProfileMapper steamProfileMapper;
    private final SteamGameService steamGameService;
    private final SteamImageService steamImageService;
    private final SteamCardMapper steamCardMapper;

    public SteamProfileService(
            SteamWebApiClient steamWebApiClient,
            SteamProperties steamProperties,
            SteamProfileMapper steamProfileMapper,
            SteamGameService steamGameService,
            SteamImageService steamImageService,
            SteamCardMapper steamCardMapper
    ) {
        this.steamWebApiClient = steamWebApiClient;
        this.steamProperties = steamProperties;
        this.steamProfileMapper = steamProfileMapper;
        this.steamGameService = steamGameService;
        this.steamImageService = steamImageService;
        this.steamCardMapper = steamCardMapper;
    }

    @Cacheable(cacheNames = STEAM_SVG_CACHE, key = "#request.cacheKey()", unless = "#result == null")
    public SteamCardData getCardData(SteamCardRequest request) {
        String steamId = resolveSteamId(request.subject());
        PlayerSummariesResponse.Player player = getPlayer(steamId);
        List<RecentlyPlayedGamesResponse.Game> recentDtos = recentGames(steamId);
        List<OwnedGamesResponse.Game> ownedDtos = ownedGames(steamId);

        Integer steamLevel = steamLevel(steamId);
        Integer friendCount = friendCount(steamId);
        Integer gameCount = ownedDtos.isEmpty() ? null : ownedDtos.size();
        SteamProfile profile = steamProfileMapper.toProfile(player, steamLevel, friendCount, gameCount);

        SteamGame selectedGame = selectMainGame(player, recentDtos, ownedDtos, request);
        List<SteamGame> recentGames = recentDtos.stream()
                .limit(3)
                .map(game -> steamGameService.fromRecentGame(game, false, request.locale(), request.layout(), GameImageType.SMALL))
                .toList();
        SteamGame mostPlayed = ownedDtos.stream()
                .max(Comparator.comparing(game -> game.playtimeForever() == null ? 0 : game.playtimeForever()))
                .map(game -> steamGameService.fromOwnedGame(game, request.locale(), request.layout(), GameImageType.SMALL))
                .orElse(null);

        SteamLibrarySummary librarySummary = new SteamLibrarySummary(gameCount, totalPlaytime(ownedDtos), mostPlayed);
        SteamStatistics statistics = new SteamStatistics(
                steamLevel,
                friendCount,
                gameCount,
                totalPlaytime(ownedDtos),
                recentDtos.stream().mapToInt(game -> game.playtimeTwoWeeks() == null ? 0 : game.playtimeTwoWeeks()).sum());

        SteamGameImages resolvedImages = selectedGame == null ? null : selectedGame.images();
        String renderedAvatar = steamImageService.renderableImageUrl(
                profile.avatarUrl(),
                profile.nickname(),
                null,
                request.theme(),
                request.imageMode());
        String renderedPrimaryImage = selectedGame == null ? null : steamImageService.renderableGameImageUrl(
                resolvedImages,
                request.gameImage(),
                selectedGame.name(),
                selectedGame.appId(),
                request.theme(),
                request.imageMode());

        return steamCardMapper.toCardData(profile, selectedGame, recentGames, librarySummary, statistics,
                request, resolvedImages, renderedAvatar, renderedPrimaryImage);
    }

    public SteamCardData getProfileData(SteamCardRequest request) {
        return getCardData(request);
    }

    private String resolveSteamId(SteamSubject subject) {
        if (StringUtils.hasText(subject.steamId())) {
            return subject.steamId();
        }
        ResolveVanityResponse vanityResponse = steamWebApiClient.resolveVanityUrl(
                steamProperties.apiKey(),
                subject.vanity());
        ResolveVanityResponse.Response response = vanityResponse.response();
        if (response == null || response.success() == null || response.success() != 1
                || !StringUtils.hasText(response.steamId())) {
            throw new SteamProfileNotFoundException("Steam vanity URL was not found");
        }
        return response.steamId();
    }

    private PlayerSummariesResponse.Player getPlayer(String steamId) {
        PlayerSummariesResponse summaries = steamWebApiClient.getPlayerSummaries(steamProperties.apiKey(), steamId);
        List<PlayerSummariesResponse.Player> players = summaries.response() == null
                ? List.of()
                : nullSafe(summaries.response().players());
        return players.stream()
                .findFirst()
                .orElseThrow(() -> new SteamProfileNotFoundException("Steam profile has no public summary"));
    }

    private List<RecentlyPlayedGamesResponse.Game> recentGames(String steamId) {
        try {
            RecentlyPlayedGamesResponse recentlyPlayedGames =
                    steamWebApiClient.getRecentlyPlayedGames(steamProperties.apiKey(), steamId, 3);
            return recentlyPlayedGames.response() == null
                    ? List.of()
                    : nullSafe(recentlyPlayedGames.response().games());
        } catch (SteamApiException exception) {
            return List.of();
        }
    }

    private List<OwnedGamesResponse.Game> ownedGames(String steamId) {
        try {
            OwnedGamesResponse ownedGames = steamWebApiClient.getOwnedGames(steamProperties.apiKey(), steamId);
            return ownedGames.response() == null ? List.of() : nullSafe(ownedGames.response().games());
        } catch (SteamApiException exception) {
            return List.of();
        }
    }

    private Integer steamLevel(String steamId) {
        try {
            SteamLevelResponse level = steamWebApiClient.getSteamLevel(steamProperties.apiKey(), steamId);
            return level.response() == null ? null : level.response().playerLevel();
        } catch (SteamApiException exception) {
            return null;
        }
    }

    private Integer friendCount(String steamId) {
        try {
            FriendListResponse friends = steamWebApiClient.getFriendList(steamProperties.apiKey(), steamId);
            return friends.friendsList() == null || friends.friendsList().friends() == null
                    ? null
                    : friends.friendsList().friends().size();
        } catch (SteamApiException exception) {
            return null;
        }
    }

    private SteamGame selectMainGame(
            PlayerSummariesResponse.Player player,
            List<RecentlyPlayedGamesResponse.Game> recentGames,
            List<OwnedGamesResponse.Game> ownedGames,
            SteamCardRequest request
    ) {
        boolean currentlyPlaying = StringUtils.hasText(player.gameExtraInfo()) && StringUtils.hasText(player.gameId());
        if (currentlyPlaying) {
            Integer activeAppId = parseAppId(player.gameId());
            return steamGameService.fromActivePlayer(
                    player,
                    matchingOwnedGame(ownedGames, activeAppId),
                    matchingRecentGame(recentGames, activeAppId),
                    request.locale(),
                    request.layout(),
                    request.gameImage());
        }
        SteamGame latestOwnedGame = latestOwnedGame(ownedGames, request);
        if (latestOwnedGame != null) {
            return latestOwnedGame;
        }
        if (!recentGames.isEmpty()) {
            return steamGameService.fromRecentGame(recentGames.getFirst(), false,
                    request.locale(), request.layout(), request.gameImage());
        }
        return ownedGames.stream()
                .max(Comparator.comparing(game -> game.playtimeForever() == null ? 0 : game.playtimeForever()))
                .map(game -> steamGameService.fromOwnedGame(game, request.locale(), request.layout(), request.gameImage()))
                .orElse(null);
    }

    private SteamGame latestOwnedGame(List<OwnedGamesResponse.Game> ownedGames, SteamCardRequest request) {
        return ownedGames.stream()
                .filter(game -> game.lastPlayed() != null && game.lastPlayed() > 0)
                .max(Comparator.comparing(OwnedGamesResponse.Game::lastPlayed))
                .map(game -> steamGameService.fromOwnedGame(game, request.locale(), request.layout(), request.gameImage()))
                .orElse(null);
    }

    private OwnedGamesResponse.Game matchingOwnedGame(List<OwnedGamesResponse.Game> ownedGames, Integer appId) {
        if (appId == null) {
            return null;
        }
        return ownedGames.stream()
                .filter(game -> appId.equals(game.appId()))
                .findFirst()
                .orElse(null);
    }

    private RecentlyPlayedGamesResponse.Game matchingRecentGame(
            List<RecentlyPlayedGamesResponse.Game> recentGames,
            Integer appId
    ) {
        if (appId == null) {
            return null;
        }
        return recentGames.stream()
                .filter(game -> appId.equals(game.appId()))
                .findFirst()
                .orElse(null);
    }

    private Integer parseAppId(String value) {
        try {
            return StringUtils.hasText(value) ? Integer.valueOf(value) : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer totalPlaytime(List<OwnedGamesResponse.Game> ownedGames) {
        if (ownedGames.isEmpty()) {
            return null;
        }
        return ownedGames.stream().mapToInt(game -> game.playtimeForever() == null ? 0 : game.playtimeForever()).sum();
    }

    private <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}

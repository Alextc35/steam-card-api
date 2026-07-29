package com.alextc.steamcardapi.service;

import com.alextc.steamcardapi.client.SteamStoreApiClient;
import com.alextc.steamcardapi.dto.steam.OwnedGamesResponse;
import com.alextc.steamcardapi.dto.steam.PlayerSummariesResponse;
import com.alextc.steamcardapi.dto.steam.RecentlyPlayedGamesResponse;
import com.alextc.steamcardapi.dto.store.SteamStoreAppDetailsResponse;
import com.alextc.steamcardapi.mapper.SteamGameMapper;
import com.alextc.steamcardapi.model.GameImageType;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.model.SteamGameImages;
import com.alextc.steamcardapi.svg.SvgLayout;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SteamGameService {

    private final SteamStoreApiClient steamStoreApiClient;
    private final SteamImageService steamImageService;
    private final SteamGameMapper steamGameMapper;

    public SteamGameService(
            SteamStoreApiClient steamStoreApiClient,
            SteamImageService steamImageService,
            SteamGameMapper steamGameMapper
    ) {
        this.steamStoreApiClient = steamStoreApiClient;
        this.steamImageService = steamImageService;
        this.steamGameMapper = steamGameMapper;
    }

    public SteamGame getGame(int appId, String language, SvgLayout layout, GameImageType imageType) {
        Optional<SteamStoreAppDetailsResponse.AppData> storeData = steamStoreApiClient.getAppDetails(appId, language);
        SteamGameImages images = steamImageService.resolveImages(appId, null, storeData, layout, imageType);
        return steamGameMapper.fromAppDetails(appId, storeData, images);
    }

    public SteamGame fromActivePlayer(
            PlayerSummariesResponse.Player player,
            String language,
            SvgLayout layout,
            GameImageType imageType
    ) {
        return fromActivePlayer(player, null, null, language, layout, imageType);
    }

    public SteamGame fromActivePlayer(
            PlayerSummariesResponse.Player player,
            OwnedGamesResponse.Game ownedGame,
            RecentlyPlayedGamesResponse.Game recentGame,
            String language,
            SvgLayout layout,
            GameImageType imageType
    ) {
        Integer appId = Integer.valueOf(player.gameId());
        Optional<SteamStoreAppDetailsResponse.AppData> storeData = steamStoreApiClient.getAppDetails(appId, language);
        SteamGameImages images = steamImageService.resolveImages(appId, activeIconHash(ownedGame, recentGame),
                storeData, layout, imageType);
        return steamGameMapper.fromActivePlayer(player, ownedGame, recentGame, storeData, images);
    }

    public SteamGame fromRecentGame(
            RecentlyPlayedGamesResponse.Game game,
            boolean currentlyPlaying,
            String language,
            SvgLayout layout,
            GameImageType imageType
    ) {
        Optional<SteamStoreAppDetailsResponse.AppData> storeData = steamStoreApiClient.getAppDetails(game.appId(), language);
        SteamGameImages images = steamImageService.resolveImages(game.appId(), game.imageIconUrl(), storeData, layout, imageType);
        return steamGameMapper.fromRecentGame(game, currentlyPlaying, storeData, images);
    }

    public SteamGame fromOwnedGame(
            OwnedGamesResponse.Game game,
            String language,
            SvgLayout layout,
            GameImageType imageType
    ) {
        Optional<SteamStoreAppDetailsResponse.AppData> storeData = steamStoreApiClient.getAppDetails(game.appId(), language);
        SteamGameImages images = steamImageService.resolveImages(game.appId(), game.imageIconUrl(), storeData, layout, imageType);
        return steamGameMapper.fromOwnedGame(game, storeData, images);
    }

    private String activeIconHash(OwnedGamesResponse.Game ownedGame, RecentlyPlayedGamesResponse.Game recentGame) {
        if (ownedGame != null && ownedGame.imageIconUrl() != null && !ownedGame.imageIconUrl().isBlank()) {
            return ownedGame.imageIconUrl();
        }
        return recentGame == null ? null : recentGame.imageIconUrl();
    }
}

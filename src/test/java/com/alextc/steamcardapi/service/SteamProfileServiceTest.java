package com.alextc.steamcardapi.service;

import static com.alextc.steamcardapi.TestFixtures.STEAM_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.alextc.steamcardapi.TestFixtures;
import com.alextc.steamcardapi.client.SteamWebApiClient;
import com.alextc.steamcardapi.dto.steam.FriendListResponse;
import com.alextc.steamcardapi.dto.steam.OwnedGamesResponse;
import com.alextc.steamcardapi.dto.steam.PlayerSummariesResponse;
import com.alextc.steamcardapi.dto.steam.RecentlyPlayedGamesResponse;
import com.alextc.steamcardapi.dto.steam.SteamLevelResponse;
import com.alextc.steamcardapi.exception.SteamApiException;
import com.alextc.steamcardapi.mapper.SteamCardMapper;
import com.alextc.steamcardapi.mapper.SteamProfileMapper;
import com.alextc.steamcardapi.model.GameImageType;
import com.alextc.steamcardapi.model.SteamCardData;
import com.alextc.steamcardapi.model.SteamCardRequest;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.svg.SvgLayout;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SteamProfileServiceTest {

    @Mock
    private SteamWebApiClient webApiClient;

    @Mock
    private SteamGameService gameService;

    @Mock
    private SteamImageService imageService;

    private SteamProfileService service;

    @BeforeEach
    void setUp() {
        service = new SteamProfileService(
                webApiClient,
                TestFixtures.properties(),
                new SteamProfileMapper(),
                gameService,
                imageService,
                new SteamCardMapper());

        lenient().when(webApiClient.getSteamLevel("test-key", STEAM_ID))
                .thenReturn(new SteamLevelResponse(new SteamLevelResponse.Response(42)));
        lenient().when(webApiClient.getFriendList("test-key", STEAM_ID))
                .thenReturn(new FriendListResponse(new FriendListResponse.FriendsList(List.of(
                        new FriendListResponse.Friend("1"),
                        new FriendListResponse.Friend("2")))));
        lenient().when(imageService.renderableImageUrl(any(), any(), nullable(Integer.class), any(), any()))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0);
                    return url == null ? "data:image/svg+xml;base64,PLACEHOLDER" : "rendered:" + url;
                });
    }

    @Test
    void prioritizesCurrentlyPlayingGame() {
        SteamCardRequest request = TestFixtures.request();
        SteamGame active = TestFixtures.game(1172470, "Apex Legends", 240, true);
        when(webApiClient.getPlayerSummaries("test-key", STEAM_ID)).thenReturn(profileWithCurrentGame());
        when(webApiClient.getRecentlyPlayedGames("test-key", STEAM_ID, 3)).thenReturn(recentResponse(List.of()));
        when(webApiClient.getOwnedGames("test-key", STEAM_ID)).thenReturn(ownedResponse(List.of()));
        when(gameService.fromActivePlayer(any(), nullable(OwnedGamesResponse.Game.class),
                nullable(RecentlyPlayedGamesResponse.Game.class), eq("en"), eq(SvgLayout.SHOWCASE),
                eq(GameImageType.AUTO)))
                .thenReturn(active);

        SteamCardData data = service.getCardData(request);

        assertThat(data.selectedGame()).isSameAs(active);
        assertThat(data.selectedGame().currentlyPlaying()).isTrue();
    }

    @Test
    void currentlyPlayingGameKeepsOwnedPlaytime() {
        SteamCardRequest request = TestFixtures.request();
        OwnedGamesResponse.Game owned = ownedGame(431730, "Aseprite", 600, 1_785_000_000L);
        SteamGame activeWithStats = TestFixtures.game(431730, "Aseprite", 600, true);
        when(webApiClient.getPlayerSummaries("test-key", STEAM_ID)).thenReturn(profile("Aseprite", "431730"));
        when(webApiClient.getRecentlyPlayedGames("test-key", STEAM_ID, 3)).thenReturn(recentResponse(List.of()));
        when(webApiClient.getOwnedGames("test-key", STEAM_ID)).thenReturn(ownedResponse(List.of(owned)));
        when(gameService.fromActivePlayer(any(), eq(owned), nullable(RecentlyPlayedGamesResponse.Game.class),
                eq("en"), eq(SvgLayout.SHOWCASE), eq(GameImageType.AUTO)))
                .thenReturn(activeWithStats);
        when(gameService.fromOwnedGame(owned, "en", SvgLayout.SHOWCASE, GameImageType.SMALL))
                .thenReturn(TestFixtures.game(431730, "Aseprite", 600, false));

        SteamCardData data = service.getCardData(request);

        assertThat(data.selectedGame().currentlyPlaying()).isTrue();
        assertThat(data.selectedGame().appId()).isEqualTo(431730);
        assertThat(data.selectedGame().hoursForever()).isEqualTo(10.0);
    }

    @Test
    void usesLastRecentGameWhenProfileIsNotPlaying() {
        SteamCardRequest request = TestFixtures.request();
        RecentlyPlayedGamesResponse.Game recent = recentGame(730, "Counter-Strike 2", 600, 1_710_000_000L);
        SteamGame selected = TestFixtures.game(730, "Counter-Strike 2", 600, false);
        when(webApiClient.getPlayerSummaries("test-key", STEAM_ID)).thenReturn(profileWithoutCurrentGame());
        when(webApiClient.getRecentlyPlayedGames("test-key", STEAM_ID, 3)).thenReturn(recentResponse(List.of(recent)));
        when(webApiClient.getOwnedGames("test-key", STEAM_ID)).thenReturn(ownedResponse(List.of()));
        when(gameService.fromRecentGame(recent, false, "en", SvgLayout.SHOWCASE, GameImageType.AUTO))
                .thenReturn(selected);
        when(gameService.fromRecentGame(recent, false, "en", SvgLayout.SHOWCASE, GameImageType.SMALL))
                .thenReturn(selected);

        SteamCardData data = service.getCardData(request);

        assertThat(data.selectedGame().appId()).isEqualTo(730);
        assertThat(data.recentGames()).containsExactly(selected);
    }

    @Test
    void usesLatestOwnedGameBeforeSteamRecentPlaytimeOrder() {
        SteamCardRequest request = TestFixtures.request();
        RecentlyPlayedGamesResponse.Game recent = recentGame(3405690, "EA SPORTS FC 26", 796, 1_710_000_000L);
        OwnedGamesResponse.Game latestOwned = ownedGame(1454400, "Cookie Clicker", 180, 1_785_000_000L);
        SteamGame recentGame = TestFixtures.game(3405690, "EA SPORTS FC 26", 796, false);
        SteamGame selected = TestFixtures.game(1454400, "Cookie Clicker", 180, false);
        when(webApiClient.getPlayerSummaries("test-key", STEAM_ID)).thenReturn(profileWithoutCurrentGame());
        when(webApiClient.getRecentlyPlayedGames("test-key", STEAM_ID, 3)).thenReturn(recentResponse(List.of(recent)));
        when(webApiClient.getOwnedGames("test-key", STEAM_ID)).thenReturn(ownedResponse(List.of(latestOwned)));
        when(gameService.fromRecentGame(recent, false, "en", SvgLayout.SHOWCASE, GameImageType.SMALL))
                .thenReturn(recentGame);
        when(gameService.fromOwnedGame(latestOwned, "en", SvgLayout.SHOWCASE, GameImageType.AUTO))
                .thenReturn(selected);
        when(gameService.fromOwnedGame(latestOwned, "en", SvgLayout.SHOWCASE, GameImageType.SMALL))
                .thenReturn(selected);

        SteamCardData data = service.getCardData(request);

        assertThat(data.selectedGame().appId()).isEqualTo(1454400);
        assertThat(data.selectedGame().name()).isEqualTo("Cookie Clicker");
    }

    @Test
    void usesMostPlayedLibraryGameWhenNoRecentGamesExist() {
        SteamCardRequest request = TestFixtures.request();
        OwnedGamesResponse.Game shortGame = ownedGame(10, "Short", 60);
        OwnedGamesResponse.Game longGame = ownedGame(20, "Long", 900);
        SteamGame selected = TestFixtures.game(20, "Long", 900, false);
        when(webApiClient.getPlayerSummaries("test-key", STEAM_ID)).thenReturn(profileWithoutCurrentGame());
        when(webApiClient.getRecentlyPlayedGames("test-key", STEAM_ID, 3)).thenReturn(recentResponse(List.of()));
        when(webApiClient.getOwnedGames("test-key", STEAM_ID)).thenReturn(ownedResponse(List.of(shortGame, longGame)));
        when(gameService.fromOwnedGame(longGame, "en", SvgLayout.SHOWCASE, GameImageType.AUTO)).thenReturn(selected);
        when(gameService.fromOwnedGame(longGame, "en", SvgLayout.SHOWCASE, GameImageType.SMALL)).thenReturn(selected);

        SteamCardData data = service.getCardData(request);

        assertThat(data.selectedGame().appId()).isEqualTo(20);
        assertThat(data.librarySummary().totalPlaytimeMinutes()).isEqualTo(960);
    }

    @Test
    void handlesPrivateLibraryAndOptionalSections() {
        SteamCardRequest request = TestFixtures.request();
        when(webApiClient.getPlayerSummaries("test-key", STEAM_ID)).thenReturn(profileWithoutCurrentGame());
        when(webApiClient.getRecentlyPlayedGames("test-key", STEAM_ID, 3)).thenThrow(new SteamApiException("private"));
        when(webApiClient.getOwnedGames("test-key", STEAM_ID)).thenThrow(new SteamApiException("private"));
        when(webApiClient.getSteamLevel("test-key", STEAM_ID)).thenThrow(new SteamApiException("private"));
        when(webApiClient.getFriendList("test-key", STEAM_ID)).thenThrow(new SteamApiException("private"));

        SteamCardData data = service.getCardData(request);

        assertThat(data.selectedGame()).isNull();
        assertThat(data.profile().steamLevel()).isNull();
        assertThat(data.profile().friendCount()).isNull();
        assertThat(data.statistics().totalPlaytimeMinutes()).isNull();
    }

    @Test
    void convertsMinutesToHoursAndUnixTimestamps() {
        SteamCardRequest request = TestFixtures.request();
        RecentlyPlayedGamesResponse.Game recent = recentGame(730, "Counter-Strike 2", 90, 1_710_000_000L);
        SteamGame selected = TestFixtures.game(730, "Counter-Strike 2", 90, false);
        when(webApiClient.getPlayerSummaries("test-key", STEAM_ID)).thenReturn(profileWithoutCurrentGame());
        when(webApiClient.getRecentlyPlayedGames("test-key", STEAM_ID, 3)).thenReturn(recentResponse(List.of(recent)));
        when(webApiClient.getOwnedGames("test-key", STEAM_ID)).thenReturn(ownedResponse(List.of()));
        when(gameService.fromRecentGame(recent, false, "en", SvgLayout.SHOWCASE, GameImageType.AUTO))
                .thenReturn(selected);
        when(gameService.fromRecentGame(recent, false, "en", SvgLayout.SHOWCASE, GameImageType.SMALL))
                .thenReturn(selected);

        SteamCardData data = service.getCardData(request);

        assertThat(data.selectedGame().hoursForever()).isEqualTo(1.5);
        assertThat(data.profile().accountCreatedAt()).isEqualTo(Instant.ofEpochSecond(1_600_000_000L));
        assertThat(data.profile().lastOnlineAt()).isEqualTo(Instant.ofEpochSecond(1_700_000_000L));
    }

    private PlayerSummariesResponse profileWithCurrentGame() {
        return profile("Apex Legends", "1172470");
    }

    private PlayerSummariesResponse profileWithoutCurrentGame() {
        return profile(null, null);
    }

    private PlayerSummariesResponse profile(String gameExtraInfo, String gameId) {
        PlayerSummariesResponse.Player player = new PlayerSummariesResponse.Player(
                STEAM_ID,
                "Alex <Dev>",
                "https://steamcommunity.com/id/alextc",
                "https://avatars.steamstatic.com/avatar.jpg",
                1,
                gameExtraInfo,
                gameId,
                1_700_000_000L,
                1_600_000_000L,
                "ES");
        return new PlayerSummariesResponse(new PlayerSummariesResponse.Response(List.of(player)));
    }

    private RecentlyPlayedGamesResponse recentResponse(List<RecentlyPlayedGamesResponse.Game> games) {
        return new RecentlyPlayedGamesResponse(new RecentlyPlayedGamesResponse.Response(games.size(), games));
    }

    private OwnedGamesResponse ownedResponse(List<OwnedGamesResponse.Game> games) {
        return new OwnedGamesResponse(new OwnedGamesResponse.Response(games.size(), games));
    }

    private RecentlyPlayedGamesResponse.Game recentGame(int appId, String name, int foreverMinutes, long lastPlayed) {
        return new RecentlyPlayedGamesResponse.Game(appId, name, 30, foreverMinutes, "iconhash", lastPlayed);
    }

    private OwnedGamesResponse.Game ownedGame(int appId, String name, int foreverMinutes) {
        return ownedGame(appId, name, foreverMinutes, null);
    }

    private OwnedGamesResponse.Game ownedGame(int appId, String name, int foreverMinutes, Long lastPlayed) {
        return new OwnedGamesResponse.Game(appId, name, null, foreverMinutes, "iconhash", lastPlayed);
    }
}

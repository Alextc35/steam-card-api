package com.alextc.steamcardapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.alextc.steamcardapi.dto.steam.OwnedGamesResponse;
import com.alextc.steamcardapi.dto.steam.PlayerSummariesResponse;
import com.alextc.steamcardapi.dto.steam.RecentlyPlayedGamesResponse;
import com.alextc.steamcardapi.dto.steam.ResolveVanityResponse;
import com.alextc.steamcardapi.exception.SteamApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SteamWebApiClientTest {

    private MockRestServiceServer server;
    private SteamWebApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.steampowered.com");
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.defaultStatusHandler(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                (request, response) -> {
                    throw new SteamApiException("Steam API returned HTTP " + response.getStatusCode().value());
                }).build();
        client = new SteamWebApiClient(restClient);
    }

    @Test
    void resolvesVanityUrl() {
        server.expect(requestTo("https://api.steampowered.com/ISteamUser/ResolveVanityURL/v1/?key=test-key&vanityurl=alextc"))
                .andExpect(queryParam("key", "test-key"))
                .andRespond(withSuccess("{\"response\":{\"steamid\":\"76561198000000000\",\"success\":1}}", MediaType.APPLICATION_JSON));

        ResolveVanityResponse response = client.resolveVanityUrl("test-key", "alextc");

        assertThat(response.response().steamId()).isEqualTo("76561198000000000");
        server.verify();
    }

    @Test
    void processesProfilesAndRecentGamesAndLibrary() {
        server.expect(requestTo("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=test-key&steamids=76561198000000000"))
                .andRespond(withSuccess("""
                        {"response":{"players":[{"steamid":"76561198000000000","personaname":"Alex","personastate":1}]}}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.steampowered.com/IPlayerService/GetRecentlyPlayedGames/v1/?key=test-key&steamid=76561198000000000&count=3&format=json"))
                .andRespond(withSuccess("""
                        {"response":{"total_count":1,"games":[{"appid":730,"name":"Counter-Strike 2","playtime_2weeks":60,"playtime_forever":600,"img_icon_url":"hash"}]}}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=test-key&steamid=76561198000000000&include_appinfo=true&include_played_free_games=true&format=json"))
                .andRespond(withSuccess("""
                        {"response":{"game_count":1,"games":[{"appid":730,"name":"Counter-Strike 2","playtime_forever":600}]}}
                        """, MediaType.APPLICATION_JSON));

        PlayerSummariesResponse profile = client.getPlayerSummaries("test-key", "76561198000000000");
        RecentlyPlayedGamesResponse recent = client.getRecentlyPlayedGames("test-key", "76561198000000000", 3);
        OwnedGamesResponse owned = client.getOwnedGames("test-key", "76561198000000000");

        assertThat(profile.response().players().getFirst().personaName()).isEqualTo("Alex");
        assertThat(recent.response().games().getFirst().appId()).isEqualTo(730);
        assertThat(owned.response().games().getFirst().playtimeForever()).isEqualTo(600);
        server.verify();
    }

    @Test
    void handlesHttpErrorsWithoutExposingApiKey() {
        server.expect(requestTo("https://api.steampowered.com/ISteamUser/ResolveVanityURL/v1/?key=test-key&vanityurl=alextc"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.resolveVanityUrl("test-key", "alextc"))
                .isInstanceOf(SteamApiException.class)
                .hasMessageNotContaining("test-key");
        server.verify();
    }
}

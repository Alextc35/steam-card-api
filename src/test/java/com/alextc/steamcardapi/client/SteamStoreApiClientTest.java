package com.alextc.steamcardapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.alextc.steamcardapi.dto.store.SteamStoreAppDetailsResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SteamStoreApiClientTest {

    private MockRestServiceServer server;
    private SteamStoreApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://store.steampowered.com");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SteamStoreApiClient(builder.build());
    }

    @Test
    void processesAppDetails() {
        server.expect(requestTo("https://store.steampowered.com/api/appdetails?appids=730&l=en"))
                .andRespond(withSuccess("""
                        {"730":{"success":true,"data":{"type":"game","name":"Counter-Strike 2","short_description":"A tactical shooter.","developers":["Valve"],"publishers":["Valve"],"genres":[{"description":"Action"}],"categories":[{"description":"Multi-player"}],"release_date":{"date":"Aug 21, 2012"},"is_free":true,"header_image":"https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg","background_raw":"https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/library_hero.jpg","metacritic":{"score":83},"recommendations":{"total":1000},"platforms":{"windows":true,"mac":false,"linux":true}}}}
                        """, MediaType.APPLICATION_JSON));

        Optional<SteamStoreAppDetailsResponse.AppData> data = client.getAppDetails(730, "en");

        assertThat(data).isPresent();
        assertThat(data.orElseThrow().name()).isEqualTo("Counter-Strike 2");
        assertThat(data.orElseThrow().genreDescriptions()).contains("Action");
        assertThat(data.orElseThrow().platforms().linux()).isTrue();
        server.verify();
    }

    @Test
    void handlesSuccessFalseAndMissingFields() {
        server.expect(requestTo("https://store.steampowered.com/api/appdetails?appids=999&l=en"))
                .andRespond(withSuccess("{\"999\":{\"success\":false}}", MediaType.APPLICATION_JSON));

        assertThat(client.getAppDetails(999, "en")).isEmpty();
        server.verify();
    }
}

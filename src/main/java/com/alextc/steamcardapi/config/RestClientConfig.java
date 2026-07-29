package com.alextc.steamcardapi.config;

import com.alextc.steamcardapi.exception.SteamApiException;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("steamWebRestClient")
    RestClient steamWebRestClient(RestClient.Builder builder, SteamProperties steamProperties) {
        return steamRestClient(builder, steamProperties, "https://api.steampowered.com");
    }

    @Bean
    @Qualifier("steamStoreRestClient")
    RestClient steamStoreRestClient(RestClient.Builder builder, SteamProperties steamProperties) {
        return steamRestClient(builder, steamProperties, "https://store.steampowered.com");
    }

    private RestClient steamRestClient(RestClient.Builder builder, SteamProperties steamProperties, String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(steamProperties.httpConnectTimeout());
        requestFactory.setReadTimeout(steamProperties.httpReadTimeout());

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, "Steam Card API/1.0 (+https://alextc.es)")
                .defaultStatusHandler(HttpStatusCode::isError, this::handleSteamError)
                .build();
    }

    private void handleSteamError(org.springframework.http.HttpRequest request, ClientHttpResponse response)
            throws IOException {
        throw new SteamApiException("Steam API returned HTTP " + response.getStatusCode().value());
    }
}

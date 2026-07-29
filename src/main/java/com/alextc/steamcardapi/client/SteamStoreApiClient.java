package com.alextc.steamcardapi.client;

import com.alextc.steamcardapi.dto.store.SteamStoreAppDetailsResponse;
import com.alextc.steamcardapi.exception.SteamApiException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static com.alextc.steamcardapi.config.CacheConfig.STEAM_STORE_CACHE;

@Component
public class SteamStoreApiClient {

    private final RestClient restClient;

    public SteamStoreApiClient(@Qualifier("steamStoreRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Cacheable(cacheNames = STEAM_STORE_CACHE, key = "#appId + ':' + #language", unless = "#result == null")
    public Optional<SteamStoreAppDetailsResponse.AppData> getAppDetails(int appId, String language) {
        try {
            SteamStoreAppDetailsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/appdetails")
                            .queryParam("appids", appId)
                            .queryParam("l", language)
                            .build())
                    .retrieve()
                    .body(SteamStoreAppDetailsResponse.class);
            if (response == null || response.apps() == null) {
                return Optional.empty();
            }
            SteamStoreAppDetailsResponse.AppEnvelope envelope = response.apps().get(String.valueOf(appId));
            if (envelope == null || !Boolean.TRUE.equals(envelope.success()) || envelope.data() == null) {
                return Optional.empty();
            }
            return Optional.of(envelope.data());
        } catch (RestClientException exception) {
            throw new SteamApiException("Unable to communicate with Steam Store API", exception);
        }
    }
}

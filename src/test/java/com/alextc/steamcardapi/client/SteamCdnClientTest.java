package com.alextc.steamcardapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alextc.steamcardapi.TestFixtures;
import com.alextc.steamcardapi.exception.SteamImageUnavailableException;
import com.alextc.steamcardapi.model.DownloadedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class SteamCdnClientTest {

    @Test
    void downloadsAllowedSteamImage() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<java.io.InputStream> response = response(200, "image/png", 3,
                new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        SteamCdnClient client = new SteamCdnClient(TestFixtures.properties(), httpClient);

        DownloadedImage image = client.download("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/730/header.jpg");

        assertThat(image.contentType()).isEqualTo("image/png");
        assertThat(image.bytes()).containsExactly(1, 2, 3);
    }

    @Test
    void rejectsUnsupportedContentTypesAndOversizedImages() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<java.io.InputStream> htmlResponse = response(200, "text/html", 10,
                new ByteArrayInputStream("<html/>".getBytes()));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(htmlResponse);
        SteamCdnClient client = new SteamCdnClient(TestFixtures.properties(), httpClient);

        assertThatThrownBy(() -> client.download("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg"))
                .isInstanceOf(SteamImageUnavailableException.class)
                .hasMessageContaining("unsupported content type");

        HttpClient tooLargeHttpClient = mock(HttpClient.class);
        HttpResponse<java.io.InputStream> tooLargeResponse = response(200, "image/jpeg", 2_000_000,
                new ByteArrayInputStream(new byte[]{1}));
        when(tooLargeHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tooLargeResponse);
        SteamCdnClient sizeClient = new SteamCdnClient(TestFixtures.properties(), tooLargeHttpClient);

        assertThatThrownBy(() -> sizeClient.download("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg"))
                .isInstanceOf(SteamImageUnavailableException.class)
                .hasMessageContaining("too large");
    }

    @Test
    void rejectsNonAllowlistedOrNonHttpsUrlsAndTimeouts() throws Exception {
        SteamCdnClient client = new SteamCdnClient(TestFixtures.properties(), mock(HttpClient.class));

        assertThatThrownBy(() -> client.validateUrl("http://shared.fastly.steamstatic.com/image.jpg"))
                .isInstanceOf(SteamImageUnavailableException.class);
        assertThatThrownBy(() -> client.validateUrl("https://localhost/image.jpg"))
                .isInstanceOf(SteamImageUnavailableException.class);
        assertThatThrownBy(() -> client.validateUrl("https://169.254.169.254/latest/meta-data"))
                .isInstanceOf(SteamImageUnavailableException.class);

        HttpClient timeoutClient = mock(HttpClient.class);
        when(timeoutClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Read timed out"));
        SteamCdnClient timeout = new SteamCdnClient(TestFixtures.properties(), timeoutClient);

        assertThatThrownBy(() -> timeout.download("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg"))
                .isInstanceOf(SteamImageUnavailableException.class)
                .hasMessageContaining("could not be downloaded");
    }

    private HttpResponse<java.io.InputStream> response(
            int status,
            String contentType,
            long contentLength,
            java.io.InputStream body
    ) {
        @SuppressWarnings("unchecked")
        HttpResponse<java.io.InputStream> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.headers()).thenReturn(HttpHeadersBuilder.headers(contentType, contentLength));
        when(response.body()).thenReturn(body);
        return response;
    }

    private static final class HttpHeadersBuilder {
        private HttpHeadersBuilder() {
        }

        static java.net.http.HttpHeaders headers(String contentType, long contentLength) {
            return java.net.http.HttpHeaders.of(
                    java.util.Map.of(
                            "Content-Type", java.util.List.of(contentType),
                            "Content-Length", java.util.List.of(String.valueOf(contentLength))),
                    (name, value) -> true);
        }
    }
}
